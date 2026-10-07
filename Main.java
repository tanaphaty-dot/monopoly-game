import javax.swing.*;
import java.awt.*;
import view.MainUI;

public class Main {
    public static void main(String[] args) {
        // --- บังคับให้ Pop-up (JOptionPane) ทั้งหมดใช้ Font ภาษาไทย ---
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 14);
        UIManager.put("OptionPane.messageFont", thaiFont);
        UIManager.put("OptionPane.buttonFont", thaiFont);

        SwingUtilities.invokeLater(() -> {
            MainUI mainUI = new MainUI();
            mainUI.setVisible(true);
        });
    }
}