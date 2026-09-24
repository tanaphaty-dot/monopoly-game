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
    private final List<Card> cards;

    public Player(int id, String name, int initialMoney) {
        this.id = id;
        this.name = name;
        this.money = initialMoney;
        this.currentTileId = 0; // เริ่มต้นที่จุด START (ช่อง 0)
        this.inJail = false;
        this.cards = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getMoney() { return money; } //เชื่อมกับเก๋า จะดึง player.getMoney() ไปเช็กว่าผู้เล่นมีเงินพอจ่ายค่าปรับ/ค่าเช่ามั้ย
    public int getCurrentTileId() { return currentTileId; }
    public boolean isInJail() { return inJail; }
    public List<Card> getCards() { return cards; }

    // --- System Actions ---

    public void addMoney(int amount) {
        this.money += amount;
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
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public void removeCard(Card card) {
        this.cards.remove(card);
    }

    // NOTICE (ถึง พีท & เก๋า): เมธอดเช็กว่าผู้เล่นยังมีชีวิตอยู่ในเกมมั้ย
    public boolean isBankrupt() {
        return this.money <= 0;
    }
}