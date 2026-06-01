package utils;

/*
 *
 *
 * @author EMTI
 */

import java.util.List;
import player.Player;
import skill.NClass;
import skill.Skill;
import models.Template.SkillTemplate;
import server.Manager;

public class SkillUtil {

    private final static NClass nClassTD;
    private final static NClass nClassNM;
    private final static NClass nClassXD;

    static {
        nClassTD = Manager.NCLASS.get(0);
        nClassNM = Manager.NCLASS.get(1);
        nClassXD = Manager.NCLASS.get(2);
    }

    public static Skill createSkill(int tempId, int level) {
        Skill skill = null;
        SkillTemplate template = findSkillTemplate(tempId);
        if (template != null) {
            List<Skill> skills = template.skillss;
            if (skills != null && level >= 1 && level <= skills.size()) {
                skill = skills.get(level - 1);
            }
        }
        return skill != null ? new Skill(skill) : null;
    }

    public static SkillTemplate findSkillTemplate(int tempId) {
        SkillTemplate template = nClassTD.getSkillTemplate(tempId);
        if (template == null) {
            template = nClassNM.getSkillTemplate(tempId);
        }
        if (template == null) {
            template = nClassXD.getSkillTemplate(tempId);
        }
        return template;
    }

    public static Skill createEmptySkill() {
        Skill skill = new Skill();
        skill.skillId = -1;
        return skill;
    }

    public static Skill createSkillLevel0(int tempId) {
        Skill skill = createEmptySkill();
        skill.template = new SkillTemplate();
        skill.template.id = (byte) tempId;
        return skill;
    }

    public static boolean isUseSkillDam(Player player) {
        int skillId = player.playerSkill.skillSelect.template.id;
        return (skillId == Skill.DRAGON || skillId == Skill.DEMON
                || skillId == Skill.GALICK || skillId == Skill.KAIOKEN
                || skillId == Skill.LIEN_HOAN);
    }

    public static boolean isUseSkillChuong(Player player) {
        int skillId = player.playerSkill.skillSelect.template.id;
        return (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC);
    }

    public static int getTimeMonkey(int level) { // thời gian tồn tại khỉ v
        return (level + 5) * 10000;
    }

    public static int getTimeBNVC(int level) { // thời gian tồn tại BNVC
        return (level + 5) * 1000;
    }

    public static int getPercentHpMonkey(int level) { // tỉ lệ máu khỉ cộng thêm v
        return (level + 3) * 10;
    }

    public static int getPercentDameMonkey(int level) { // tỉ lệ dam khỉ cộng thêm v
        return (level + 3);
    }

    public static int getTimeStun(int level) { // thời gian choáng thái dương hạ san v
        return (level + 2) * 1000;
    }

    public static int getTimeSocola() {
        return 30000;
    }

    public static int getTimeShield(int level) { // thời gian tồn tại khiên v
        return (level + 2) * 5000;
    }

    public static int getTimeTanHinh(Skill skill) {
        if (skill == null)
            return 2000;
        return skill.timeTangHinh > 0 ? skill.timeTangHinh : 2000 + skill.point * 1000;
    }

    public static int getTimeChoangTanHinh(Skill skill) {
        if (skill == null)
            return 2000;
        return skill.timeChoang > 0 ? skill.timeChoang : 1000 + skill.point * 1000;
    }

    public static int getTiLeChoangTanHinh(Skill skill) {
        if (skill == null)
            return 50;
        return skill.tiLeChoang > 0 ? skill.tiLeChoang : 45 + skill.point * 5;
    }

    public static int getTimeTroi(int level) { // thời gian trói v
        return level * 5000;
    }

    public static int getTimeDCTT(int level) { // thời gian choáng dịch chuyển tức thời v
        return (level + 1) * 500;
    }

    public static int getTimeThoiMien(int level) { // thời gian thôi miên
        return (level + 4) * 1000;
    }

    public static int getRangeStun(int level) { // phạm vi thái dương hạ san
        return 120 + level * 30;
    }

    public static int getRangeBom(int level) { // phạm vi tự sát
        return 400 + level * 30;
    }

    public static int getRangeQCKK(int level) { // phạm vi quả cầu kênh khi
        return 350 + level * 30;
    }

