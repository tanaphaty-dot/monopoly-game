package pattern;

/**
 * Interface สำหรับการอัปเดตหน้าจอ GUI แบบอัตโนมัติเมื่อข้อมูลในเกมเปลี่ยน (Observer Pattern)
 * @author พีท controller-system
 */
public interface GameObserver {
    /**
     * NOTICE GUI Team: คลาส GamePanel จะต้อง implements อินเทอร์เฟซนี้
     * ให้ GameController เรียก repaint() หน้าจออัตโนมัติตอนสถานะเกมเปลี่ยน
     */
    void onGameStateChanged();
}