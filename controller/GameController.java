package controller;

import java.util.*;
import model.board.*;
import model.combat.*;
import model.entity.*;
import pattern.GameObserver;

/**
 * ควบคุมวงรอบของเกม Monopoly และการคำนวณกฎกติกา
 * เทิร์น ทอยเต๋า ซื้อ ตีเมือง จ่ายค่าผ่านทาง จบเกม เซฟ/โหลด
 * @author พีท 
 */
public class GameController {

    // =========================================================================
    // PART 1: FIELDS & CONSTRUCTOR (ตัวแปรและคอนสตรัคเตอร์)
    // =========================================================================
    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private final List<GameObserver> observers;
    private final GameSetting settings;

    private int currentPlayerIndex;
    private int totalTurnsPlayed;
    private PendingAction pendingAction;
    private String lastGameLog;
    private boolean hasRolledThisTurn;
    private boolean gameOver;
    private Player winner;
    private int lastRoll;

    /** ทอยได้เท่านี้ขึ้นไปถึงออกจากคุก */
    public static final int JAIL_ESCAPE_ROLL = 5;
    /** ทอยหนีคุกครบกี่ครั้งแล้วออกเอง */
    public static final int JAIL_MAX_TRIES = 3;
    /** จ่ายประกันออกจากคุก เงินไม่พอแค่จ่ายไม่ได้ ไม่ล้มละลาย */
    public static final int JAIL_BAIL = 100;

    public GameController(GameSetting settings) {
        this.settings = settings;
        this.board = new Board();
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.observers = new ArrayList<>();
        this.pendingAction = PendingAction.NONE;
        this.currentPlayerIndex = 0;
        this.totalTurnsPlayed = 1;
        this.hasRolledThisTurn = false;
        this.gameOver = false;
        this.lastRoll = 0;
        this.lastGameLog = "เริ่มเกมแล้ว! กำหนดเล่นสูงสุด " + settings.getMaxTurns() + " เทิร์น";

        for (int i = 0; i < settings.getPlayerCount(); i++) {
            players.add(new Player(i, "Player " + (i + 1), settings.getInitialMoney()));
        }
    }

    // =========================================================================
    // PART 2: OBSERVER PATTERN (ระบบแจ้งเตือน UI)
    // =========================================================================
    public void addObserver(GameObserver observer) {
        if (!observers.contains(observer)) observers.add(observer);
    }

