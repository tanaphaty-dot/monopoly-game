package model.board;

/**
 * โรงงานสร้างช่อง
 * Board ไม่ต้อง new CityTile / EventTile เอง เรียกเมธอดในนี้แทน
 * ชื่อกับชนิดของช่องพิเศษอยู่ที่นี่ที่เดียว
 *
 * @author ฟิล์ม (feature/board-setup)
 */
public class TileFactory {
    public static Tile createCityTile(int id, String name, int price, int rent, String zone) {
        return new CityTile(id, name, price, rent, zone);
    }

    public static Tile createEventTile(int id, String name, String eventType) {
        return new EventTile(id, name, eventType);
    }

    public static Tile createStartTile(int id) {
        return new EventTile(id, "Start", EventTile.START);
    }

    public static Tile createCampTile(int id) {
        return new EventTile(id, "Camp", EventTile.CAMP);
    }

    public static Tile createWatchtowerTile(int id) {
        return new EventTile(id, "Watch", EventTile.WATCHTOWER);
    }

    public static Tile createWarJailTile(int id) {
        return new EventTile(id, "Jail", EventTile.WAR_JAIL);
    }

    public static Tile createTrainingGroundTile(int id) {
        return new EventTile(id, "Train", EventTile.TRAINING_GROUND);
    }
}
