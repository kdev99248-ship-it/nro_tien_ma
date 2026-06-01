package matches;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TOP {
    public String getName() {
        return name;
    }
    private String name;
    private byte gender;
    private short head;
    private short body;
    private short leg;
    private long power;
    private long ki;
    private long hp;
    private long sd;
    private byte nv;
    private byte subnv;
    private int sk;
    private int pvp;
    private int diemsm;
    private int vongquay;
    private int diemtet;
    private int bossday;
    private int diemhopqua;
    private int nhs;
    private int dicanh;
    private int divdst;
    private int juventus;
    private long lasttime;
    private long time;
    private int level;
    private int cash;
    private int thoivang;
    private long banghoi;
}
