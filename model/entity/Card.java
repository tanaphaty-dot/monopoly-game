package model.entity;

/**
 * เก็บข้อมูลการ์ด การ์ดเหตุการณ์
 * @author เดียร์ (feature/entity-player)
 */
public class Card {
    private final String id;
    private final String title;
    private final String description;
    private final String type; // "BUFF_DEFENSE", "TAX_PENALTY", "FREE_MOVE"

    public Card(String id, String title, String description, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    // NOTICE (ถึง เก๋า): ดึง type ไปประมวลผลเอฟเฟกต์การ์ดใน EventCard.java ของระบบ combat
    public String getType() { return type; }
}