    public static int getPercentHPHuytSao(int level) { // tỉ lệ máu huýt sáo cộng thêm v
        return (level + 3) * 10;
    }

    public static int getPercentTriThuong(int level) { // tỉ lệ máu hồi phục trị thương v
        return (level + 9) * 5;
    }

    public static int getPercentCharge(int level) { // tỉ lệ hp ttnl
        return level + 3;
    }

    public static int getTempMobMe(int level) { // template đẻ trứng
        int[] temp = { 8, 11, 32, 25, 43, 49, 50 };
        return temp[level - 1];
    }

    public static int getTimeSurviveMobMe(int level) { // thời gian trứng tồn tại
        return getTimeMonkey(level) * 2;
    }

    public static int getPercentHpBienHinh(int level) { // tỉ lệ máu khỉ cộng thêm v
        return (level + 1) * 2;
    }

    public static int getTimeBienHinh(boolean lastLevel, int coolDown) {// thời gian hồi biến hình
        int per = lastLevel ? 50 : 50;
        return coolDown * per / 100;
    }

    public static long getHPMobMe(long hpMaxPlayer, int level) { // lấy hp max của đệ trứng theo hp max player
        long[] perHPs = { 30, 40, 50, 60, 70, 80, 90 };
        return hpMaxPlayer * perHPs[level - 1] / 100L;
    }

    public static Skill getSkillbyId(Player player, int id) {
        for (Skill skill : player.playerSkill.skills) {
            if (skill.template.id == id) {
                return skill;
            }
        }
        return null;
    }

    public static int getTimeSuper(int level) {
        switch (level) {
            case 1:
                return 60000;
            case 2:
                return 90000;
            case 3:
                return 120000;
            case 4:
                return 180000;
            case 5:
                return 240000;
            case 6:
                return 300000;
        }
        return 0;
    }

    public static boolean upSkillPet(List<Skill> skills, int index) {
        int tempId = skills.get(index).template.id;
        int level = skills.get(index).point + 1;
        if (level > 7) {
            return false;
        }
        Skill skill = null;
        SkillTemplate template = findSkillTemplate(tempId);
        if (template != null) {
            List<Skill> skillss = template.skillss;
            if (skillss != null && level >= 1 && level <= skillss.size()) {
                skill = skillss.get(level - 1);
            }
        }
        skill = new Skill(skill);
        if (index == 1) {
            skill.coolDown = 1000;
        }
        skills.set(index, skill);
        return true;
    }

    public static byte getTempSkillSkillByItemID(int id) {
        if (id >= 66 && id <= 72) {
            return Skill.DRAGON;
        } else if (id >= 79 && id <= 84 || id == 86) {
            return Skill.DEMON;
        } else if (id >= 87 && id <= 93) {
            return Skill.GALICK;
        } else if (id >= 94 && id <= 100) {
            return Skill.KAMEJOKO;
        } else if (id >= 101 && id <= 107) {
            return Skill.MASENKO;
        } else if (id >= 108 && id <= 114) {
            return Skill.ANTOMIC;
        } else if (id >= 115 && id <= 121) {
            return Skill.THAI_DUONG_HA_SAN;
        } else if (id >= 122 && id <= 128) {
            return Skill.TRI_THUONG;
        } else if (id >= 129 && id <= 135) {
            return Skill.TAI_TAO_NANG_LUONG;
        } else if (id >= 300 && id <= 306) {
            return Skill.KAIOKEN;
        } else if (id >= 307 && id <= 313) {
            return Skill.QUA_CAU_KENH_KHI;
        } else if (id >= 314 && id <= 320) {
            return Skill.BIEN_KHI;
        } else if (id >= 321 && id <= 327) {
            return Skill.TU_SAT;
        } else if (id >= 328 && id <= 334) {
            return Skill.MAKANKOSAPPO;
        } else if (id >= 335 && id <= 341) {
            return Skill.DE_TRUNG;
        } else if (id >= 434 && id <= 440) {
            return Skill.KHIEN_NANG_LUONG;
        } else if (id >= 474 && id <= 480) {
            return Skill.SOCOLA;
        } else if (id >= 481 && id <= 487) {
            return Skill.LIEN_HOAN;
        } else if (id >= 488 && id <= 494) {
            return Skill.DICH_CHUYEN_TUC_THOI;
        } else if (id >= 495 && id <= 501) {
            return Skill.THOI_MIEN;
        } else if (id >= 502 && id <= 508) {
            return Skill.TROI;
        } else if (id >= 509 && id <= 515) {
            return Skill.HUYT_SAO;
        } else if (id >= 1865 && id <= 1871) {
            return Skill.PHAN_THAN;
        } else if (id >= 1905 && id <= 1922) {
            return Skill.BIEN_HINH_SUPER;
        } else if (id >= 1991 && id <= 1997) {
            return Skill.TANG_HINH;
        } else {
            return -1;
        }
    }

