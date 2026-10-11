package view;

import javax.swing.*;
import java.awt.*;
import controller.GameSetting;

/**
 * หน้าจอปรับแต่งกติกาเกมก่อนเริ่มเล่น
 * @author พีท (feature/ui-system)
 */
public class SettingsPanel extends JPanel {

    public SettingsPanel(MainUI mainUI, GameSetting settings) {
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 242, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblTitle = new JLabel("ตั้งค่าแมตช์การเล่น (Game Setup)");
        lblTitle.setFont(FontUtil.getThaiFontBold(22f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        // 1. เลือกจำนวนผู้เล่น
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblPlayerCount = new JLabel("จำนวนผู้เล่น:");
        lblPlayerCount.setFont(FontUtil.getThaiFont(14f));
        add(lblPlayerCount, gbc);

        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<Integer> cbPlayerCount = new JComboBox<>(new Integer[]{2, 3, 4});
        cbPlayerCount.setFont(FontUtil.getThaiFont(14f));
        cbPlayerCount.setSelectedItem(settings.getPlayerCount());
        add(cbPlayerCount, gbc);

        // 2. กรอกเงินเริ่มต้น
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblMoney = new JLabel("เงินเริ่มต้น (บาท):");
        lblMoney.setFont(FontUtil.getThaiFont(14f));
        add(lblMoney, gbc);

        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JTextField txtMoney = new JTextField(String.valueOf(settings.getInitialMoney()), 10);
        txtMoney.setFont(FontUtil.getThaiFont(14f));
        add(txtMoney, gbc);

        // 3. เลือกจำนวนเทิร์นสูงสุด
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblTurns = new JLabel("จำนวนเทิร์นสูงสุด:");
        lblTurns.setFont(FontUtil.getThaiFont(14f));
        add(lblTurns, gbc);

        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<Integer> cbMaxTurns = new JComboBox<>(new Integer[]{20, 30, 50});
        cbMaxTurns.setFont(FontUtil.getThaiFont(14f));
        cbMaxTurns.setSelectedItem(settings.getMaxTurns());
        add(cbMaxTurns, gbc);

        // ปุ่มเข้าสู่เกม / ย้อนกลับ
        JPanel btnPanel = new JPanel();
        JButton btnStartGame = new JButton("เข้าสู่เกม (Start Match)");
        JButton btnBack = new JButton("ย้อนกลับ");

        btnStartGame.setFont(FontUtil.getThaiFontBold(14f));
        btnBack.setFont(FontUtil.getThaiFont(14f));

        // ตรวจสอบค่าที่กรอกและเริ่มเกม
        btnStartGame.addActionListener(e -> {
            try {
                int money = Integer.parseInt(txtMoney.getText().trim());
                if (money < 500) {
                    JLabel lblErr = new JLabel("เงินเริ่มต้นต้องไม่น้อยกว่า 500 บาท");
                    lblErr.setFont(FontUtil.getThaiFont(14f));
                    JOptionPane.showMessageDialog(this, lblErr, "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                settings.setPlayerCount((Integer) cbPlayerCount.getSelectedItem());
                settings.setInitialMoney(money);
                settings.setMaxTurns((Integer) cbMaxTurns.getSelectedItem());

                mainUI.startGame();
            } catch (NumberFormatException ex) {
                JLabel lblErr = new JLabel("กรุณากรอกตัวเลขเงินเริ่มต้นให้ถูกต้อง");
                lblErr.setFont(FontUtil.getThaiFont(14f));
                JOptionPane.showMessageDialog(this, lblErr, "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBack.addActionListener(e -> mainUI.showScreen("MENU_SCREEN"));

        btnPanel.add(btnStartGame);
        btnPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(btnPanel, gbc);
    }
}