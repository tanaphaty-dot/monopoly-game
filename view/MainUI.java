package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import controller.FileManager;
import controller.GameController;
import controller.GameSetting;

/**
 * หน้าต่างหลักของโปรแกรมสำหรับจัดการเปลี่ยนหน้าจอ (CardLayout)
 * @author พีท (feature/ui-system)
 */
public class MainUI extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainContainer;
    private final GameSetting settings;
    private final MainMenuPanel menuPanel;
    private GameController controller;
    private GamePanel gamePanel;

    public MainUI() {
        setTitle("Monopoly & Zone Rush - Board Game");
        
        // ดักจับปุ่มกากบาทปิดหน้าต่างเพื่อถามเซฟก่อนออก
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null); // แสดงหน้าต่างตรงกลางจอ

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        settings = new GameSetting();

        // เพิ่มหน้าจอหลักเข้า CardLayout
        menuPanel = new MainMenuPanel(this);
        mainContainer.add(menuPanel, "MENU_SCREEN");
        mainContainer.add(new SettingsPanel(this, settings), "SETTINGS_SCREEN");

        add(mainContainer);
        showScreen("MENU_SCREEN");

        // Event ดักการปิดหน้าต่างผ่านปุ่มกากบาทมุมขวาบน
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExitApplication();
            }
        });
    }

    // สลับแสดงหน้าจอตามชื่อ Card
    public void showScreen(String name) {
        cardLayout.show(mainContainer, name);
        if ("MENU_SCREEN".equals(name)) {
            menuPanel.refreshContinueButtonState();
        }
    }

    // เริ่มเกมแมตช์ใหม่
    public void startGame() {
        controller = new GameController(settings);
        setupGamePanel();
    }

    // ดำเนินเกมต่อจากไฟล์เซฟล่าสุด
    public void continueGame() {
        GameController loaded = GameController.loadGame();
        if (loaded == null) {
            JOptionPane.showMessageDialog(this,
                "ไม่พบไฟล์เซฟ หรือไฟล์เซฟไม่สมบูรณ์",
                "ดำเนินเกมต่อไม่ได้",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        controller = loaded;
        setupGamePanel();
    }

    // บันทึกเกมปัจจุบันและพักกลับไปหน้าเมนูหลัก
    public void pauseAndReturnToMenu() {
        if (controller != null && !controller.isGameOver()) {
            controller.saveGame();
        }
        showScreen("MENU_SCREEN");
    }

    public boolean hasSaveFile() {
        return FileManager.saveExists();
    }

    private void setupGamePanel() {
        if (gamePanel != null) {
            mainContainer.remove(gamePanel);
        }
        gamePanel = new GamePanel(this, controller);
        mainContainer.add(gamePanel, "GAME_SCREEN");
        showScreen("GAME_SCREEN");
    }

    // ยืนยันก่อนปิดโปรแกรม (แก้ปัญหาภาษาไทยตัวอักษรกลายเป็นกล่องสี่เหลี่ยมด้วย HTML)
    void confirmExitApplication() {
        if (controller != null && !controller.isGameOver()) {
            String message = "<html><font face='Tahoma' size='4'>ต้องการบันทึกเกม ณ จุดนี้ก่อนออกจากโปรแกรมหรือไม่?</font></html>";

            int confirm = JOptionPane.showConfirmDialog(
                this,
                message,
                "ออกจากโปรแกรม",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
                return; // ยกเลิกการปิด ปล่อยให้ผู้เล่นเล่นต่อ
            }
            if (confirm == JOptionPane.YES_OPTION) {
                controller.saveGame(); // บันทึกเกมก่อนออก
            }
        }
        
        System.exit(0); // ปิดโปรแกรม
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainUI ui = new MainUI();
            ui.setVisible(true);
        });
    }
}