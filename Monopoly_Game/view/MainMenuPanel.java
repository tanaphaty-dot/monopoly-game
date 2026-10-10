package view;

import javax.swing.*;
import java.awt.*;
import controller.FileManager;

/**
 * หน้าเมนูหลัก
 * @author พีท (feature/ui-system)
 */
public class MainMenuPanel extends JPanel {
    private final JButton btnContinue;

    public MainMenuPanel(MainUI mainUI) {
        setLayout(new GridBagLayout());
        setBackground(new Color(40, 44, 52));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // ข้อความชื่อเกม
        JLabel lblTitle = new JLabel("MONOPOLY & ZONE RUSH", SwingConstants.CENTER);
        lblTitle.setFont(FontUtil.getThaiFontBold(28f));
        lblTitle.setForeground(Color.WHITE);

        // ปุ่มกดเมนู
        JButton btnStart = new JButton("เริ่มเกมใหม่ (New Game)");
        btnContinue = new JButton("ดำเนินเกมต่อ (Continue)");
        JButton btnExit = new JButton("ออกจากเกม (Exit Game)");

        Font btnFont = FontUtil.getThaiFont(16f);
        btnStart.setFont(btnFont);
        btnContinue.setFont(btnFont);
        btnExit.setFont(btnFont);

        // Event ปุ่มกด
        btnStart.addActionListener(e -> mainUI.showScreen("SETTINGS_SCREEN"));
        btnContinue.addActionListener(e -> mainUI.continueGame());
        btnExit.addActionListener(e -> mainUI.confirmExitApplication());

        // เพิ่มคอมโพเนนต์ลงในหน้าจอ
        gbc.gridy = 0; add(lblTitle, gbc);
        gbc.gridy = 1; add(Box.createVerticalStrut(20), gbc);
        gbc.gridy = 2; add(btnStart, gbc);
        gbc.gridy = 3; add(btnContinue, gbc);
        gbc.gridy = 4; add(btnExit, gbc);

        refreshContinueButtonState();
    }

    // เปิดปุ่ม "ดำเนินเกมต่อ" เฉพาะตอนมีไฟล์เซฟอยู่จริงเท่านั้น
    public void refreshContinueButtonState() {
        btnContinue.setEnabled(FileManager.saveExists());
    }
}