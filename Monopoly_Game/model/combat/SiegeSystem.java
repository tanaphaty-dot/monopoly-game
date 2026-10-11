package model.combat;

import model.board.Board;
import model.board.CityTile;
import model.board.EventTile;
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
        int armor = baseDefense + territoryBonus;

        // โบนัสจาก Event เปลี่ยนแค่ราคาค่าบุก
        // เกราะค่ายทำให้แพงขึ้น, ซ้อมรบและส่วนลดการ์ดทำให้ถูกลง
        // กติกาเดิมยังอยู่: เงินพอจ่ายค่าบุก = ยึดเมืองได้ ไม่ได้สุ่มเต๋าต่อสู้
        // เงินไม่พอ = ยังไม่ใช้โบนัส เก็บไว้ตาหน้า
        int campBonus = defender.hasCampDefense() ? EventTile.CAMP_DEFENSE_BONUS : 0;
        int trainBonus = attacker.hasTrainingBoost() ? EventTile.TRAINING_ATTACK_BONUS : 0;
        int discount = attacker.hasSiegeDiscount() ? EventCard.ATTACK_DISCOUNT_AMOUNT : 0;
        int siegeCost = armor + campBonus - trainBonus - discount;
        if (siegeCost < 0) siegeCost = 0;

        // ตรวจสอบว่าฝ่ายบุกมีเงินพอตีเมือง
        if (attacker.getMoney() < siegeCost) {
            // หากเงินไม่พอตีเมือง ฝ่ายบุกจะแพ้ทันทีและโดนค่าปรับ
            int penalty = city.getBaseRent();
            attacker.deductMoney(penalty);
            defender.addMoney(penalty);
            return new BattleResult(false, 0, penalty, 
                attacker.getName() + " มีเงินไม่พอตีเมือง! จ่ายค่าผ่านทางปรับ " + penalty + " ให้ " + defender.getName());
        }

        // เงินพอแล้วค่อยใช้โบนัส ทิ้งธงทันที เพราะใช้ได้ครั้งเดียว
        if (campBonus > 0) defender.consumeCampDefense();
        if (trainBonus > 0) attacker.consumeTrainingBoost();
        if (discount > 0) attacker.consumeSiegeDiscount();

        // ฝ่ายบุกมีเงินพอ -> ชนะสงครามยึดเมือง
        attacker.deductMoney(siegeCost);
        defender.addMoney(siegeCost / 2); // เจ้าของเดิมได้เงินชดเชยครึ่งหนึ่ง
        city.setOwner(attacker); // เปลี่ยนเจ้าของเมืองเป็นฝ่ายบุก สลับโรล

        String detail = "เกราะ " + armor;
        if (campBonus > 0) detail += " +เกราะค่าย " + campBonus;
        if (trainBonus > 0) detail += " -ซ้อม " + trainBonus;
        if (discount > 0) detail += " -ส่วนลด " + discount;
        detail += " จ่าย " + siegeCost;

        return new BattleResult(true, siegeCost, 0,
            attacker.getName() + " บุกยึดเมือง " + city.getName() + " สำเร็จ! (" + detail + ")");
    }
}