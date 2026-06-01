package network.io;

import network.CommandMessage;
import network.handler.IMessageHandler;
import network.handler.IMessageSendCollect;
import network.server.EMTIServer;
import network.session.ISession;
import network.session.TypeSession;

import java.io.DataInputStream;
import java.net.Socket;

public class Collector
      implements Runnable {
   private ISession session;
   private DataInputStream dis;
   private IMessageSendCollect collect;
   private IMessageHandler messageHandler;

   public Collector(ISession session, Socket socket) {
      this.session = session;
      setSocket(socket);
   }

   public Collector setSocket(Socket socket) {
      try {
         this.dis = new DataInputStream(socket.getInputStream());
      } catch (Exception exception) {
      }

      return this;
   }

   public void run() {
      try {
         while (true) {
            if (this.session.isConnected()) {
               // === Packet Rate Limit: Chống flood từ client ===
               if (this.session instanceof network.session.Session) {
                  network.session.Session s = (network.session.Session) this.session;
                  if (!s.checkPacketRate()) {
                     System.out.println("[AntiDDoS] Packet flood từ " + this.session.getIP() + " → kick!");
                     break;
                  }
               }
               Message msg = this.collect.readMessage(this.session, this.dis);

               if (msg.command == CommandMessage.REQUEST_KEY) {
                  if (this.session.getTypeSession() == TypeSession.SERVER) {
                     this.session.sendKey();
                  } else {
                     this.session.setKey(msg);
                  }
               } else {
                  this.messageHandler.onMessage(this.session, msg);
               }
               msg.cleanup();
            }
            Thread.sleep(1L);
         }
      } catch (Exception ex) {
         try {
            EMTIServer.gI().getAcceptHandler().sessionDisconnect(this.session);
         } catch (Exception exception) {
         }

         if (this.session != null) {
            System.out.println("Mất kết nối với session " + this.session.getIP() + "...");
            this.session.disconnect();
         }
         return;
      }
   }

   public void setCollect(IMessageSendCollect collect) {
      this.collect = collect;
   }

   public void setMessageHandler(IMessageHandler handler) {
      this.messageHandler = handler;
   }

   public void close() {
      if (this.dis != null) {
         try {
            this.dis.close();
         } catch (Exception exception) {
         }
      }
   }

   public void dispose() {
      this.session = null;
      this.dis = null;
      this.collect = null;
   }
}
