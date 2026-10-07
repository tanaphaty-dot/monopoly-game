package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.swing.border.TitledBorder;
import controller.GameController;
import model.entity.Player;

/**
 * แสดงเงิน และสถานะของผู้เล่นทั้งหมด
 * @author พีท (feature/ui-system)
 */
public class PlayerStatusPanel extends JPanel {
    private final GameController controller;
    private final JPanel listContainer;

    public PlayerStatusPanel(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        // กำหนด TitledBorder พร้อมใส่ Font ภาษาไทย
        TitledBorder border = BorderFactory.createTitledBorder("สถานะผู้เล่น");
        border.setTitleFont(FontUtil.getThaiFontBold(14f)); // เซ็ตฟอนต์ภาษาไทยให้หัวข้อกรอบ
        setBorder(border);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        add(new JScrollPane(listContainer), BorderLayout.CENTER);
    }

    // วาดรายการสถานะผู้เล่นใหม่ทุกครั้งที่มีการอัปเดต
    public void updateStatus() {
        listContainer.removeAll();
        List<Player> players = controller.getPlayers();
        Player current = controller.getCurrentPlayer();

        for (Player p : players) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getName());
            if (p == current) sb.append(" [ตาปัจจุบัน]");
            sb.append(" - เงิน: ").append(p.getMoney()).append(" บาท");
            if (p.isBankrupt()) sb.append(" (ล้มละลาย)");

            JLabel lbl = new JLabel(sb.toString());
            lbl.setFont(FontUtil.getThaiFont(13f));
            
            // เปลี่ยนสีข้อความตามสถานะผู้เล่น
            if (p == current) {
                lbl.setForeground(new Color(0, 102, 204)); // สีฟ้าสำหรับตาปัจจุบัน
                lbl.setFont(FontUtil.getThaiFontBold(13f));
            } else if (p.isBankrupt()) {
                lbl.setForeground(Color.RED); // สีแดงสำหรับคนล้มละลาย
            }
            
            listContainer.add(lbl);
            listContainer.add(Box.createVerticalStrut(4));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }
}