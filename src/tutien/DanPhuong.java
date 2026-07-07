package tutien;

import java.util.ArrayList;
import java.util.List;

// M8 Luyen Dan: 1 dan phuong (cong thuc) = 1 vien dan (output pill_item_id) + danh sach nguyen lieu.
// Load tu DB (tutien_dan_phuong + tutien_dan_phuong_nl) qua DanPhuongTemplate.
public class DanPhuong {

    public int     id;
    public String  name;         // ten hien thi (rong -> client lay ten item pillItemId)
    public int     pillItemId;   // item_template vien dan (output)
    public int     realm;        // canh gioi 1..10 (nhom hien thi: 1 Luyen Khi ... 6 Nguyen Anh ...)
    public int     reqLevel;     // cap luyen dan su CAN co (BAT BUOC tu khai trong SQL)
    public String  description;
    public boolean active = true;

    // moi phan tu = {itemId, qty} — giu thu tu khai bao trong DB
    public final List<int[]> materials = new ArrayList<>();

    public void addMaterial(int itemId, int qty) {
        materials.add(new int[]{ itemId, qty });
    }
}
