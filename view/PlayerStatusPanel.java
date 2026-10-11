package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import controller.GameController;
import model.board.CityTile;
import model.board.EventTile;
import model.board.Tile;
import model.entity.Player;

/**
 * แผงซ้าย: บอกว่าใครอยู่ช่องไหน มีเงินเท่าไร และถือสถานะอะไรอยู่
 * สีตัวอักษรยังใช้แบบเดิม (ฟ้า = ตาปัจจุบัน, แดง = ล้มละลาย)
 *
 * @author พีท (feature/ui-system)
 */
public class PlayerStatusPanel extends JPanel {
    /** สีเดียวกับวงกลมตัวเดินบนกระดาน */
    private static final Color[] PLAYER_COLORS = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};

    private final GameController controller;
    private final JLabel nowLabel;
    private final JPanel listContainer;

    public PlayerStatusPanel(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(0, 8));

        TitledBorder border = BorderFactory.createTitledBorder("สถานะผู้เล่น");
        border.setTitleFont(FontUtil.getThaiFontBold(14f));
        setBorder(border);

        nowLabel = new JLabel(" ");
        nowLabel.setFont(FontUtil.getThaiFontBold(13f));
        nowLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        add(nowLabel, BorderLayout.NORTH);

        listContainer = new JPanel(new GridLayout(0, 1, 0, 8));
        listContainer.setOpaque(false);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(listContainer, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(topWrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JLabel legend = new JLabel("<html>Special tiles<br>"
            + "0 Start (+" + EventTile.PASS_START_BONUS + ")<br>"
            + "5 Camp · 11 Watch<br>"
            + "14 Jail · 17 Train<br>"
            + "Event = draw a card</html>");
        legend.setFont(FontUtil.getThaiFont(12f));
        legend.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
        add(legend, BorderLayout.SOUTH);
    }

    public void updateStatus() {
        List<Player> players = controller.getPlayers();
        Player current = controller.getCurrentPlayer();

        nowLabel.setText("ตาของ " + current.getName() + "  |  " + positionText(current));

        listContainer.removeAll();
        for (Player p : players) {
            listContainer.add(buildPlayerCard(p, p == current));
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel buildPlayerCard(Player p, boolean isCurrent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);

        Color borderColor = Color.LIGHT_GRAY;
        if (isCurrent) borderColor = new Color(0, 102, 204);
        if (p.isBankrupt()) borderColor = Color.RED;
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        String tag = "";
        if (isCurrent) tag = "  [ตาปัจจุบัน]";
        if (p.isBankrupt()) tag = "  (ล้มละลาย)";

        JLabel name = new JLabel("●  " + p.getName() + tag);
        name.setFont(isCurrent ? FontUtil.getThaiFontBold(13f) : FontUtil.getThaiFont(13f));
        if (p.isBankrupt()) name.setForeground(Color.RED);
        else if (isCurrent) name.setForeground(new Color(0, 102, 204));
        else name.setForeground(PLAYER_COLORS[p.getId() % PLAYER_COLORS.length]);
        card.add(name);

        card.add(line(positionText(p)));
        card.add(line("เงิน: " + p.getMoney() + " บาท"));
        card.add(line("เมือง: " + countCities(p) + " เมือง"));
        card.add(line("สถานะ: " + statusText(p)));
        return card;
    }

    private JLabel line(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontUtil.getThaiFont(12f));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private String positionText(Player p) {
        Tile tile = controller.getBoard().getTile(p.getCurrentTileId());
        return "ตำแหน่ง: ช่อง " + p.getCurrentTileId() + " (" + tile.getName() + ")";
    }

    private int countCities(Player p) {
        int owned = 0;
        for (Tile tile : controller.getBoard().getAllTiles()) {
            if (tile instanceof CityTile && ((CityTile) tile).getOwner() == p) owned++;
        }
        return owned;
    }

    private String statusText(Player p) {
        if (p.isBankrupt()) return "ออกจากเกมแล้ว";

        List<String> tags = new ArrayList<>();
        if (p.isInJail()) {
            tags.add("ติดคุก " + p.getJailTurns() + "/" + GameController.JAIL_MAX_TRIES);
        }
        if (p.hasCampDefense()) tags.add("เกราะค่าย");
        if (p.hasTrainingBoost()) tags.add("ซ้อมโจมตี");
        if (p.hasSiegeDiscount()) tags.add("ส่วนลดบุก");
        if (!p.getCards().isEmpty()) tags.add("การ์ด " + p.getCards().size() + " ใบ");
        if (tags.isEmpty()) return "ปกติ";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(tags.get(i));
        }
        return sb.toString();
    }
}
