package data;

/*
 *
 *
 * @author EMTI
 */

import models.Template;
import models.Template.ArrHead2Frames;
import models.Template.ItemOptionTemplate;
import server.Manager;
import network.Message;
import server.io.MySession;

public class ItemData {

    public static void updateItem(MySession session) {
        int totalItems = Manager.ITEM_TEMPLATES.size();
        System.out
                .println("[SERVER] Sending item templates - vsItem=" + DataGame.vsItem + ", totalItems=" + totalItems);
        updateItemOptionItemplate(session);
        updateItemArrHead2FItemplate(session);

        // Chia nhỏ việc gửi item template thành nhiều batch để tránh message quá lớn
        int batchSize = 500; // Mỗi batch tối đa 500 items

        // Gửi batch đầu tiên (case 1 - reload toàn bộ, clear cũ)
        int firstBatchSize = Math.min(batchSize, totalItems);
        boolean isLastBatch = (firstBatchSize >= totalItems);
        updateItemTemplate(session, firstBatchSize, isLastBatch);

        // Gửi các batch còn lại (case 2 - add thêm)
        for (int start = firstBatchSize; start < totalItems; start += batchSize) {
            int end = Math.min(start + batchSize, totalItems);
            isLastBatch = (end >= totalItems);
            updateItemTemplate(session, start, end, isLastBatch);
        }
    }

    private static void updateItemOptionItemplate(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem); // vcitem
            msg.writer().writeByte(0); // update option
            msg.writer().writeByte(Manager.ITEM_OPTION_TEMPLATES.size());
            for (ItemOptionTemplate io : Manager.ITEM_OPTION_TEMPLATES) {
                msg.writer().writeUTF(io.name);
                msg.writer().writeByte(io.type);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    private static void updateItemTemplate(MySession session, int count, boolean isLastBatch) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem); // vcitem
            msg.writer().writeByte(1); // reload itemtemplate
            msg.writer().writeShort(count);
            msg.writer().writeBoolean(isLastBatch); // Flag để client biết đây là batch cuối
            for (int i = 0; i < count; i++) {
                Template.ItemTemplate itemTemplate = Manager.ITEM_TEMPLATES.get(i);
                msg.writer().writeShort(itemTemplate.id); // GỬI ID THỰC CỦA ITEM
                msg.writer().writeByte(itemTemplate.type);
                msg.writer().writeByte(itemTemplate.gender);
                msg.writer().writeUTF(itemTemplate.name);
                msg.writer().writeUTF(itemTemplate.description);
                msg.writer().writeByte(itemTemplate.level);
                msg.writer().writeInt(itemTemplate.strRequire);
                msg.writer().writeShort(itemTemplate.iconID);
                msg.writer().writeShort(itemTemplate.part);
                msg.writer().writeBoolean(itemTemplate.isUpToUp);
            }
            session.doSendMessage(msg);
            msg.cleanup();
            System.out.println("[SERVER] Sent batch 1 - count=" + count + ", isLastBatch=" + isLastBatch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateItemTemplate(MySession session, int start, int end, boolean isLastBatch) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);

            msg.writer().writeByte(DataGame.vsItem); // vcitem
            msg.writer().writeByte(2); // add itemtemplate
            msg.writer().writeShort(end - start); // Gửi số lượng items
            msg.writer().writeBoolean(isLastBatch); // Flag để client biết đây là batch cuối
            for (int i = start; i < end; i++) {
                Template.ItemTemplate itemTemplate = Manager.ITEM_TEMPLATES.get(i);
                msg.writer().writeShort(itemTemplate.id); // GỬI ID THỰC CỦA ITEM
                msg.writer().writeByte(itemTemplate.type);
                msg.writer().writeByte(itemTemplate.gender);
                msg.writer().writeUTF(itemTemplate.name);
                msg.writer().writeUTF(itemTemplate.description);
                msg.writer().writeByte(itemTemplate.level);
                msg.writer().writeInt(itemTemplate.strRequire);
                msg.writer().writeShort(itemTemplate.iconID);
                msg.writer().writeShort(itemTemplate.part);
                msg.writer().writeBoolean(itemTemplate.isUpToUp);
            }
            session.doSendMessage(msg);
            msg.cleanup();
            System.out.println(
                    "[SERVER] Sent batch 2 - start=" + start + ", end=" + end + ", isLastBatch=" + isLastBatch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateItemArrHead2FItemplate(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);
            msg.writer().writeByte(8);
            msg.writer().writeByte(DataGame.vsItem); // vcitem
            msg.writer().writeByte(100); // update ArrHead2F
            msg.writer().writeShort(Manager.ARR_HEAD_2_FRAMES.size());
            for (ArrHead2Frames io : Manager.ARR_HEAD_2_FRAMES) {
                msg.writeByte(io.frames.size());
                for (int i : io.frames) {
                    msg.writer().writeShort(i);
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
}
