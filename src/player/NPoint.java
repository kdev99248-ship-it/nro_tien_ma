package player;

import models.Card.Card;
import models.Card.OptionCard;
import consts.ConstPlayer;
import consts.ConstRatio;
import intrinsic.Intrinsic;
import item.Item;
import item.Item.ItemOption;
import player.badges.BagesTemplate;
import skill.Skill;
import server.Manager;
import services.EffectSkillService;
import services.ItemService;
import services.MapService;
import services.PlayerService;
import services.Service;
import services.TaskService;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

import jdbc.daos.EventDAO;
import lombok.Setter;
import mob.Mob;
import power.PowerLimit;
import power.PowerLimitManager;
import services.func.ChangeMapService;
import utils.TimeUtil;

public class NPoint {

    public static final byte MAX_LIMIT = 13;

    @Setter
    private Player player;;
    public boolean diexinbato;
    public boolean diexoihecquen;
    public boolean xoihecquen;
    public boolean isxinbato;

    public NPoint(Player player) {
        this.player = player;
        this.tlHp = new ArrayList<>();
        this.tlMp = new ArrayList<>();
        this.tlDef = new ArrayList<>();
        this.tlDame = new ArrayList<>();
        this.tlDameAttMob = new ArrayList<>();
        this.tlTNSM = new ArrayList<>();
        this.tlDameCrit = new ArrayList<>();
    }

    public boolean isCrit;
    public boolean isCrit100;
    public boolean isCritTele;

    private Intrinsic intrinsic;
    private int percentDameIntrinsic;
    public long dameAfter;
    private PowerLimit powerLimit;
    /*-----------------------Chỉ số cơ bản------------------------------------*/
    public byte numAttack;
    public short stamina, maxStamina;

    public byte limitPower;
    public long power;
    public long tiemNang;

    public long hp, hpMax, hpg;
    public long mp, mpMax, mpg;
    public long dame, dameg;
    public short damChay, damChayLH, damchayKoK;
    public int def, defg, banhtet;
    public int crit, critg;
    public byte speed = 5;
    public long chisonap;
    public boolean teleport;
    public int hpbang, mpbang, damebang, critbang;
    public boolean khangTDHS;

    public void initPowerLimit() {
        powerLimit = PowerLimitManager.getInstance().get(limitPower);
    }

    /**
     * Chỉ số cộng thêm
     */
    public int hpAdd, mpAdd, dameAdd, defAdd, critAdd, hpHoiAdd, mpHoiAdd;

    /**
     * //+#% sức đánh chí mạng
     */
    public List<Integer> tlDameCrit;
    public int tlSDCM;

    /**
     * Tỉ lệ hp, mp cộng thêm
     */
    public List<Integer> tlHp, tlMp;

    /**
     * Tỉ lệ giáp cộng thêm
     */
    public List<Integer> tlDef;

    /**
     * Tỉ lệ sức đánh/ sức đánh khi đánh quái
     */
    public List<Integer> tlDame, tlDameAttMob;

    /**
     * Lượng hp, mp hồi mỗi 30s, mp hồi cho người khác
     */
    public long hpHoi, mpHoi, mpHoiCute;

    /**
     * Tỉ lệ hp, mp hồi cộng thêm
     */
    public short tlHpHoi, tlMpHoi;

    /**
     * Tỉ lệ hp, mp hồi bản thân và đồng đội cộng thêm
     */
    public short tlHpHoiBanThanVaDongDoi, tlMpHoiBanThanVaDongDoi;

    /**
     * Tỉ lệ hút hp, mp khi đánh, hp khi đánh quái
     */
    public short tlHutHp, tlHutMp, tlHutHpMob;
    //
    /**
     * Tỉ lệ hút hp, mp xung quanh mỗi 5s
     */
    public short tlHutHpMpXQ;

    /**
     * Tỉ lệ phản sát thương
     */
    public short tlPST;

    /**
     * Tỉ lệ tiềm năng sức mạnh
     */
    public List<Integer> tlTNSM;

    /**
     * Tỉ lệ vàng cộng thêm
     */
    public short tlGold;

    /**
     * Tỉ lệ né đòn
     */
    public short tlNeDon;

    public short tlBom;

    public short speedat;

    public short dctt;

    public short dttrung;
    public short Tangbienkhi;

    public short ts;
    public short laze;
    public int csbang;
    // bien hinh
    public boolean bienhinh;
    public int csSdHuman;
    public short hoikhien;
    public short dtkame;
    public short hoitroi;

    public short tlGiap;

    public short tlxgcc;

    public short tlxgc;

    public short tlchinhxac;

    public short tlTNSMPet;
    public short xChuong;

    public short setltdb;
    public short setTinhAn;
    public short setNhatAn;
    public short setNguyetAn;
    public int kamejokoop, kaiokenop, quackkop;
    public int lienhoanop, detrungop, lazeop;

    /**
     * Tỉ lệ sức đánh đẹp cộng thêm cho bản thân và người xung quanh
     */
    public int tlSexyDame;

    /**
     * Tỉ lệ giảm sức đánh
     */
    public short tlSubSD;

    public int voHieuChuong;

    /*------------------------Effect skin-------------------------------------*/
    public Item trainArmor;
    public boolean wearingTrainArmor;
    public static int kimcuongday = 0;
    public static int mockimcuong = 0;
    public boolean wearingVoHinh;
    public boolean isKhongLanh;
    public boolean islinhthuydanhbac;
    public boolean isTinhAn;
    public boolean isNhatAn;
    public boolean isNguyetAn;
    public boolean isTanHinh;
    public boolean isHoaDa;
    public boolean isLamCham;
    public boolean isDoSPL;
    public boolean isThoBulma;
    public boolean isKhungLong;
    public boolean isDietQuy;
    public boolean isBunmaTocMau;
    public boolean isTiecBaiBien;
    public short tlHpGiamODo;
    // hop the
    public boolean isGogeta;
    public boolean isBroly;
    public boolean isKamiOren;

    public boolean isToppo;
    public boolean isThanToiThuong;
    public boolean isJrenCn;
    public boolean isBaby;
    public boolean isMiNuong;
    public boolean isHacMiNuong;
    public int dameminuong;
    public boolean isAuLac;
    public boolean isSamehada;
    public boolean isSarigan;
    public boolean isObitold;
    public boolean isNaruto;
    public boolean isSuperGohan;
    public boolean isMinato;
    public boolean isZoro;
    public boolean isBongBang;
    public boolean isBongBanggold;
    public boolean isEnma;
    public boolean isBanthan;
    public short HPjr;
    public short HPjrcn;
    public short dametld;
    public int tlSpeed;
    public boolean isThienlongdao;
    public boolean isBrolyTraiDat;
    public boolean isBrolyNamec;
    public boolean isBrolyXayda;
    public boolean isLabubu;
    public boolean isCerBerus;
    public boolean isTenLuaCaMap;
    public boolean isOG73;
    public boolean isSieuQuyLao;
    public boolean isBroly2025c;
    public boolean isBlackgoku;
    public boolean isAdroiTaac;
    public boolean isAdroi19;

    public boolean isthuykiem;
    public boolean ishuyetkiem;
    public boolean ishoakiem;
    public boolean isBuma;
    public boolean isBena;
    public int dammhv;
    public boolean isMyhauvuong;
    public boolean isKimcobong;
    public int levelBT;
    public int chimanglabubu;
    public boolean isMeoAcMa;
    public boolean isFireSoul;
    public boolean isMeoWar;
    public boolean isMeoHoaThan;

    public boolean isAtm;

    public boolean bb3brown, bb3rose, bb3green, bb3panda;
    public int damecc, damec, dametc, damgalick, csRong;
    public boolean vegeta1, vegeta2, vegeta3, vegeta4, vegeta5;
    public boolean trunk1, trunk2, trunk3, trunk4, trunk5;
    public boolean namec1, namec2, namec3, namec4, namec5;
    public boolean broly1, broly2, broly3, broly4, broly5;
    public int dambrolyhp;
    public int dambrolycm;
    public boolean isRongOrange, daulau;
    public boolean isQuanDiBien;

    /*-------------------------------------------------------------------------*/
    /**
     * Tính toán mọi chỉ số sau khi có thay đổi
     */

    public void calPoint() {
        if (this.player != null && this.player.pet != null && this.player.pet.nPoint != null) {
            this.player.pet.nPoint.setPointWhenWearClothes();
        }
        this.setPointWhenWearClothes();
    }

