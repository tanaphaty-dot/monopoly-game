package model.combat;

import model.board.Board;
import model.board.CityTile;
import model.entity.Player;

/**
 * ระบบคำนวณสงครามยึดเมือง พลังป้องกันจาก BFS และผลกระทบทางการเงิน
 * @author เก๋า combat-system
 */
public class SiegeSystem {

    /**
     * คำนวณผลการตีเมืองระหว่างผู้บุกรุก (Attacker) และเจ้าของเมืองเดิม (Defender)
     * 
     * NOTICE (ถึง พีท): พีทเรียกใช้เมธอดนี้ใน GameController ตอนที่ผู้เล่นเลือกคำสั่ง ประกาศสงคราม
     */
    public static BattleResult resolveSiege(Player attacker, Player defender, CityTile city, Board board) {
        if (city == null || defender == null || city.getOwner() != defender) {
            return new BattleResult(false, 0, 0, "ไม่สามารถตีเมืองได้: เมืองไม่มีเจ้าของหรือข้อมูลไม่ถูกต้อง");
        }

        // คำนวณพลังป้องกันเมือง (Defense Power)
        // ดึง BFS ฟิล์มมาหาจำนวนเมืองติดกัน เพิ่มเกราะป้องกัน
        int connectedTerritories = board.calculateConnectedTerritory(city.getId(), defender);
        int baseDefense = city.getBaseRent();
        int territoryBonus = connectedTerritories * 50; // โบนัสเกราะเมืองละ +50
        int totalDefensePower = baseDefense + territoryBonus;

        // คำนวณค่าใช้จ่ายการบุก (Siege Cost)
        int siegeCost = totalDefensePower;

        // ตรวจสอบว่าฝ่ายบุกมีเงินพอตีเมือง
        if (attacker.getMoney() < siegeCost) {
            // หากเงินไม่พอตีเมือง ฝ่ายบุกจะแพ้ทันทีและโดนค่าปรับ
            int penalty = city.getBaseRent();
            attacker.deductMoney(penalty);
            defender.addMoney(penalty);
            return new BattleResult(false, 0, penalty, 
                attacker.getName() + " มีเงินไม่พอตีเมือง! จ่ายค่าผ่านทางปรับ " + penalty + " ให้ " + defender.getName());
        }

        // ฝ่ายบุกมีเงินพอ -> ชนะสงครามยึดเมือง
        attacker.deductMoney(siegeCost);
        defender.addMoney(siegeCost / 2); // เจ้าของเดิมได้เงินชดเชยครึ่งหนึ่ง
        city.setOwner(attacker); // เปลี่ยนเจ้าของเมืองเป็นฝ่ายบุก สลับโรล

        return new BattleResult(true, siegeCost, 0, 
            attacker.getName() + " บุกยึดเมือง " + city.getName() + " สำเร็จ! (เกราะเมืองเดิม: " + totalDefensePower + ")");
    }
}