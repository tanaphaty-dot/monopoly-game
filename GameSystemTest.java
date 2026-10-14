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
        testDebtBankruptcyAndFinishedSave(); // หนี้ ล้มละลาย และเซฟเกมที่จบแล้ว

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

    // ----------------------------------------------------------------
    // 7. หนี้ที่จ่ายไม่ครบต้องล้มละลายจริง และเกมที่จบแล้วต้องมีผู้ชนะ
    // ----------------------------------------------------------------
    private static void testDebtBankruptcyAndFinishedSave() {
        System.out.println("[TEST GROUP 7] Debt bankruptcy and finished saves");

        Player payer = new Player(1, "Payer", 40);
        Player creditor = new Player(2, "Creditor", 100);
        assertTrue(payer.payTo(creditor, 40) && payer.getMoney() == 0 && !payer.isBankrupt(),
            "Paying a debt in full down to 0 is not bankruptcy");
        assertTrue(creditor.getMoney() == 140, "Creditor receives the full debt");

        Player broke = new Player(3, "Broke", 15);
        Player rich = new Player(4, "Rich", 500);
        assertTrue(!broke.payTo(rich, 40) && broke.getMoney() == 0 && broke.isBankrupt(),
            "An unpaid debt zeroes the payer and marks bankruptcy");
        assertTrue(rich.getMoney() == 515, "Creditor receives only what the payer had");

        Board board = new Board();
        Player attacker = new Player(5, "SiegeAttacker", 10);
        Player defender = new Player(6, "SiegeDefender", 500);
        CityTile target = (CityTile) board.getTile(1);
        CityTile owned = (CityTile) board.getTile(4);
        target.setOwner(defender);
        owned.setOwner(attacker);
        BattleResult failed = SiegeSystem.resolveSiege(attacker, defender, target, board);
        assertTrue(!failed.isAttackerWon() && failed.getPenaltyFee() == 10
            && attacker.isBankrupt() && attacker.getMoney() == 0,
            "Failed siege with too little money bankrupts the attacker");
        assertTrue(defender.getMoney() == 510 && owned.getOwner() == null && target.getOwner() == defender,
            "Failed siege pays only the attacker's cash and releases their cities");

        Player exact = new Player(7, "ExactSiege", 70);
        Player holder = new Player(8, "Holder", 1000);
        CityTile forest = (CityTile) board.getTile(2);
        forest.setOwner(holder);
        BattleResult taken = SiegeSystem.resolveSiege(exact, holder, forest, board);
        assertTrue(taken.isAttackerWon() && forest.getOwner() == exact
            && exact.getMoney() == 0 && !exact.isBankrupt(),
            "Paying a siege cost down to 0 still keeps the captured city");

        GameSetting settings = new GameSetting();
        settings.setPlayerCount(2);
        settings.setInitialMoney(1500);
        GameController bailGame = new GameController(settings);
        Player bailPlayer = bailGame.getCurrentPlayer();
        bailPlayer.setMoney(GameController.JAIL_BAIL);
        bailPlayer.setInJail(true);
        bailGame.payBail();
        assertTrue(bailPlayer.getMoney() == 0 && !bailPlayer.isBankrupt()
            && !bailPlayer.isInJail() && !bailGame.isGameOver(),
            "Bail that spends the last coin does not bankrupt the player");

        FileManager.saveGame(saveText(false, "BUY_CITY",
            "Player:0,Player 1,100,1,false,0,false,false,false,false",
            "Player:1,Player 2,500,0,false,0,false,false,false,false",
            "1:-1"));
        GameController buyGame = GameController.loadGame();
        buyGame.processPendingAction(true);
        Player buyer = buyGame.getPlayers().get(0);
        CityTile bought = (CityTile) buyGame.getBoard().getTile(1);
        assertTrue(buyer.getMoney() == 0 && !buyer.isBankrupt()
            && bought.getOwner() == buyer && !buyGame.isGameOver(),
            "Buying a city with exact money does not bankrupt or release it");

        FileManager.saveGame(saveText(false, "SIEGE_OR_PAY",
            "Player:0,Player 1,15,1,false,0,false,false,false,false",
            "Player:1,Player 2,500,0,false,0,false,false,false,false",
            "1:1", "2:0"));
        GameController rentGame = GameController.loadGame();
        rentGame.processPendingAction(false);
        Player renter = rentGame.getPlayers().get(0);
        Player landlord = rentGame.getPlayers().get(1);
        CityTile keptByLandlord = (CityTile) rentGame.getBoard().getTile(1);
        CityTile released = (CityTile) rentGame.getBoard().getTile(2);
        assertTrue(renter.isBankrupt() && renter.getMoney() == 0 && released.getOwner() == null,
            "Unpaid rent bankrupts the visitor and releases their cities");
        assertTrue(landlord.getMoney() == 515 && keptByLandlord.getOwner() == landlord,
            "Unpaid rent transfers only the visitor's cash");
        assertTrue(rentGame.isGameOver() && rentGame.getWinner() == landlord
            && rentGame.getLastGameLog().contains("ล้มละลาย"),
            "The last solvent player wins after a rent bankruptcy");
        String savedAfterOver = FileManager.loadGame();
        rentGame.saveGame();
        assertTrue(savedAfterOver.equals(FileManager.loadGame())
            && rentGame.getLastGameLog().contains("บันทึกไม่ได้"),
            "A finished game refuses to overwrite the save");

        FileManager.saveGame(saveText(false, "SIEGE_OR_PAY",
            "Player:0,Player 1,20,1,false,0,false,false,false,false",
            "Player:1,Player 2,500,0,false,0,false,false,false,false",
            "1:1", "4:0"));
        GameController exactRent = GameController.loadGame();
        exactRent.processPendingAction(false);
        Player exactRenter = exactRent.getPlayers().get(0);
        CityTile stillOwned = (CityTile) exactRent.getBoard().getTile(4);
        assertTrue(exactRenter.getMoney() == 0 && !exactRenter.isBankrupt()
            && stillOwned.getOwner() == exactRenter && !exactRent.isGameOver(),
            "Rent paid in full down to 0 keeps the payer and their cities");
        assertTrue(exactRent.getPlayers().get(1).getMoney() == 520, "Landlord receives the full rent");

        FileManager.saveGame(saveText(false, "SIEGE_OR_PAY",
            "Player:0,Player 1,10,1,false,0,false,false,false,false",
            "Player:1,Player 2,500,0,false,0,false,false,false,false",
            "1:1", "4:0"));
        GameController siegeGame = GameController.loadGame();
        siegeGame.processPendingAction(true);
        assertTrue(siegeGame.getPlayers().get(0).isBankrupt()
            && ((CityTile) siegeGame.getBoard().getTile(4)).getOwner() == null,
            "Choosing to siege without enough money releases the attacker's cities");
        assertTrue(siegeGame.getPlayers().get(1).getMoney() == 510
            && ((CityTile) siegeGame.getBoard().getTile(1)).getOwner() == siegeGame.getPlayers().get(1),
            "A failed siege from the controller does not mint penalty money");

        FileManager.saveGame(saveText(true, "NONE",
            "Player:0,Player 1,100,0,false,0,false,false,false,false",
            "Player:1,Player 2,450,0,false,0,false,false,false,false",
            "18:0"));
        GameController finished = GameController.loadGame();
        assertTrue(finished.isGameOver() && finished.getWinner() == finished.getPlayers().get(0),
            "Loading a finished game restores the richer winner");
        finished.saveGame();
        assertTrue(finished.getLastGameLog().contains("บันทึกไม่ได้"),
            "Save stays closed after loading a finished game");

        FileManager.saveGame(saveText(false, "PAY_RENT",
            "Player:0,Player 1,1500,1,false,0,false,false,false,false",
            "Player:1,Player 2,1500,0,false",
            "1:1"));
        GameController legacy = GameController.loadGame();
        assertTrue(legacy.getPendingAction() == PendingAction.SIEGE_OR_PAY,
            "An old PAY_RENT save loads as SIEGE_OR_PAY");

        FileManager.saveGame(saveText(false, "IN_JAIL",
            "Player:0,Player 1,1500,14,true,1,false,false,false,false",
            "Player:1,Player 2,1500,0,false",
            ""));
        GameController oldJail = GameController.loadGame();
        assertTrue(oldJail.getPendingAction() == PendingAction.NONE && oldJail.getCurrentPlayer().isInJail(),
            "An old IN_JAIL save keeps the jail flag and clears the dead pending state");

        GameController aliveBroke = new GameController(settings);
        aliveBroke.getCurrentPlayer().setMoney(0);
        ((CityTile) aliveBroke.getBoard().getTile(4)).setOwner(aliveBroke.getCurrentPlayer());
        aliveBroke.saveGame();
        GameController reloaded = GameController.loadGame();
        assertTrue(reloaded.getPlayers().get(0).getMoney() == 0 && !reloaded.getPlayers().get(0).isBankrupt()
            && ((CityTile) reloaded.getBoard().getTile(4)).getOwner() == reloaded.getPlayers().get(0),
            "A solvent player with 0 money stays solvent and keeps their city after save and load");

        FileManager.saveGame(saveText(false, "NONE",
            "Player:0,Player 1,0,0,false",
            "Player:1,Player 2,800,0,false",
            "2:0"));
        GameController oldZero = GameController.loadGame();
        assertTrue(oldZero.getPlayers().get(0).isBankrupt() && !oldZero.getPlayers().get(1).isBankrupt()
            && ((CityTile) oldZero.getBoard().getTile(2)).getOwner() == null,
            "An old save with 0 money and no flag still loads as bankrupt and releases cities");
        System.out.println();
    }

    /** เขียนเซฟสั้นๆ สำหรับเทส เมืองที่ไม่ได้ระบุถือว่าว่าง */
    private static String saveText(boolean gameOver, String pending, String player0, String player1, String... owned) {
        java.util.Map<Integer, String> owners = new java.util.HashMap<Integer, String>();
        for (String item : owned) {
            if (item == null || item.isEmpty()) continue;
            String[] pair = item.split(":");
            owners.put(Integer.parseInt(pair[0]), pair[1]);
        }
        int[] cities = {1, 2, 4, 6, 8, 9, 12, 13, 15, 18, 19, 21};
        StringBuilder sb = new StringBuilder();
        sb.append("PlayerCount:2\n");
        sb.append("InitialMoney:1500\n");
        sb.append("MaxTurns:20\n");
        sb.append("CurrentTurn:1\n");
        sb.append("CurrentPlayer:0\n");
        sb.append("HasRolled:true\n");
        sb.append("PendingAction:").append(pending).append("\n");
        sb.append("GameOver:").append(gameOver).append("\n");
        sb.append("LastRoll:1\n");
        sb.append(player0).append("\n");
        sb.append(player1).append("\n");
        for (int id : cities) {
            String owner = "-1";
            if (owners.containsKey(id)) owner = owners.get(id);
            sb.append("City:").append(id).append(",").append(owner).append("\n");
        }
        return sb.toString();
    }
}