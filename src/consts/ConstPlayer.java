package consts;

public class ConstPlayer {

        public static final int[] HEADMONKEY = { 192, 195, 196, 199, 197, 200, 198 };

        public static final byte[][] AURABIENHINH = {
                        // LẦN LƯỢT TỪ LB 1-5
                        { 20, 20, 21, 27, 29 }, // td
                        { 0, 22, 23, 24, 30 }, // nm
                        { 20, 20, 21, 23, 25 } // xd
        };
        // SỬA NGOẠI HÌNH TỪ LV 1-5 Ở ĐÂY
        public static final short[][] HEADBIENHINH = {
                        { 1774, 1778, 1780, 1783, 1786 }, // head TD
                        { 1822, 1825, 1826, 1827, 1828 }, // head NM
                        { 1789, 1793, 1796, 1798, 1800 }, // head XD
        };
        // THÂN NGOẠI HÌNH LV 1-5
        public static final short[] BODYBIENHINH = { 1776, 1823, 1791 }; // TD /NM/ XD
        public static final short[] LEGBIENHINH = { 1777, 1824, 1792 }; // TD /NM/ XD

        public static final byte TRAI_DAT = 0;
        public static final byte NAMEC = 1;
        public static final byte XAYDA = 2;

        // type pk
        public static final byte NON_PK = 0;
        public static final byte PK_PVP = 3;
        public static final byte PK_PVP_2 = 4;
        public static final byte PK_ALL = 5;

        // type fushion
        public static final byte NON_FUSION = 0;
        public static final byte LUONG_LONG_NHAT_THE = 4;
        public static final byte HOP_THE_PORATA = 6;
        public static byte HOP_THE_PORATA2 = 8;
        public static byte LUONG_LONG_NHAT_THE_GOGETA = 12;
        public static byte LUONG_LONG_NHAT_THE_Broly = 14;
}
