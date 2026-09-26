package controller;

/**
 * คลาสเก็บข้อมูลคอนฟิกเริ่มต้นของเกมที่ได้รับมาจากหน้า SettingsPanel (เช่น จำนวนผู้เล่น และเงินเริ่มต้น)
 * @author พีท ui-system
 */
public class GameSetting {
    private int playerCount;
    private int initialMoney;

    /**
     * Default Constructor (2 คน, 1,500 บาท)
     */
    public GameSetting() {
        this.playerCount = 2;
        this.initialMoney = 1500;
    }

    public GameSetting(int playerCount, int initialMoney) {
        this.playerCount = playerCount;
        this.initialMoney = initialMoney;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getInitialMoney() {
        return initialMoney;
    }

    public void setInitialMoney(int initialMoney) {
        this.initialMoney = initialMoney;
    }
}