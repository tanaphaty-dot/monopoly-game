package controller;

/**
 * คลาสเก็บข้อมูลการตั้งค่ากติกาเกมก่อนเริ่มเล่น
 * @author พีท 
 */
public class GameSetting {
    private int playerCount;
    private int initialMoney;
    private int maxTurns;

    public GameSetting() {
        this.playerCount = 2;
        this.initialMoney = 1500;
        this.maxTurns = 20;
    }

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }

    public int getInitialMoney() { return initialMoney; }
    public void setInitialMoney(int initialMoney) { this.initialMoney = initialMoney; }

    public int getMaxTurns() { return maxTurns; }
    public void setMaxTurns(int maxTurns) { this.maxTurns = maxTurns; }
}