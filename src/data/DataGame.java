package data;

/*
 *
 *
 * @author EMTI
 */
import encrypt.IconEncrypt;
import encrypt.ImageUtil;
import static encrypt.ImageUtil.encryptImage;
import static encrypt.ImageUtil.encryptString;
import static encrypt.ImageUtil.generateRandomKey;
import models.Template.HeadAvatar;
import models.Template.MapTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import network.Message;
import utils.FileIO;
import services.Service;
import skill.NClass;
import skill.Skill;
import models.Template.MobTemplate;
import models.Template.NpcTemplate;
import models.Template.SkillTemplate;
import network.inetwork.ISession;

import java.io.IOException;
import java.util.List;

import server.Manager;
import server.io.MySession;
import utils.Logger;

import models.Template.BgItem;
import power.Caption;
import power.CaptionManager;
import utils.Util;
import models.Farm.CropTemplate;

public class DataGame {

    public static byte vsData = 9;
    public static byte vsMap = 2;
    public static byte vsSkill = 1;
    public static byte vsItem = 9; // Tăng vì thay đổi format - thêm flag isLastBatch
    public static int vsRes = 4;
    public static short maxSmallVersion = 32767;

    public static String LINK_IP_PORT = "LOCAL:127.0.00.1:14445:0";
    public static Map MAP_MOUNT_NUM = new HashMap();

