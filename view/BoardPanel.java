package view;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import controller.GameController;
import model.board.CityTile;
import model.board.Tile;
import model.entity.Player;

/**
 * วาดกระดานแบบ 24 ช่องรอบขอบ
 * @author พีท (feature/ui-system)
 */
public class BoardPanel extends JPanel {
    private final GameController controller;
    private final Color[] playerColors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};

    public BoardPanel(GameController controller) {
        this.controller = controller;
        setBackground(new Color(230, 235, 240));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int tileSize = Math.min(width, height) / 8; // คำนวณขนาดช่องตามขนาดหน้าจอ

        // ดึงพิกัด (X, Y) ของทั้ง 24 ช่องรอบขอบกระดาน
        Point[] tilePositions = getTilePositions(width, height, tileSize);

        // 1. วาดช่องกระดานทั้ง 24 ช่อง
        for (int i = 0; i < 24; i++) {
            Point p = tilePositions[i];
            Tile tile = controller.getBoard().getTile(i);

            // วาดกรอบและพื้นหลังช่อง
            g2.setColor(Color.WHITE);
            g2.fillRect(p.x, p.y, tileSize, tileSize);
            g2.setColor(Color.BLACK);
            g2.drawRect(p.x, p.y, tileSize, tileSize);

            // ถ้าเป็นช่องเมืองและมีเจ้าของ ให้วาด แถบสีเจ้าของเมือง ไว้ด้านบนช่อง
            if (tile instanceof CityTile) {
                CityTile city = (CityTile) tile;
                if (city.getOwner() != null) {
                    g2.setColor(playerColors[city.getOwner().getId() % playerColors.length]);
                    g2.fillRect(p.x + 2, p.y + 2, tileSize - 4, 8);
                }
            }

            // วาดชื่อช่อง
            g2.setFont(FontUtil.getThaiFont(10f));
            g2.setColor(Color.DARK_GRAY);
            String name = tile != null ? tile.getName() : "Tile " + i;
            g2.drawString(name, p.x + 4, p.y + 22);
        }

        // 2. วาดตัวผู้เล่น (วงกลมสี) บนกระดาน
        List<Player> players = controller.getPlayers();
        for (Player player : players) {
            if (player.isBankrupt()) continue; // ข้ามคนล้มละลาย

            int tileId = player.getCurrentTileId();
            Point p = tilePositions[tileId];
            int pId = player.getId();

            // คำนวณ Offset ไม่ให้ตัวผู้เล่นทับกันสนิทกรณียืนช่องเดียวกัน
            int offsetX = (pId % 2) * (tileSize / 2) + 6;
            int offsetY = (pId / 2) * (tileSize / 2) + 26;

            g2.setColor(playerColors[pId % playerColors.length]);
            g2.fillOval(p.x + offsetX, p.y + offsetY, 14, 14);
            g2.setColor(Color.BLACK);
            g2.drawOval(p.x + offsetX, p.y + offsetY, 14, 14);
        }
    }

    // คำนวณพิกัด X, Y ของช่องกระดานวนรอบขอบเป็นสี่เหลี่ยม (ล่าง -> ซ้าย -> บน -> ขวา)
    private Point[] getTilePositions(int w, int h, int size) {
        Point[] pos = new Point[24];
        int margin = 20;

        // ด้านล่าง (ช่อง 0 -> 6)
        for (int i = 0; i <= 6; i++) pos[i] = new Point(margin + (6 - i) * size, margin + 6 * size);
        // ด้านซ้าย (ช่อง 7 -> 11)
        for (int i = 1; i <= 5; i++) pos[6 + i] = new Point(margin, margin + (6 - i) * size);
        // ด้านบน (ช่อง 12 -> 18)
        for (int i = 0; i <= 6; i++) pos[12 + i] = new Point(margin + i * size, margin);
        // ด้านขวา (ช่อง 19 -> 23)
        for (int i = 1; i <= 5; i++) pos[18 + i] = new Point(margin + 6 * size, margin + i * size);

        return pos;
    }
}