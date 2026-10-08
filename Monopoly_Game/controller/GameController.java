package controller;

import java.util.*;
import model.board.*;
import model.combat.*;
import model.entity.*;
import pattern.GameObserver;

/**
 * ควบคุมวงรอบของเกม Monopoly และการคำนวณกฎกติกา
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

        int roll = dice.roll();
        int oldTileId = current.getCurrentTileId();
        int newRawTileId = oldTileId + roll;

        hasRolledThisTurn = true;

        // เช็กการเดินผ่านจุดเริ่มต้น (ช่อง 24 ขึ้นไป)
        boolean passedStart = newRawTileId >= 24;
        if (passedStart) {
            current.addMoney(200);
        }

        current.moveTo(newRawTileId);
        Tile landedTile = board.getTile(current.getCurrentTileId());

        lastGameLog = current.getName() + " ทอยเต๋าได้ " + roll + " เดินไปตกช่อง [" + landedTile.getName() + "]";
        if (passedStart) {
            lastGameLog += "\n[โบนัส] เดินผ่านจุดเริ่มต้น รับเงิน 200 บาท!";
        }

        // ตรวจสอบประเภทช่องที่ลง
        if (landedTile instanceof CityTile) {
            CityTile city = (CityTile) landedTile;
            if (city.getOwner() == null) {
                pendingAction = PendingAction.BUY_CITY;
                lastGameLog += "\n-> เมืองนี้ยังไม่มีเจ้าของ (ราคา " + city.getBasePrice() + " บาท)";
            } else if (city.getOwner() != current) {
                pendingAction = PendingAction.SIEGE_OR_PAY;
                lastGameLog += "\n-> ตกเมืองของ " + city.getOwner().getName() + " (เลือกบุกยึดหรือจ่ายค่าเช่า)";
            } else {
                pendingAction = PendingAction.NONE;
            }
        } else if (landedTile instanceof EventTile) {
            Card drawnCard = EventCard.getRandomCard();
            EventCard.applyEffect(drawnCard, current, board);
            lastGameLog += "\n[เหตุการณ์] สุ่มได้การ์ด [" + drawnCard.getTitle() + "]: " + drawnCard.getDescription();
            pendingAction = PendingAction.NONE;
        } else {
            pendingAction = PendingAction.NONE;
        }

        checkGameOver();
        notifyObservers();
    }

    // =========================================================================
    // PART 4: PENDING ACTIONS (การตอบรับการตัดสินใจของผู้เล่น)
    // =========================================================================
    public void processPendingAction(boolean confirm) {
        if (gameOver) return;
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

                if (attacker.deductMoney(rent)) {
                    if (defender != null) defender.addMoney(rent);
                    lastGameLog = attacker.getName() + " จ่ายค่าเช่า " + rent + " บาท ให้แก่ " + (defender != null ? defender.getName() : "เจ้าของเมือง");
                } else {
                    lastGameLog = attacker.getName() + " เงินไม่พอจ่ายค่าเช่า! ล้มละลาย";
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
        StringBuilder sb = new StringBuilder();
        sb.append("PlayerCount:").append(settings.getPlayerCount()).append("\n");
        sb.append("InitialMoney:").append(settings.getInitialMoney()).append("\n");
        sb.append("MaxTurns:").append(settings.getMaxTurns()).append("\n");
        sb.append("CurrentTurn:").append(totalTurnsPlayed).append("\n");
        sb.append("CurrentPlayer:").append(currentPlayerIndex).append("\n");
        sb.append("HasRolled:").append(hasRolledThisTurn).append("\n");
        sb.append("PendingAction:").append(pendingAction.name()).append("\n");
        sb.append("GameOver:").append(gameOver).append("\n");

        for (Player p : players) {
            sb.append("Player:").append(p.getId()).append(",")
              .append(p.getName()).append(",")
              .append(p.getMoney()).append(",")
              .append(p.getCurrentTileId()).append(",")
              .append(p.isInJail()).append("\n");
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

        for (String rawLine : data.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("Player:")) {
                playerLines.add(line.substring("Player:".length()).split(","));
            } else if (line.startsWith("City:")) {
                cityLines.add(line.substring("City:".length()).split(","));
            } else {
                int idx = line.indexOf(':');
                if (idx > 0) meta.put(line.substring(0, idx), line.substring(idx + 1));
            }
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
        try {
            controller.pendingAction = PendingAction.valueOf(meta.getOrDefault("PendingAction", "NONE"));
        } catch (IllegalArgumentException e) {
            controller.pendingAction = PendingAction.NONE;
        }

        for (String[] fields : playerLines) {
            int id = Integer.parseInt(fields[0].trim());
            int money = Integer.parseInt(fields[2].trim());
            int tileId = Integer.parseInt(fields[3].trim());
            boolean inJail = Boolean.parseBoolean(fields[4].trim());

            Player player = controller.players.get(id);
            player.setMoney(money);
            player.moveTo(tileId);
            player.setInJail(inJail);
        }

        for (String[] fields : cityLines) {
            int tileId = Integer.parseInt(fields[0].trim());
            int ownerId = Integer.parseInt(fields[1].trim());
            Tile tile = controller.board.getTile(tileId);
            if (tile instanceof CityTile) {
                ((CityTile) tile).setOwner(ownerId == -1 ? null : controller.players.get(ownerId));
            }
        }

        controller.lastGameLog = "โหลดเกมที่บันทึกไว้สำเร็จ (ดำเนินเกมต่อจากเทิร์นที่ " + controller.totalTurnsPlayed + ")";
        return controller;
    }

    // =========================================================================
    // PART 7: GAME OVER & WINNER CALCULATION (การเช็กจบเกมและคิดผลชนะ)
    // =========================================================================
    private void checkGameOver() {
        int activePlayers = 0;
        Player lastActive = null;
        for (Player p : players) {
            if (!p.isBankrupt()) {
                activePlayers++;
                lastActive = p;
            }
        }
        if (activePlayers <= 1) {
            gameOver = true;
            winner = lastActive;
            lastGameLog = "เกมจบลงแล้ว! ผู้ชนะคือ: " + (winner != null ? winner.getName() : "ไม่มีผู้ชนะ");
        }
    }

    private void calculateSuddenDeathWinner() {
        gameOver = true;
        Player topPlayer = null;
        int maxWealth = -1;

        for (Player p : players) {
            if (p.isBankrupt()) continue;

            int wealth = p.getMoney();
            for (int i = 0; i < 24; i++) {
                Tile tile = board.getTile(i);
                if (tile instanceof CityTile) {
                    CityTile city = (CityTile) tile;
                    if (city.getOwner() == p) {
                        wealth += city.getBasePrice();
                    }
                }
            }

            if (wealth > maxWealth) {
                maxWealth = wealth;
                topPlayer = p;
            }
        }

        winner = topPlayer;
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
}