package model.combat;

/**
 * Data Transfer Object เก็บผลลัพธ์จากการตีเมือง
 * @author เก๋า combat-system
 */
public class BattleResult {
    private final boolean attackerWon;
    private final int damageDealt;
    private final int penaltyFee;
    private final String message;

    public BattleResult(boolean attackerWon, int damageDealt, int penaltyFee, String message) {
        this.attackerWon = attackerWon;
        this.damageDealt = damageDealt;
        this.penaltyFee = penaltyFee;
        this.message = message;
    }

    public boolean isAttackerWon() { return attackerWon; }
    public int getDamageDealt() { return damageDealt; }
    public int getPenaltyFee() { return penaltyFee; }
    public String getMessage() { return message; }

    // NOTICE (ถึง พีท): พีทเรียก getMessage() หรือดึงค่าผ่านทาง/ค่าปรับไปโชว์ใน Pop-up GUI ได้เลย
}