    private void setPointWhenWearClothes() {
        if (this.player == null || this.player.inventory == null || this.player.inventory.itemsBody == null) {
            return;
        }
        resetPoint();
        if (this.player.rewardBlackBall.timeOutOfDateReward[2] > System.currentTimeMillis()) {
            tlHutHp += RewardBlackBall.R3S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[3] > System.currentTimeMillis()) {
            tlPST += RewardBlackBall.R4S_2;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[4] > System.currentTimeMillis()) {
            tlDameCrit.add(RewardBlackBall.R5S_1);
            tlSDCM += RewardBlackBall.R5S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[6] > System.currentTimeMillis()) {
            tlNeDon += RewardBlackBall.R7S_1;
        }

        Card card = player.Cards.stream().filter(r -> r != null && r.Used == 1).findFirst().orElse(null);
        if (card != null) {
            for (OptionCard io : card.Options) {
                if (io.active == card.Level || (card.Level == -1 && io.active == 0)) {
                    switch (io.id) {
                        case 0: // Tấn công +#
                            this.dameAdd += io.param;
                            break;
                        case 2: // HP, KI+#000
                            this.hpAdd += io.param * 1000;
                            this.mpAdd += io.param * 1000;
                            break;
                        case 3:// vô hiệu chưởng
                            this.voHieuChuong += io.param;
                            break;
                        case 5: // +#% sức đánh chí mạng
                            this.tlDameCrit.add(io.param);
                            this.tlSDCM += io.param;
                            break;
                        case 6: // HP+#
                            this.hpAdd += io.param;
                            break;
                        case 7: // KI+#
                            this.mpAdd += io.param;
                            break;
                        case 8: // Hút #% HP, KI xung quanh mỗi 5 giây
                            this.tlHutHpMpXQ += io.param;
                            break;
                        case 14: // Chí mạng+#%
                            this.critAdd += io.param;
                            break;
                        case 16: // Speed
                        case 114:
                        case 148:
                            this.tlSpeed += io.param;
                            break;
                        case 18: // Chinh xac
                            this.tlchinhxac += io.param;
                            break;
                        case 19: // Tấn công+#% khi đánh quái
                            this.tlDameAttMob.add(io.param);
                            break;
                        case 22: // HP+#K
                            this.hpAdd += io.param * 1000;
                            break;
                        case 23: // MP+#K
                            this.mpAdd += io.param * 1000;
                            break;
                        case 27: // +# HP/30s
                            this.hpHoiAdd += io.param;
                            break;
                        case 28: // +# KI/30s
                            this.mpHoiAdd += io.param;
                            break;
                        case 33: // dịch chuyển tức thời
                            this.teleport = true;
                            break;
                        case 34:
                            this.setTinhAn += 1;
                            break;
                        case 35:
                            this.setNguyetAn += 1;
                            break;
                        case 36:
                            this.setNhatAn += 1;
                            break;
                        case 47: // Giáp+#
                            this.defAdd += io.param;
                            break;
                        case 48: // HP/KI+#
                            this.hpAdd += io.param;
                            this.mpAdd += io.param;
                            break;
                        case 49: // Tấn công+#%
                        case 50: // Sức đánh+#%
                            this.tlDame.add(io.param);
                            break;
                        case 77: // HP+#%
                            this.tlHp.add(io.param);
                            break;
                        case 80: // HP+#%/30s
                            this.tlHpHoi += io.param;
                            break;
                        case 81: // MP+#%/30s
                            this.tlMpHoi += io.param;
                            break;
                        case 88: // Cộng #% exp khi đánh quái
                            this.tlTNSM.add(io.param);
                            break;
                        case 94: // Giáp #%
                            this.tlGiap += io.param;
                            break;
                        case 95: // Biến #% tấn công thành HP
                            this.tlHutHp += io.param;
                            break;
                        case 96: // Biến #% tấn công thành MP
                            this.tlHutMp += io.param;
                            break;
                        case 97: // Phản #% sát thương
                            this.tlPST += io.param;
                            break;
                        case 98: // Xuyen giap chuong
                            this.tlxgc += io.param;
                            break;
                        case 99: // Xuyen giap can chien
                            this.tlxgcc += io.param;
                            break;
                        case 100: // +#% vàng từ quái
                            this.tlGold += io.param;
                            break;
                        case 101: // +#% TN,SM
                            this.tlTNSM.add(io.param);
                            break;
                        case 103: // KI +#%
                            this.tlMp.add(io.param);
                            break;
                        case 104: // Biến #% tấn công quái thành HP
                            this.tlHutHpMob += io.param;
                            break;
                        case 105: // Vô hình khi không đánh quái và boss
                            this.wearingVoHinh = true;
                            break;
                        case 106: // Không ảnh hưởng bởi cái lạnh
                            this.isKhongLanh = true;
                            break;
                        case 108: // #% Né đòn
                            this.tlNeDon += io.param;
                            break;
                        case 109: // Hôi, giảm #% HP
                            this.tlHpGiamODo += io.param;
                            break;
                        case 116: // Kháng thái dương hạ san
                            this.khangTDHS = true;
                            break;
                        case 123: // Hồi trói
                            this.hoitroi += io.param;
                            break;
                        case 153: // Kháng thái dương hạ san
                            this.tlBom += io.param;
                            break;
                        case 117: // Đẹp +#% SĐ cho mình và người xung quanh
                            if (io.param > this.tlSexyDame) {
                                this.tlSexyDame = io.param;
                            }
                            break;
                        case 120:
                            long hpkiAtm = 0;
                            if (player.getSession() != null) {
                                hpkiAtm = player.getSession().danap / 100L;
                            }
                            this.hpAdd += io.param * hpkiAtm;
                            this.mpAdd += io.param * hpkiAtm;
                            break;
                        case 147: // +#% sức đánh
                            this.tlDame.add(io.param);
                            break;
                        case 156: // Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                            // this.tlSubSD += 50;
                            // this.tlTNSM.add(io.param);
                            // this.tlGold += io.param;
                            this.damChay += io.param;
                            break;
                        case 157:
                            this.damChayLH += io.param;
                            break;
                        case 158:
                            this.damchayKoK += io.param;
                            break;
                        case 161: // ts;
                            this.ts += io.param;
                            break;
                        case 162: // Cute hồi #% KI/s bản thân và xung quanh
                            this.mpHoiCute += io.param;
                            break;
                        case 163: // st laze
                            this.laze += io.param;
                            break;
                        case 173: // Phục hồi #% HP và KI cho đồng đội
                            this.tlHpHoiBanThanVaDongDoi += io.param;
                            this.tlMpHoiBanThanVaDongDoi += io.param;
                            break;
                        case 181: // Dịch chuyển tức thời +#% sát thương
                            this.dctt += io.param;
                            break;
                        case 182: // +#% sát thương đệ từ trứng
                            this.dttrung += io.param;
                            break;
                        case 183: // Giảm #% thời gian hồi Khiên
                            this.hoikhien += io.param;
                        case 186:
                            long dameAtm = 0;
                            if (player.getSession() != null) {
                                dameAtm = player.getSession().danap / 100L;
                            }
                            this.dameAdd += io.param * dameAtm;
                            break;
                        case 188: // Đệ tử chưởng kamejoko +#% sát thương
                            this.dtkame += io.param;
                            break;
                        case 189: // biến khỉ cộng hp,sd
                            this.Tangbienkhi += io.param;
                            break;
                        case 190: // biến khỉ cộng hp,sd
                            this.speedat += io.param;
                            break;
                        case 193:
                            this.damgalick += io.param;
                            break;
                        case 194:
                            this.csSdHuman += io.param * this.player.zone.getNumOfPlayers();
                            ;
                            break;

                    }
                }
            }
        }

        // Bông tai cấp 2
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            this.player.inventory.itemsBag.stream().filter(it -> it.isNotNullItem() && it.template.id == 921)
                    .findFirst().ifPresent(btc2 -> {
                        for (ItemOption io : btc2.itemOptions) {
                            addOption(io);
                            if (io.optionTemplate.id == 72) {
                                this.levelBT = io.param;
                            }
                        }
                    });
        }
        if (player.clan != null) {
            int a = player.clan.level;
            if (a >= 2) {
                this.tlTNSM.add(20);
            }
            if (a >= 3) {
                this.csbang += 1;
            }
            if (a >= 8) {
                this.csbang += 5;
            }
        }
        if (this.bb3brown) {
            damecc += 5;
        }
        if (this.bb3rose) {
            damec += 5;
        }
        if (this.bb3green) {
            dametc += 5;
        }
        if (this.isRongOrange) {
            this.csRong += 2 * this.player.zone.getNumOfPlayers();

        }
        // if(isAtm&&player.getSession()!=null){
        // dameatm+=player.getSession().danap/100;
        // }
        if (this.isMeoAcMa) {
            speedat += 20;
        }
        if (this.isMeoWar) {
            this.csRong += 2 * this.player.zone.getNumOfPlayers();
        }
        if (player.luachon == 1) {
            this.ts += 10;
        }
        if (player.luachon == 2) {
            this.laze += 10;
        }
        if (player.luachon == 3) {
            this.speedat += 10;
        }
        if (this.isKhungLong) {
            this.csSdHuman += 2 * this.player.zone.getNumOfPlayers();
        }

        // trunk
        if (this.trunk1 || this.trunk2 || this.trunk3) {
            this.kaiokenop += 5;
        }
        if (this.trunk2 || this.trunk3) {
            this.kamejokoop += 5;
        }
        if (this.trunk3) {
            this.tlDameCrit.add(5);
            this.tlSDCM += 5;
        }
        if (this.trunk4) {
            this.kaiokenop += 7;
            this.kamejokoop += 7;
            this.tlDameCrit.add(10);
            this.tlSDCM += 10;
        }
        if (this.trunk5) {
            this.kaiokenop += 10;
            this.kamejokoop += 10;
            this.tlDameCrit.add(15);
            this.tlSDCM += 15;
        }
        // namec
        if (this.namec1 || this.namec2 || this.namec3) {
            this.lienhoanop += 5;

        }
        if (this.namec2 || this.namec3) {
            this.speedat += 5;
        }
        if (this.namec3) {
            this.laze += 10;
        }
        if (this.namec4) {
            this.lienhoanop += 7;
            this.speedat += 7;
            this.laze += 15;
        }
        if (this.namec5) {
            this.lienhoanop += 10;
            this.speedat += 10;
            this.laze += 20;
        }

        // vegeta
        if (this.vegeta1 || this.vegeta2 || this.vegeta3) {
            this.Tangbienkhi += 5;

        }
        if (this.vegeta2 || this.vegeta3) {
            this.damgalick += 5;
        }
        if (this.vegeta3) {
            this.ts = 10;
        }
        if (this.vegeta4) {
            this.Tangbienkhi += 7;
            this.damgalick += 7;
            this.ts += 15;
        }
        if (this.vegeta5) {
            this.Tangbienkhi += 10;
            this.damgalick += 10;
            this.ts += 20;
        }
        // broly

        // setjr

        if (this.isBena && this.isBroly2025c) {
            this.Tangbienkhi += 20;
        }
        if (this.isBongBanggold) {
            this.tlTNSM.add(40);
            if (player.zone.map.mapId >= 105 && player.zone.map.mapId <= 110) {
                this.dameAdd += 1000;
                this.hpAdd += 20000;
                this.mpAdd += 20000;
            }
        }
        if (this.isHacMiNuong) {
            this.dameminuong = 20;
        }
        if (this.isSamehada) {
            this.tlHutHpMpXQ += 10;
        }
        if (this.isNaruto && this.isMinato) {
            this.isTanHinh = true;
        }
        if (this.isSarigan) {
            this.isHoaDa = true;
        }
        if (this.player.effectSkill.isMucoi) {
            this.speedat += 20;
        }
        if (this.isBroly) {
            this.speedat += 40;
        }
        if (this.isKamiOren) {
            this.dametc += 20;
        }
        if (this.isObitold || this.isMyhauvuong && this.isKimcobong) {
            this.teleport = true;
        }
        if (this.isZoro && this.isEnma) {
            this.tlxgcc += 80;
            this.tlHutHp += 15;
            this.tlHutMp += 15;
        }

