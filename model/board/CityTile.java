package model.board;

import model.entity.Player;

/**
 * คลาสสำหรับช่องเมือง ซื้อขายได้ มีระบบค่าเช่าและโซน
 * @author ฟิล์ม (feature/board-setup)
 */
public class CityTile extends Tile { // สืบทอดรับค่าสถานะของเมือง เช่น ราคา ค่าเช่า โซนสี เจ้าของ
    private final int basePrice;
    private final int baseRent;
    private final String zone; // Green, Blue, Yellow, Red

    private Player owner;

    public CityTile(int id, String name, int basePrice, int baseRent, String zone) { 
        super(id, name);
        this.basePrice = basePrice;
        this.baseRent = baseRent;
        this.zone = zone;
        this.owner = null; // เริ่มต้นยังไม่มีเจ้าของ
    }

    public int getBasePrice() { return basePrice; }
    public int getBaseRent() { return baseRent; }
    public String getZone() { return zone; }

    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }
}