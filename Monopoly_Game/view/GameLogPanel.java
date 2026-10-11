package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import controller.GameController;

/**
 * แผงแสดง Log เหตุการณ์ต่างๆ ของเกม
 * @author พีท (feature/ui-system)
 */
public class GameLogPanel extends JPanel {
    private final GameController controller;
    private final JTextArea logArea;

    public GameLogPanel(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // กำหนด TitledBorder พร้อมใส่ Font ภาษาไทย
        TitledBorder border = BorderFactory.createTitledBorder("ประวัติการเล่น ( Game Log )");
        border.setTitleFont(FontUtil.getThaiFontBold(14f)); // เซ็ตฟอนต์ภาษาไทยให้หัวข้อกรอบ
        setBorder(border);

        // สร้างพื้นที่แสดงข้อความ Log
        logArea = new JTextArea();
        logArea.setEditable(false); // ห้ามผู้เล่นแก้ไขข้อความ
        logArea.setFont(FontUtil.getThaiFont(12f));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        // ใส่ ScrollPane เพื่อให้เลื่อนดูข้อความย้อนหลังได้
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    // อัปเดตเพิ่มข้อความ Log ล่าสุดลงในช่องแสดงผล
    public void appendLog() {
        String log = controller.getLastGameLog();
        if (log != null && !log.isEmpty()) {
            logArea.append(log + "\n---------------------\n");
            // เลื่อน Scrollbar ลงมาล่างสุดเสมอ
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
}