    private void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onGameStateChanged();
        }
    }

    // =========================================================================
    // PART 3: ROLL DICE & MOVEMENT (การทอยเต๋าและการเดิน)
    // ลำดับ: ติดคุก? -> เดิน -> ผ่านจุดเริ่ม? -> ดูว่าตกช่องอะไร
    // =========================================================================
    public void rollDice() {
        if (hasRolledThisTurn || gameOver) return;

        Player current = getCurrentPlayer();
        if (current.isBankrupt()) {
            lastGameLog = current.getName() + " ล้มละลายแล้ว ข้ามเทิร์นนี้";
            nextTurn();
            notifyObservers();
            return;
        }

        // ติดคุกแล้วเทิร์นนี้ยังไม่เดิน ต้องทอยหนีหรือจ่ายประกัน
        if (current.isInJail()) {
            tryEscapeJail(current);
            return;
        }

        int roll = dice.roll();
        lastRoll = roll;
        int oldTileId = current.getCurrentTileId();
        int newRawTileId = oldTileId + roll;

        hasRolledThisTurn = true;

        boolean passedStart = newRawTileId >= 24;
        if (passedStart) {
            current.addMoney(EventTile.PASS_START_BONUS);
        }

        current.moveTo(newRawTileId);
        Tile landedTile = board.getTile(current.getCurrentTileId());

        lastGameLog = current.getName() + " ทอยเต๋าได้ " + roll + " เดินไปตกช่อง "
            + current.getCurrentTileId() + " [" + landedTile.getName() + "]";
        if (passedStart) {
            lastGameLog += "\n[โบนัส] เดินผ่านจุดเริ่มต้น รับเงิน " + EventTile.PASS_START_BONUS + " บาท";
        }

        resolveLanding(current);
        checkGameOver();
        notifyObservers();
    }

    /**
     * ดูช่องที่ผู้เล่นยืนอยู่ตอนนี้ แล้วตั้งงานค้างหรือให้ผลช่องพิเศษ
     * ใช้ทั้งหลังทอยเต๋า และหลังการ์ดเดินฟรี
     */
    private void resolveLanding(Player current) {
        Tile landedTile = board.getTile(current.getCurrentTileId());

        if (landedTile instanceof CityTile) {
            CityTile city = (CityTile) landedTile;
            if (city.getOwner() == null) {
                pendingAction = PendingAction.BUY_CITY;
                appendLog("-> เมืองนี้ยังไม่มีเจ้าของ (ราคา " + city.getBasePrice() + " บาท)");
            } else if (city.getOwner() != current) {
                pendingAction = PendingAction.SIEGE_OR_PAY;
                appendLog("-> ตกเมืองของ " + city.getOwner().getName() + " (เลือกบุกยึดหรือจ่ายค่าเช่า)");
            } else {
                pendingAction = PendingAction.NONE;
                appendLog("-> เมืองของตัวเอง");
            }
            return;
        }

        if (landedTile instanceof EventTile && !((EventTile) landedTile).isCardTile()) {
            applySpecialTile(current, (EventTile) landedTile);
            return;
        }

        if (landedTile instanceof EventTile) {
            pendingAction = PendingAction.DRAW_CARD;
            appendLog("-> ช่องการ์ด กดจั่วการ์ด");
            return;
        }

        pendingAction = PendingAction.NONE;
    }

    /** ผู้เล่นกดปุ่มจั่ว ระบบสุ่มให้ 1 ใบ */
    public void drawPendingCard() {
        drawPendingCard(EventCard.getRandomCard());
    }

    /**
     * จั่วใบที่กำหนด (ปุ่มจั่วส่งใบสุ่มเข้ามา, เทสต์ส่งใบที่ต้องการได้)
     * เก็บใบไว้ในมือก่อน แล้วค่อยทำผล
     */
    public void drawPendingCard(Card drawnCard) {
        if (gameOver || pendingAction != PendingAction.DRAW_CARD || drawnCard == null) return;

        Player current = getCurrentPlayer();
        current.addCard(drawnCard);
        int oldTile = current.getCurrentTileId();

        EventCard.applyEffect(drawnCard, current, board);
        lastGameLog = current.getName() + " จั่วการ์ด [" + drawnCard.getTitle() + "]: " + drawnCard.getDescription();

        if (EventCard.FREE_MOVE.equals(drawnCard.getType())) {
            // เดินฟรีอาจข้ามจุดเริ่ม ต้องให้เงินก่อน แล้วค่อยตรวจช่องใหม่
            if (oldTile + EventCard.FREE_MOVE_STEPS >= 24) {
                current.addMoney(EventTile.PASS_START_BONUS);
                appendLog("[โบนัส] เดินฟรีผ่านจุดเริ่ม รับเงิน " + EventTile.PASS_START_BONUS + " บาท");
            }
            Tile now = board.getTile(current.getCurrentTileId());
            appendLog("เดินฟรีไปช่อง " + current.getCurrentTileId() + " [" + now.getName() + "]");
            resolveLanding(current);
        } else {
            pendingAction = PendingAction.NONE;
            if (current.isBankrupt()) {
                appendLog(current.getName() + " จ่ายไม่ไหว ล้มละลายและเสียเมืองทั้งหมด");
            }
            if (current.isInJail()) {
                appendLog(current.getName() + " ถูกส่งเข้าคุก ตาหน้าต้องทอยหนีหรือจ่ายประกัน");
            }
        }

        checkGameOver();
        notifyObservers();
    }

    /** ผลของช่องพิเศษ 5 ช่อง ได้ทันที ไม่ต้องกดจั่ว */
    private void applySpecialTile(Player current, EventTile tile) {
        String type = tile.getEventType();
        if (EventTile.CAMP.equals(type)) {
            current.addMoney(EventTile.CAMP_HEAL);
            current.setCampDefense(true);
            appendLog("[ช่องพิเศษ] " + tile.getDescription());
        } else if (EventTile.WATCHTOWER.equals(type)) {
            appendLog("[ช่องพิเศษ] " + tile.getDescription());
            appendLog(buildScoutReport(current));
        } else if (EventTile.TRAINING_GROUND.equals(type)) {
            current.setTrainingBoost(true);
            appendLog("[ช่องพิเศษ] " + tile.getDescription());
        } else {
            appendLog("[ช่องพิเศษ] " + tile.getDescription());
        }
        pendingAction = PendingAction.NONE;
    }

    /** หอคอย: ไล่เมืองของคนอื่น แล้วพิมพ์เจ้าของ ค่าเช่า เกราะ */
    public String buildScoutReport(Player viewer) {
        StringBuilder sb = new StringBuilder("ส่องเมือง:");
        boolean found = false;
        for (Tile tile : board.getAllTiles()) {
            if (!(tile instanceof CityTile)) continue;
            CityTile city = (CityTile) tile;
            Player owner = city.getOwner();
            if (owner == null || owner == viewer || owner.isBankrupt()) continue;
            found = true;
            int linked = board.calculateConnectedTerritory(city.getId(), owner);
            int armor = city.getBaseRent() + linked * 50;
            if (owner.hasCampDefense()) armor += EventTile.CAMP_DEFENSE_BONUS;
            sb.append("\n- ช่อง ").append(city.getId()).append(" ").append(city.getName())
              .append(" | ").append(owner.getName())
              .append(" | ค่าเช่า ").append(city.getBaseRent())
              .append(" | เกราะ ").append(armor);
        }
        if (!found) return "ส่องเมือง: ยังไม่มีเมืองของคนอื่น";
        return sb.toString();
    }

    /**
     * ทอยหนีคุก
     * ได้ 5 หรือ 6 = ออก แต่เทิร์นนี้ยังไม่เดิน
     * ไม่ได้ และยังไม่ครบ 3 ครั้ง = อยู่ต่อ
     * ครบ 3 ครั้ง = ออกเอง เทิร์นนี้ก็ยังไม่เดิน
     */
    private void tryEscapeJail(Player current) {
        int roll = dice.roll();
        lastRoll = roll;
        hasRolledThisTurn = true;
        pendingAction = PendingAction.NONE;
        current.setJailTurns(current.getJailTurns() + 1);

        if (roll >= JAIL_ESCAPE_ROLL) {
            current.setInJail(false);
            lastGameLog = current.getName() + " ทอยได้ " + roll + " ออกจากคุกได้ (เทิร์นนี้ยังไม่เดิน)";
        } else if (current.getJailTurns() >= JAIL_MAX_TRIES) {
            current.setInJail(false);
            lastGameLog = current.getName() + " ทอยได้ " + roll
                + " ครบ " + JAIL_MAX_TRIES + " ครั้ง ถูกปล่อยออกจากคุก (เทิร์นนี้ยังไม่เดิน)";
        } else {
            lastGameLog = current.getName() + " ทอยได้ " + roll
                + " ยังออกจากคุกไม่ได้ (" + current.getJailTurns() + "/" + JAIL_MAX_TRIES
                + ") ต้องได้ " + JAIL_ESCAPE_ROLL + " ขึ้นไป";
        }
        notifyObservers();
    }

    /** จ่าย 100 ออกจากคุก เงินไม่พอไม่ล้มละลาย และเทิร์นนี้ยังไม่เดิน */
    public void payBail() {
        if (gameOver) return;
        Player current = getCurrentPlayer();
        if (!current.isInJail() || hasRolledThisTurn) return;

        if (!current.deductMoney(JAIL_BAIL)) {
            lastGameLog = current.getName() + " มีเงินไม่พอจ่ายประกัน " + JAIL_BAIL + " บาท";
            notifyObservers();
            return;
        }
        current.setInJail(false);
        hasRolledThisTurn = true;
        pendingAction = PendingAction.NONE;
        lastGameLog = current.getName() + " จ่ายประกัน " + JAIL_BAIL + " บาท ออกจากคุก (เทิร์นนี้ยังไม่เดิน)";
        notifyObservers();
    }

    private void appendLog(String line) {
        if (line == null || line.isEmpty()) return;
        if (lastGameLog == null || lastGameLog.isEmpty()) lastGameLog = line;
        else lastGameLog += "\n" + line;
    }

    // =========================================================================
    // PART 4: PENDING ACTIONS (การตอบรับการตัดสินใจของผู้เล่น)
    // =========================================================================
    public void processPendingAction(boolean confirm) {
        if (gameOver) return;
        // ช่องการ์ดข้ามไม่ได้ ทั้งกดตกลงและปฏิเสธจะจั่วให้
        if (pendingAction == PendingAction.DRAW_CARD) {
            drawPendingCard();
            return;
        }
        Player current = getCurrentPlayer();
        Tile landedTile = board.getTile(current.getCurrentTileId());

        if (pendingAction == PendingAction.BUY_CITY && landedTile instanceof CityTile) {
            CityTile city = (CityTile) landedTile;
            if (confirm) {
                if (current.deductMoney(city.getBasePrice())) {
                    city.setOwner(current);
                    lastGameLog = current.getName() + " ซื้อเมือง " + city.getName() + " สำเร็จ!";
                } else {
                    lastGameLog = current.getName() + " เงินไม่พอซื้อเมือง " + city.getName();
                }
            } else {
                lastGameLog = current.getName() + " ข้ามการซื้อเมือง " + city.getName();
            }
        } else if (pendingAction == PendingAction.SIEGE_OR_PAY && landedTile instanceof CityTile) {
            CityTile city = (CityTile) landedTile;
            Player attacker = current;
            Player defender = city.getOwner();

            if (confirm) { // ยอมบุกตีเมือง
                BattleResult result = SiegeSystem.resolveSiege(attacker, defender, city, board);
                lastGameLog = result.getMessage();
            } else { // ยอมจ่ายค่าเช่า
                int connectedCount = board.calculateConnectedTerritory(city.getId(), defender);
                int rent = city.getBaseRent() * connectedCount;
                String defenderName = defender != null ? defender.getName() : "เจ้าของเมือง";
                int held = Math.max(attacker.getMoney(), 0);

                // จ่ายครบแม้เหลือ 0 ก็ยังไม่ล้มละลาย
                // จ่ายไม่ครบ: โอนเท่าที่มี เหลือ 0 ล้มละลาย และคืนเมือง
                if (board.settleDebt(attacker, defender, rent)) {
                    lastGameLog = attacker.getName() + " จ่ายค่าเช่า " + rent + " บาท ให้แก่ " + defenderName;
                } else {
                    int paid = Math.min(held, rent);
                    lastGameLog = attacker.getName() + " จ่ายค่าเช่าไม่ครบ (จ่าย " + paid + " จาก " + rent
                        + " บาท) ล้มละลายและเสียเมืองทั้งหมด";
                }
            }
        }

        pendingAction = PendingAction.NONE;
        checkGameOver();
        notifyObservers();
    }

    // =========================================================================
    // PART 5: TURN MANAGEMENT (การสลับเทิร์นและนับรอบ)
    // =========================================================================
    public void endTurn() {
        if (gameOver) return;
        // ยังจั่วไม่เสร็จ หรือยังไม่ตอบเมือง ห้ามส่งเทิร์น
        if (pendingAction != PendingAction.NONE) return;

        hasRolledThisTurn = false;
        nextTurn();

        if (totalTurnsPlayed > settings.getMaxTurns()) {
            calculateSuddenDeathWinner();
        } else {
            lastGameLog = "เปลี่ยนเทิร์น -> เป็นตาของ " + getCurrentPlayer().getName() + " (เทิร์นที่ " + totalTurnsPlayed + "/" + settings.getMaxTurns() + ")";
        }

        notifyObservers();
    }

    private void nextTurn() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            if (currentPlayerIndex == 0) {
                totalTurnsPlayed++;
            }
        } while (getCurrentPlayer().isBankrupt() && !gameOver);
    }

    // =========================================================================
    // PART 6: SAVE & LOAD SYSTEM (ระบบบันทึกและโหลดเกม)
    // =========================================================================
    public void saveGame() {
        if (gameOver) {
            this.lastGameLog = "เกมจบแล้ว บันทึกไม่ได้";
            notifyObservers();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("PlayerCount:").append(settings.getPlayerCount()).append("\n");
        sb.append("InitialMoney:").append(settings.getInitialMoney()).append("\n");
        sb.append("MaxTurns:").append(settings.getMaxTurns()).append("\n");
        sb.append("CurrentTurn:").append(totalTurnsPlayed).append("\n");
        sb.append("CurrentPlayer:").append(currentPlayerIndex).append("\n");
        sb.append("HasRolled:").append(hasRolledThisTurn).append("\n");
        sb.append("PendingAction:").append(pendingAction.name()).append("\n");
        sb.append("GameOver:").append(gameOver).append("\n");
        sb.append("LastRoll:").append(lastRoll).append("\n");

        for (Player p : players) {
            sb.append("Player:").append(p.getId()).append(",")
              .append(p.getName()).append(",")
              .append(p.getMoney()).append(",")
              .append(p.getCurrentTileId()).append(",")
              .append(p.isInJail()).append(",")
              .append(p.getJailTurns()).append(",")
              .append(p.hasCampDefense()).append(",")
              .append(p.hasTrainingBoost()).append(",")
              .append(p.hasSiegeDiscount()).append(",")
              .append(p.isBankrupt()).append("\n");
            for (Card card : p.getCards()) {
                sb.append("Card:").append(p.getId()).append(",").append(card.getType()).append("\n");
            }
        }

        for (Tile tile : board.getAllTiles()) {
            if (tile instanceof CityTile) {
                CityTile city = (CityTile) tile;
                int ownerId = (city.getOwner() != null) ? city.getOwner().getId() : -1;
                sb.append("City:").append(city.getId()).append(",").append(ownerId).append("\n");
            }
        }

        FileManager.saveGame(sb.toString());
        this.lastGameLog = "บันทึกสถานะเกม ณ จุดนี้เรียบร้อยแล้ว (เทิร์นที่ " + totalTurnsPlayed + ")";
        notifyObservers();
    }

    public static GameController loadGame() {
        String data = FileManager.loadGame();
        if (data == null) return null;

        try {
            return fromSaveData(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static GameController fromSaveData(String data) {
        Map<String, String> meta = new HashMap<>();
        List<String[]> playerLines = new ArrayList<>();
        List<String[]> cityLines = new ArrayList<>();
        List<String[]> cardLines = new ArrayList<>();

        for (String rawLine : data.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;
            
            if (line.startsWith("Player:")) {
                playerLines.add(line.substring("Player:".length()).split(","));
            } else if (line.startsWith("City:")) {
                cityLines.add(line.substring("City:".length()).split(","));
            } else if (line.startsWith("Card:")) {
                cardLines.add(line.substring("Card:".length()).split(","));
            } else {
                int idx = line.indexOf(':');
                if (idx > 0) meta.put(line.substring(0, idx), line.substring(idx + 1));
            }
            //indexOf() ใช้สำหรับค้นหาตำแหน่ง (Index) ของคำหรือตัวอักษรที่เราต้องการในข้อความ ว่ามันอยู่ตรงตำแหน่งไหน
            //substring JavaScript text.substring(0, 4); ผลลัพธ์: "Java"
            //.startsWith() ใช้สำหรับ ตรวจสอบว่าข้อความเริ่มต้นด้วยคำหรือตัวอักษรที่กำหนดไว้หรือไม่
            //containsKey() ใช้สำหรับ ตรวจสอบว่ามี Key (กุญแจ หรือชื่อหัวข้อ) ที่เราต้องการ อยู่ในชุดข้อมูล (Map/Dictionary) นั้นๆ
            //parseInt() ตัดเศษทิ้งอัตโนมัติ: เพราะมันทำหน้าที่แปลงเป็น จำนวนเต็ม ถ้าข้อความมีจุดทศนิยม มันจะตัดทศนิยมทิ้งหมดเลย (เช่น parseInt("10.99") จะได้ 10)
        }

        if (playerLines.isEmpty() || !meta.containsKey("PlayerCount")) return null;

        GameSetting settings = new GameSetting();
        settings.setPlayerCount(Integer.parseInt(meta.getOrDefault("PlayerCount", "2")));
        settings.setInitialMoney(Integer.parseInt(meta.getOrDefault("InitialMoney", "1500")));
        settings.setMaxTurns(Integer.parseInt(meta.getOrDefault("MaxTurns", "20")));

        GameController controller = new GameController(settings);

        controller.currentPlayerIndex = Integer.parseInt(meta.getOrDefault("CurrentPlayer", "0"));
        controller.totalTurnsPlayed = Integer.parseInt(meta.getOrDefault("CurrentTurn", "1"));
        controller.hasRolledThisTurn = Boolean.parseBoolean(meta.getOrDefault("HasRolled", "false"));
        controller.gameOver = Boolean.parseBoolean(meta.getOrDefault("GameOver", "false"));
        controller.lastRoll = Integer.parseInt(meta.getOrDefault("LastRoll", "0"));
        String pendingName = meta.getOrDefault("PendingAction", "NONE");
        // เซฟเก่าอาจยังเขียนสถานะที่ถอดออกแล้ว
        if ("PAY_RENT".equals(pendingName)) pendingName = "SIEGE_OR_PAY";
        if ("IN_JAIL".equals(pendingName)) pendingName = "NONE";
        try {
            controller.pendingAction = PendingAction.valueOf(pendingName);
        } catch (IllegalArgumentException e) {
            controller.pendingAction = PendingAction.NONE;
        }

        for (String[] fields : playerLines) {
            int id = Integer.parseInt(fields[0].trim());
            int money = Integer.parseInt(fields[2].trim());
            int tileId = Integer.parseInt(fields[3].trim());
            boolean inJail = fields.length > 4 && Boolean.parseBoolean(fields[4].trim());

            Player player = controller.players.get(id);
            player.setMoney(money);
            player.moveTo(tileId);
            // setInJail รีเซ็ตจำนวนครั้งที่ทอยหนี ดังนั้นต้องเซ็ตจำนวนครั้งทีหลัง
            player.setInJail(inJail);
            if (fields.length > 5) player.setJailTurns(Integer.parseInt(fields[5].trim()));
            if (fields.length > 6) player.setCampDefense(Boolean.parseBoolean(fields[6].trim()));
            if (fields.length > 7) player.setTrainingBoost(Boolean.parseBoolean(fields[7].trim()));
            if (fields.length > 8) player.setSiegeDiscount(Boolean.parseBoolean(fields[8].trim()));
            if (fields.length > 9) {
                player.setBankrupt(Boolean.parseBoolean(fields[9].trim()));
            } else if (money <= 0) {
                // เซฟเก่าไม่มีธงล้มละลาย เงินไม่เกิน 0 ถือว่าออกจากเกมแล้วตามกติกาเดิม
                player.setBankrupt(true);
            }
        }

        for (String[] fields : cardLines) {
            if (fields.length < 2) continue;
            int playerId = Integer.parseInt(fields[0].trim());
            Card card = EventCard.cardOfType(fields[1].trim());
            if (card != null && playerId >= 0 && playerId < controller.players.size()) {
                controller.players.get(playerId).addCard(card);
            }
        }

        for (String[] fields : cityLines) {
            int tileId = Integer.parseInt(fields[0].trim());
            int ownerId = Integer.parseInt(fields[1].trim());
            Tile tile = controller.board.getTile(tileId);
            if (tile instanceof CityTile) {
                ((CityTile) tile).setOwner(ownerId == -1 ? null : controller.players.get(ownerId));
            }
        }

        for (Player p : controller.players) {
            if (p.isBankrupt()) controller.board.releaseCitiesOf(p);
        }

        if (controller.gameOver) {
            controller.winner = controller.findWinner();
            String who = controller.winner != null ? controller.winner.getName() : "ไม่มีผู้ชนะ";
            controller.lastGameLog = "โหลดเกมที่จบแล้ว ผู้ชนะคือ " + who;
        } else {
            controller.lastGameLog = "โหลดเกมที่บันทึกไว้สำเร็จ (ดำเนินเกมต่อจากเทิร์นที่ " + controller.totalTurnsPlayed + ")";
        }
        return controller;
    }

    // =========================================================================
    // PART 7: GAME OVER & WINNER CALCULATION (การเช็กจบเกมและคิดผลชนะ)
    // =========================================================================
    private int countActivePlayers() {
        int active = 0;
        for (Player p : players) {
            if (!p.isBankrupt()) active++;
        }
        return active;
    }

    /** เงินสดบวกกับราคาเมืองที่ยังถืออยู่ */
    private int wealthOf(Player player) {
        int wealth = player.getMoney();
        for (Tile tile : board.getAllTiles()) {
            if (tile instanceof CityTile && ((CityTile) tile).getOwner() == player) {
                wealth += ((CityTile) tile).getBasePrice();
            }
        }
        return wealth;
    }

    /**
     * เหลือคนไม่ล้มละลายคนเดียว คนนั้นชนะ
     * ถ้ายังเหลือหลายคน ให้คนที่ทรัพย์สินสูงสุดชนะ (กรณีหมดเทิร์น)
     */
    private Player findWinner() {
        Player lastActive = null;
        int active = 0;
        for (Player p : players) {
            if (p.isBankrupt()) continue;
            active++;
            lastActive = p;
        }
        if (active <= 1) return lastActive;

        Player topPlayer = null;
        int maxWealth = -1;
        for (Player p : players) {
            if (p.isBankrupt()) continue;
            int wealth = wealthOf(p);
            if (wealth > maxWealth) {
                maxWealth = wealth;
                topPlayer = p;
            }
        }
        return topPlayer;
    }

    private void checkGameOver() {
        if (countActivePlayers() > 1) return;
        gameOver = true;
        winner = findWinner();
        String winLine = "เกมจบลงแล้ว! ผู้ชนะคือ: " + (winner != null ? winner.getName() : "ไม่มีผู้ชนะ");
        if (lastGameLog == null || lastGameLog.isEmpty()) {
            lastGameLog = winLine;
        } else if (!lastGameLog.contains(winLine)) {
            lastGameLog = lastGameLog + "\n" + winLine;
        }
    }

    private void calculateSuddenDeathWinner() {
        gameOver = true;
        winner = findWinner();
        int maxWealth = winner == null ? -1 : wealthOf(winner);
        lastGameLog = "หมดเวลา " + settings.getMaxTurns() + " เทิร์น! ผู้ที่มีทรัพย์สินสูงสุด (" + maxWealth + " บาท) คือ: " + (winner != null ? winner.getName() : "ไม่มีผู้ชนะ");
    }

    // =========================================================================
    // PART 8: GETTERS (เมธอดดึงสถานะไปแสดงผลบน GUI)
    // =========================================================================
    public boolean hasRolledThisTurn() { return hasRolledThisTurn; }
    public boolean isGameOver() { return gameOver; }
    public Player getWinner() { return winner; }
    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public PendingAction getPendingAction() { return pendingAction; }
    public String getLastGameLog() { return lastGameLog; }
    public int getLastRoll() { return lastRoll; }
}