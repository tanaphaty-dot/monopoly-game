import model.board.*;
import model.entity.*;
import model.combat.*;
import controller.*;

/**
 * สคริปต์ทดสอบระบบรวม (Integration Test Suite)
 * ครอบคลุมการทำงานของ Board (BFS), Player/Dice, SiegeSystem, EventCard, GameController และ FileManager
 * @author ทีม Monopoly & Zone Rush
 */
public class GameSystemTest {

    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   MONOPOLY GAME - SYSTEM INTEGRATION TEST RUNNER");
        System.out.println("==================================================\n");

        // รันเคสทดสอบตามลำดับโมดูลของทุกคนในทีม
        testBoardInitializationAndBFS();   // ฟิล์ม (Board & BFS)
        testPlayerAndDiceMechanism();      // เดียร์ (Player & Dice)
        testSiegeSystemCombat();           // เก๋า (SiegeSystem)
        testEventCardEffects();            // เก๋า (EventCard)
        testGameControllerAndFileFlow();   // พีท (GameController & Save/Load)
        testEventSystem();                 // ระบบ Event ที่เพิ่มเข้ามา

        // สรุปผลการทดสอบ
        System.out.println("\n==================================================");
        System.out.println("TEST SUMMARY:");
        System.out.println("  TOTAL PASSED : " + passCount);
        System.out.println("  TOTAL FAILED : " + failCount);
        System.out.println("  VERDICT      : " + (failCount == 0 ? "ALL SYSTEM PASSED :D" : "SOME TESTS FAILED!"));
        System.out.println("==================================================");
    }

    /**
     * ฟังก์ชันช่วยตรวจเช็กเงื่อนไขและพิมพ์ [PASS] / [FAIL]
     */
    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passCount++;
        } else {
            System.err.println("  [FAIL] " + testName);
            failCount++;
        }
    }

    // ----------------------------------------------------------------
    // 1. TEST MODULE: ฟิล์ม (model.board & BFS)
    // ----------------------------------------------------------------
    private static void testBoardInitializationAndBFS() {
        System.out.println("[TEST GROUP 1] Board & BFS Algorithm (Film)");
        Board board = new Board();

        // Check 1.1: สร้างกระดานครบ 24 ช่องหรือไม่
        assertTrue(board.getAllTiles().size() == 24, "Board should have 24 tiles");

        // Check 1.2: ตรวจสอบประเภทช่อง
        assertTrue(board.getTile(1) instanceof CityTile, "Tile ID 1 should be a CityTile");
        assertTrue(board.getTile(0) instanceof EventTile, "Tile ID 0 should be an EventTile");

        // Check 1.3: อัลกอริทึม BFS คำนวณอาณาเขตเมืองติดกัน
        Player player1 = new Player(1, "Tester", 1000);
        CityTile city1 = (CityTile) board.getTile(1);
        CityTile city2 = (CityTile) board.getTile(2);
        
        city1.setOwner(player1);
        city2.setOwner(player1);

        int connectedCount = board.calculateConnectedTerritory(1, player1);
        assertTrue(connectedCount == 2, "BFS should find 2 connected tiles for Tile 1 & 2");
        System.out.println();
    }

    // ----------------------------------------------------------------
    // 2. TEST MODULE: เดียร์ (model.entity)
    // ----------------------------------------------------------------
    private static void testPlayerAndDiceMechanism() {
        System.out.println("[TEST GROUP 2] Player State & Dice Rolling (Dear)");
        Player player = new Player(1, "DearTester", 1000);
        Dice dice = new Dice();

        // Check 2.1: การหักและเพิ่มเงิน
        player.addMoney(500);
        assertTrue(player.getMoney() == 1500, "Player money should be 1500 after adding 500");

        boolean deductSuccess = player.deductMoney(1000);
        assertTrue(deductSuccess && player.getMoney() == 500, "Deduct money should succeed and have 500 left");

        boolean deductFail = player.deductMoney(2000); // เงินไม่พอ
        assertTrue(!deductFail && player.getMoney() == 500, "Deduct 2000 should fail and money remains 500");

        // Check 2.2: การเคลื่อนที่และการเปลี่ยนตำแหน่ง
        player.moveTo(5);
        assertTrue(player.getCurrentTileId() == 5, "Player position should move to Tile 5");

        // Check 2.3: การทอยเต๋าอยู่ในช่วง 1 - 6
        boolean validDice = true;
        for (int i = 0; i < 50; i++) {
            int roll = dice.roll();
            if (roll < 1 || roll > 6) {
                validDice = false;
                break;
            }
        }
        assertTrue(validDice, "Dice rolls 50 times should be between 1 and 6");
        System.out.println();
    }

    // ----------------------------------------------------------------
    // 3. TEST MODULE: เก๋า (model.combat - SiegeSystem)
    // ----------------------------------------------------------------
    private static void testSiegeSystemCombat() {
        System.out.println("[TEST GROUP 3] Siege System & Territory Defense (Kao)");
        Board board = new Board();
        Player attacker = new Player(1, "Attacker", 2000);
        Player defender = new Player(2, "Defender", 1000);

        CityTile city = (CityTile) board.getTile(1);
        city.setOwner(defender);

        // Scenario 3.1: ตีเมืองสำเร็จเมื่อเงินพอ
        BattleResult result = SiegeSystem.resolveSiege(attacker, defender, city, board);
        assertTrue(result.isAttackerWon(), "Attacker should win the siege with sufficient money");
        assertTrue(city.getOwner() == attacker, "City owner should change to Attacker after defeat");

        // Scenario 3.2: ตีเมืองแพ้เพราะเงินไม่พอ
        Player poorAttacker = new Player(3, "PoorAttacker", 10);
        CityTile city2 = (CityTile) board.getTile(2);
        city2.setOwner(defender);

        BattleResult resultFail = SiegeSystem.resolveSiege(poorAttacker, defender, city2, board);
        assertTrue(!resultFail.isAttackerWon(), "Siege should fail when attacker has insufficient funds");
        System.out.println();
    }

    // ----------------------------------------------------------------
    // 4. TEST MODULE: เก๋า (model.combat - EventCard)
    // ----------------------------------------------------------------
    private static void testEventCardEffects() {
        System.out.println("[TEST GROUP 4] Event Card Effects (Kao)");
        Board board = new Board();
        Player player = new Player(1, "CardTester", 1000);

        // Check 4.1: การ์ดสมบัติ (+200)
        Card treasureCard = new Card("C1", "Treasure", "Got money", "TREASURE");
        EventCard.applyEffect(treasureCard, player, board);
        assertTrue(player.getMoney() == 1200, "Treasure card should add +200 money");

        // Check 4.2: การ์ดติดกับดักคุก
        Card jailCard = new Card("C2", "Jail Trap", "Go to jail", "JAIL_TRAP");
        EventCard.applyEffect(jailCard, player, board);
        assertTrue(player.isInJail() && player.getCurrentTileId() == 14, "Jail card should move player to Tile 14 and set inJail");
        System.out.println();
    }

    // ----------------------------------------------------------------
    // 5. TEST MODULE: พีท (controller - GameController & File System)
    // ----------------------------------------------------------------
    private static void testGameControllerAndFileFlow() {
        System.out.println("[TEST GROUP 5] Core Game Loop Controller & Save/Load System (Pete)");
        GameSetting settings = new GameSetting();
        settings.setPlayerCount(2);
        settings.setInitialMoney(1500);

        GameController controller = new GameController(settings);

        // Check 5.1: ตัวละครและกระดานสร้างถูกต้องตามคอนฟิก
        assertTrue(controller.getPlayers().size() == 2, "GameController must have 2 players");
        assertTrue(controller.getCurrentPlayer().getId() == 0, "Turn should start at Player 1 (Index 0)");

        // Check 5.2: การทอยเต๋าและย้ายตำแหน่ง
        controller.rollDice();
        int currentPos = controller.getCurrentPlayer().getCurrentTileId();
        assertTrue(currentPos > 0, "Player position should change after rollDice()");

        // แก้ไขจุดนี้: ตรวจเช็ก PendingAction ให้ยืดหยุ่นตามประเภทช่องที่เดินไปตกจริง
        Tile landedTile = controller.getBoard().getTile(currentPos);
        if (landedTile instanceof CityTile) {
            CityTile city = (CityTile) landedTile;
            if (city.getOwner() == null || city.getOwner() != controller.getCurrentPlayer()) {
                assertTrue(controller.getPendingAction() != PendingAction.NONE, "PendingAction should be set on an unowned or enemy city");
            } else {
                assertTrue(controller.getPendingAction() == PendingAction.NONE, "Own city should not wait for an action");
            }
        } else if (landedTile instanceof EventTile && ((EventTile) landedTile).isCardTile()) {
            assertTrue(controller.getPendingAction() == PendingAction.DRAW_CARD, "Card tile should wait for DRAW_CARD");
        } else {
            assertTrue(controller.getPendingAction() == PendingAction.NONE, "Special tile should resolve immediately");
        }

        // Check 5.3: เคลียร์ Action ค้างและเปลี่ยนเทิร์น
        // การ์ดเดินฟรีอาจไปตกช่องที่ต้องตอบอีกครั้ง จึงเคลียร์เป็นลูปสั้นๆ
        int guard = 0;
        while (controller.getPendingAction() != PendingAction.NONE && guard++ < 8) {
            controller.processPendingAction(false);
        }
        assertTrue(controller.getPendingAction() == PendingAction.NONE, "PendingAction should reset to NONE after processPendingAction()");

        controller.endTurn();
        assertTrue(controller.getCurrentPlayer().getId() == 1, "Turn should switch to Player 2 (Index 1) after endTurn()");

        // Check 5.4: ทดสอบระบบเซฟและอ่านไฟล์ savegame.txt
        controller.saveGame();
        String savedData = FileManager.loadGame();
        assertTrue(savedData != null && savedData.contains("CurrentTurn:"), "FileManager should save and load game state correctly");
        System.out.println();
    }

    // ----------------------------------------------------------------
    // 6. TEST MODULE: ระบบ Event ที่เพิ่ม (การ์ด ช่องพิเศษ โบนัสบุก เซฟ)
    // ----------------------------------------------------------------
    private static void testEventSystem() {
        System.out.println("[TEST GROUP 6] Full Event System");
        Board board = new Board();

        EventTile start = (EventTile) board.getTile(0);
        EventTile camp = (EventTile) board.getTile(5);
        EventTile watch = (EventTile) board.getTile(11);
        EventTile jail = (EventTile) board.getTile(14);
        EventTile train = (EventTile) board.getTile(17);
        EventTile chance = (EventTile) board.getTile(3);
        assertTrue(EventTile.START.equals(start.getEventType()) && !start.isCardTile(), "Tile 0 is Start");
        assertTrue(EventTile.CAMP.equals(camp.getEventType()) && !camp.isCardTile(), "Tile 5 is Camp");
        assertTrue(EventTile.WATCHTOWER.equals(watch.getEventType()) && !watch.isCardTile(), "Tile 11 is Watchtower");
        assertTrue(EventTile.WAR_JAIL.equals(jail.getEventType()) && !jail.isCardTile(), "Tile 14 is War Jail");
        assertTrue(EventTile.TRAINING_GROUND.equals(train.getEventType()) && !train.isCardTile(), "Tile 17 is Training Ground");
        assertTrue(chance.isCardTile(), "Tile 3 is still a card tile");

        java.util.List<Card> deck = EventCard.createDeck();
        assertTrue(deck.size() == EventCard.DECK_SIZE, "Deck should have 8 cards");

        Player rich = new Player(1, "Rich", 500);
        EventCard.applyEffect(EventCard.cardOfType(EventCard.TREASURE), rich, board);
        assertTrue(rich.getMoney() == 700, "Treasure card adds 200");
        EventCard.applyEffect(EventCard.cardOfType(EventCard.SUPPLY_DROP), rich, board);
        assertTrue(rich.getMoney() == 800, "Supply card adds 100");
        EventCard.applyEffect(EventCard.cardOfType(EventCard.TAX_PENALTY), rich, board);
        assertTrue(rich.getMoney() == 700, "Tax card subtracts 100 when the player can pay");

        Player poor = new Player(2, "Poor", 50);
        CityTile owned = (CityTile) board.getTile(1);
        owned.setOwner(poor);
        EventCard.applyEffect(EventCard.cardOfType(EventCard.TAX_PENALTY), poor, board);
        assertTrue(poor.getMoney() == 0 && poor.isBankrupt() && owned.getOwner() == null,
            "Unpaid tax bankrupts the player and releases their cities");

        Player walker = new Player(3, "Walker", 1000);
        walker.moveTo(0);
        EventCard.applyEffect(EventCard.cardOfType(EventCard.FREE_MOVE), walker, board);
        assertTrue(walker.getCurrentTileId() == EventCard.FREE_MOVE_STEPS, "Free move walks 3 tiles");

        EventCard.applyEffect(EventCard.cardOfType(EventCard.BUFF_DEFENSE), walker, board);
        EventCard.applyEffect(EventCard.cardOfType(EventCard.ATTACK_DISCOUNT), walker, board);
        EventCard.applyEffect(EventCard.cardOfType(EventCard.COMBAT_DRILL), walker, board);
        assertTrue(walker.hasCampDefense() && walker.hasSiegeDiscount() && walker.hasTrainingBoost(),
            "Defense, discount, and drill cards set one-shot flags");

        // เกราะค่ายทำให้ค่าบุกแพงขึ้น เงินไม่พอแล้วโบนัสต้องยังอยู่
        Player attacker = new Player(4, "Attacker", 100);
        Player defender = new Player(5, "Defender", 500);
        CityTile city = (CityTile) board.getTile(6);
        city.setOwner(defender);
        defender.setCampDefense(true);
        BattleResult blocked = SiegeSystem.resolveSiege(attacker, defender, city, board);
        assertTrue(!blocked.isAttackerWon() && defender.hasCampDefense() && city.getOwner() == defender,
            "Camp defense raises the siege cost and is kept when the attacker cannot pay");

        Player richAttacker = new Player(6, "RichAttacker", 500);
        richAttacker.setTrainingBoost(true);
        richAttacker.setSiegeDiscount(true);
        BattleResult captured = SiegeSystem.resolveSiege(richAttacker, defender, city, board);
        assertTrue(captured.isAttackerWon() && city.getOwner() == richAttacker, "Discount and training can bring the cost down enough to capture");
        assertTrue(!richAttacker.hasTrainingBoost() && !richAttacker.hasSiegeDiscount() && !defender.hasCampDefense(),
            "Buffs are consumed only when the siege is actually paid");

        // เซฟต้องจำคุก โบนัส และการ์ดในมือ
        GameSetting settings = new GameSetting();
        settings.setPlayerCount(2);
        settings.setInitialMoney(1500);
        GameController gc = new GameController(settings);
        Player saver = gc.getCurrentPlayer();
        saver.setInJail(true);
        saver.setJailTurns(2);
        saver.setCampDefense(true);
        saver.addCard(EventCard.cardOfType(EventCard.TREASURE));
        gc.saveGame();
        GameController loaded = GameController.loadGame();
        Player loadedPlayer = loaded.getPlayers().get(0);
        assertTrue(loadedPlayer.isInJail() && loadedPlayer.getJailTurns() == 2 && loadedPlayer.hasCampDefense(),
            "Save/Load keeps jail turns and camp defense");
        assertTrue(loadedPlayer.getCards().size() == 1
            && EventCard.TREASURE.equals(loadedPlayer.getCards().get(0).getType()),
            "Save/Load keeps cards in hand");
        System.out.println();
    }
}