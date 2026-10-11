package view;

import javax.swing.*;
import java.awt.*;
import controller.GameController;
import pattern.GameObserver;

/**
 * หน้าจอเล่นเกมหลักที่รวมกระดาน, ข้อมูลผู้เล่น, ปุ่มสั่งการ และ Log
 * @author พีท (feature/ui-system)
 */
public class GamePanel extends JPanel implements GameObserver {
    private final GameController controller;
    private final BoardPanel boardPanel;
    private final PlayerStatusPanel statusPanel;
    private final ActionControlPanel controlPanel;
    private final GameLogPanel logPanel;

    public GamePanel(MainUI mainUI, GameController controller) {
        this.controller = controller;
        this.controller.addObserver(this); // ลงทะเบียนเป็น Observer คอยรับการแจ้งเตือน

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. สร้าง Panel ย่อยทั้งหมด
        boardPanel = new BoardPanel(controller);
        statusPanel = new PlayerStatusPanel(controller);
        controlPanel = new ActionControlPanel(controller, mainUI);
        logPanel = new GameLogPanel(controller);

        // 2. วาง BoardPanel ไว้ตรงกลาง
        add(boardPanel, BorderLayout.CENTER);

        // 3. รวม Panel ข้อมูล, Log, และปุ่มกดไว้ฝั่งขวา
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(360, 0));
        rightPanel.add(statusPanel, BorderLayout.NORTH);
        rightPanel.add(logPanel, BorderLayout.CENTER);
        rightPanel.add(controlPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);

        // ซิงค์สถานะหน้าจอครั้งแรก
        onGameStateChanged();
    }

    // เมื่อข้อมูลในเกมเปลี่ยนแปลง ให้สั่งสั่งงานทุก Panel ย่อยให้อัปเดต
    @Override
    public void onGameStateChanged() {
        boardPanel.repaint();              // วาดกระดานและตัวผู้เล่นใหม่
        statusPanel.updateStatus();        // อัปเดตยอดเงินและคิวผู้เล่น
        controlPanel.updateControlState(); // เปิด-ปิดปุ่มตามสิทธิ์
        logPanel.appendLog();              // แสดง Log เหตุการณ์ล่าสุด
    }
}