    public static byte getLevelSkillByItemID(int tempId) {
        switch (tempId) {
            // TRÁI ĐẤT
            case 1334:
            case 1340:
            case 1346:
            case 1356:
            case 1363:
            case 1370:
            case 1905: // sách trái đất lv1
            case 1911: // sách namec lv1
            case 1917: // sách xayda lv1
                return 1;
            case 1335:
            case 1341:
            case 1347:
            case 1357:
            case 1364:
            case 1371:
            case 1906: // sách trái đất lv2
            case 1912: // sách namec lv2
            case 1918: // sách xayda lv2
                return 2;
            case 1336:
            case 1342:
            case 1348:
            case 1358:
            case 1365:
            case 1372:
            case 1907: // sách trái đất lv3
            case 1913: // sách namec lv3
            case 1919: // sách xayda lv3
                return 3;
            case 1337:
            case 1343:
            case 1349:
            case 1359:
            case 1366:
            case 1373:
            case 1908: // sách trái đất lv4
            case 1914: // sách namec lv4
            case 1920: // sách xayda lv4
                return 4;
            case 1338:
            case 1344:
            case 1350:
            case 1360:
            case 1367:
            case 1374:
            case 1909: // sách trái đất lv5
            case 1915: // sách namec lv5
            case 1921: // sách xayda lv5
                return 5;
            case 1339:
            case 1345:
            case 1351:
            case 1361:
            case 1368:
            case 1375:
            case 1910: // sách trái đất lv6
            case 1916: // sách namec lv6
            case 1922: // sách xayda lv6
                return 6;
            case 1362:
            case 1369:
            case 1376:
                return 7;
            // Sách Tàng Hình
            case 1991:
                return 1;
            case 1992:
                return 2;
            case 1993:
                return 3;
            case 1994:
                return 4;
            case 1995:
                return 5;
            case 1996:
                return 6;
            case 1997:
                return 7;
        }
        return -1;
    }

