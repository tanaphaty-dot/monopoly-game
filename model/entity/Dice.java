package model.entity;

import java.util.Random;

/**
 * คลาสระบบสุ่มลูกเต๋า แยกเป็นคลาสเดี่ยว เพราะถ้าอยากเปลี่ยนกฎทอยลูกจะง่ายหน่อย (เช่น เพิ่มลูกเต๋าเป็น 2 ลูก)
 * @author เดียร์ entity-player
 */
public class Dice {
    private final Random random;
    private int lastRollResult;

    public Dice() {
        this.random = new Random();
        this.lastRollResult = 1;
    }

    /**
     * สุ่มแต้มลูกเต๋า 1 ถึง 6
     * NOTICE (ถึง พีท): เรียกใช้ใน GameController ช่วงแบบถึงตาแล้วทอยเต๋า
     */
    public int roll() {
        this.lastRollResult = random.nextInt(6) + 1;
        return lastRollResult;
    }

    public int getLastRollResult() {
        return lastRollResult;
    }
}