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

    public boolean deductMoney(int amount) {
        if (this.money >= amount) {
            this.money -= amount;
            return true; // จ่ายเงินสำเร็จ
        }
        return false; // เงินไม่พอ (ผู้เล่นล้มละลายหรือต้องหาเงินเพิ่ม)
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
     * จ่ายเงินให้ผู้เล่นอีกคน หรือจ่ายธนาคารถ้าระบุ creditor เป็น null
     * เงินพอ: หักเต็มจำนวน คืน true
     * เงินไม่พอ: เหลือ 0 แล้วล้มละลาย คืน false
     */
    public boolean payTo(Player creditor, int amount) {
        if (amount <= 0) return true;
        if (this.money >= amount) {
            this.money -= amount;
            if (creditor != null) creditor.addMoney(amount);
            return true;
        }
        if (creditor != null && this.money > 0) creditor.addMoney(this.money);
        this.money = 0;
        return false;
    }

    public void addCard(Card card) {
        if (card != null) this.cards.add(card);
    }

    public void removeCard(Card card) {
        this.cards.remove(card);
    }

    // NOTICE (ถึง พีท & เก๋า): เมธอดเช็กว่าผู้เล่นยังมีชีวิตอยู่ในเกมมั้ย
    public boolean isBankrupt() {
        return this.money <= 0;
    }
}