package view;

import javax.swing.*;
import java.awt.*;
import controller.GameController;
import pattern.GameObserver;

/**
 * หน้าเล่นเกม
 * ซ้าย = สถานะผู้เล่น, กลาง = กระดาน, ขวา = บันทึก, ล่าง = ปุ่ม
 * หน้าตายังเป็นกรอบกับฟอนต์เดิม แค่ย้ายตำแหน่งแผง
 *
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
        controller.addObserver(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusPanel = new PlayerStatusPanel(controller);
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.add(statusPanel, BorderLayout.CENTER);

        boardPanel = new BoardPanel(controller);

        logPanel = new GameLogPanel(controller);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.add(logPanel, BorderLayout.CENTER);

        controlPanel = new ActionControlPanel(controller, mainUI);

        add(leftPanel, BorderLayout.WEST);
        add(boardPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        onGameStateChanged();
    }

    @Override
    public void onGameStateChanged() {
        boardPanel.repaint();
        statusPanel.updateStatus();
        controlPanel.updateControlState();
        logPanel.appendLog();
    }
}
