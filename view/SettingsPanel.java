package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import controller.GameSetting;

/**
 * หน้าตั้งกติกา จัดวางแบบเลือกการ์ด
 * จำนวนคน เงินเริ่มต้น จำนวนเทิร์น
 * สีใช้พื้นเทาและกรอบเทาแบบหน้าเดิม ไม่ใช้ธีมเข้ม
 *
 * @author พีท (feature/ui-system)
 */
public class SettingsPanel extends JPanel {
    private static final Color PAGE_BG = new Color(240, 242, 245);
    private static final Color SELECT_BG = new Color(210, 228, 245);
    private static final Color SELECT_LINE = new Color(0, 102, 204);

    public SettingsPanel(MainUI mainUI, GameSetting settings) {
        setLayout(new GridBagLayout());
        setBackground(PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        final int[] playerCount = { settings.getPlayerCount() };
        final int[] money = { settings.getInitialMoney() };
        final int[] maxTurns = { settings.getMaxTurns() };

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(20, 24, 18, 24)
        ));
        card.setPreferredSize(new Dimension(760, 560));

        card.add(headerRow());
        card.add(Box.createVerticalStrut(16));

        card.add(sectionRow("[ 01 ] จำนวนผู้เล่น", "เลือกได้ 2 ถึง 4 คน"));
        card.add(Box.createVerticalStrut(8));
        ChoiceBox p2 = new ChoiceBox("2 คน", "ดวลกัน");
        ChoiceBox p3 = new ChoiceBox("3 คน", "สามคน");
        ChoiceBox p4 = new ChoiceBox("4 คน", "สี่คน");
        List<ChoiceBox> playerBoxes = List.of(p2, p3, p4);
        p2.setSelected(playerCount[0] == 2);
        p3.setSelected(playerCount[0] == 3);
        p4.setSelected(playerCount[0] == 4);
        p2.onSelect(() -> { playerCount[0] = 2; selectOnly(playerBoxes, p2); });
        p3.onSelect(() -> { playerCount[0] = 3; selectOnly(playerBoxes, p3); });
        p4.onSelect(() -> { playerCount[0] = 4; selectOnly(playerBoxes, p4); });
        card.add(rowOf(p2, p3, p4));
        card.add(Box.createVerticalStrut(16));

        card.add(sectionRow("[ 02 ] เงินเริ่มต้น", "หน่วยเป็นบาท อย่างน้อย 500"));
        card.add(Box.createVerticalStrut(8));
        JLabel moneyLabel = new JLabel(formatMoney(money[0]), SwingConstants.CENTER);
        moneyLabel.setFont(FontUtil.getThaiFontBold(28f));
        moneyLabel.setForeground(Color.DARK_GRAY);

        ChoiceBox minus = new ChoiceBox("- 100", "");
        ChoiceBox plus = new ChoiceBox("+ 100", "");
        ChoiceBox m500 = new ChoiceBox("500", "จบเร็ว");
        ChoiceBox m1000 = new ChoiceBox("1000", "ปกติ");
        ChoiceBox m2500 = new ChoiceBox("2500", "เงินเยอะ");
        List<ChoiceBox> moneyBoxes = List.of(m500, m1000, m2500);
        markMoney(moneyBoxes, m500, m1000, m2500, money[0]);

        minus.onSelect(() -> {
            money[0] = Math.max(100, money[0] - 100);
            moneyLabel.setText(formatMoney(money[0]));
            markMoney(moneyBoxes, m500, m1000, m2500, money[0]);
        });
        plus.onSelect(() -> {
            money[0] = Math.min(5000, money[0] + 100);
            moneyLabel.setText(formatMoney(money[0]));
            markMoney(moneyBoxes, m500, m1000, m2500, money[0]);
        });
        m500.onSelect(() -> { money[0] = 500; moneyLabel.setText(formatMoney(500)); selectOnly(moneyBoxes, m500); });
        m1000.onSelect(() -> { money[0] = 1000; moneyLabel.setText(formatMoney(1000)); selectOnly(moneyBoxes, m1000); });
        m2500.onSelect(() -> { money[0] = 2500; moneyLabel.setText(formatMoney(2500)); selectOnly(moneyBoxes, m2500); });

        JPanel stepper = new JPanel(new GridLayout(1, 3, 12, 0));
        stepper.setOpaque(false);
        stepper.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        stepper.add(minus);
        stepper.add(moneyLabel);
        stepper.add(plus);
        card.add(stepper);
        card.add(Box.createVerticalStrut(8));
        card.add(rowOf(m500, m1000, m2500));
        card.add(Box.createVerticalStrut(16));

