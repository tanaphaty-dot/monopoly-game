package model.board;


import model.entity.Player;

/**
 * ช่องที่ไม่ใช่เมือง มี 2 แบบ
 * 1) ช่องพิเศษ 5 ช่อง (จุดเริ่ม ค่าย หอคอย คุก สนามซ้อม) ได้ผลทันที
 * 2) ช่องการ์ด (CHANCE) ผู้เล่นต้องกดจั่วการ์ด
 *
 * ผลจริงไม่ได้ทำใน onStep()
 * GameController เป็นคนอ่าน getEventType() แล้วทำให้
 * เพื่อไม่ให้กฎเกมไปกองอยู่ในคลาสวาดช่อง
 *
 * @author ฟิล์ม (feature/board-setup)
 */
public class EventTile extends Tile {
    public static final String CHANCE = "CHANCE";
    public static final String START = "START";
    public static final String CAMP = "CAMP";
    public static final String WATCHTOWER = "WATCHTOWER";
    public static final String WAR_JAIL = "WAR_JAIL";
    public static final String TRAINING_GROUND = "TRAINING_GROUND";

    /** เดินผ่านจุดเริ่มได้เงินก้อนนี้ (ให้ตอนเดินใน GameController) */
    public static final int PASS_START_BONUS = 200;
    /** ตกค่ายได้เงินพักฟื้น */
    public static final int CAMP_HEAL = 100;
    /** เกราะค่าย ทำให้ค่าบุกครั้งถัดไปแพงขึ้น */
    public static final int CAMP_DEFENSE_BONUS = 50;
    /** สนามซ้อม / การ์ดซ้อม ทำให้ค่าบุกครั้งถัดไปถูกลง */
    public static final int TRAINING_ATTACK_BONUS = 50;

    private final String eventType;

    public EventTile(int id, String name, String eventType) {
        super(id, name);
        this.eventType = eventType;
    }

    public String getEventType() { return eventType; }

    /** ช่องการ์ดเท่านั้นที่ต้องกดจั่ว ช่องพิเศษไม่จั่ว */
    public boolean isCardTile() {
        return CHANCE.equals(eventType);
    }

    /** ป้ายสั้นบนช่อง ใช้คำอังกฤษตาม step12 ไม่ได้แปลเป็นไทย */
    public String getShortTag() {
        switch (eventType) {
            case START: return "START";
            case CAMP: return "CAMP";
            case WATCHTOWER: return "WATCH";
            case WAR_JAIL: return "JAIL";
            case TRAINING_GROUND: return "TRAIN";
            default: return "EVENT";
        }
    }

    /** คำอธิบายกติกา */
    public String getDescription() {
        switch (eventType) {
            // *** \u00B7 คือ Unicode Escape Sequence ที่หมายถึงตัวอักษร Middle Dot (·) หรือจุดกึ่งกลาง  ***
            case START:
                return "START  \u00B7  PASS THIS TILE TO GET +" + PASS_START_BONUS;
            case CAMP:
                return "CAMP  \u00B7  HEAL +" + CAMP_HEAL + " AND DEFENSE +" + CAMP_DEFENSE_BONUS + " NEXT SIEGE";
            case WATCHTOWER:
                return "WATCHTOWER  \u00B7  SCOUT ENEMY SECTORS (RENT AND ARMOR)";
            case WAR_JAIL:
                return "WAR JAIL  \u00B7  JUST VISITING UNLESS A CARD SENDS YOU HERE";
            case TRAINING_GROUND:
                return "TRAINING GROUND  \u00B7  NEXT ATTACK +" + TRAINING_ATTACK_BONUS;
            default:
                return "EVENT SECTOR  \u00B7  PRESS DRAW CARD";
        }
    }

    @Override
    public void onStep(Player player) {
        // ผลของช่องทำที่ GameController.resolveLanding()
    }
}
