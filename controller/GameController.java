package controller;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.CityTile;
import model.board.Tile;
import model.entity.Dice;
import model.entity.PendingAction;
import model.entity.Player;
import pattern.GameObserver;
import model.combat.BattleResult;
import model.combat.SiegeSystem;

/**
 * Controller หลักคุมวงจรเกม (Turn Loop), การสุ่มเต๋า, Actions ค้าง และการส่งแจ้งเตือนผ่าน Observer
 * @author พีท ui-system
 */
public class GameController {
    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private final List<GameObserver> observers;

    private int currentPlayerIndex;
    private PendingAction pendingAction;
    private String lastGameLog;

    public GameController(GameSetting settings) {
        this.board = new Board();
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.observers = new ArrayList<>();
        this.pendingAction = PendingAction.NONE;
        this.currentPlayerIndex = 0;
        this.lastGameLog = "เกมเริ่มต้นขึ้นแล้ว! ยินดีต้อนรับสู่ Monopoly & Zone Rush";

        // สร้างตัวละครผู้เล่นตามการตั้งค่าจาก GameSettings
        for (int i = 0; i < settings.getPlayerCount(); i++) {
            players.add(new Player(i, "Player " + (i + 1), settings.getInitialMoney()));
        }
    }

    public void addObserver(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onGameStateChanged();
        }
    }

    /**
     * ทอยเต๋า คำนวณตำแหน่งใหม่ และตรวจเช็กประเภทช่องที่ไปตก
     */
    public void rollDice() {
        Player current = getCurrentPlayer();

        if (current.isBankrupt()) {
            lastGameLog = current.getName() + " ล้มละลายแล้ว ไม่สามารถเดินได้";
            nextTurn();
            notifyObservers();
            return;
        }

        int roll = dice.roll();
        current.moveTo(current.getCurrentTileId() + roll);

        Tile landedTile = board.getTile(current.getCurrentTileId());
        lastGameLog = current.getName() + " ทอยเต๋าได้ " + roll + " เดินไปตกที่ช่อง [" + landedTile.getName() + "]";

        // ตรวจสอบแอ็กชันที่ต้องทำเมื่อตกช่องต่างๆ
        if (landedTile instanceof CityTile) {
            CityTile city = (CityTile) landedTile;
            if (city.getOwner() == null) {
                pendingAction = PendingAction.BUY_CITY;
                lastGameLog += " -> เมืองนี้ยังไม่มีเจ้าของ (ราคา 200 บาท)";
            } else if (city.getOwner() != current) {
                pendingAction = PendingAction.SIEGE_OR_PAY;
                lastGameLog += " -> ตกเมืองของ " + city.getOwner().getName() + " (เลือกยึดเมืองหรือจ่ายค่าเช่า)";
            } else {
                pendingAction = PendingAction.NONE;
            }
        } else {
            pendingAction = PendingAction.NONE;
        }

        notifyObservers();
    }

    /**
     * ประมวลผลคำตอบจากปุ่มกด UI (ตกลง/ยกเลิก)
     */
    public void processPendingAction(boolean confirm) {
        Player current = getCurrentPlayer();
        Tile landedTile = board.getTile(current.getCurrentTileId());

        if (pendingAction == PendingAction.BUY_CITY && landedTile instanceof CityTile) {
            CityTile city = (CityTile) landedTile;
            if (confirm) {
                int cityPrice = 200;
                if (current.deductMoney(cityPrice)) {
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
        if (confirm) {
        lastGameLog = current.getName() + " ประกาศสงครามบุกยึดเมือง " + city.getName() + "!";
        // NOTICE (ถึง เก๋า): เรียก SiegeSystem.resolveSiege(...) ตรงนี้
            } else {
        int rent = 50;
        current.deductMoney(rent);
        if (city.getOwner() != null) {
            city.getOwner().addMoney(rent);
        }
        lastGameLog = current.getName() + " ยอมจ่ายค่าเช่า " + rent + " บาท ให้แก่ " + city.getOwner().getName();
    }
}

        pendingAction = PendingAction.NONE;
        notifyObservers();
    }

    /**
     * จบเทิร์นของผู้เล่นปัจจุบัน และเปลี่ยนเป็นเทิร์นถัดไป
     */
    public void endTurn() {
        nextTurn();
        lastGameLog = "เปลี่ยนเทิร์น -> เป็นตาของ " + getCurrentPlayer().getName();
        notifyObservers();
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public void saveGame() {
        StringBuilder sb = new StringBuilder();
        sb.append("CurrentTurn:").append(currentPlayerIndex).append("\n");
        for (Player p : players) {
            sb.append(p.getId()).append(",").append(p.getName()).append(",").append(p.getMoney()).append(",").append(p.getCurrentTileId()).append("\n");
        }
        if (FileManager.saveGame(sb.toString())) {
            lastGameLog = "บันทึกเกมลงไฟล์ savegame.txt เรียบร้อยแล้ว";
        } else {
            lastGameLog = "เกิดข้อผิดพลาดในการบันทึกเกม";
        }
        notifyObservers();
    }

    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public PendingAction getPendingAction() { return pendingAction; }
    public String getLastGameLog() { return lastGameLog; }
}