        // tốc đánh item
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2
                && this.player.itemTime.iconMeal2 == 20603) {
            this.speedat += 20;
        }
        // stc 1-2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal && this.player.itemTime.iconMeal == 12766) {

            this.tlxgc += 50;
            this.tlxgcc += 50;
        }
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal && this.player.itemTime.iconMeal == 12767) {
            this.tlxgc += 50;
            this.tlxgcc += 50;
        }
        if (this.player.isPet && (((Pet) this.player).typePet == 2)
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            if (player.choice == 1) {
                this.kamejokoop += player.optde;
            } else if (player.choice == 2) {
                this.kaiokenop += player.optde;
            } else {
                this.quackkop += player.optde;
            }
        }
        if (this.player.isPet && (((Pet) this.player).typePet == 3)
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            if (player.choice == 1) {
                this.damgalick += player.optde;
            } else if (player.choice == 2) {
                this.Tangbienkhi += player.optde;
            } else {
                this.ts += player.optde;
            }
        }
        if (this.player.isPet && (((Pet) this.player).typePet == 4)
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            if (player.choice == 1) {
                this.lienhoanop += player.optde;
            } else if (player.choice == 2) {
                this.detrungop += player.optde;
            } else {
                this.laze += player.optde;
            }
        }
        // de evibu ht tăng 20% st laze

        // broly bass ht tăng 20% st bom
        // if (this.player.isPet && ((Pet) this.player).typePet == 3 && (((Pet)
        // this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet)
        // this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
        // this.ts += 20;
        // }
        // king kong cool ht tăng 10% stcm

        // xu ly duoi khi
        // if (this.player.itemTime != null && this.player.itemTime.isUseDK) {
        //
        // this.player.effectSkill.levelMonkey =7;
        // }
        if (BagesTemplate.sendListItemOption(player) != null) {
            for (ItemOption io : BagesTemplate.sendListItemOption(player)) {
                addOption(io);
            }
        }

        this.player.setClothes.worldcup = 0;
        for (Item item : this.player.inventory.itemsBody) {
            if (item.isNotNullItem()) {
                switch (item.template.id) {
                    case 966:
                    case 982:
                    case 983:
                    case 883:
                    case 904:
                        player.setClothes.worldcup++;
                }
                if (item.template.id >= 592 && item.template.id <= 594) {
                    teleport = true;
                }
                for (ItemOption io : item.itemOptions) {
                    addOption(io);
                }
            }
        }
        setCaiTrang();
        setDameTrainArmor();
        setBasePoint();
        setOutfitFusion();
        setOutfitFusion1();
        setOutfitFusion2();
        setPet();
        setLinhThu();

        setDeoLung();
        setThuCuoi();

        setSpeed();

    }

    private void addOption(ItemOption io) {
        switch (io.optionTemplate.id) {
            case 0: // Tấn công +#
                this.dameAdd += io.param;
                break;
            case 2: // HP, KI+#000
                this.hpAdd += io.param * 1000;
                this.mpAdd += io.param * 1000;
                break;
            case 3:// vô hiệu chưởng
                this.voHieuChuong += io.param;
                break;
            case 5: // +#% sức đánh chí mạng
                this.tlDameCrit.add(io.param);
                this.tlSDCM += io.param;
                break;
            case 6: // HP+#
                this.hpAdd += io.param;
                break;
            case 7: // KI+#
                this.mpAdd += io.param;
                break;
            case 8: // Hút #% HP, KI xung quanh mỗi 5 giây
                this.tlHutHpMpXQ += io.param;
                break;
            case 14: // Chí mạng+#%
                this.critAdd += io.param;
                break;
            case 16: // Speed
            case 114:
            case 148:
                this.tlSpeed += io.param;
                break;
            case 18: // Chinh xac
                this.tlchinhxac += io.param;
                break;
            case 19: // Tấn công+#% khi đánh quái
                this.tlDameAttMob.add(io.param);
                break;
            case 22: // HP+#K
                this.hpAdd += io.param * 1000;
                break;
            case 23: // MP+#K
                this.mpAdd += io.param * 1000;
                break;
            case 24: // Làm chậm
                this.isLamCham = true;
                break;
            case 25: // Tàn hình
                this.isTanHinh = true;
                break;
            case 26: // Hóa đá
                this.isHoaDa = true;
                break;
            case 27: // +# HP/30s
                this.hpHoiAdd += io.param;
                break;
            case 28: // +# KI/30s
                this.mpHoiAdd += io.param;
                break;
            case 33: // dịch chuyển tức thời
                this.teleport = true;
                break;
            case 34:
                this.setTinhAn += 1;
                break;
            case 35:
                this.setNguyetAn += 1;
                break;
            case 36:
                this.setNhatAn += 1;
                break;
            case 47: // Giáp+#
                this.defAdd += io.param;
                break;
            case 48: // HP/KI+#
                this.hpAdd += io.param;
                this.mpAdd += io.param;
                break;
            case 49: // Tấn công+#%
            case 50: // Sức đánh+#%
                this.tlDame.add(io.param);
                break;
            case 77: // HP+#%
                this.tlHp.add(io.param);
                break;
            case 80: // HP+#%/30s
                this.tlHpHoi += io.param;
                break;
            case 81: // MP+#%/30s
                this.tlMpHoi += io.param;
                break;
            case 88: // Cộng #% exp khi đánh quái
                this.tlTNSM.add(io.param);
                break;
            case 94: // Giáp #%
                this.tlGiap += io.param;
                break;
            case 95: // Biến #% tấn công thành HP
                this.tlHutHp += io.param;
                break;
            case 96: // Biến #% tấn công thành MP
                this.tlHutMp += io.param;
                break;
            case 97: // Phản #% sát thương
                this.tlPST += io.param;
                break;
            case 98: // Xuyen giap chuong
                this.tlxgc += io.param;
                break;
            case 99: // Xuyen giap can chien
                this.tlxgcc += io.param;
                break;
            case 100: // +#% vàng từ quái
                this.tlGold += io.param;
                break;
            case 101: // +#% TN,SM
                this.tlTNSM.add(io.param);
                break;
            case 103: // KI +#%
                this.tlMp.add(io.param);
                break;
            case 104: // Biến #% tấn công quái thành HP
                this.tlHutHpMob += io.param;
                break;
            case 105: // Vô hình khi không đánh quái và boss
                this.wearingVoHinh = true;
                break;
            case 106: // Không ảnh hưởng bởi cái lạnh
                this.isKhongLanh = true;
                break;
            case 108: // #% Né đòn
                this.tlNeDon += io.param;
                break;
            case 109: // Hôi, giảm #% HP
                this.tlHpGiamODo += io.param;
                break;
            case 110: // Do spl
                this.isDoSPL = true;
                break;
            case 116: // Kháng thái dương hạ san
                this.khangTDHS = true;
                break;

            case 117: // Đẹp +#% SĐ cho mình và người xung quanh
                if (io.param > this.tlSexyDame) {
                    this.tlSexyDame = io.param;
                }
                break;
            case 120:
                long hpkiAtm = 0;
                if (player.getSession() != null) {
                    hpkiAtm = player.getSession().danap / 100L;
                }
                this.hpAdd += io.param * hpkiAtm;
                this.mpAdd += io.param * hpkiAtm;
                break;
            case 123: // Hồi trói
                this.hoitroi += io.param;
                break;
            case 147: // +#% sức đánh
                this.tlDame.add(io.param);
                break;
            case 156: // Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                // this.tlSubSD += 50;
                // this.tlTNSM.add(io.param);
                // this.tlGold += io.param;
                this.damChay += io.param;
                break;
            case 157:
                this.damChayLH += io.param;
                break;
            case 158:
                this.damchayKoK += io.param;
                break;
            case 162: // Cute hồi #% KI/s bản thân và xung quanh
                this.mpHoiCute += io.param;
                break;
            case 159: // x chưởng
                this.xChuong = (short) io.param;
                break;
            case 160: // TNSM PET;
                this.tlTNSMPet += io.param;
                break;
            case 161: // ts;
                this.ts += io.param;
                break;
            case 163: // laze;
                this.laze += io.param;
                break;
            case 173: // Phục hồi #% HP và KI cho đồng đội
                this.tlHpHoiBanThanVaDongDoi += io.param;
                this.tlMpHoiBanThanVaDongDoi += io.param;
                break;
            case 176: //
                setInfoOption176();
                break;
            case 211:
                this.setltdb += 1;
                break;
            case 153: // % phát nổ sau khi chết
                this.tlBom += io.param;
                break;
            case 181: // Dịch chuyển tức thời +#% sát thương
                this.dctt += io.param;
                break;
            case 182: // +#% sát thương đệ từ trứng
                this.dttrung += io.param;
                break;
            case 183: // Giảm #% thời gian hồi Khiên
                this.hoikhien += io.param;
                break;
            case 186:
                long dameAtm = 0;
                if (player.getSession() != null) {
                    dameAtm = player.getSession().danap / 100L;
                }
                this.dameAdd += io.param * dameAtm;
                break;
            case 188: // Đệ tử chưởng kamejoko +#% sát thương
                this.dtkame += io.param;
                break;
            case 189: // biến khỉ cộng hp,sd
                this.Tangbienkhi += io.param;
                break;
            case 190: // biến khỉ cộng hp,sd
                this.speedat += io.param;
                break;
            case 194:
                this.csSdHuman += io.param;
                break;

        }
    }

    private void setSpeed() {
        if (player.isPl()) {
            speed = (byte) (5 + 3 * (tlSpeed / 100));
        }
    }

    private void setInfoOption176() {
        if (player.isPl()) {
            this.tlDame.add(10);
            speed = (byte) (5 + 3 * (50 / 100));
        }
    }

    private void setOutfitFusion() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.pet == null
                || this.player.pet.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.pet.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isGogeta = skin.template.id == 1693 && pskin.template.id == 1553
                    || skin.template.id == 1553 && pskin.template.id == 1693;
        } else {
            this.isGogeta = false;
        }
        // if (skin.isNotNullItem() && pskin.isNotNullItem()) {
        // this.isBroly = skin.template.id == 1780 && pskin.template.id == 1782 ||
        // skin.template.id == 1782 && pskin.template.id == 1780;
        // } else {
        // this.isBroly = false;
        // }
    }

    private void setOutfitFusion1() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.pet == null
                || this.player.pet.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.pet.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isBroly = skin.template.id == 1780 && pskin.template.id == 1782
                    || skin.template.id == 1782 && pskin.template.id == 1780;
        } else {
            this.isBroly = false;
        }
        //
        //
    }

    private void setOutfitFusion2() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.pet == null
                || this.player.pet.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.pet.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isKamiOren = skin.template.id == 1765 && pskin.template.id == 1764
                    || skin.template.id == 1764 && pskin.template.id == 1765;
        } else {
            this.isKamiOren = false;
        }
        //
        //
    }

    /// set cải trang
    private void setCaiTrang() {
        if (this.player.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);

        // Đặt tất cả biến về false trước
        this.isMiNuong = false;
        this.isToppo = false;
        this.isJrenCn = false;
        this.isBaby = false;
        this.isBongBanggold = false;
        this.isBongBang = false;
        this.isThanToiThuong = false;
        this.isHacMiNuong = false;
        this.isBlackgoku = false;
        this.isAdroi19 = false;
        this.isMyhauvuong = false;
        this.isSarigan = false;
        this.isSieuQuyLao = false;
        this.isOG73 = false;
        this.isBroly2025c = false;
        this.isSuperGohan = false;
        this.isNaruto = false;
        this.isBrolyTraiDat = false;
        this.isBrolyNamec = false;
        this.isBrolyXayda = false;
        this.isObitold = false;
        this.isZoro = false;
        this.vegeta1 = false;
        this.vegeta2 = false;
        this.vegeta3 = false;
        this.vegeta4 = false;
        this.vegeta5 = false;
        this.trunk1 = false;
        this.trunk2 = false;
        this.trunk3 = false;
        this.trunk4 = false;
        this.trunk5 = false;
        this.namec1 = false;
        this.namec2 = false;
        this.namec3 = false;
        this.namec4 = false;
        this.namec5 = false;
        this.broly1 = false;
        this.broly2 = false;
        this.broly3 = false;
        this.broly4 = false;
        this.broly5 = false;
        this.isBuma = false;
        this.isAtm = false;
        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            int id = skin.template.id;

            switch (id) {
                case 860 -> this.isMiNuong = true;
                case 1773 -> this.isToppo = true;
                case 1824 -> this.isJrenCn = true;
                case 1437 -> this.isBongBanggold = true;
                case 450 -> this.isBongBang = true;
                case 1838 -> this.isThanToiThuong = true;
                case 1557 -> this.isHacMiNuong = true;
                case 1848 -> this.isBlackgoku = true;
                case 962 -> this.isAdroi19 = true;
                case 1404 -> this.isMyhauvuong = true;
                case 1798 -> this.isSieuQuyLao = true;
                case 1806 -> this.isOG73 = true;
                case 1842 -> this.isBroly2025c = true;
                case 1840 -> this.isSuperGohan = true;
                case 1817 -> this.isNaruto = true;
                case 1018 -> this.isBrolyTraiDat = true;
                case 1019 -> this.isBrolyNamec = true;
                case 1020 -> this.isBrolyXayda = true;
                case 1818 -> this.isObitold = true;
                case 1265 -> this.isZoro = true;
                case 1417 -> this.vegeta1 = true;
                case 1418 -> this.vegeta2 = true;
                case 1419 -> this.vegeta3 = true;
                case 1420 -> this.vegeta4 = true;
                case 1421 -> this.vegeta5 = true;
                case 1747 -> this.trunk1 = true;
                case 1748 -> this.trunk2 = true;
                case 1749 -> this.trunk3 = true;
                case 1750 -> this.trunk4 = true;
                case 1751 -> this.trunk5 = true;
                case 1753 -> this.namec1 = true;
                case 1754 -> this.namec2 = true;
                case 1755 -> this.namec3 = true;
                case 1756 -> this.namec4 = true;
                case 1757 -> this.namec5 = true;
                case 1759 -> this.broly1 = true;
                case 1760 -> this.broly2 = true;
                case 1761 -> this.broly3 = true;
                case 1762 -> this.broly4 = true;
                case 1763 -> this.broly5 = true;
                case 1119 -> this.isBaby = true;
                case 1121 -> this.isBuma = true;
                // case 1124 -> this.isAtm = true;

            }
        }
    }

    private void setDeoLung() {
        if (this.player.inventory.itemsBody.size() < 9) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(8);

        // Đặt tất cả biến về false trước
        this.isSamehada = false;
        this.isEnma = false;
        this.isBanthan = false;
        this.isthuykiem = false;
        this.ishuyetkiem = false;
        this.ishoakiem = false;
        this.isKimcobong = false;
        this.isThienlongdao = false;
        this.isLabubu = false;
        this.isSarigan = false;

        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            switch (skin.template.id) {
                case 1809 -> this.isSamehada = true;
                case 1456 -> this.isEnma = true;
                case 1638 -> this.isBanthan = true;
                case 1850 -> this.isthuykiem = true;
                case 1851 -> this.ishuyetkiem = true;
                case 1852 -> this.ishoakiem = true;
                case 1847 -> this.isKimcobong = true;
                case 1802 -> this.isThienlongdao = true;
                case 1694 -> this.isLabubu = true;
                case 1813 -> this.isSarigan = true;
            }
        }
    }

    // set pet
    private void setPet() {
        if (this.player.inventory.itemsBody.size() < 8) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(7);

        // Đặt tất cả biến về false trước
        this.isKhungLong = false;
        this.isBena = false;
        this.isCerBerus = false;
        this.bb3brown = false;
        this.bb3rose = false;
        this.bb3green = false;
        this.bb3panda = false;

        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            switch (skin.template.id) {
                case 1843 -> this.isBena = true;
                case 1654 -> this.isCerBerus = true;
                case 1426 -> this.bb3brown = true;
                case 1427 -> this.bb3rose = true;
                case 1428 -> this.bb3green = true;
                case 1429 -> this.bb3panda = true;
                case 1118 -> this.isKhungLong = true;
            }
        }
    }

    private void setLinhThu() {
        if (this.player.inventory.itemsBody.size() < 12) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(11);
        this.isMeoAcMa = false;
        this.isFireSoul = false;
        this.isMeoHoaThan = false;
        this.isMeoWar = false;
        // Đặt tất cả biến về false trước

        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            switch (skin.template.id) {
                case 1649 -> this.isMeoAcMa = true;
                case 1491 -> this.isFireSoul = true;
                case 1650 -> this.isMeoHoaThan = true;
                case 1744 -> this.isMeoWar = true;
            }
        }
    }

    private void setQuanDiBien() {
        if (this.player.inventory.itemsBody.size() < 6) {
            return;
        }
        Item qdb = this.player.inventory.itemsBody.get(1);
        Item ao = this.player.inventory.itemsBody.get(0);
        Item ct = this.player.inventory.itemsBody.get(5);
        if (qdb.isNotNullItem() && ao == null && ct == null) {
            this.isQuanDiBien = (qdb.template.id == 691 || qdb.template.id == 692 || qdb.template.id == 693);
        }

    }

    private void setThuCuoi() {
        if (this.player.inventory.itemsBody.size() < 10) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(9);

        // Đặt tất cả biến về false trước
        this.isRongOrange = false;
        this.isAuLac = false;
        this.isTenLuaCaMap = false;
        this.daulau = false;

        // Nếu item hợp lệ, chỉ bật biến tương ứng
        if (skin.isNotNullItem()) {
            switch (skin.template.id) {
                case 1856 -> this.isRongOrange = true;
                case 1534 -> this.isAuLac = true;
                case 1603 -> this.isTenLuaCaMap = true;
                case 1857 -> this.daulau = true;
            }
        }
    }

    private void setDameTrainArmor() {
        if (!this.player.isPet && !this.player.isBoss) {
            if (this.player.inventory.itemsBody.size() < 7) {
                return;
            }
            try {
                Item gtl = this.player.inventory.itemsBody.get(6);
                if (gtl.isNotNullItem()) {
                    this.wearingTrainArmor = true;
                    this.player.inventory.trainArmor = gtl;
                    this.tlSubSD += ItemService.gI().getPercentTrainArmor(gtl);
                } else {
                    if (this.player.inventory.trainArmor == null) {
                        gtl = this.player.inventory.itemsBag.stream()
                                .filter(item -> item.isNotNullItem() && item.template.type == 32
                                        && item.itemOptions != null
                                        && item.itemOptions.stream()
                                                .filter(io -> io.optionTemplate.id == 9 && io.param > 0).findFirst()
                                                .orElse(null) != null)
                                .findFirst().orElse(null);
                        if (gtl == null) {
                            return;
                        }
                        this.player.inventory.trainArmor = gtl;
                    }
                    this.wearingTrainArmor = false;
                    for (Item.ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                        if (io.optionTemplate.id == 9 && io.param > 0) {
                            this.tlDame.add(ItemService.gI().getPercentTrainArmor(this.player.inventory.trainArmor));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("Lỗi get giáp tập luyện " + this.player.name + "\n" + e + "\n");
            }
        }
    }

    public void setBasePoint() {
        checkLevel();
        setHpMax();
        setHp();
        setMpMax();
        setMp();
        setDame();
        setDef();
        setCrit();
        setHpHoi();
        setMpHoi();
        setLtdb();
        setThoBulma();
        setDietQuy();
        setTiecbaiBien();
        setBunmaTocMau();
        setTinhNhatNguyetAn();
        setCaiTrang();
        setDeoLung();
        setPet();
        setThuCuoi();
        setLinhThu();
    }

    private void setLtdb() {
        this.islinhthuydanhbac = this.setltdb >= 5;
    }

    private void setThoBulma() {
        this.isThoBulma = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id == 584);
    }

    private void setDietQuy() {
        this.isDietQuy = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id >= 1087
                && this.player.inventory.itemsBody.get(5).template.id <= 1091);
    }

    private void setBunmaTocMau() {
        this.isBunmaTocMau = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id >= 1208
                && this.player.inventory.itemsBody.get(5).template.id <= 1210);
    }

    private void setTiecbaiBien() {
        this.isTiecBaiBien = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id >= 1234
                && this.player.inventory.itemsBody.get(5).template.id <= 1236);
    }

    private void setTinhNhatNguyetAn() {
        this.isTinhAn = this.setTinhAn >= 5;
        this.isNhatAn = this.setNhatAn >= 5;
        this.isNguyetAn = this.setNguyetAn >= 5;
    }

    private void setHpHoi() {
        this.hpHoi = this.hpMax / 100;
        this.hpHoi += this.hpHoiAdd;

        // Kiểm tra giá trị tlHpHoi không vượt quá giới hạn
        if (this.tlHpHoi > 100) {
            this.tlHpHoi = 100;
        } else if (this.tlHpHoi < 0) {
            this.tlHpHoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoi / 100);

        // Kiểm tra giá trị tlHpHoiBanThanVaDongDoi không vượt quá giới hạn
        if (this.tlHpHoiBanThanVaDongDoi > 100) {
            this.tlHpHoiBanThanVaDongDoi = 100;
        } else if (this.tlHpHoiBanThanVaDongDoi < 0) {
            this.tlHpHoiBanThanVaDongDoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoiBanThanVaDongDoi / 100);
    }

    private void setMpHoi() {
        this.mpHoi = this.mpMax / 100;
        this.mpHoi += this.mpHoiAdd;

        // Kiểm tra giá trị tlMpHoi không vượt quá giới hạn
        if (this.tlMpHoi > 100) {
            this.tlMpHoi = 100;
        } else if (this.tlMpHoi < 0) {
            this.tlMpHoi = 0;
        }

        this.mpHoi += ((long) this.mpMax * this.tlMpHoi / 100);

        // Kiểm tra giá trị tlMpHoiBanThanVaDongDoi không vượt quá giới hạn
        if (this.tlMpHoiBanThanVaDongDoi > 100) {
            this.tlMpHoiBanThanVaDongDoi = 100;
        } else if (this.tlMpHoiBanThanVaDongDoi < 0) {
            this.tlMpHoiBanThanVaDongDoi = 0;
        }

        this.mpHoi += ((long) this.mpMax * this.tlMpHoiBanThanVaDongDoi / 100);
    }

    private void setHpMax() {
        if (this.player.isClone) {
            if (this.player instanceof PlayerClone) {
                this.hpMax = Util.getPercent((int) ((PlayerClone) this.player).master.nPoint.hpMax,
                        SkillUtil.getPercentPhanThan(player));
            } else if (this.player instanceof LinhDanhThue) {
                // LinhDanhThue already has hpMax set from master in constructor
                return;
            }
            return;
        }
        // Tính toán giới hạn hpMax
        long hpMax = Util.maxIntValue(this.hpg + this.hpAdd);

        // Áp dụng các yếu tố ảnh hưởng đến hpMax
        for (Integer tl : this.tlHp) {
            hpMax += (hpMax * tl / 100L);
        }

        // Xử lý set nappa
        if (this.player.setClothes.nappa == 5) {
            hpMax += (hpMax * 80L / 100L);
        }

        if (this.player.setClothes.cadicM >= 2) {
            hpMax += (hpMax * 20L / 100L);
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            hpMax += (hpMax * 10 / 100L);
        }
        // Xử lý bánh trưng zl NRO_MOD

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            hpMax += (hpMax * 10L / 100L);
        }

        // Xử lý set nhật ấn
        if (this.isNhatAn) {
            hpMax += (hpMax * 15L / 100L);
        }

        // Xử lý ngọc rồng đen 2 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[1] > System.currentTimeMillis()) {
            hpMax += (hpMax * RewardBlackBall.R2S_1 / 100L);
        }

        // Xử lý khỉ
        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentHpMonkey(player.effectSkill.levelMonkey);
                hpMax += (hpMax * percent / 100L);
            }
        }
        if (this.player.isPet && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            int level = ((Pet) this.player).level;
            if (level > 0) {
                hpMax += (hpMax * level / 100L);
            }
        }
        // Xử broly bass
        if (this.player.isPet && ((Pet) this.player).typePet >= 2
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            hpMax += (hpMax * 20 / 100L);
        }
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            hpMax += (hpMax * 10 / 100L);
        }
        // }
        //
        // }

        // Xử lý pet mabư

        // }
        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            hpMax *= this.player.effectSkin.xHPKI;
        }

        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8062) {
            hpMax += (hpMax * 5 / 100L);
        }
        // Xử lý thức ăn 3
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3 && this.player.itemTime.iconMeal3 == 8244) {
            hpMax += (hpMax * 10 / 100L);
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseLoX15) {
            hpMax -= (hpMax * 85 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX10) {
            hpMax -= (hpMax * 65 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX7) {
            hpMax -= (hpMax * 45 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX5) {
            hpMax -= (hpMax * 35 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX2) {
            hpMax -= (hpMax * 25 / 100L);
        }

        // Xử lý gogeta
        if (this.isGogeta || this.isKamiOren) {
            hpMax += (hpMax * 10 / 100L);

        }

        if (player.luachon == 1) {
            hpMax += hpMax * 20 / 100L;
        }

        hpMax += (hpMax * dambrolyhp / 100L);
        if (this.player.zone != null) {
            if (this.bb3panda) {
                hpMax += 1000 * this.player.zone.getNumOfPlayers();
            }
        }

        if (player.clan != null) {
            if (player.hpbang > 5000 * player.clan.level) {
                hpbang = 5000 * player.clan.level;
            } else {
                hpbang = player.hpbang;
            }
        }
        hpMax += hpbang;

        if (this.isMyhauvuong && this.isKimcobong) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.isSuperGohan && this.isThienlongdao) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.isNaruto && this.isMinato) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.isThanToiThuong) {
            this.tlxgcc = 100;
            this.tlxgc = 100;
        }
        if (this.isToppo) {
            hpMax += (hpMax * HPjr / 100L);
        }
        if (this.isJrenCn) {
            hpMax += (hpMax * HPjrcn / 100L);
        }

        if (this.player.effectSkill.isMucoi) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.player.effectSkill.isObito) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.isBroly) {
            hpMax += (hpMax * 10 / 100L);
        }
        if (this.player.effectSkill.isMonkey) {
            if (Tangbienkhi > 0) {
                hpMax += (hpMax * Tangbienkhi / 100L);
            }
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            hpMax += 1_000_000;
        }

        // Xử lý +hp đệ
        if (this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            hpMax += this.player.pet.nPoint.hpMax;
        }

        // Xử lý bổ huyết
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet && !this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2;
        }

        // Xử lý item sieu cap
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2.2;
        }

        // Xử lý huýt sáo
        if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
            if (this.player.effectSkill.tiLeHPHuytSao != 0) {
                hpMax += (hpMax * this.player.effectSkill.tiLeHPHuytSao / 100L);
            }
        }
        // Xử lý chibi
        if (this.player.effectSkill != null && this.player.effectSkill.isChibi && this.player.typeChibi == 3) {
            hpMax *= 2;
        }

        // Xử lý map lạnh
        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            hpMax /= 2;
        }

        if (!this.player.isBoss && !this.player.isNewPet
                && TimeUtil.checkTime(EventDAO.getRemainingTimeToIncreaseHP())) {
            hpMax += hpMax / 10;
        }

        // if (hpMax > 2_000_000_000) {
        // hpMax = 2_000_000_000;
        // }
        if (this.player.effectSkill.isSuper) {
            hpMax += hpMax * SkillUtil.getPercentHpSuper(this.player.effectSkill.levelSuper) / 100L;
        }
        if (this.player.effectSkill.isBienHinh) {
            hpMax += hpMax * SkillUtil.getPercentHpBienHinh(this.player.effectSkill.levelBienHinh) / 100L;
        }

        hpMax += hpMax * csbang / 100L;
        this.hpMax = hpMax;
    }

    private void setHp() {
        // Giới hạn giá trị hp không vượt quá hpMax
        if (this.hp > this.hpMax) {
            this.hp = this.hpMax;
        }
    }

    private void setMpMax() {
        if (this.player.isClone) {
            if (this.player instanceof PlayerClone) {
                this.mpMax = Util.getPercent((int) ((PlayerClone) this.player).master.nPoint.mpMax,
                        SkillUtil.getPercentPhanThan(player));
            } else if (this.player instanceof LinhDanhThue) {
                // LinhDanhThue already has mpMax set from master in constructor
                return;
            }
            return;
        }
        // Tính toán giới hạn mpMax
        long mpMax = Util.maxIntValue(this.mpg + this.mpAdd);

        // Áp dụng các yếu tố ảnh hưởng đến mpMax
        for (Integer tl : this.tlMp) {
            mpMax += (mpMax * tl / 100L);
        }

        // Xử lý set picolo
        if (this.player.setClothes.ocTieu == 5) {
            mpMax *= 2;
        }

        // Xử lý set nguyệt ấn
        if (this.isNguyetAn) {
            mpMax += (mpMax * 15L / 100L);
        }

        // Xử lý ngọc rồng đen 6 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[5] > System.currentTimeMillis()) {
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100L);
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            mpMax += (this.mpMax * 10 / 100L);
        }
        if (this.player.isPet && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            int level = ((Pet) this.player).level;
            if (level > 0) {
                mpMax += (mpMax * level / 100L);

            }
        }

        // Xử lý broly base
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            mpMax += (this.mpMax * 10 / 100L);
        }
        if (this.player.isPet && ((Pet) this.player).typePet >= 2
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            mpMax += (this.mpMax * 20 / 100L);
        }

        // if (this.player.isPet && (((Pet) this.player).master.fusion.typeFusion ==
        // ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion ==
        // ConstPlayer.HOP_THE_PORATA2)) {
        // int level = ((Pet) this.player).level;
        // if (level > 0) {
        // mpMax += (mpMax * level / 100L);
        //
        // }
        // }
        // Xử lý pet mabư

        // Xử lý pet black

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            mpMax *= this.player.effectSkin.xHPKI;
        }

        // Xử lý gogeta
        if (player.luachon == 2) {
            mpMax += mpMax * 20 / 100L;
        }
        mpMax += (mpMax * dambrolyhp / 100L);

        if (this.bb3panda && this.player.zone != null) {
            mpMax += 1000 * this.player.zone.getNumOfPlayers();

        }

        if (this.isGogeta || this.isKamiOren) {
            mpMax += (mpMax * 10 / 100L);
        }
        if (player.clan != null) {
            if (player.mpbang > 5000 * player.clan.level) {
                mpbang = 5000 * player.clan.level;
            } else {
                mpbang = player.mpbang;
            }
        }
        mpMax += mpbang;

        if (this.isMyhauvuong && this.isKimcobong) {
            mpMax += (mpMax * 10 / 100L);
        }
        if (this.isSuperGohan && this.isThienlongdao) {
            mpMax += (mpMax * 10 / 100L);
        }
        if (this.isNaruto && this.isMinato) {
            mpMax += (mpMax * 10 / 100L);
        }
        if (this.player.effectSkill.isMucoi) {
            mpMax += (mpMax * 10 / 100L);
        }
        if (this.player.effectSkill.isObito) {
            mpMax += (mpMax * 10 / 100L);
        }
        if (this.isBroly) {
            mpMax += (mpMax * 10 / 100L);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            mpMax += 1_000_000;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseLoX15) {
            mpMax -= (mpMax * 85 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX10) {
            mpMax -= (mpMax * 65 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX7) {
            mpMax -= (mpMax * 45 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX5) {
            mpMax -= (mpMax * 35 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX2) {
            mpMax -= (mpMax * 25 / 100L);
        }
        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            mpMax += (mpMax * 10L / 100L);
        }

        // Xử lý hợp thể
        if (this.player.fusion.typeFusion != 0) {
            mpMax += this.player.pet.nPoint.mpMax;
        }

        // Xử lý bổ khí
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi && !this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2;
        }

        // Xử lý item sieu cap
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2.2;
        }

        if (!this.player.isBoss && !this.player.isNewPet
                && TimeUtil.checkTime(EventDAO.getRemainingTimeToIncreaseMP())) {
            mpMax += mpMax / 10;
        }

        // if (mpMax > 2_000_000_000) {
        // mpMax = 2_000_000_000;
        // }
        if (this.player.effectSkill.isSuper) {
            mpMax += mpMax * SkillUtil.getPercentMpSuper(this.player.effectSkill.levelSuper) / 100L;
        }
        if (this.player.effectSkill.isBienHinh) {
            mpMax += mpMax * SkillUtil.getPercentHpBienHinh(this.player.effectSkill.levelBienHinh) / 100L;
        }

        mpMax += mpMax * csbang / 100L;

        this.mpMax = mpMax;
    }

    private void setMp() {
        if (this.mp > this.mpMax) {
            this.mp = this.mpMax;
        }
    }

    public long getHP() {
        return this.hp <= this.hpMax ? this.hp : this.hpMax;
    }

    public void setHP(long hp) {
        if (hp > 0) {
            this.hp = (hp <= this.hpMax ? hp : this.hpMax);
        } else {
            player.setDie();
        }
    }

    public long getMP() {
        return this.mp <= this.mpMax ? this.mp : this.mpMax;
    }

    public void setMP(long mp) {
        if (mp > 0) {
            this.mp = (mp <= this.mpMax ? mp : this.mpMax);
        } else {
            this.mp = 0;
        }
    }

    private void setDame() {
        if (this.player.isClone) {
            if (this.player instanceof PlayerClone) {
                this.dame = ((PlayerClone) this.player).master.nPoint.dame * SkillUtil.getPercentPhanThan(player)
                        / 100L;
            } else if (this.player instanceof LinhDanhThue) {
                // LinhDanhThue already has dame set from master in constructor
                return;
            }
            return;
        }
        // Tính toán giới hạn dame
        long dame = Util.maxIntValue(this.dameg + this.dameAdd);

        // Áp dụng các yếu tố ảnh hưởng đến dame
        for (Integer tl : this.tlDame) {
            dame += (dame * tl / 100L);
        }
        // thuốc mỡ
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                && this.player.itemTime.iconMeal3 == 20605) {
            dame += hpMax * 1 / 1000L;
        }
        if (this.isGogeta || this.isKamiOren) {
            dame += dame * 10 / 100L;
        }

        if (player.luachon == 3) {
            dame += dame * 20 / 100L;
        }
        dame += dame * csSdHuman / 100L;
        dame += (dame * dambrolyhp / 100L);

        if (daulau && player.clan != null) {
            int nPlSameClan = 0;
            for (Player pl : player.zone.getPlayers()) {
                if (!pl.equals(player) && pl.clan != null
                        && pl.clan.equals(player.clan) && pl.location.x >= 1285
                        && pl.location.x <= 1645) {
                    nPlSameClan++;
                }
            }
            if (nPlSameClan >= 2) {
                dame += dame * 30 / 100;
            }
        }
        if (this.player.zone != null) {
            if (this.bb3panda) {
                dame += 200 * this.player.zone.getNumOfPlayers();
            }
        }
        if (player.clan != null) {
            if (player.damebang > 200 * player.clan.level) {
                damebang = 200 * player.clan.level;
            } else {
                damebang = player.damebang;
            }
        }
        dame += damebang;

        if (this.isMyhauvuong && this.isKimcobong) {
            dame += (dame * 10 / 100L);
        }
        if (this.isSuperGohan && this.isThienlongdao) {
            dame += (dame * 10 / 100L);
        }
        if (this.isNaruto && this.isMinato) {
            dame += (dame * 10 / 100L);
        }
        // for (Integer tl : this.tlSDDep) {
        // dame += (dame * tl / 100L);
        // }
        // Xử lý pet broly base

        if (this.player.isPet && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            int level = ((Pet) this.player).level;
            if (level > 0) {
                dame += (dame * level / 100L);

            }
        }
        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            dame += (dame * 10 / 100L);
        }

        // Xử lý pet br
        if (this.player.isPet && ((Pet) this.player).typePet >= 2
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA
                        || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
            dame += (dame * 20 / 100L);
        }

        // Xử lý set tinh ấn
        if (this.isTinhAn) {
            dame += (dame * 15L / 100L);
        }

        // Xử lý thức ăn
        if ((this.player.isPet && this.player.itemTime != null && ((Pet) this.player).master.itemTime.isEatMeal
                && ((Pet) this.player).master.itemTime.iconMeal == 14839)) {
            dame += dame * 20 / 100L;
            hpMax += hpMax * 20 / 100L;
            mpMax += mpMax * 20 / 100L;
            this.tlTNSMPet += 100;

        }
        if (this.player.isPet && ((Pet) this.player).master.vip > 0) {
            this.tlHutHp += 10;
            this.tlHutMp += 10;
            this.tlTNSM.add(20);
        }
        if (this.player.isPet && ((Pet) this.player).master.nPoint.isMeoHoaThan) {
            dame += dame * 20 / 100L;
            hpMax += hpMax * 20 / 100L;
            mpMax += mpMax * 20 / 100L;
        }
        if (this.player.isPet && ((Pet) this.player).master.vip > 1) {
            this.kamejokoop += 50;
        }
        if (this.player.isPet && ((Pet) this.player).master.vip > 2) {
            this.isKhongLanh = true;
        }
        if (this.player.isPet && ((Pet) this.player).master.vip > 3) {
            dame += dame * 10 / 100L;
            hpMax += hpMax * 10 / 100L;
            mpMax += mpMax * 10 / 100L;
        }
        if (player.vip > 0) {
            this.tlHutHp += 10;
            this.tlHutMp += 10;
            this.tlTNSM.add(20);
        }
        if ((!this.player.isPet && this.player.itemTime != null && this.player.itemTime.isEatMeal
                && this.player.itemTime.iconMeal != 14839
                || this.player.isPet && this.player.itemTime != null && ((Pet) this.player).master.itemTime.isEatMeal
                        && ((Pet) this.player).master.itemTime.iconMeal != 14839)) {
            dame += dame * 10 / 100L;
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseLoX15) {
            dame -= (dame * 85 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX10) {
            dame -= (dame * 65 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX7) {
            dame -= (dame * 45 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX5) {
            dame -= (dame * 35 / 100L);
        } else if (this.player.itemTime != null && this.player.itemTime.isUseLoX2) {
            dame -= (dame * 25 / 100L);
        }
        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8060) {
            dame += (dame * 5 / 100L);
        }

        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8061) {
            this.tlDameCrit.add(5);
            this.tlSDCM += 5;
        }

        // Xử lý cuồng nộ
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo && !this.player.itemTime.isUseCuongNo2) {
            dame *= 2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo2) {
            dame *= 2.2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseKhauTrang) {
            this.tlDameAttMob.add(10);
        }

        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3 && this.player.itemTime.iconMeal3 == 8247) {
            dame += (dame * 10 / 100L);
        }

        // Xử lý ngọc rồng đen 1 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[0] > System.currentTimeMillis()) {
            dame += (dame * RewardBlackBall.R1S_2 / 100L);
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            dame += (dame * 10 / 100L);
        }
        // dietquy

        // Xử lý set nail
        if (this.player.setClothes.nail >= 2) {
            this.tlDameCrit.add(20);
        }
        // Xử lý gogeta

        if (this.isMyhauvuong && this.isKimcobong) {
            this.tlSDCM += 10;
        }
        if (this.isLabubu && this.isTenLuaCaMap) {

            this.tlDameCrit.add(chimanglabubu);

        }
        if (this.isNaruto && this.isMinato) {
            dame += (dame * 10 / 100L);
            this.tlDameCrit.add(10);
            this.tlSDCM += 10;
        }
        if (this.player.effectSkill.isMucoi) {
            dame += (dame * 10 / 100L);
        }
        if (this.player.effectSkill.isObito) {
            dame += (dame * 10 / 100L);
        }

        if (this.isAuLac && this.isMiNuong || this.isAuLac && this.isHacMiNuong) {
            this.tlSDCM += 15;
        }
        // if (this.player.isPet && ((Pet) this.player).typePet == 4 && (((Pet)
        // this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet)
        // this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2)) {
        // this.tlDameCrit.add(10);
        // this.tlSDCM += 10;
        //
        // }
        if (this.isZoro && this.isEnma) {
            dame += dame * 15 / 100L;
        }
        if (isThienlongdao) {
            dame += (dame * dametld / 100L);
        }

        if (this.player.effectSkill.isMonkey) {
            if (Tangbienkhi > 0) {
                dame += (dame * Tangbienkhi / 100L);
            }
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            dame += 10_000;
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            dame += (dame * 10L / 100L);
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            dame *= this.player.effectSkin.xDame;
        }

        // Xử lý hợp thể
        if (this.player.fusion.typeFusion != 0) {
            dame += this.player.pet.nPoint.dame;
        }

        // Xử lý khỉ
        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentDameMonkey(player.effectSkill.levelMonkey);
                dame += (dame * percent / 100L);
            }
        }

        // Sức đánh đẹp
        dame += (dame * tlSexyDame / 100L);

        // Xử lý giảm dame
        dame -= (dame * tlSubSD / 100L);

        // Xử lý map cold
        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            dame /= 2;
        }

        if (!this.player.isBoss && !this.player.isNewPet
                && TimeUtil.checkTime(EventDAO.getRemainingTimeToIncreaseDame())) {
            dame += dame / 10;
        }

        // if (dame > 2_000_000_000) {
        // dame = 2_000_000_000;
        // }
        if (this.player.effectSkill.isSuper) {
            dame += dame * SkillUtil.getPercentDameSuper(this.player.effectSkill.levelSuper) / 100L;
        }
        if (this.player.effectSkill.isBienHinh) {
            dame += dame * SkillUtil.getPercentHpBienHinh(this.player.effectSkill.levelBienHinh) / 100L;
        }

        dame += dame * csbang / 100L;

        if (dame > player.pointfusion.getDameFusion()) {
            // int damepet = (int)((Pet) player).master.nPoint.dame;
            player.pointfusion.setDameFusion((int) dame);
        }

        this.dame = dame;
    }

    private void setDef() {
        this.def = this.defg * 4;
        this.def += this.defAdd;
        // Xử lý thức ăn 3
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3 && this.player.itemTime.iconMeal3 == 8246) {
            this.def += (this.def * 10 / 100L);
        }
    }

    private void setCrit() {
        this.crit = this.critg;
        this.crit += this.critAdd;
        // biến khỉ
        if (this.player.effectSkill.isMonkey) {
            this.crit = 110;
        }
        if (player.setClothes.thanVuTruKaio >= 2) {
            this.crit += 20;
        }

        this.crit += dambrolycm;
        this.tlSDCM += dambrolycm;

        // Xử lý gogeta
        if (this.isGogeta) {
            crit += 10;
        }
        if (player.clan != null) {
            if (player.critbang > player.clan.level) {
                critbang = player.clan.level;
            } else {
                critbang = player.critbang;
            }
        }
        crit += critbang;
        if (this.isMyhauvuong) {
            crit += dammhv;
        }

        if (this.player.effectSkill.isMucoi) {
            crit += 10;
        }
        if (this.player.effectSkill.isObito) {
            crit += 10;
        }
        if (this.isBroly) {
            crit += 10;
        }
        if (this.player.effectSkill.isSuper) {
            crit += SkillUtil.getPercentCritSuper(this.player.effectSkill.levelSuper);
        }

        // Xử lý thức ăn 3
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal3 && this.player.itemTime.iconMeal3 == 8244) {
            this.crit = this.crit + 5;
        }
    }

    private void resetPoint() {
        this.voHieuChuong = 0;
        this.hpAdd = 0;
        this.mpAdd = 0;
        this.dameAdd = 0;
        this.defAdd = 0;
        this.critAdd = 0;
        this.tlHp.clear();
        this.tlMp.clear();
        this.tlDef.clear();
        this.tlDame.clear();
        this.tlDameCrit.clear();
        this.tlDameAttMob.clear();
        this.tlSDCM = 0;

        this.csSdHuman = 0;
        this.tlHpHoiBanThanVaDongDoi = 0;
        this.tlMpHoiBanThanVaDongDoi = 0;
        this.hpHoi = 0;
        this.mpHoi = 0;
        this.mpHoiCute = 0;
        this.tlHpHoi = 0;
        this.tlMpHoi = 0;
        this.tlHutHp = 0;
        this.tlHutMp = 0;
        this.tlHutHpMob = 0;
        this.tlHutHpMpXQ = 0;
        this.tlPST = 0;
        this.tlTNSM.clear();
        this.tlDameAttMob.clear();
        this.tlGold = 0;
        this.tlNeDon = 0;
        this.tlBom = 0;
        this.speedat = 0;

        this.tlGiap = 0;
        this.tlxgcc = 0;
        this.tlxgc = 0;
        this.tlchinhxac = 0;
        this.damecc = 0;
        this.damec = 0;
        this.dametc = 0;
        this.damgalick = 0;
        this.kamejokoop = 0;
        this.kaiokenop = 0;
        this.quackkop = 0;
        this.lienhoanop = 0;
        this.detrungop = 0;
        this.tlTNSMPet = 0;
        // thu cuoi
        this.isTenLuaCaMap = false;
        this.isAuLac = false;
        this.isRongOrange = false;
        this.csRong = 0;
        this.daulau = false;

        this.dameminuong = 0;
        this.dammhv = 0;
        this.dameminuong = 0;

        this.xChuong = 0;
        this.setltdb = 0;
        this.setTinhAn = 0;
        this.setNhatAn = 0;
        this.setNguyetAn = 0;
        this.tlSexyDame = 0;
        this.tlSubSD = 0;
        this.tlHpGiamODo = 0;
        this.isQuanDiBien = false;
        this.tlSpeed = 0;
        this.teleport = false;

        this.dctt = 0;
        this.dttrung = 0;
        this.Tangbienkhi = 0;
        this.ts = 0;
        this.laze = 0;
        this.hpbang = 0;
        this.mpbang = 0;
        this.damebang = 0;
        this.critbang = 0;
        this.csbang = 0;
        this.hoikhien = 0;
        this.hoitroi = 0;
        this.dtkame = 0;

        this.banhtet = 0;

        this.wearingVoHinh = false;
        this.isKhongLanh = false;
        this.khangTDHS = false;
        this.isTanHinh = false;
        this.isHoaDa = false;
        this.isLamCham = false;
        this.isAdroiTaac = false;
        this.isxinbato = false;
        this.diexinbato = false;
        this.xoihecquen = false;
        this.diexoihecquen = false;
        this.isDoSPL = false;
        this.isThoBulma = false;
        this.isDietQuy = false;
        this.isBunmaTocMau = false;
        this.isTiecBaiBien = false;

    }

    public void addHp(long hp) {
        if (hp > 0) {
            long potentialHp = this.hp + hp;
            if (potentialHp > this.hpMax) {
                this.hp = this.hpMax;
            } else {
                this.hp = potentialHp;
            }
        }
    }

    public void addMp(long mp) {
        long potentialMp = this.mp + mp;

        if (potentialMp > this.mpMax) {
            this.mp = this.mpMax;
        } else if (potentialMp < 0) {
            this.mp = 0;
        } else {
            this.mp = potentialMp;
        }
    }

    public void setHp(long hp) {
        if (hp < 0) {
            this.hp = 0;
        } else {
            this.hp = hp;
        }
    }

    public void setMp(long mp) {
        // if (mp > this.mpMax) {
        // this.mp = this.mpMax;
        // } else
        if (mp < 0) {
            this.mp = 0;
        } else {
            this.mp = mp;
        }
    }

    private void setIsCrit() {
        if (intrinsic != null && intrinsic.id == 25 && this.getCurrPercentHP() <= intrinsic.param1) {
            isCrit = true;
        } else if (isCrit100) {
            isCrit100 = false;
            isCrit = true;
        } else {
            isCrit = Util.isTrue(this.crit, ConstRatio.PER100);
        }
    }

    public int countDam;
    public int countDamLH;
    public int countDamkok;

    public long getDameAttack(boolean isAttackMob) {
        setCaiTrang();

        setThuCuoi();
        setIsCrit();

        setPet();

        setLinhThu();

        setDeoLung();

        long dameAttack = this.dame;
        intrinsic = this.player.playerIntrinsic.intrinsic;
        percentDameIntrinsic = 0;
        int percentDameSkill = 0;
        byte percentXDame = 0;
        Skill skillSelect = player.playerSkill.skillSelect;
        if (skillSelect.template.id != Skill.DICH_CHUYEN_TUC_THOI && isCritTele) {
            isCrit = true;
            isCritTele = false;
        }
        int skillId = skillSelect.template.id;
        if (skillId == Skill.DRAGON || skillId == Skill.DEMON
                || skillId == Skill.GALICK) {
            if (countDam < damChay) {
                countDam++;
            }
            if (this.broly1 || this.broly2 || this.broly3 || this.broly4 || this.broly5) {
                if (this.broly1 || broly2 || broly3) {
                    if (dambrolyhp <= 10) {
                        dambrolyhp++;
                    }
                }
                if (this.broly3) {
                    if (dambrolycm <= 5) {
                        dambrolycm++;
                    }
                }
                if (this.broly4) {
                    if (dambrolycm <= 8) {
                        dambrolycm++;
                    }
                    if (dambrolyhp <= 15) {
                        dambrolyhp++;
                    }
                }
                if (this.broly5) {
                    if (dambrolycm <= 12) {
                        dambrolycm++;
                    }
                    if (dambrolyhp <= 20) {
                        dambrolyhp++;
                    }
                }
                Service.gI().point(player);
            } else {
                dambrolyhp = 0;
                dambrolycm = 0;
            }

            if (this.isToppo) {
                if (HPjr <= 30) {
                    HPjr++;
                }
                Service.gI().point(player);
            } else {
                HPjr = 0;

            }
            if (this.isMyhauvuong) {
                if (dammhv <= 50) {
                    dammhv++;
                }
                Service.gI().point(player);
            } else {
                dammhv = 0;

            }
            if (this.isLabubu && this.isTenLuaCaMap) {
                if (chimanglabubu <= 30) {
                    chimanglabubu++;
                }
                Service.gI().point(player);
            } else {
                chimanglabubu = 0;

            }
            if (this.isJrenCn) {
                if (HPjrcn <= 40) {
                    HPjrcn += 2;
                }
                Service.gI().point(player);
            } else {
                HPjrcn = 0;

            }

            if (this.isThienlongdao) {
                if (dametld <= 20) {
                    dametld++;
                }
                Service.gI().point(player);
            } else {
                dametld = 0;

            }
        } else {
            countDam = 0;
            dambrolyhp = 0;
            dambrolycm = 0;
            HPjr = 0;
            HPjrcn = 0;
            dametld = 0;
            dammhv = 0;
            chimanglabubu = 0;
            Service.gI().point(player);
        }
        if (skillId == Skill.LIEN_HOAN) {
            if (countDamLH < damChayLH) {
                countDamLH++;
            }
        } else {
            countDamLH = 0;
        }
        if (skillId == Skill.KAIOKEN) {
            if (countDamkok < damchayKoK) {
                countDamkok++;
            }
        } else {
            countDamkok = 0;
        }

        switch (skillId) {
            case Skill.DRAGON:
                if (intrinsic.id == 1) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                        && this.player.itemTime.iconMeal3 == 24741) {
                    percentDameSkill += 15;
                }

                percentDameSkill += (countDam + dameminuong + damecc + csRong);
                break;
            case Skill.KAMEJOKO:
                if (intrinsic.id == 2) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (Util.isTrue(20, 100) && this.isSieuQuyLao) {
                    percentDameSkill *= 2;
                }
                percentDameSkill += dameminuong + damec + kamejokoop;
                if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                        && this.player.itemTime.iconMeal3 == 24734) {
                    percentDameSkill += 15;
                }
                if (dtkame > 0) {
                    percentDameSkill += dtkame;
                }

                if (this.player.setClothes.songoku == 5) {
                    percentXDame = 100;
                }
                break;
            case Skill.GALICK:
                if (intrinsic.id == 16) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                percentDameSkill += (countDam + damecc + dameminuong + damgalick + csRong);

                if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                        && this.player.itemTime.iconMeal3 == 24741) {
                    percentDameSkill += 15;
                }
                if (this.player.setClothes.kakarot == 5) {
                    percentXDame = 100;
                }

                break;
            case Skill.ANTOMIC:
                if (intrinsic.id == 17) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (Util.isTrue(20, 100) && this.isSieuQuyLao) {
                    percentDameSkill *= 2;
                }
                percentDameSkill += dameminuong + damec;
                if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                        && this.player.itemTime.iconMeal3 == 24734) {
                    percentDameSkill += 15;
                }

                break;
            case Skill.DEMON:
                if (intrinsic.id == 8) {
                    percentDameIntrinsic = intrinsic.param1;
                }

                percentDameSkill = skillSelect.damage;
                percentDameSkill += dameminuong + damecc + csRong;
                if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                        && this.player.itemTime.iconMeal3 == 24741) {
                    percentDameSkill += 15;
                }

                break;
            case Skill.MASENKO:
                if (intrinsic.id == 9) {
                    percentDameIntrinsic = intrinsic.param1;
                }

                if (this.player.setClothes.nail == 5) {
                    percentXDame = 50;
                }
                percentDameSkill = skillSelect.damage;
                if (Util.isTrue(20, 100) && this.isSieuQuyLao) {
                    percentDameSkill *= 2;
                }
                percentDameSkill += (dameminuong + damec);
                if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                        && this.player.itemTime.iconMeal3 == 24734) {
                    percentDameSkill += 15;
                }
                break;
            case Skill.LIEN_HOAN:
                if (intrinsic.id == 13) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                percentDameSkill += countDamLH + damecc + csRong + lienhoanop;
                if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                        && this.player.itemTime.iconMeal3 == 24741) {
                    percentDameSkill += 15;
                }
                if (this.player.setClothes.lienHoan == 5) {
                    percentXDame = 100;
                }

                break;
            case Skill.KAIOKEN:
                if (intrinsic.id == 26) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                percentDameSkill += countDamkok + dameminuong + damecc + csRong + kaiokenop;

                if (this.player.itemTime != null && this.player.itemTime.isEatMeal3
                        && this.player.itemTime.iconMeal3 == 24741) {
                    percentDameSkill += 15;
                }
                if (this.player.setClothes.kaioken == 5) {
                    percentXDame = 40;
                } else if (player.setClothes.thanVuTruKaio == 5) {
                    percentXDame = 80;
                }

                break;
            case Skill.TU_SAT:
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.cadicM == 4) {
                    percentXDame = 30;
                } else if (this.player.setClothes.cadicM == 5) {
                    percentXDame = 60;
                }
                if (ts > 0) {
                    percentDameSkill += ts;
                }
                if (Util.isTrue(30, 100) && this.isBrolyXayda) {
                    percentDameSkill += 150;
                }
                percentDameSkill += dametc;
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                isCrit = true;
                isCritTele = true;
                dameAttack = Util.nextLong(Util.maxIntValue((dameAttack - (dameAttack / 100 * 5))),
                        Util.maxIntValue((dameAttack + (dameAttack / 100 * 5))));
                if (dctt > 0) {
                    dameAttack += dameAttack * dctt / 100;
                }
                break;
            case Skill.MAKANKOSAPPO:
                countDamLH = 0;
                percentDameSkill = skillSelect.damage;
                if (laze > 0) {
                    percentDameSkill += laze;
                }
                percentDameSkill += dametc;
                if (Util.isTrue(30, 100) && this.isBrolyNamec) {
                    percentDameSkill += 150;
                }
                long dameSkill = Util.maxIntValue((long) this.mpMax * percentDameSkill / 100);
                if (this.player.setClothes.picolo == 5) {
                    dameSkill += dameSkill * 50 / 100;
                }

                return dameSkill;
            case Skill.QUA_CAU_KENH_KHI:
                long hpmob = 0;
                long hppl = 0;

                for (Mob mob : this.player.zone.mobs) {
                    if (!mob.isDie() && Util.getDistance(this.player, mob) <= SkillUtil
                            .getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hpmob += mob.point.hp;
                    }
                }

                for (Player pl : this.player.zone.getHumanoids()) {
                    if (!pl.isDie() && this.player.id != pl.id && Util.getDistance(this.player, pl) <= SkillUtil
                            .getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hppl += pl.nPoint.hp;
                    }
                }

                long dameqckk = (hpmob * 1 / 100) + (hppl * 1 / 100) + this.dame * 10;
                if (this.isNaruto) {
                    dameqckk += dameqckk * 15 / 100;
                }
                if (this.player.setClothes.kirin == 5) {
                    dameqckk *= 2;
                }
                if (Util.isTrue(30, 100) && this.isBrolyTraiDat) {
                    dameqckk *= 150 / 100;
                }
                dameqckk = dameqckk * (this.quackkop) / 100;
                dameqckk = dameqckk + (Util.nextInt(-5, 5) * dameqckk / 100);
                // if (dameqckk > 2_000_000_000) {
                // dameqckk = 2_000_000_000;
                // }
                return dameqckk;
            case Skill.DE_TRUNG:
                if (player.setClothes.pikkoroDaimao == 5) {
                    dameAttack *= 2;
                }

                if (dttrung > 0) {
                    dameAttack += dameAttack * dttrung / 100;
                }
                dameAttack += dameAttack * detrungop / 100;
                // if (dameAttack > 2_000_000_000) {
                // dameAttack = 2_000_000_000;
                // }
                return dameAttack;
        }

        if (intrinsic.id == 18 && this.player.effectSkill.isMonkey) {
            percentDameIntrinsic = intrinsic.param1;
        }

        if (percentDameSkill != 0) {
            dameAttack = dameAttack * percentDameSkill / 100;
        }

        dameAttack += (dameAttack * percentDameIntrinsic / 100);
        dameAttack += (dameAttack * dameAfter / 100);
        if (this.player.effectSkill != null && this.player.effectSkill.isDameBuff && tlSexyDame == 0) {
            int tiLeDame = this.player.effectSkill.tileDameBuff;
            dameAttack += (dameAttack * tiLeDame / 100L);
        }
        if (isAttackMob) {
            for (Integer tl : this.tlDameAttMob) {
                dameAttack += (dameAttack * tl / 100);
            }
            if (this.player.isPet && ((Pet) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                dameAttack *= 2;
            }
        }

        dameAfter = 0;

        if (isCrit) {
            dameAttack *= 2;
            dameAttack += (dameAttack * tlSDCM / 100);
        }

        dameAttack += ((long) dameAttack * percentXDame / 100);

        long tempDameAttack = (long) (dameAttack / 100L * 5L);
        if (tempDameAttack <= 0) {
            tempDameAttack = 1;
        }
        dameAttack += (long) (Util.getOne(-1, 1) * Util.nextInt((int) tempDameAttack) + 1);

        if (player.effectSkin != null && player.effectSkin.isXChuong
                && (player.playerSkill.skillSelect.template.id == Skill.KAMEJOKO
                        || player.playerSkill.skillSelect.template.id == Skill.ANTOMIC
                        || player.playerSkill.skillSelect.template.id == Skill.MASENKO)) {
            dameAttack *= xChuong;
            player.effectSkin.isXDame = true;
            player.effectSkin.isXChuong = false;
            player.effectSkin.lastTimeXChuong = System.currentTimeMillis();
        }

        // if (dameAttack > 2_000_000_000) {
        // dameAttack = 2_000_000_000;
        // }
        return dameAttack;
    }

    public int getCurrPercentHP() {
        if (this.hpMax == 0) {
            return 100;
        }
        return (int) ((long) this.hp * 100 / this.hpMax);
    }

    public int getCurrPercentMP() {
        return (int) ((long) this.mp * 100 / this.mpMax);
    }

    public void setFullHpMp() {
        this.hp = this.hpMax;
        this.mp = this.mpMax;
    }

    public void subHP(long sub) {
        this.hp -= sub;
        if (this.hp <= 0) {
            this.hp = 0;
            this.setHp(0);
        }
    }

    public void subMP(long sub) {
        this.mp -= sub;
        if (this.mp <= 0) {
            this.mp = 0;
        }

        // if (this.mp > 2_000_000_000) {
        // this.mp = 2_000_000_000;
        // }
    }

    private void checkLevel() {
        if (getDoneLevel(8)) {
            tlHp.add(10);
            tlMp.add(10);
            tlDame.add(10);
        } else if (getDoneLevel(7)) {
            tlHp.add(5);
            tlMp.add(5);
            tlDame.add(5);
        } else if (getDoneLevel(6)) {
            tlHp.add(3);
            tlMp.add(3);
            tlDame.add(3);
        }
    }

    public boolean getDoneLevel(int level) {
        int j = 0;
        for (int i = 0; i < this.player.inventory.itemsBody.size(); i++) {
            if (i < 5) {
                Item item = this.player.inventory.itemsBody.get(i);
                if (item == null || item.template == null) {
                    return false;
                }
                ItemOption itemOption = findParam(item, 72);
                if (itemOption != null && itemOption.param >= level) {
                    j++;
                }
                if (j >= 4) {
                    return true;
                }
            }
        }
        return false;
    }

    public ItemOption findParam(Item item, int id) {
        for (ItemOption itemOption : item.itemOptions) {
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                return itemOption;
            }
        }
        return null;
    }

    public long calSucManhTiemNang(long tiemNang) {
        // if (player.zone.map.type == 3) {
        // return 0;
        // }
        if (power < getPowerLimit()) {
            for (Integer tl : this.tlTNSM) {
                tiemNang += ((long) tiemNang * tl / 100);
            }
            if (this.player.cFlag != 0) {
                if (this.player.cFlag == 8) {
                    tiemNang += ((long) tiemNang * 10 / 100);
                } else {
                    tiemNang += ((long) tiemNang * 5 / 100);
                }
            }
            long tn = tiemNang;
            if (this.player.charms.tdTriTue > System.currentTimeMillis()) {
                tiemNang += tn;
            }
            if (this.player.charms.tdTriTue3 > System.currentTimeMillis()) {
                tiemNang += tn * 2;
            }
            if (this.player.charms.tdTriTue4 > System.currentTimeMillis()) {
                tiemNang += tn * 3;
            }
            // if (this.player.charms.tdTriTue4 > System.currentTimeMillis()) {
            // tiemNang += tn * 3;
            // }
            if (this.player.effectSkill.isChibi && this.player.typeChibi == 2) {
                tiemNang += tn * 2;
            }
            // if (this.player.getSession() != null && this.player.getSession().vip > 0 ||
            // this.player.isPet && ((Pet) this.player).master.getSession() != null &&
            // ((Pet) this.player).master.getSession().vip > 0) {
            // tiemNang += tn * 3;
            // }
            // if (this.player.itemTime != null && this.player.itemTime.isUseDK) {
            // tiemNang += tn * 2;
            // }
            if (this.player.itemTime != null && this.player.itemTime.isUseKhauTrang) {
                tiemNang += tn * 5 / 100;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX2) {
                tiemNang += tn * 2;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX5) {
                tiemNang += tn * 5;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX7) {
                tiemNang += tn * 7;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX10) {
                tiemNang += tn * 10;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseLoX15) {
                tiemNang += tn * 15;
            }
            if (this.player.satellite != null && this.player.satellite.isIntelligent) {
                tiemNang += tn / 5;
            }
            if (this.intrinsic != null && this.intrinsic.id == 24) {
                tiemNang += ((long) tiemNang * this.intrinsic.param1 / 100);
            }
            // if (this.power >= 60000000000L) {
            // tiemNang -= ((long) tiemNang * 20 / 100);
            // }
            if (this.player.isPet) {
                if (((Pet) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                    tiemNang += tn * 2;
                }
                if (((Pet) this.player).itemTime.lastTimeBuax2DeTu > System.currentTimeMillis()) {
                    tiemNang += tn * 2;
                }
                if (((Pet) this.player).master.nPoint != null && ((Pet) this.player).master.nPoint.tlTNSMPet > 0) {
                    tiemNang += tn / 100 * (((Pet) this.player).master.nPoint.tlTNSMPet + 100);
                }

            }

            if (TimeUtil.checkTime(EventDAO.getRemainingTimeToIncreasePotentialAndPower())) {
                tiemNang *= 2;
            }
            if (MapService.gI().isMapNguHanhSon(this.player.zone.map.mapId)) {
                tiemNang *= 1;
            }
            if (MapService.gI().isMapBanDoKhoBau(this.player.zone.map.mapId)) {
                tiemNang *= 3;
            }
            tiemNang *= Manager.RATE_EXP_SERVER;
            tiemNang = calSubTNSM(tiemNang);
            if (tiemNang <= 0) {
                tiemNang = 1;
            }
        } else {
            tiemNang = 0;
        }
        return tiemNang;
    }

    public long calSubTNSM(long tiemNang) {
        tiemNang = (long) (tiemNang * 1);
        if (player.nPoint.power >= 80_000_000_000L) {
            tiemNang = tiemNang / 50;
        } else if (player.nPoint.power >= 60_000_000_000L) {
            tiemNang = tiemNang / 40;
        } else if (player.nPoint.power >= 40_000_000_000L) {
            tiemNang = tiemNang / 30;
        } else if (player.nPoint.power >= 100_000_000_000L) {
            tiemNang = tiemNang / 90;
        } else if (player.nPoint.power >= 130_000_000_000L) {
            tiemNang = tiemNang / 100;
        }
        if (player.nPoint.power >= 150_000_000_000L) {
            tiemNang = tiemNang / 150;
        }
        return tiemNang;
    }

    public short getTileHutHp(boolean isMob) {
        if (isMob) {
            return (short) (this.tlHutHp + this.tlHutHpMob);
        } else {
            return this.tlHutHp;
        }
    }

    public short getTiLeHutMp() {
        return this.tlHutMp;
    }

    public int subDameInjureWithDeff(long dame) {
        long def = this.def;
        dame -= def;
        if (dame < 0) {
            dame = 1;
        }
        return (int) dame;
    }

    /*------------------------------------------------------------------------*/
    public boolean canOpenPower() {
        return this.power >= getPowerLimit();
    }

    public long getPowerLimit() {
        if (powerLimit != null) {
            return powerLimit.getPower();
        }
        return 0;
    }

    public long getPowerNextLimit() {
        PowerLimit powerLimit = PowerLimitManager.getInstance().get(limitPower + 1);
        if (powerLimit != null) {
            return powerLimit.getPower();
        }
        return 0;
    }

    // **************************************************************************
    // POWER - TIEM NANG
    public void powerUp(long power) {
        this.power += power;
        TaskService.gI().checkDoneTaskPower(player, this.power);
    }

    public void tiemNangUp(long tiemNang) {
        this.tiemNang += tiemNang;
    }

    public void increasePoint(byte type, short point, boolean manualForPet) {
        if (powerLimit == null) {
            return;
        }
        if (point <= 0) {
            return;
        }
        boolean updatePoint = false;
        long tiemNangUse = 0;
        if (type == 0) {
            int pointHp = point * 20;
            tiemNangUse = point * (2 * (this.hpg + 1000) + pointHp - 20) / 2;
            if ((this.hpg + pointHp) <= powerLimit.getHp()) {
                if (doUseTiemNang(tiemNangUse)) {
                    hpg += pointHp;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "HP của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 1) {
            int pointMp = point * 20;
            tiemNangUse = point * (2 * (this.mpg + 1000) + pointMp - 20) / 2;
            if ((this.mpg + pointMp) <= powerLimit.getMp()) {
                if (doUseTiemNang(tiemNangUse)) {
                    mpg += pointMp;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "KI của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 2) {
            tiemNangUse = point * (2 * this.dameg + point - 1) / 2 * 100;
            if ((this.dameg + point) <= powerLimit.getDamage()) {
                if (doUseTiemNang(tiemNangUse)) {
                    dameg += point;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "Sức đánh của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 3) {
            tiemNangUse = 2 * (this.defg + 5) / 2 * 100000;
            if ((this.defg + point) <= powerLimit.getDefense()) {
                if (doUseTiemNang(tiemNangUse)) {
                    defg += point;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "Giáp của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (type == 4) {
            tiemNangUse = 50000000L;
            for (int i = 0; i < this.critg; i++) {
                tiemNangUse *= 5L;
            }
            if ((this.critg + point) <= powerLimit.getCritical()) {
                if (doUseTiemNang(tiemNangUse)) {
                    critg += point;
                    updatePoint = true;
                }
            } else {
                Service.gI().sendThongBao(player, "Chí mạng của bạn đã đạt mức tối đa");
                Service.gI().sendMoney(player);
                return;
            }
        }
        if (updatePoint) {
            Service.gI().point(player);
        }
        if (manualForPet) {
            Service.gI().sendChiSoPetGoc(((Pet) player).master);
            Service.gI().showInfoPet(((Pet) player).master);
            Service.gI().point(((Pet) player).master);
        }
    }

    // public void increasePoint(byte type, short point) {
    // if (point <= 0 || point > 100) {
    // return;
    // }
    // long tiemNangUse;
    // if (type == 0) {
    // int pointHp = point * 20;
    // tiemNangUse = point * (2 * (this.hpg + 1000) + pointHp - 20) / 2;
    // if ((this.hpg + pointHp) <= getHpMpLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // hpg += pointHp;
    // }
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return; Service.
    // }
    // }
    // if (type == 1) {
    // int pointMp = point * 20;
    // tiemNangUse = point * (2 * (this.mpg + 1000) + pointMp - 20) / 2;
    // if ((this.mpg + pointMp) <= getHpMpLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // mpg += pointMp;
    // }
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // if (type == 2) {
    // TaskService.gI().checkDoneTaskNangCS(player);
    // tiemNangUse = point * (2 * this.dameg + point - 1) / 2 * 100;
    // if ((this.dameg + point) <= getDameLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // dameg += point;
    // }
    // TaskService.gI().checkDoneTaskNangCS(player);
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // if (type == 3) {
    // tiemNangUse = 2 * (this.defg + 5) / 2 * 100000;
    // if ((this.defg + point) <= getDefLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // defg += point;
    // }
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // if (type == 4) {
    // tiemNangUse = 50000000L;
    // for (int i = 0; i < this.critg; i++) {
    // tiemNangUse *= 5L;
    // }
    // if ((this.critg + point) <= getCritLimit()) {
    // if (doUseTiemNang(tiemNangUse)) {
    // critg += point;
    // }
    // } else {
    // Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
    // return;
    // }
    // }
    // Service.gI().point(player);
    // }
    private boolean doUseTiemNang(long tiemNang) {
        if (this.tiemNang < tiemNang) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ tiềm năng");
            return false;
        }
        if (this.tiemNang >= tiemNang && this.tiemNang - tiemNang >= 0) {
            this.tiemNang -= tiemNang;
            TaskService.gI().checkDoneTaskUseTiemNang(player);
            return true;
        }
        return false;
    }

    public long getFullTN() {
        long tnhp = 0, tnki = 0, tnsd = 0, tng = 0, tncm = 0;

        if (hpg > 0) {
            tnhp = (((hpg / 20L) * (50L + (50L + (hpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (mpg > 0) {
            tnki = (((mpg / 20L) * (50L + (50L + (mpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (dameg > 0) {
            tnsd = ((dameg * (dameg - 1L) * 100L) / 2L);
        }
        if (defg > 0) {
            tng = ((defg * (500000L + (500000L + (defg - 1L) * 100000L))) / 2L);
        }
        if (critg > 0) {
            tncm = ((50L * (((long) Math.pow(5L, critg) - 1L)) / (5L - 1L) * 1000000L));
        }
        return tnhp + tnki + tnsd + tng + tncm;
    }

    // --------------------------------------------------------------------------
    private long lastTimeHoiPhuc;
    private long lastTimeHoiStamina;

    public void update() {
        if (player != null && player.effectSkill != null) {
            if (player.effectSkill.isCharging && player.effectSkill.countCharging < 10) {
                int tiLeHoiPhuc = SkillUtil.getPercentCharge(player.playerSkill.skillSelect.point);
                if (player.effectSkill.isCharging && !player.isDie() && !player.effectSkill.isHaveEffectSkill()
                        && (hp < hpMax || mp < mpMax)) {
                    long hpRecovered = hpMax / 100 * tiLeHoiPhuc;
                    long mpRecovered = mpMax / 100 * tiLeHoiPhuc;

                    // if (hp + hpRecovered > 2_000_000_000) {
                    // hpRecovered = 2_000_000_000 - hp;
                    // }
                    // if (mp + mpRecovered > 2_000_000_000) {
                    // mpRecovered = 2_000_000_000 - mp;
                    // }
                    PlayerService.gI().hoiPhuc(player, Util.maxIntValue(hpRecovered), Util.maxIntValue(mpRecovered));

                    if (player.effectSkill.countCharging % 3 == 0) {
                        Service.gI().chat(player, "Phục hồi năng lượng " + getCurrPercentHP() + "%");
                    }
                } else {
                    EffectSkillService.gI().stopCharge(player);
                }
                if (++player.effectSkill.countCharging >= 10) {
                    EffectSkillService.gI().stopCharge(player);
                }
            }

            if (Util.canDoWithTime(lastTimeHoiPhuc, 30000)) {
                PlayerService.gI().hoiPhuc(this.player, Util.maxIntValue(hpHoi), Util.maxIntValue(mpHoi));
                this.lastTimeHoiPhuc = System.currentTimeMillis();
            }

            if (Util.canDoWithTime(lastTimeHoiStamina, 60000) && this.stamina < this.maxStamina) {
                this.stamina++;
                this.lastTimeHoiStamina = System.currentTimeMillis();

                if (!this.player.isBoss && !this.player.isPet) {
                    PlayerService.gI().sendCurrentStamina(this.player);
                }
            }
        }
    }

    public void dispose() {
        this.intrinsic = null;
        this.player = null;
        this.tlHp = null;
        this.tlMp = null;
        this.tlDef = null;
        this.tlDame = null;
        this.tlDameAttMob = null;
        this.tlTNSM = null;
    }
}
