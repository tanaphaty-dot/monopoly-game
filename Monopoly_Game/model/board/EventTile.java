package model.board;

/**
 * คลาสสำหรับช่องสุ่มเหตุการณ์การ์ด
 * @author ฟิล์ม (feature/board-setup)
 */
public class EventTile extends Tile {
    private final String eventType;

    public EventTile(int id, String name, String eventType) {
        super(id, name);
        this.eventType = eventType;
    }

    public String getEventType() { return eventType; }

    @Override
    public void onStep(Object player) {
        // TODO (ถึง เก๋า): ลิงก์ช่องนี้เข้ากับระบบจั่วการ์ด EventCard ของเก๋าที่เป็น combat
        System.out.println("[Board] Player landed on EventTile: " + getName() + " (Type: " + eventType + ")");
    }
}