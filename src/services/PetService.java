package services;

/*
 *
 *
 * @author EMTI
 */
import consts.ConstPlayer;
import player.NewPet;
import player.Pet;
import player.Player;
import services.func.ChangeMapService;
import utils.SkillUtil;
import utils.Util;

public class PetService {

    private static PetService instance;

    public static PetService gI() {
        if (instance == null) {
            instance = new PetService();
        }
        return instance;
    }

    public void createNormalPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận làm đệ tử");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void createNormalPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createMabuPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, true, false, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Oa oa oa...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createMabuPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, true, false, false, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Oa oa oa...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createBeerusPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, true, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Black goku đây quỳ mẹ mày xuống!!!...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createBeerusPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, true, false, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Hủy diệt đi, tao mệt rồi!...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createPicPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, true, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Sư Phụ SooMe hiện thân tụi m quỳ xuống...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createPicPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, true, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Sư Phụ SooMe hiện thân tụi m quỳ xuống...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createBlackPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, true);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet,
                        "Ta sẽ cho người biết sức mạnh của một vị thần là như thế nào !");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createBlackPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, true, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                    player.pet.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet,
                        "Ta sẽ cho người biết sức mạnh của một vị thần là như thế nào !");
            } catch (Exception e) {
            }
        }).start();
    }

    public void changeNormalPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createNormalPet(player, gender, limitPower);
    }

    public void changeNormalPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createNormalPet(player, limitPower);
    }

    public void changeMabuPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuPet(player, limitPower);
    }

    public void changeMabuPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuPet(player, gender, limitPower);
    }

    public void changeBeerusPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createBeerusPet(player, limitPower);
    }

    public void changeBeerusPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createBeerusPet(player, gender, limitPower);
    }

    public void changePicPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createPicPet(player, limitPower);
    }

    public void changePicPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createPicPet(player, gender, limitPower);
    }

    public void changeBlackPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createBlackPet(player, limitPower);
    }

    public void changeBlackPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createBlackPet(player, gender, limitPower);
    }

    public void changeNamePet(Player player, String name) {
        try {
            if (!InventoryService.gI().isExistItemBag(player, 400)) {
                Service.gI().sendThongBao(player, "Bạn cần thẻ đặt tên đệ tử, mua tại Santa");
                return;
            } else if (Util.haveSpecialCharacter(name)) {
                Service.gI().sendThongBao(player, "Tên không được chứa ký tự đặc biệt");
                return;
            } else if (name.length() > 10) {
                Service.gI().sendThongBao(player, "Tên quá dài");
                return;
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.name = "$" + name.toLowerCase().trim();
            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, 400), 1);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Service.gI().chatJustForMe(player, player.pet, "Cảm ơn sư phụ đã đặt cho con tên " + name);
                } catch (Exception e) {
                }
            }).start();
        } catch (Exception ex) {

        }
    }

    private int[] getDataPetNormal() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; // hp
        petData[1] = Util.nextInt(40, 105) * 20; // mp
        petData[2] = Util.nextInt(20, 45); // dame
        petData[3] = Util.nextInt(9, 50); // def
        petData[4] = Util.nextInt(0, 2); // crit
        return petData;
    }

    private int[] getDataPetMabu() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; // hp
        petData[1] = Util.nextInt(40, 105) * 20; // mp
        petData[2] = Util.nextInt(50, 120); // dame
        petData[3] = Util.nextInt(9, 50); // def
        petData[4] = Util.nextInt(0, 2); // crit
        return petData;
    }

    private int[] getDataPetPic() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 115) * 20; // hp
        petData[1] = Util.nextInt(40, 115) * 20; // mp
        petData[2] = Util.nextInt(70, 140); // dame
        petData[3] = Util.nextInt(9, 50); // def
        petData[4] = Util.nextInt(0, 2); // crit
        return petData;
    }

    public void createPetFideNhi(Player player, boolean isChange, byte gender) {
        byte limitPower;
        if (isChange) {
            limitPower = player.pet.nPoint.limitPower;
            if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                player.pet.unFusion();
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.dispose();
            player.pet = null;
        } else {
            limitPower = 1;
        }
        new Thread(() -> {
            try {
                Pet pet = new Pet(player);
                pet.name = "$Fide Nhí";
                pet.gender = gender;
                pet.id = -player.id;
                pet.nPoint.power = 1500000;
                pet.typePet = 5;
                pet.nPoint.stamina = (short) 1000;
                pet.nPoint.maxStamina = (short) 1000;
                pet.nPoint.hpg = Util.nextInt(2000, 5000);
                pet.nPoint.mpg = Util.nextInt(2000, 5000);
                pet.nPoint.hpMax = Util.nextInt(2000, 5000);
                pet.nPoint.mpMax = Util.nextInt(2000, 5000);
                pet.nPoint.dameg = Util.nextInt(200, 300);
                pet.nPoint.defg = Util.nextInt(10, 30);
                pet.nPoint.critg = 5;
                for (int i = 0; i < 8; i++) {
                    pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                int skillId[] = { 9, 4, 17 };
                pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
                for (int i = 0; i < 5; i++) {
                    pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
                }
                pet.nPoint.setFullHpMp();
                player.pet = pet;
                player.pet.nPoint.limitPower = limitPower;
                player.pointfusion.setHpFusion(Util.nextInt(20, 30));
                player.pointfusion.setMpFusion(Util.nextInt(20, 30));
                player.pointfusion.setDameFusion(Util.nextInt(20, 30));
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "\b|1|Con đây sư phụ ơi!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void createPetCellNhi(Player player, boolean isChange, byte gender) {
        byte limitPower;
        if (isChange) {
            limitPower = player.pet.nPoint.limitPower;
            if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                player.pet.unFusion();
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.dispose();
            player.pet = null;
        } else {
            limitPower = 1;
        }
        new Thread(() -> {
            try {
                Pet pet = new Pet(player);
                pet.name = "$Cell Nhí";
                pet.gender = gender;
                pet.id = -player.id;
                pet.nPoint.power = 1500000;
                pet.typePet = 6;
                pet.nPoint.stamina = (short) 1000;
                pet.nPoint.maxStamina = (short) 1000;
                pet.nPoint.hpg = Util.nextInt(2000, 5000);
                pet.nPoint.mpg = Util.nextInt(2000, 5000);
                pet.nPoint.hpMax = Util.nextInt(2000, 5000);
                pet.nPoint.mpMax = Util.nextInt(2000, 5000);
                pet.nPoint.dameg = Util.nextInt(200, 300);
                pet.nPoint.defg = Util.nextInt(25, 50);
                pet.nPoint.critg = 5;
                for (int i = 0; i < 8; i++) {
                    pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                int skillId[] = { 9, 4, 17 };
                pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
                for (int i = 0; i < 5; i++) {
                    pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
                }
                pet.nPoint.setFullHpMp();
                player.pet = pet;
                player.pet.nPoint.limitPower = limitPower;
                player.pointfusion.setHpFusion(Util.nextInt(25, 45));
                player.pointfusion.setMpFusion(Util.nextInt(25, 45));
                player.pointfusion.setDameFusion(Util.nextInt(25, 45));
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "\b|1|Con đây sư phụ ơi!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void createPetBuuNhi(Player player, boolean isChange, byte gender) {
        byte limitPower;
        if (isChange) {
            limitPower = player.pet.nPoint.limitPower;
            if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                player.pet.unFusion();
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.dispose();
            player.pet = null;
        } else {
            limitPower = 1;
        }
        new Thread(() -> {
            try {
                Pet pet = new Pet(player);
                pet.name = "$Bưu Nhí";
                pet.gender = gender;
                pet.id = -player.id;
                pet.nPoint.power = 1500000;
                pet.typePet = 7;
                pet.nPoint.stamina = (short) 1000;
                pet.nPoint.maxStamina = (short) 1000;
                pet.nPoint.hpg = Util.nextInt(2000, 5000);
                pet.nPoint.mpg = Util.nextInt(2000, 5000);
                pet.nPoint.hpMax = Util.nextInt(2000, 5000);
                pet.nPoint.mpMax = Util.nextInt(2000, 5000);
                pet.nPoint.dameg = Util.nextInt(200, 300);
                pet.nPoint.defg = Util.nextInt(50, 100);
                pet.nPoint.critg = 15;
                for (int i = 0; i < 8; i++) {
                    pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                int skillId[] = { 9, 4, 17 };
                pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
                for (int i = 0; i < 5; i++) {
                    pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
                }
                pet.nPoint.setFullHpMp();
                player.pet = pet;
                player.pet.nPoint.limitPower = limitPower;
                player.pointfusion.setHpFusion(Util.nextInt(40, 55));
                player.pointfusion.setMpFusion(Util.nextInt(40, 55));
                player.pointfusion.setDameFusion(Util.nextInt(40, 55));
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "\b|1|Con đây sư phụ ơi!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void createPetAdrBeach(Player player, boolean isChange, byte gender) {
        byte limitPower;
        if (isChange) {
            limitPower = player.pet.nPoint.limitPower;
            if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                player.pet.unFusion();
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.dispose();
            player.pet = null;
        } else {
            limitPower = 1;
        }
        new Thread(() -> {
            try {
                Pet pet = new Pet(player);
                pet.name = "$Adr Bãi biển";
                pet.gender = gender;
                pet.id = -player.id;
                pet.nPoint.power = 1500000;
                pet.typePet = 8;
                pet.nPoint.stamina = (short) 1000;
                pet.nPoint.maxStamina = (short) 1000;
                pet.nPoint.hpg = Util.nextInt(2000, 5000);
                pet.nPoint.mpg = Util.nextInt(2000, 5000);
                pet.nPoint.hpMax = Util.nextInt(2000, 5000);
                pet.nPoint.mpMax = Util.nextInt(2000, 5000);
                pet.nPoint.dameg = Util.nextInt(200, 300);
                pet.nPoint.defg = Util.nextInt(50, 100);
                pet.nPoint.critg = 15;
                for (int i = 0; i < 8; i++) {
                    pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                int skillId[] = { 9, 4, 17 };
                pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
                for (int i = 0; i < 5; i++) {
                    pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
                }
                pet.nPoint.setFullHpMp();
                player.pet = pet;
                player.pet.nPoint.limitPower = limitPower;
                player.pointfusion.setHpFusion(Util.nextInt(40, 60));
                player.pointfusion.setMpFusion(Util.nextInt(40, 60));
                player.pointfusion.setDameFusion(Util.nextInt(40, 60));
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "\b|1|Con đây sư phụ ơi!!!");
            } catch (Exception e) {// Zalo: 0358124452//Name: EMTI
                e.printStackTrace();
            }
        }).start();
    }

    public void createPetBerrusNhi(Player player, boolean isChange, byte gender) {
        byte limitPower;
        if (isChange) {
            limitPower = player.pet.nPoint.limitPower;
            if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                player.pet.unFusion();
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.dispose();
            player.pet = null;
        } else {
            limitPower = 1;
        }
        new Thread(() -> {
            try {
                Pet pet = new Pet(player);
                pet.name = "$Black Goku";
                pet.gender = gender;
                pet.id = -player.id;
                pet.nPoint.power = 1500000;
                pet.typePet = 9;
                pet.nPoint.stamina = (short) 1000;
                pet.nPoint.maxStamina = (short) 1000;
                pet.nPoint.hpg = Util.nextInt(2000, 5000);
                pet.nPoint.mpg = Util.nextInt(2000, 5000);
                pet.nPoint.hpMax = Util.nextInt(2000, 5000);
                pet.nPoint.mpMax = Util.nextInt(2000, 5000);
                pet.nPoint.dameg = Util.nextInt(200, 300);
                pet.nPoint.defg = Util.nextInt(50, 100);
                pet.nPoint.critg = 15;
                for (int i = 0; i < 8; i++) {
                    pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                int skillId[] = { 9, 4, 17 };
                pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
                for (int i = 0; i < 5; i++) {
                    pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
                }
                pet.nPoint.setFullHpMp();
                player.pet = pet;
                player.pet.nPoint.limitPower = limitPower;
                player.pointfusion.setHpFusion(Util.nextInt(45, 80));
                player.pointfusion.setMpFusion(Util.nextInt(45, 80));
                player.pointfusion.setDameFusion(Util.nextInt(45, 80));
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "\b|1|Con đây sư phụ ơi!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void createPetMabuGay(Player player, boolean isChange, byte gender) {
        byte limitPower;
        if (isChange) {
            limitPower = player.pet.nPoint.limitPower;
            if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                player.pet.unFusion();
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.dispose();
            player.pet = null;
        } else {
            limitPower = 1;
        }
        new Thread(() -> {
            try {
                Pet pet = new Pet(player);
                pet.name = "$Mabu gầy";
                pet.gender = gender;
                pet.id = -player.id;
                pet.nPoint.power = 1500000;
                pet.typePet = 10;
                pet.nPoint.stamina = (short) 1000;
                pet.nPoint.maxStamina = (short) 1000;
                pet.nPoint.hpg = Util.nextInt(2000, 5000);
                pet.nPoint.mpg = Util.nextInt(2000, 5000);
                pet.nPoint.hpMax = Util.nextInt(2000, 5000);
                pet.nPoint.mpMax = Util.nextInt(2000, 5000);
                pet.nPoint.dameg = Util.nextInt(200, 300);
                pet.nPoint.defg = Util.nextInt(50, 100);
                pet.nPoint.critg = 15;
                for (int i = 0; i < 8; i++) {
                    pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
                }
                int skillId[] = { 9, 4, 17 };
                pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
                for (int i = 0; i < 5; i++) {
                    pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
                }
                pet.nPoint.setFullHpMp();
                player.pet = pet;
                player.pet.nPoint.limitPower = limitPower;
                player.pointfusion.setHpFusion(Util.nextInt(45, 80));
                player.pointfusion.setMpFusion(Util.nextInt(45, 80));
                player.pointfusion.setDameFusion(Util.nextInt(45, 80));
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "\b|1|Con đây sư phụ ơi!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createNewPet(Player player, boolean isMabu, boolean isBeerus, boolean isPic, boolean isBlack,
            byte... gender) {
        int[] data = isMabu ? isPic ? getDataPetMabu() : getDataPetPic() : getDataPetNormal();
        Pet pet = new Pet(player);
        pet.name = "$" + (isMabu ? "Mabư" : isBeerus ? "Black Goku" : isPic ? "Pic" : isBlack ? "Black" : "Đệ tử");
        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);
        pet.id = player.isPl() ? -player.id : -Math.abs(player.id) - 100000;
        pet.nPoint.power = isMabu || isBeerus || isPic || isBlack ? 1500000 : 2000;
        pet.typePet = (byte) (isMabu ? 1 : isBeerus ? 2 : isPic ? 3 : isBlack ? 4 : 0);
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.hpMax = data[0];
        pet.nPoint.mpMax = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int i = 0; i < 8; i++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        int skillId[] = { 9, 4, 17 };
        pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
        for (int i = 0; i < 5; i++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        player.pet = pet;
    }

    public void createNormalPetSuperGender(Player player, int gender, byte type) {
        new Thread(() -> {
            try {
                createNewPetSuperGender(player, (byte) gender, type);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận làm đệ tử");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void createNormalPetSuper(Player player, int gender, byte type) {
        new Thread(() -> {
            try {
                createNewPetSuper(player, (byte) gender, type);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận làm đệ tử");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createNewPetSuper(Player player, byte gender, byte type) {
        int[] data = getDataPetNormal();
        Pet pet = new Pet(player);
        if (type == 2) {
            pet.name = "$" + "Songoku";
        } else if (type == 3) {
            pet.name = "$" + "Vegeta";
        } else if (type == 4) {
            pet.name = "$" + "Picolo";
        } else {
            pet.name = "$" + "Mabu";
        }

        pet.gender = (byte) Util.nextInt(0, 2);
        pet.id = -player.id;
        pet.nPoint.power = 1500000;
        pet.typePet = type;
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.hpMax = data[0];
        pet.nPoint.mpMax = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int i = 0; i < 8; i++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        int skillId[] = { 9, 4, 17 };
        pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
        for (int i = 0; i < 5; i++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        player.pet = pet;
        player.pointfusion.setHpFusion(0);
        player.pointfusion.setMpFusion(0);
        player.pointfusion.setDameFusion(0);
    }

    private void createNewPetSuperGender(Player player, byte gender, byte type) {
        int[] data = getDataPetNormal();
        Pet pet = new Pet(player);
        pet.name = "$" + (type == 1 ? "[Broly]Mabư"
                : type == 2 ? "Songoku" : type == 3 ? "Vegeta" : type == 4 ? "Fide" : "[Broly]Đệ tử");
        pet.gender = (byte) Util.nextInt(0, 2);
        pet.id = -player.id;
        pet.gender = player.gender;
        pet.nPoint.power = 1500000;
        pet.typePet = type;
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.hpMax = data[0];
        pet.nPoint.mpMax = data[1];

        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int i = 0; i < 8; i++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        int skillId[] = { 9, 4, 17 };
        pet.playerSkill.skills.add(SkillUtil.createSkill(skillId[Util.nextInt(0, 2)], 1));
        for (int i = 0; i < 5; i++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        player.pet = pet;
        player.pointfusion.setHpFusion(0);
        player.pointfusion.setMpFusion(0);
        player.pointfusion.setDameFusion(0);
    }

    public static void Pet2(Player pl, int h, int b, int l) {
        if (pl.newPet != null) {
            pl.newPet.dispose();
        }
        pl.newPet = new NewPet(pl, (short) h, (short) b, (short) l);
        pl.newPet.name = "$";
        pl.newPet.gender = pl.gender;
        pl.newPet.nPoint.tiemNang = 1;
        pl.newPet.nPoint.power = 1;
        pl.newPet.nPoint.limitPower = 1;
        pl.newPet.nPoint.hpg = 500000;
        pl.newPet.nPoint.mpg = 500000;
        pl.newPet.nPoint.hp = 500000;
        pl.newPet.nPoint.mp = 500000;
        pl.newPet.nPoint.dameg = 1;
        pl.newPet.nPoint.defg = 1;
        pl.newPet.nPoint.critg = 1;
        pl.newPet.nPoint.stamina = 1;
        pl.newPet.nPoint.setBasePoint();
        pl.newPet.nPoint.setFullHpMp();
    }

    public void deletePet(Player player) {
        Pet pet = player.pet;
        if (pet != null) {
            if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                pet.unFusion();
            }
            ChangeMapService.gI().exitMap(pet);
            pet.dispose();
            player.pet = null;
        }
    }
}