        card.add(sectionRow("[ 03 ] จำนวนเทิร์นสูงสุด", "หมดเทิร์นแล้วคิดผู้ชนะจากเงินและเมือง"));
        card.add(Box.createVerticalStrut(8));
        ChoiceBox t20 = new ChoiceBox("20 เทิร์น", "เกมสั้น");
        ChoiceBox t30 = new ChoiceBox("30 เทิร์น", "เกมปกติ");
        ChoiceBox t50 = new ChoiceBox("50 เทิร์น", "เกมยาว");
        List<ChoiceBox> turnBoxes = List.of(t20, t30, t50);
        t20.setSelected(maxTurns[0] == 20);
        t30.setSelected(maxTurns[0] == 30);
        t50.setSelected(maxTurns[0] == 50);
        t20.onSelect(() -> { maxTurns[0] = 20; selectOnly(turnBoxes, t20); });
        t30.onSelect(() -> { maxTurns[0] = 30; selectOnly(turnBoxes, t30); });
        t50.onSelect(() -> { maxTurns[0] = 50; selectOnly(turnBoxes, t50); });
        card.add(rowOf(t20, t30, t50));
        card.add(Box.createVerticalStrut(20));

        JButton btnCancel = new JButton("ยกเลิก");
        JButton btnStart = new JButton("เริ่มเล่น");
        btnCancel.setFont(FontUtil.getThaiFont(14f));
        btnStart.setFont(FontUtil.getThaiFontBold(14f));
        btnCancel.addActionListener(e -> mainUI.showScreen("MENU_SCREEN"));
        btnStart.addActionListener(e -> {
            if (money[0] < 500) {
                JLabel err = new JLabel("เงินเริ่มต้นต้องไม่น้อยกว่า 500 บาท");
                err.setFont(FontUtil.getThaiFont(14f));
                JOptionPane.showMessageDialog(this, err, "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                return;
            }
            settings.setPlayerCount(playerCount[0]);
            settings.setInitialMoney(money[0]);
            settings.setMaxTurns(maxTurns[0]);
            mainUI.startGame();
        });

        JPanel footer = new JPanel(new GridLayout(1, 2, 12, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        footer.add(btnCancel);
        footer.add(btnStart);
        card.add(footer);

        add(card, new GridBagConstraints());

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startMatch");
        getActionMap().put("startMatch", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { btnStart.doClick(); }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "backToMenu");
        getActionMap().put("backToMenu", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { mainUI.showScreen("MENU_SCREEN"); }
        });
    }

    private JPanel headerRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("ตั้งกติกาก่อนเล่น");
        title.setFont(FontUtil.getThaiFontBold(20f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("เลือกจำนวนคน เงินเริ่มต้น และจำนวนเทิร์น");
        sub.setFont(FontUtil.getThaiFont(13f));
        sub.setForeground(Color.GRAY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);
        left.add(sub);

        JLabel badge = new JLabel("กติกา");
        badge.setFont(FontUtil.getThaiFont(12f));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        row.add(left, BorderLayout.WEST);
        row.add(badge, BorderLayout.EAST);
        return row;
    }

    private JPanel sectionRow(String title, String hint) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel left = new JLabel(title);
        left.setFont(FontUtil.getThaiFontBold(14f));
        JLabel right = new JLabel(hint);
        right.setFont(FontUtil.getThaiFont(12f));
        right.setForeground(Color.GRAY);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel rowOf(JComponent a, JComponent b, JComponent c) {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.setPreferredSize(new Dimension(700, 68));
        row.add(a);
        row.add(b);
        row.add(c);
        return row;
    }

    private static String formatMoney(int amount) {
        return String.format("%,d บาท", amount);
    }

    private static void selectOnly(List<ChoiceBox> group, ChoiceBox chosen) {
        for (ChoiceBox box : group) box.setSelected(box == chosen);
    }

    private static void markMoney(List<ChoiceBox> group, ChoiceBox c500, ChoiceBox c1000, ChoiceBox c2500, int amount) {
        c500.setSelected(amount == 500);
        c1000.setSelected(amount == 1000);
        c2500.setSelected(amount == 2500);
    }

    /** กล่องตัวเลือกธรรมดา กดแล้วเปลี่ยนกรอบสีฟ้าของโปรเจกต์เดิม */
    private static class ChoiceBox extends JPanel {
        private final String title;
        private final String sub;
        private boolean selected;
        private final List<Runnable> listeners = new ArrayList<>();

        ChoiceBox(String title, String sub) {
            this.title = title;
            this.sub = sub == null ? "" : sub;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    for (Runnable listener : listeners) listener.run();
                }
            });
        }

        void onSelect(Runnable listener) { listeners.add(listener); }

        void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(selected ? SELECT_BG : Color.WHITE);
            g2.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
            g2.setColor(selected ? SELECT_LINE : Color.GRAY);
            g2.setStroke(new BasicStroke(selected ? 2f : 1f));
            g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(FontUtil.getThaiFontBold(15f));
            FontMetrics titleFm = g2.getFontMetrics();
            int titleY = sub.isEmpty()
                ? (getHeight() + titleFm.getAscent()) / 2 - 2
                : getHeight() / 2 - 2;
            String shown = title;
            g2.drawString(shown, (getWidth() - titleFm.stringWidth(shown)) / 2, titleY);

            if (!sub.isEmpty()) {
                g2.setFont(FontUtil.getThaiFont(12f));
                g2.setColor(Color.GRAY);
                FontMetrics subFm = g2.getFontMetrics();
                g2.drawString(sub, (getWidth() - subFm.stringWidth(sub)) / 2, titleY + 18);
            }
            g2.dispose();
        }
    }
}
