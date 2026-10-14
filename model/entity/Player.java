package model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * ตัวแปร Player ถูกนำไปตั้งค่าเป็น owner ใน CityTile และเป็นเงื่อนไขเช็กเจ้าของในอัลกอริทึม BFS ของ Board
 * 
 * จัดการข้อมูลสถานะผู้เล่น เงิน ตำแหน่ง และการ์ดในครอบครอง
 * @author เดียร์ entity-player
 */
public class Player {
    private final int id;
    private final String name;
    private int money;
    private int currentTileId;
    private boolean inJail;
    private int jailTurns;          // ทอยหนีคุกไปแล้วกี่ครั้ง
    private boolean campDefense;    // เกราะค่าย ใช้ครั้งเดียวตอนถูกบุก
    private boolean trainingBoost;  // โบนัสซ้อม ใช้ครั้งเดียวตอนไปบุก
    private boolean siegeDiscount;  // ส่วนลดการ์ด ใช้ครั้งเดียวตอนไปบุก
    private boolean bankrupt;       // จ่ายหนี้ไม่ครบเท่านั้น เงินเหลือ 0 หลังจ่ายสำเร็จไม่นับ
    private final List<Card> cards;

    public Player(int id, String name, int initialMoney) {
        this.id = id;
        this.name = name;
        this.money = initialMoney;
        this.currentTileId = 0; // เริ่มต้นที่จุด START (ช่อง 0)
        this.inJail = false;
        this.jailTurns = 0;
        this.campDefense = false;
        this.trainingBoost = false;
        this.siegeDiscount = false;
        this.bankrupt = false;
        this.cards = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getMoney() { return money; } //เชื่อมกับเก๋า จะดึง player.getMoney() ไปเช็กว่าผู้เล่นมีเงินพอจ่ายค่าปรับ/ค่าเช่ามั้ย
    public int getCurrentTileId() { return currentTileId; }
    public boolean isInJail() { return inJail; }
    public int getJailTurns() { return jailTurns; }
    public boolean hasCampDefense() { return campDefense; }
    public boolean hasTrainingBoost() { return trainingBoost; }
    public boolean hasSiegeDiscount() { return siegeDiscount; }
    public List<Card> getCards() { return cards; }

    // --- System Actions ---

    public void addMoney(int amount) {
        this.money += amount;
    }

    // เพิ่มใหม่: ใช้ตอนโหลดเซฟกลับมา (FileManager/GameController) เพื่อคืนค่าเงินตรงๆ
    public void setMoney(int money) {
        this.money = money;
    }

    /**
     * หักเงินเมื่อมีพอเท่านั้น
     * ใช้กับของที่เลือกจ่ายเอง เช่น ซื้อเมือง ค่าบุกที่จ่ายไหว และประกันคุก
     * เงินไม่พอจะไม่หัก และไม่ล้มละลาย
     * หักจนเหลือ 0 พอดีก็ยังไม่ล้มละลาย
     */
    public boolean deductMoney(int amount) {
        if (amount < 0) return false;
        if (this.money >= amount) {
            this.money -= amount;
            return true;
        }
        return false;
    }

    public void moveTo(int tileId) {
        this.currentTileId = tileId % 24; // ป้องกัน Index เกินกระดาน 24 ช่อง
    }

    public void setInJail(boolean inJail) {
        this.inJail = inJail;
        this.jailTurns = 0;
    }

    public void setJailTurns(int jailTurns) {
        this.jailTurns = jailTurns;
    }

    public void setCampDefense(boolean campDefense) { this.campDefense = campDefense; }

    /** คืน true ครั้งเดียว แล้วล้างธงทิ้ง */
    public boolean consumeCampDefense() {
        if (!campDefense) return false;
        campDefense = false;
        return true;
    }

    public void setTrainingBoost(boolean trainingBoost) { this.trainingBoost = trainingBoost; }

    public boolean consumeTrainingBoost() {
        if (!trainingBoost) return false;
        trainingBoost = false;
        return true;
    }

    public void setSiegeDiscount(boolean siegeDiscount) { this.siegeDiscount = siegeDiscount; }

    public boolean consumeSiegeDiscount() {
        if (!siegeDiscount) return false;
        siegeDiscount = false;
        return true;
    }

    /**
     * จ่ายหนี้ให้ผู้เล่นอีกคน หรือจ่ายธนาคารถ้า creditor เป็น null
     * จ่ายครบ: หักเต็มจำนวน คืน true แม้เงินจะเหลือ 0 ก็ยังไม่ล้มละลาย
     * จ่ายไม่ครบ: โอนเท่าที่มีให้เจ้าหนี้ เงินตัวเองเหลือ 0 แล้วล้มละลาย คืน false
     * ผู้เรียกต้องคืนเมืองเองผ่าน Board.settleDebt() หรือ releaseCitiesOf()
     */
    
    public boolean payTo(Player creditor, int amount) {
        if (amount <= 0) return true; //ไม่มียอดต้องจ่าย ก็จบทันที

        if (this.money >= amount) { // ตรวจสอบเงื่อนไข เงินพอ หักเงินตัวเองตามยอด
            this.money -= amount; 
            if (creditor != null) creditor.addMoney(amount); //ถ้ามีเจ้าหนี้ ให้โอนเงินจำนวนเดียวกันเข้าไป
            return true;
        }

        if (creditor != null && this.money > 0) creditor.addMoney(this.money); // เงินไม่พอ เข้าเงื่อนไขนี้
        this.money = 0;
        this.bankrupt = true; //ตั้งธงล้มละลาย
        return false; //คืน false ให้ผู้เรียกรู้ว่าจ่ายไม่ครบ
    }

    public void addCard(Card card) {
        if (card != null) this.cards.add(card);
    }

    public void removeCard(Card card) {
        this.cards.remove(card);
    }

    /**
     * ล้มละลายเฉพาะตอนจ่ายหนี้ไม่ครบ (payTo คืน false)
     * ซื้อเมือง จ่ายค่าเช่าครบ จ่ายค่าบุกครบ หรือจ่ายประกันจนเหลือ 0 ยังเล่นต่อได้
     */
    public boolean isBankrupt() {
        return bankrupt;
    }

    /** ใช้ตอนโหลดเซฟ ค่านี้ไม่ตามเงินคงเหลือ */
    public void setBankrupt(boolean bankrupt) {
        this.bankrupt = bankrupt;
    }
}