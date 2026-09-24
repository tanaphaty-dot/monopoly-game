package model.combat;

import model.board.Board;
import model.entity.Card;
import model.entity.Player;

/**
 * คำนวณเอฟเฟกต์ของการ์ดเมื่อคนตกช่อง EventTile หรือใช้การ์ด
 * @author เก๋า combat-system
 */
public class EventCard {

    /**
     * NOTICE (ถึง พีท & เดียร์): พีทเรียกใช้เมธอดถ้าคนตกช่อง EventTile 
     * โค้ดจะอ่านประเภทการ์ด (Card.getType()) ของเดียร์ แล้วส่งผลต่อตัวเกม
     */
    public static String applyEffect(Card card, Player player, Board board) {
        if (card == null || player == null) return "ไม่มีผลเกิดขึ้น";

        String type = card.getType();
        String resultText = "";

        switch (type) {
            case "TREASURE":
                player.addMoney(200);
                resultText = player.getName() + "ได้รับสมบัติคลังหลวง! ได้รับเงิน +200";
                break;

            case "TAX_PENALTY":
                player.deductMoney(100);
                resultText = player.getName() + " โดนเรียกเก็บภาษีสงคราม! เสียเงิน -100";
                break;

            case "TELEPORT_START":
                player.moveTo(0); // ย้ายไปช่องจุดเริ่มต้น (0)
                player.addMoney(100); // ได้เงินผ่านจุดเริ่มต้น
                resultText = player.getName() + " วาร์ปกลับจุดเริ่มต้น! ได้รับเงิน +100";
                break;

            case "JAIL_TRAP":
                player.moveTo(14); // ย้ายไปช่องคุก (14)
                player.setInJail(true);
                resultText = player.getName() + " ติดกับดัก! ถูกส่งเข้าคุกสงคราม";
                break;

            default:
                resultText = player.getName() + " จั่วได้การ์ด: " + card.getTitle();
                break;
        }

        return resultText;
    }
}