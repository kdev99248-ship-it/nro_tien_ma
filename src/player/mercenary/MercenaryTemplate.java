package player.mercenary;

/**
 * Template lính đánh thuê - được load từ database
 * Định nghĩa các thuộc tính cơ bản của một loại lính đánh thuê
 */
public class MercenaryTemplate {
    // ID định danh
    private int id;

    // Thông tin cơ bản
    private String name;
    private int planetType; // 0: Trái Đất, 1: Xayda, 2: Namec

    // Giá thuê (số thỏi vàng - item id 457)
    private int price30Min; // Giá thuê 30 phút
    private int price1Hour; // Giá thuê 1 giờ
    private int price5Hour; // Giá thuê 5 giờ

    // Chỉ số cơ bản
    private long hp;
    private long mp;
    private long dame;
    private int def;
    private int crit;

    // Ngoại hình
    private int head;
    private int body;
    private int leg;
    private int gender;

    // Khả năng đặc biệt
    private boolean canAttackBoss;

    // Trạng thái
    private boolean active;

    public MercenaryTemplate() {
    }

    public MercenaryTemplate(int id, String name, int planetType,
            int price30Min, int price1Hour, int price5Hour,
            long hp, long mp, long dame, int def, int crit,
            int head, int body, int leg, int gender,
            boolean canAttackBoss, boolean active) {
        this.id = id;
        this.name = name;
        this.planetType = planetType;
        this.price30Min = price30Min;
        this.price1Hour = price1Hour;
        this.price5Hour = price5Hour;
        this.hp = hp;
        this.mp = mp;
        this.dame = dame;
        this.def = def;
        this.crit = crit;
        this.head = head;
        this.body = body;
        this.leg = leg;
        this.gender = gender;
        this.canAttackBoss = canAttackBoss;
        this.active = active;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPlanetType() {
        return planetType;
    }

    public int getPrice30Min() {
        return price30Min;
    }

    public int getPrice1Hour() {
        return price1Hour;
    }

    public int getPrice5Hour() {
        return price5Hour;
    }

    public long getHp() {
        return hp;
    }

    public long getMp() {
        return mp;
    }

    public long getDame() {
        return dame;
    }

    public int getDef() {
        return def;
    }

    public int getCrit() {
        return crit;
    }

    public int getHead() {
        return head;
    }

    public int getBody() {
        return body;
    }

    public int getLeg() {
        return leg;
    }

    public int getGender() {
        return gender;
    }

    public boolean isCanAttackBoss() {
        return canAttackBoss;
    }

    public boolean isActive() {
        return active;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlanetType(int planetType) {
        this.planetType = planetType;
    }

    public void setPrice30Min(int price30Min) {
        this.price30Min = price30Min;
    }

    public void setPrice1Hour(int price1Hour) {
        this.price1Hour = price1Hour;
    }

    public void setPrice5Hour(int price5Hour) {
        this.price5Hour = price5Hour;
    }

    public void setHp(long hp) {
        this.hp = hp;
    }

    public void setMp(long mp) {
        this.mp = mp;
    }

    public void setDame(long dame) {
        this.dame = dame;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public void setCrit(int crit) {
        this.crit = crit;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public void setBody(int body) {
        this.body = body;
    }

    public void setLeg(int leg) {
        this.leg = leg;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setCanAttackBoss(boolean canAttackBoss) {
        this.canAttackBoss = canAttackBoss;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Lấy giá thuê theo thời gian
     * 
     * @param durationOption 0: 30 phút, 1: 1 giờ, 2: 5 giờ
     * @return số thỏi vàng cần trả
     */
    public int getPriceByDuration(int durationOption) {
        return switch (durationOption) {
            case 0 -> price30Min;
            case 1 -> price1Hour;
            case 2 -> price5Hour;
            default -> price30Min;
        };
    }

    /**
     * Lấy thời gian thuê theo option
     * 
     * @param durationOption 0: 30 phút, 1: 1 giờ, 2: 5 giờ
     * @return số giây thuê
     */
    public static int getDurationSeconds(int durationOption) {
        return switch (durationOption) {
            case 0 -> 1800; // 30 phút = 1800 giây
            case 1 -> 3600; // 1 giờ = 3600 giây
            case 2 -> 18000; // 5 giờ = 18000 giây
            default -> 1800;
        };
    }

    /**
     * Lấy tên hành tinh theo type
     */
    public String getPlanetName() {
        return switch (planetType) {
            case 0 -> "Trái Đất";
            case 1 -> "Xayda";
            case 2 -> "Namec";
            default -> "Không rõ";
        };
    }

    /**
     * Tạo text hiển thị trên menu
     */
    public String getMenuDisplayName() {
        return name + "\n(" + getPlanetName() + ")";
    }

    /**
     * Tạo text thông tin chi tiết để hiển thị trong menu NPC
     * Format đẹp với màu sắc và icon
     */
    public String getDetailInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("|1|=== ").append(name).append(" ===")
                .append("\n|7|Hành tinh: ").append(getPlanetName())
                .append("\n")
                .append("\n|2|--- CHỈ SỐ ---")
                .append("\n|3|HP: ").append(formatNumber(hp))
                .append("\n|4|KI: ").append(formatNumber(mp))
                .append("\n|1|Sức đánh: ").append(formatNumber(dame))
                .append("\n|7|Giáp: ").append(formatNumber(def))
                .append("\n|6|Chí mạng: ").append(crit).append("%")
                .append("\n")
                .append("\n|2|--- GIÁ THUÊ (Thỏi Vàng) ---")
                .append("\n30 phút: ").append(price30Min).append(" thỏi")
                .append("\n1 giờ: ").append(price1Hour).append(" thỏi")
                .append("\n5 giờ: ").append(price5Hour).append(" thỏi")
                .append("\n")
                .append("\n|2|--- KHẢ NĂNG ĐẶC BIỆT ---");

        if (canAttackBoss) {
            sb.append("\n|1|✓ Có thể tấn công Boss");
        } else {
            sb.append("\n|5|✗ Không thể tấn công Boss");
        }

        return sb.toString();
    }

    /**
     * Format số lớn cho dễ đọc
     */
    private String formatNumber(long num) {
        if (num >= 1_000_000_000) {
            return String.format("%.1fT", num / 1_000_000_000.0);
        } else if (num >= 1_000_000) {
            return String.format("%.1fTr", num / 1_000_000.0);
        } else if (num >= 1_000) {
            return String.format("%.1fK", num / 1_000.0);
        }
        return String.valueOf(num);
    }

    @Override
    public String toString() {
        return "MercenaryTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", planetType=" + planetType +
                ", canAttackBoss=" + canAttackBoss +
                '}';
    }
}
