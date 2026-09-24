package model.board;

/**
 * Factory Class สำหรับสร้างวัตถุประเภท Tile (คลาสช่วยสร้าง Tile) 
 *  ถ้าเพิ่ม CityTile ใหม่ ให้มาแก้ในนี้ไม่ต้องไล่แก้โค้ดสร้างกระดานหมด 
 * @author ฟิล์ม (feature/board-setup)
 */
public class TileFactory { // ส่งออกคืนค่าเป็น Tile (CityTile or EventTile)
    public static Tile createCityTile(int id, String name, int price, int rent, String zone) {
        return new CityTile(id, name, price, rent, zone);
    }

    public static Tile createEventTile(int id, String name, String eventType) {
        return new EventTile(id, name, eventType);
    }
}