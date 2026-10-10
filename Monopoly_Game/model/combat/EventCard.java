package model.combat;

import java.util.Random;
import model.board.Board;
import model.entity.Card;
import model.entity.Player;

/**
 * ระบบสุ่มและการ์ดเหตุการณ์
 * @author เก๋า (feature/combat-system)
 */
public class EventCard {

    public static Card getRandomCard() {
        Random rand = new Random();
        int roll = rand.nextInt(3);

        if (roll == 0) {
            return new Card("C1", "ขุดพบสมบัติ", "ได้รับเงินโบนัส 200 บาท", "TREASURE");
        } else if (roll == 1) {
            return new Card("C2", "ติดกับดักคุก", "ถูกส่งเข้าคุกช่อง 14 ทันที", "JAIL");
        } else {
            return new Card("C3", "จ่ายภาษีสังคม", "เสียเงินค่าปรับ 100 บาท", "TAX_PENALTY");
        }
    }

    public static void applyEffect(Card card, Player player, Board board) {
        if (card == null || player == null) return;

        String type = card.getType() != null ? card.getType().toUpperCase() : "";

        if (type.contains("TREASURE") || type.contains("BUFF")) {
            player.addMoney(200);
        } else if (type.contains("JAIL") || type.contains("TRAP")) {
            player.moveTo(14);
            player.setInJail(true);
        } else if (type.contains("TAX")) {
            player.deductMoney(100);
        }
    }
}