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

        drawCenter(g2, width, height, tileSize);
    }

    /** ลูกเต๋าตรงกลางช่องว่างของกระดาน ใช้สีขาวดำแบบช่องเดิม */
    private void drawCenter(Graphics2D g2, int width, int height, int tileSize) {
        int margin = 20;
        int x = margin + tileSize;
        int y = margin + tileSize;
        int w = tileSize * 5;
        int h = tileSize * 5;
        if (w < 40 || h < 40) return;

        // TODO(พีท): ตอนนี้ GameController ยังไม่มี getLastRoll() ใส่ 0 ไว้ก่อนจะได้ไม่แดง
        // พีทเพิ่มเมธอด getLastRoll() แล้วเปลี่ยนบรรทัดล่างเป็น: int roll = controller.getLastRoll();
        int roll = 0;
        int die = Math.max(48, Math.min(110, Math.min(w, h) / 3));
        int dieX = x + (w - die) / 2;
        int dieY = y + (h - die) / 2 - 16;
        drawDie(g2, dieX, dieY, die, roll);

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(FontUtil.getThaiFontBold(18f));
        String label = roll > 0 ? "ทอยได้ " + roll : "ทอยลูกเต๋า";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, dieY + die + 28);

        if (controller.isGameOver()) {
            Player winner = controller.getWinner();
            String line = winner == null ? "เกมจบแล้ว" : "ผู้ชนะ: " + winner.getName();
            g2.setFont(FontUtil.getThaiFont(14f));
            fm = g2.getFontMetrics();
            g2.drawString(line, x + (w - fm.stringWidth(line)) / 2, dieY + die + 50);
        }
    }

    private void drawDie(Graphics2D g2, int x, int y, int size, int roll) {
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, size, size, 16, 16);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, size, size, 16, 16);
        if (roll < 1 || roll > 6) return;

        int pip = Math.max(8, size / 7);
        int margin = size / 4;
        int[] xs = { x + margin, x + size / 2, x + size - margin };
        int[] ys = { y + margin, y + size / 2, y + size - margin };
        boolean[][] dots = pipMap(roll);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (!dots[row][col]) continue;
                g2.fillOval(xs[col] - pip / 2, ys[row] - pip / 2, pip, pip);
            }
        }
    }

    private static boolean[][] pipMap(int n) {
        boolean[][] map = new boolean[3][3];
        switch (n) {
            case 1: map[1][1] = true; break;
            case 2: map[0][0] = true; map[2][2] = true; break;
            case 3: map[0][0] = true; map[1][1] = true; map[2][2] = true; break;
            case 4: map[0][0] = true; map[0][2] = true; map[2][0] = true; map[2][2] = true; break;
            case 5:
                map[0][0] = true; map[0][2] = true; map[1][1] = true;
                map[2][0] = true; map[2][2] = true;
                break;
            default:
                map[0][0] = true; map[1][0] = true; map[2][0] = true;
                map[0][2] = true; map[1][2] = true; map[2][2] = true;
                break;
        }
        return map;
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