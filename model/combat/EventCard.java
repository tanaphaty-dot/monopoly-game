package model.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.board.Board;
import model.board.EventTile;
import model.entity.Card;
import model.entity.Player;

/**
 * สำรับการ์ดเหตุการณ์ 8 ใบ และผลของการ์ดแต่ละใบ
 *
 * คลาสนี้ทำแค่ "เปลี่ยนตัวผู้เล่น" (เงิน ตำแหน่ง ธงโบนัส)
 * ถ้าการ์ดทำให้เดินต่อ (เดินฟรี) GameController จะไปตรวจช่องใหม่เอง
 * จะได้ไม่เรียกวนกันสองทาง
 *
 * @author เก๋า (feature/combat-system)
 */
public class EventCard {
    private static final Random RNG = new Random();

    public static final String TREASURE = "TREASURE";
    public static final String SUPPLY_DROP = "SUPPLY_DROP";
    public static final String TAX_PENALTY = "TAX_PENALTY";
    public static final String JAIL = "JAIL";
    public static final String BUFF_DEFENSE = "BUFF_DEFENSE";
    public static final String FREE_MOVE = "FREE_MOVE";
    public static final String ATTACK_DISCOUNT = "ATTACK_DISCOUNT";
    public static final String COMBAT_DRILL = "COMBAT_DRILL";

    public static final int TREASURE_AMOUNT = 200;
    public static final int SUPPLY_AMOUNT = 100;
    public static final int TAX_AMOUNT = 100;
    public static final int FREE_MOVE_STEPS = 3;
    public static final int ATTACK_DISCOUNT_AMOUNT = 50;
    public static final int JAIL_TILE = 14;
    public static final int DECK_SIZE = 8;

    /** สำรับใบจริง ใช้ทั้งตอนสุ่มและตอนโหลดเซฟ */
    public static List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        deck.add(new Card("C1", "Treasure", "Bonus +" + TREASURE_AMOUNT, TREASURE));
        deck.add(new Card("C2", "Supply Drop", "Supply +" + SUPPLY_AMOUNT, SUPPLY_DROP));
        deck.add(new Card("C3", "Tax", "Pay " + TAX_AMOUNT, TAX_PENALTY));
        deck.add(new Card("C4", "Jail Trap", "Sent to jail tile " + JAIL_TILE, JAIL));
        deck.add(new Card("C5", "Armor",
            "Next siege against you +" + EventTile.CAMP_DEFENSE_BONUS, BUFF_DEFENSE));
        deck.add(new Card("C6", "Free Move", "Move +" + FREE_MOVE_STEPS + " tiles", FREE_MOVE));
        deck.add(new Card("C7", "Siege Discount",
            "Next siege -" + ATTACK_DISCOUNT_AMOUNT, ATTACK_DISCOUNT));
        deck.add(new Card("C8", "Combat Drill",
            "Next siege -" + EventTile.TRAINING_ATTACK_BONUS, COMBAT_DRILL));
        return deck;
    }

    public static Card getRandomCard() {
        return getRandomCard(RNG);
    }

    public static Card getRandomCard(Random rand) {
        List<Card> deck = createDeck();
        return deck.get(rand.nextInt(deck.size()));
    }

    /** หาการ์ดจากชนิด ใช้ตอนโหลดเซฟและตอนทดสอบ */
    public static Card cardOfType(String type) {
        if (type == null) return null;
        for (Card card : createDeck()) {
            if (type.equals(card.getType())) return card;
        }
        return null;
    }

    public static void applyEffect(Card card, Player player, Board board) {
        if (card == null || player == null) return;

        String type = card.getType() != null ? card.getType().toUpperCase() : "";

        if (TREASURE.equals(type) || type.contains("TREASURE")) {
            player.addMoney(TREASURE_AMOUNT);
        } else if (SUPPLY_DROP.equals(type)) {
            player.addMoney(SUPPLY_AMOUNT);
        } else if (JAIL.equals(type) || type.contains("JAIL") || type.contains("TRAP")) {
            player.moveTo(JAIL_TILE);
            player.setInJail(true);
        } else if (TAX_PENALTY.equals(type) || type.contains("TAX")) {
            // จ่ายให้ธนาคาร (เจ้าหนี้เป็น null) เงินไม่พอ = ล้มละลาย และเสียเมืองทั้งหมด
            if (!player.payTo(null, TAX_AMOUNT) && board != null) {
                board.releaseCitiesOf(player);
            }
        } else if (BUFF_DEFENSE.equals(type)) {
            player.setCampDefense(true);
        } else if (FREE_MOVE.equals(type)) {
            // แค่ขยับช่อง เงินผ่านจุดเริ่มและการตกช่องใหม่ทำที่ GameController
            player.moveTo(player.getCurrentTileId() + FREE_MOVE_STEPS);
        } else if (ATTACK_DISCOUNT.equals(type)) {
            player.setSiegeDiscount(true);
        } else if (COMBAT_DRILL.equals(type)) {
            player.setTrainingBoost(true);
        }
    }
}
