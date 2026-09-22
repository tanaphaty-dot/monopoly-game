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

    // NOTICE (ถึง เดียร์): Mockup โดยใช้ Object owner ชั่วคราว (Mockup คือเป็นแบบจำลองเฉยๆ)
    // เดียร์สร้างคลาส Player เสร็จแล้ว ให้เปลี่ยนชนิดตัวแปรเป็น Player owner ด้วยนะ
    private Player owner; // รอรับค่าถ้ามีคนเป็นเจ้าของ ซื้อ

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

    // แก้เป็น Player ละ
    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }

    @Override
    public void onStep(Player player) {
        // TODO (ถึง พีท/เก๋า): เมื่อตกเมืองนี้ ให้ GameController หรือ SiegeSystem เรียกคำนวณการซื้อ/จ่ายค่าเช่า
        System.out.println("[Board] Player landed on CityTile: " + getName() + " (Zone: " + zone + ")");
    }
}