    public static void sendVersionGame(MySession session) {
        Message msg;
        try {
            msg = Service.gI().messageNotMap((byte) 4);
            msg.writer().writeByte(vsData);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(vsItem);
            msg.writer().writeByte(0);
            List<Caption> captions = CaptionManager.getInstance().getCaptions();
            msg.writer().writeByte(captions.size());
            for (Caption caption : captions) {
                msg.writer().writeLong(caption.getPower());
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    // vData
    public static void updateData(MySession session) {
        final byte[] dart = FileIO.readFile("data/update_data/dart");
        final byte[] arrow = FileIO.readFile("data/update_data/arrow");
        final byte[] effect = FileIO.readFile("data/update_data/effect");
        final byte[] image = FileIO.readFile("data/update_data/image");
        final byte[] part = FileIO.readFile("data/update_data/part");
        final byte[] skill = FileIO.readFile("data/update_data/skill");

        Message msg;
        try {
            msg = new Message(-87);
            msg.writer().writeByte(vsData);
            msg.writer().writeInt(dart.length);
            msg.writer().write(dart);
            msg.writer().writeInt(arrow.length);
            msg.writer().write(arrow);
            msg.writer().writeInt(effect.length);
            msg.writer().write(effect);
            msg.writer().writeInt(image.length);
            msg.writer().write(image);
            msg.writer().writeInt(part.length);
            msg.writer().write(part);
            msg.writer().writeInt(skill.length);
            msg.writer().write(skill);

            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // vMap
    public static void updateMap(MySession session) {
        Message msg;
        try {
            msg = Service.gI().messageNotMap((byte) 6);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(Manager.MAP_TEMPLATES.length);
            for (MapTemplate temp : Manager.MAP_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
            }
            msg.writer().writeByte(Manager.NPC_TEMPLATES.size());
            for (NpcTemplate temp : Manager.NPC_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
                msg.writer().writeShort(temp.head);
                msg.writer().writeShort(temp.body);
                msg.writer().writeShort(temp.leg);
                msg.writer().writeByte(0);
            }
            msg.writer().writeByte(Manager.MOB_TEMPLATES.size());
            for (MobTemplate temp : Manager.MOB_TEMPLATES) {
                msg.writer().writeByte(temp.type);
                msg.writer().writeUTF(temp.name);
                msg.writer().writeInt(temp.hp);
                msg.writer().writeByte(temp.rangeMove);
                msg.writer().writeByte(temp.speed);
                msg.writer().writeByte(temp.dartType);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // vSkill
    public static void updateSkill(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);

            msg.writer().writeByte(7);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(0); // count skill option

            msg.writer().writeByte(Manager.NCLASS.size());
            for (NClass nClass : Manager.NCLASS) {
                msg.writer().writeUTF(nClass.name);

                msg.writer().writeByte(nClass.skillTemplatess.size());
                for (SkillTemplate skillTemp : nClass.skillTemplatess) {
                    msg.writer().writeByte(skillTemp.id);
                    msg.writer().writeUTF(skillTemp.name);
                    msg.writer().writeByte(skillTemp.maxPoint);
                    msg.writer().writeByte(skillTemp.manaUseType);
                    msg.writer().writeByte(skillTemp.type);
                    msg.writer().writeShort(skillTemp.iconId);
                    msg.writer().writeUTF(skillTemp.damInfo);
                    msg.writer().writeUTF("NRO_MOD");
                    if (skillTemp.id != 0) {
                        msg.writer().writeByte(skillTemp.skillss.size());
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                    } else {
                        // Thêm 2 skill trống 105, 106
                        msg.writer().writeByte(skillTemp.skillss.size() + 2);
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                        for (int i = 105; i <= 106; i++) {
                            msg.writer().writeShort(i);
                            msg.writer().writeByte(0);
                            msg.writer().writeLong(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeInt(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeByte(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeUTF("");
                        }
                    }
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendDataImageVersion(MySession session) {
        Message msg;
        try {
            // msg = new Message(-111);
            // msg.writer().writeShort(0);
            // msg.writer().writeUTF("NRO_MOD");
            // msg.writer().writeByte(0);
            // msg.writer().writeUTF("NRO_MOD");
            // msg.writer().writeByte(1);
            // msg.writer().writeUTF("VuDangCapVaiLonRaMaBanDeoBietThoiDitMeBan");
            // msg.writer().writeByte(2);
            // session.doSendMessage(msg);
            // msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendEffectTemplate(MySession session, int id, int... idtemp) {
        int idT = id;
        if (idtemp.length > 0 && idtemp[0] != 0) {
            idT = idtemp[0];
        }
        Message msg;
        try {
            final byte[] effData = FileIO.readFile("data/effdata/DataEffect_" + idT);
            final byte[] effImg = FileIO.readFile("data/effect/x" + session.zoomLevel + "/ImgEffect_" + idT + ".png");
            if (effData == null || effImg == null) {
                return;
            }
            msg = new Message(-66);
            msg.writer().writeShort(id);
            msg.writer().writeInt(effData.length);
            msg.writer().write(effData);
            if (session.version > 216) {
                msg.writer().write(idT == 60 ? 2 : 0);
            }
            msg.writer().writeInt(effImg.length);
            msg.writer().write(effImg);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendBgItemVersion(MySession session) {
        Message msg;
        try {
            msg = new Message(-93);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeByte(bgItem.id);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendItemBGTemplate(MySession session, int id) {
        Message msg;
        try {
            final byte[] bg_temp = FileIO.readFile("data/item_bg_temp/x" + session.zoomLevel + "/" + id + ".png");
            if (bg_temp == null) {
                return;
            }
            msg = new Message(-32);
            msg.writer().writeShort(id);
            msg.writer().writeInt(bg_temp.length);
            msg.writer().write(bg_temp);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendDataItemBG(MySession session) {
        Message msg;
        try {
            msg = new Message(-31);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeShort(bgItem.idImage);
                msg.writer().writeByte(bgItem.layer);
                msg.writer().writeShort(bgItem.dx);
                msg.writer().writeShort(bgItem.dy);
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendIcon(MySession session, int id) {
        Message msg;
        try {
            final byte[] icon = FileIO.readFile("data/icon/x" + session.zoomLevel + "/" + id + ".png");
            msg = new Message(-67);
            msg.writer().writeInt(id);
            msg.writer().writeInt(icon.length);
            msg.writer().write(icon);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendVersionRes(ISession session) {
        Message msg;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(0);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
    // public static void sendIcon(MySession session, int id) {
    // Message msg;
    // try {
    // byte zoomLevel = session.zoomLevel;
    // IconEncrypt icon = ImageUtil.ICON_IMAGE.get(zoomLevel).getOrDefault(id,
    // null);
    // if (icon == null) {
    // String key = generateRandomKey(15);
    // //byte[] encrypt = encryptImage(new
    // File(String.format("data/girlkun/icon/x%s/%s.png", zoomLevel, id)), key);
    //// File imageFile = new File(String.format("data/icon/x%s/%s.png", zoomLevel,
    // id));
    // File imageFile = new File(String.format("data/icon/x%s/%s.png", zoomLevel,
    // id));
    // if (!imageFile.exists()) {
    // System.err.println("Image file does not exist: " + imageFile.getPath());
    // return;
    // }
    // byte[] encrypt = encryptImage(imageFile, key);
    // if (encrypt == null) {
    // System.err.println("Failed to encrypt image: " + imageFile.getPath());
    // return;
    // }
    // icon = new IconEncrypt();
    // icon.dataImageEncrypt = encrypt;
    // icon.keyDecryptImage = encryptString(key);
    // icon.keyOrigin = key;
    // ImageUtil.ICON_IMAGE.get(zoomLevel).put(id, icon);
    //// Manager.SMALL_VERSION_DATA[session.zoomLevel - 1][id] = (byte)
    // (encrypt.length % 127);
    // }
    // msg = new Message(-67);
    // msg.writer().writeInt(id);
    // msg.writer().writeUTF(icon.keyDecryptImage);
    // msg.writer().writeInt(icon.dataImageEncrypt.length);
    // msg.writer().write(icon.dataImageEncrypt);
    // session.sendMessage(msg);
    // msg.cleanup();
    // } catch (Exception e) {
    //// e.printStackTrace();
    // }
    // }
    // //download data res --------------------------------------------------------
    //
    // public static void sendVersionRes(ISession session) {
    // Message msg;
    // try {
    // msg = new Message(-74);
    // msg.writer().writeByte(0);
    // msg.writer().writeInt(vsRes);
    // msg.writer().writeUTF(ImageUtil.key);
    // session.sendMessage(msg);
    // msg.cleanup();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    public static void sendSmallVersion(MySession session) {
        Message msg;
        try {
            msg = new Message(-77);
            msg.writer().writeShort(maxSmallVersion);
            for (int i = 0; i < maxSmallVersion; i++) {
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void requestMobTemplate(MySession session, int id) {
        Message msg;
        try {
            final byte[] mob = FileIO.readFile("data/mob/x" + session.zoomLevel + "/" + id);
            msg = new Message(11);
            msg.writer().writeByte(id);
            msg.writer().write(mob);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendTileSetInfo(MySession session) {
        Message msg;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_set_info");
            msg = new Message(-82);
            msg.writer().write(data);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // data vẽ map
    public static void sendMapTemp(MySession session, int id) {
        Message msg;
        try {
            final byte[] data = FileIO.readFile("data/map/tile_map_data/" + id);
            if (data == null) {
                return;
            }
            msg = new Message(-28);
            msg.writer().writeByte(10);
            msg.writer().write(data);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // head-avatar
    public static void sendHeadAvatar(Message msg) {
        try {
            msg.writer().writeShort(Manager.HEAD_AVATARS.size());
            for (HeadAvatar ha : Manager.HEAD_AVATARS) {
                msg.writer().writeShort(ha.headId);
                msg.writer().writeShort(ha.avatarId);
            }
        } catch (Exception e) {
        }
    }

    public static void sendImageByName(MySession session, String imgName) {
        Message msg;
        try {
            msg = new Message(66);
            msg.writer().writeUTF(imgName);
            msg.writer().writeByte(Manager.getNFrameImageByName(imgName));
            final byte[] data = FileIO.readFile("data/img_by_name/x" + session.zoomLevel + "/" + imgName + ".png");
            msg.writer().writeInt(data.length);
            msg.writer().write(data);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendSizeRes(MySession session) {
        Message msg;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(1);
            final File[] files = new File("data/res/x" + session.zoomLevel).listFiles();
            if (files != null) {
                msg.writer().writeShort(files.length);
            } else {
                msg.writer().writeShort(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendRes(MySession session) {
        Message msg;
        try {
            // Gửi main resources
            for (final File fileEntry : new File("data/res/x" + session.zoomLevel).listFiles()) {
                String original = fileEntry.getName();
                byte[] res = FileIO.readFile(fileEntry.getAbsolutePath());
                msg = new Message(-74);
                msg.writer().writeByte(2);
                msg.writer().writeUTF(original);
                msg.writer().writeInt(res.length);
                msg.writer().write(res);
                session.sendMessage(msg);
                msg.cleanup();
            }

            // Gửi crop template info TRƯỚC để client có mapping crop ID <-> filename
            sendCropTemplateInfo(session);

            // Gửi farm assets nếu thư mục tồn tại
            sendFarmResources(session);

            msg = new Message(-74);
            msg.writer().writeByte(3);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi tất cả farm resources trong thư mục data/famer/x{zoom}
     */
    public static void sendFarmResources(MySession session) {
        try {
            File farmDir = new File(consts.ConstFarm.getAssetPath(session.zoomLevel));
            if (!farmDir.exists() || !farmDir.isDirectory()) {
                System.out.println("Farm assets directory not found: " + farmDir.getPath() + "\n");
                return;
            }

            File[] files = farmDir.listFiles();
            if (files == null || files.length == 0) {
                return;
            }

            // Gửi từng file farm asset
            for (final File fileEntry : files) {
                if (fileEntry.isFile() && fileEntry.getName().endsWith(".png")) {
                    String fileName = fileEntry.getName();
                    byte[] assetData = FileIO.readFile(fileEntry.getAbsolutePath());

                    if (assetData != null && assetData.length > 0) {
                        Message msg = new Message(-74);
                        msg.writer().writeByte(5); // Sub-type 5 = Farm asset
                        msg.writer().writeUTF("farm/" + fileName);
                        msg.writer().writeInt(assetData.length);
                        msg.writer().write(assetData);
                        session.sendMessage(msg);
                        msg.cleanup();
                    }
                }
            }

            System.out.println("Sent " + files.length + " farm assets to client\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendLinkIP(MySession session) {
        Message msg;
        try {
            msg = new Message(-29);
            msg.writer().writeByte(2);
            msg.writer().writeUTF(LINK_IP_PORT + ",0,0");
            msg.writer().writeByte(1);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // ===================== FARM ASSETS =====================

    /**
     * Gửi farm asset theo zoom level của client
     * 
     * @param session   Session của client
     * @param assetId   ID của asset (có thể là plot, crop stage, icon)
     * @param assetName Tên file asset (không bao gồm path và zoom)
     */
    public static void sendFarmAsset(MySession session, int assetId, String assetName) {
        Message msg;
        try {
            String assetPath = consts.ConstFarm.getAssetPath(session.zoomLevel) + assetName;
            final byte[] assetData = FileIO.readFile(assetPath);
            if (assetData == null) {
                System.err.println("Farm asset not found: " + assetPath);
                return;
            }
            msg = new Message(-58); // Sử dụng message type cho farm assets
            msg.writer().writeByte(10); // Sub-type farm asset
            msg.writer().writeShort(assetId);
            msg.writer().writeInt(assetData.length);
            msg.writer().write(assetData);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi asset cây trồng theo loại cây và giai đoạn, theo zoom level của client
     * 
     * @param session  Session của client
     * @param cropType Loại cây (0-3)
     * @param stage    Giai đoạn (0-6)
     */
    public static void sendCropStageAsset(MySession session, byte cropType, byte stage) {
        try {
            String assetPath = consts.ConstFarm.getCropStageAsset(session.zoomLevel, cropType, stage);
            final byte[] assetData = FileIO.readFile(assetPath);
            if (assetData == null) {
                System.err.println("Crop asset not found: " + assetPath);
                return;
            }
            Message msg = new Message(-58);
            msg.writer().writeByte(11); // Sub-type crop stage asset
            msg.writer().writeByte(cropType);
            msg.writer().writeByte(stage);
            msg.writer().writeInt(assetData.length);
            msg.writer().write(assetData);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi farm icon theo zoom level
     * 
     * @param session  Session của client
     * @param iconName Tên icon (hoe, water, harvest, seed, arrow_down)
     * @param iconId   ID của icon
     */
    public static void sendFarmIcon(MySession session, String iconName, int iconId) {
        try {
            String assetPath = consts.ConstFarm.getIconAsset(session.zoomLevel, iconName);
            final byte[] assetData = FileIO.readFile(assetPath);
            if (assetData == null) {
                System.err.println("Farm icon not found: " + assetPath);
                return;
            }
            Message msg = new Message(-58);
            msg.writer().writeByte(12); // Sub-type farm icon
            msg.writer().writeShort(iconId);
            msg.writer().writeUTF(iconName);
            msg.writer().writeInt(assetData.length);
            msg.writer().write(assetData);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi tất cả farm assets cho client (khi vào map farming)
     * 
     * @param session Session của client
     */
    public static void sendAllFarmAssets(MySession session) {
        // Gửi thông tin Crop Templates cho client
        sendCropTemplateInfo(session);

        // Gửi plot empty
        sendFarmAsset(session, consts.ConstFarm.ICON_PLOT_EMPTY, consts.ConstFarm.ASSET_PLOT_EMPTY);

        // Gửi các icon
        sendFarmIcon(session, "hoe", consts.ConstFarm.ICON_HOE);
        sendFarmIcon(session, "seed", consts.ConstFarm.ICON_SEED);
        sendFarmIcon(session, "water", consts.ConstFarm.ICON_WATER);

        // sendFarmIcon(session, "harvest", consts.ConstFarm.ICON_HARVEST);
        // Gửi thu_hoach (tên file không có prefix icon_)
        try {
            String assetPath = consts.ConstFarm.getAssetPath(session.zoomLevel) + "thu_hoach.png";
            final byte[] assetData = FileIO.readFile(assetPath);
            if (assetData != null) {
                Message msg = new Message(-58);
                msg.writer().writeByte(12); // Sub-type farm icon
                msg.writer().writeShort(consts.ConstFarm.ICON_HARVEST);
                msg.writer().writeUTF("harvest"); // Client key is still "harvest" to keep client logic simple or
                                                  // "thu_hoach" if I change client.
                // Let's keep "harvest" key for the logical identifier, but load "thu_hoach.png"
                msg.writer().writeInt(assetData.length);
                msg.writer().write(assetData);
                session.sendMessage(msg);
                msg.cleanup();
            } else {
                System.err.println("File not found: " + assetPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendFarmIcon(session, "arrow_down_1", consts.ConstFarm.ICON_ARROW_DOWN_1);
        sendFarmIcon(session, "arrow_down_2", consts.ConstFarm.ICON_ARROW_DOWN_2);
        // sendFarmIcon(session, "khung_raucu", consts.ConstFarm.ICON_KHUNG_RAUCU);

        // Gửi khung_raucu (tên file không có prefix icon_)
        try {
            String assetPath = consts.ConstFarm.getAssetPath(session.zoomLevel) + "khung_raucu.png";
            final byte[] assetData = FileIO.readFile(assetPath);
            if (assetData != null) {
                Message msg = new Message(-58);
                msg.writer().writeByte(12); // Sub-type farm icon
                msg.writer().writeShort(consts.ConstFarm.ICON_KHUNG_RAUCU);
                msg.writer().writeUTF("khung_raucu"); // Client key
                msg.writer().writeInt(assetData.length);
                msg.writer().write(assetData);
                session.sendMessage(msg);
                msg.cleanup();
            } else {
                System.err.println("File not found: " + assetPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Gửi icon khóa (khoa_0, khoa_1) cho các ô đất bị khóa
        String[] lockIcons = { "khoa_0", "khoa_1" };
        for (String lockIcon : lockIcons) {
            try {
                String assetPath = consts.ConstFarm.getAssetPath(session.zoomLevel) + lockIcon + ".png";
                final byte[] assetData = FileIO.readFile(assetPath);
                if (assetData != null) {
                    Message msg = new Message(-58);
                    msg.writer().writeByte(12); // Sub-type farm icon
                    msg.writer().writeShort(0); // Icon ID không quan trọng vì dùng tên
                    msg.writer().writeUTF(lockIcon); // Client key: "khoa_0" hoặc "khoa_1"
                    msg.writer().writeInt(assetData.length);
                    msg.writer().write(assetData);
                    session.sendMessage(msg);
                    msg.cleanup();
                } else {
                    System.err.println("Lock icon file not found: " + assetPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Gửi image hạt giống từ database crop_template
        for (CropTemplate crop : CropTemplate.CROP_TEMPLATES) {
            short seedId = crop.seedItemId;
            try {
                models.Template.ItemTemplate seedTemplate = Manager.ITEM_TEMPLATE_MAP.get(seedId);
                if (seedTemplate == null) {
                    System.err.println("Seed template not found for ID: " + seedId);
                    continue;
                }
                short iconId = seedTemplate.iconID;

                String assetPath = "data/icon/x" + session.zoomLevel + "/" + iconId + ".png";
                final byte[] assetData = FileIO.readFile(assetPath);
                if (assetData != null) {
                    Message msg = new Message(-58);
                    msg.writer().writeByte(12); // Sub-type farm icon
                    msg.writer().writeShort(seedId);
                    msg.writer().writeUTF(String.valueOf(seedId)); // Client key
                    msg.writer().writeInt(assetData.length);
                    msg.writer().write(assetData);
                    session.sendMessage(msg);
                    msg.cleanup();
                } else {
                    System.err.println("Seed file not found: " + assetPath);
                }
            } catch (Exception e) {
                Logger.logException(DataGame.class, e);
            }
        }

        for (CropTemplate crop : CropTemplate.CROP_TEMPLATES) {
            byte cropType = crop.id;
            for (byte stage = consts.ConstFarm.STAGE_SEED; stage <= consts.ConstFarm.STAGE_WITHERED; stage++) {
                sendCropStageAsset(session, cropType, stage);
            }
        }
    }

    /**
     * Gửi thông tin các loại cây (Crop Templates) cho client
     */
    public static void sendCropTemplateInfo(MySession session) {
        try {
            Message msg = new Message(-58);
            msg.writer().writeByte(13); // Sub-type: 13 (CROP_TEMPLATE_INFO)
            msg.writer().writeByte(CropTemplate.CROP_TEMPLATES.size());
            for (CropTemplate crop : CropTemplate.CROP_TEMPLATES) {
                msg.writer().writeByte(crop.id);
                msg.writer().writeUTF(crop.name);
                msg.writer().writeShort(crop.seedItemId);
                msg.writer().writeShort(crop.harvestItemId);
                msg.writer().writeUTF(crop.imgYoung != null ? crop.imgYoung : "");
                msg.writer().writeUTF(crop.imgMature != null ? crop.imgMature : "");
                msg.writer().writeUTF(crop.imgWithered != null ? crop.imgWithered : "");
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