    public static Skill getSkillByItemID(Player pl, int tempId) {
        if (tempId >= 66 && tempId <= 72) {
            return getSkillbyId(pl, Skill.DRAGON);
        } else if (tempId >= 79 && tempId <= 84 || tempId == 86) {
            return getSkillbyId(pl, Skill.DEMON);
        } else if (tempId >= 87 && tempId <= 93) {
            return getSkillbyId(pl, Skill.GALICK);
        } else if (tempId >= 94 && tempId <= 100) {
            return getSkillbyId(pl, Skill.KAMEJOKO);
        } else if (tempId >= 101 && tempId <= 107) {
            return getSkillbyId(pl, Skill.MASENKO);
        } else if (tempId >= 108 && tempId <= 114) {
            return getSkillbyId(pl, Skill.ANTOMIC);
        } else if (tempId >= 115 && tempId <= 121) {
            return getSkillbyId(pl, Skill.THAI_DUONG_HA_SAN);
        } else if (tempId >= 122 && tempId <= 128) {
            return getSkillbyId(pl, Skill.TRI_THUONG);
        } else if (tempId >= 129 && tempId <= 135) {
            return getSkillbyId(pl, Skill.TAI_TAO_NANG_LUONG);
        } else if (tempId >= 300 && tempId <= 306) {
            return getSkillbyId(pl, Skill.KAIOKEN);
        } else if (tempId >= 307 && tempId <= 313) {
            return getSkillbyId(pl, Skill.QUA_CAU_KENH_KHI);
        } else if (tempId >= 314 && tempId <= 320) {
            return getSkillbyId(pl, Skill.BIEN_KHI);
        } else if (tempId >= 321 && tempId <= 327) {
            return getSkillbyId(pl, Skill.TU_SAT);
        } else if (tempId >= 328 && tempId <= 334) {
            return getSkillbyId(pl, Skill.MAKANKOSAPPO);
        } else if (tempId >= 335 && tempId <= 341) {
            return getSkillbyId(pl, Skill.DE_TRUNG);
        } else if (tempId >= 434 && tempId <= 440) {
            return getSkillbyId(pl, Skill.KHIEN_NANG_LUONG);
        } else if (tempId >= 474 && tempId <= 480) {
            return getSkillbyId(pl, Skill.SOCOLA);
        } else if (tempId >= 481 && tempId <= 487) {
            return getSkillbyId(pl, Skill.LIEN_HOAN);
        } else if (tempId >= 488 && tempId <= 494) {
            return getSkillbyId(pl, Skill.DICH_CHUYEN_TUC_THOI);
        } else if (tempId >= 495 && tempId <= 501) {
            return getSkillbyId(pl, Skill.THOI_MIEN);
        } else if (tempId >= 502 && tempId <= 508) {
            return getSkillbyId(pl, Skill.TROI);
        } else if (tempId >= 509 && tempId <= 515) {
            return getSkillbyId(pl, Skill.HUYT_SAO);
        } else if (tempId == 251003) {
            switch (pl.gender) {
                case 0:
                    return getSkillbyId(pl, Skill.SUPER_KAME);
                case 1:
                    return getSkillbyId(pl, Skill.MA_PHONG_BA);
                case 2:
                    return getSkillbyId(pl, Skill.LIEN_HOAN_CHUONG);
            }
            return null;
        } else if (tempId >= 1865 && tempId <= 1871) {
            Skill skill = getSkillbyId(pl, Skill.PHAN_THAN);
            if (skill != null) {
                return skill;
            }
            return createSkillLevel0(Skill.PHAN_THAN);
        } else if (tempId >= 1905 && tempId <= 1922) {
            Skill skill = getSkillbyId(pl, Skill.BIEN_HINH_SUPER);
            if (skill != null) {
                return skill;
            }
            return createSkillLevel0(Skill.BIEN_HINH_SUPER);
        } else if (tempId >= 1991 && tempId <= 1997) {
            Skill skill = getSkillbyId(pl, Skill.TANG_HINH);
            if (skill != null) {
                return skill;
            }
            return createSkillLevel0(Skill.TANG_HINH);
        } else {
            return null;
        }
    }

    public static void setSkill(Player pl, Skill skill) {
        boolean checkskill = false;
        for (int i = 0; i < pl.playerSkill.skills.size(); i++) {
            if (pl.playerSkill.skills.get(i).template.id == skill.template.id) {
                checkskill = true;
                pl.playerSkill.skills.set(i, skill);
                break;
            }
        }
        if (!checkskill) {
            pl.playerSkill.skills.add(skill);
        }
    }

    public static byte getTyleSkillAttack(Skill skill) {
        switch (skill.template.id) {
            case Skill.TRI_THUONG:
                return 2;
            case Skill.KAMEJOKO:
            case Skill.MASENKO:
            case Skill.ANTOMIC:
                return 1;
            default:
                return 0;
        }
    }

    public static int getPercentPhanThan(Player player) {
        try {
            if (player.isClone) {
                Player master = ((player.PlayerClone) player).master;
                Skill skill = getSkillbyId(master, Skill.PHAN_THAN);
                int level = skill != null ? skill.point : 1;
                return 60 + (level * 10);
            }
            if (player.playerSkill.skillSelect.template.id == Skill.PHAN_THAN) {
                return 60 + (player.playerSkill.skillSelect.point * 10);
            }
        } catch (Exception e) {
        }
        return 10;
    }

    public static int getPercentDameSuper(int level) {
        return level * 5;
    }

    public static int getPercentHpSuper(int level) {
        return level * 10;
    }

    public static int getPercentMpSuper(int level) {
        return level * 10;
    }

    public static int getPercentCritSuper(int level) {
        return level * 2;
    }
}
