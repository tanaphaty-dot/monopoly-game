package view;

import javax.swing.*;
import java.awt.*;
import controller.GameController;
import model.entity.PendingAction;
import model.entity.Player;

/**
 * ปุ่มล่างจอ
 * ทอย / จั่วการ์ด / จ่ายประกัน / จบเทิร์น
 * ซื้อหรือบุก / ปฏิเสธ / บันทึก / ออก
 *
 * @author พีท (feature/ui-system)
 */
public class ActionControlPanel extends JPanel {
    private final GameController controller;
    private final MainUI mainUI;

    private final JLabel hintLabel;
    private final JButton btnRoll;
    private final JButton btnDraw;
    private final JButton btnBail;
    private final JButton btnConfirm;
    private final JButton btnRefuse;
    private final JButton btnEndTurn;
    private final JButton btnSave;
    private final JButton btnExit;

    public ActionControlPanel(GameController controller, MainUI mainUI) {
        this.controller = controller;
        this.mainUI = mainUI;

        setLayout(new BorderLayout(0, 6));

        hintLabel = new JLabel(" ");
        hintLabel.setFont(FontUtil.getThaiFont(13f));
        add(hintLabel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(2, 4, 6, 6));
        btnRoll = new JButton("ทอยลูกเต๋า");
        btnDraw = new JButton("จั่วการ์ด");
        btnBail = new JButton("จ่ายประกัน " + GameController.JAIL_BAIL);
        btnEndTurn = new JButton("จบเทิร์น");
        btnConfirm = new JButton("ตกลง");
        btnRefuse = new JButton("ปฏิเสธ");
        btnSave = new JButton("บันทึกเกม");
        btnExit = new JButton("ออกเกม");

        Font font = FontUtil.getThaiFont(12f);
        for (JButton button : new JButton[]{
            btnRoll, btnDraw, btnBail, btnEndTurn, btnConfirm, btnRefuse, btnSave, btnExit
        }) {
            button.setFont(font);
            buttons.add(button);
        }
        add(buttons, BorderLayout.CENTER);

        btnRoll.addActionListener(e -> controller.rollDice());
        btnDraw.addActionListener(e -> controller.drawPendingCard());
        btnBail.addActionListener(e -> controller.payBail());
        btnConfirm.addActionListener(e -> controller.processPendingAction(true));
        btnRefuse.addActionListener(e -> controller.processPendingAction(false));
        btnEndTurn.addActionListener(e -> controller.endTurn());
        btnSave.addActionListener(e -> controller.saveGame());
        btnExit.addActionListener(e -> confirmExit());
    }

    private void confirmExit() {
        String message = controller.isGameOver()
            ? "<html><font face='Tahoma' size='4'>เกมจบแล้ว จะกลับไปหน้าเมนูหลักหรือไม่?</font></html>"
            : "<html><font face='Tahoma' size='4'>ระบบจะบันทึกเกม ณ จุดนี้ไว้ก่อนออก แล้วกลับไปหน้าเมนูหลัก ยืนยันหรือไม่?</font></html>";
        int confirm = JOptionPane.showConfirmDialog(
            this, message, "ออกเกม / บันทึกและพัก", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            mainUI.pauseAndReturnToMenu();
        }
    }

    public void updateControlState() {
        boolean hasRolled = controller.hasRolledThisTurn();
        PendingAction pending = controller.getPendingAction();
        boolean isGameOver = controller.isGameOver();
        Player current = controller.getCurrentPlayer();

        if (isGameOver) {
            btnRoll.setEnabled(false);
            btnDraw.setEnabled(false);
            btnBail.setEnabled(false);
            btnConfirm.setEnabled(false);
            btnRefuse.setEnabled(false);
            btnEndTurn.setEnabled(false);
            btnSave.setEnabled(false);
            Player winner = controller.getWinner();
            hintLabel.setText(winner == null ? "เกมจบแล้ว" : "เกมจบแล้ว ผู้ชนะคือ " + winner.getName());
            return;
        }
        btnSave.setEnabled(true);

        btnRoll.setEnabled(!hasRolled);
        btnDraw.setEnabled(pending == PendingAction.DRAW_CARD);
        btnBail.setEnabled(current.isInJail() && !hasRolled);

        boolean chooseCity = pending == PendingAction.BUY_CITY
            || pending == PendingAction.SIEGE_OR_PAY;
        btnConfirm.setEnabled(chooseCity);
        btnRefuse.setEnabled(chooseCity);
        btnEndTurn.setEnabled(hasRolled && pending == PendingAction.NONE);

        if (pending == PendingAction.BUY_CITY) {
            btnConfirm.setText("ซื้อเมือง");
            btnRefuse.setText("ไม่ซื้อ");
            hintLabel.setText("เมืองว่าง: กดซื้อเมือง หรือไม่ซื้อ");
        } else if (pending == PendingAction.SIEGE_OR_PAY) {
            btnConfirm.setText("บุกยึด");
            btnRefuse.setText("จ่ายค่าเช่า");
            hintLabel.setText("เมืองคนอื่น: กดบุกยึด หรือจ่ายค่าเช่า");
        } else if (pending == PendingAction.DRAW_CARD) {
            btnConfirm.setText("ตกลง");
            btnRefuse.setText("ปฏิเสธ");
            hintLabel.setText("ตกช่องการ์ด: กดจั่วการ์ด (ข้ามไม่ได้)");
        } else if (current.isInJail() && !hasRolled) {
            btnConfirm.setText("ตกลง");
            btnRefuse.setText("ปฏิเสธ");
            hintLabel.setText("ติดคุก: ทอยได้ " + GameController.JAIL_ESCAPE_ROLL
                + " ขึ้นไปเพื่อออก หรือจ่ายประกัน " + GameController.JAIL_BAIL);
        } else if (!hasRolled) {
            btnConfirm.setText("ตกลง");
            btnRefuse.setText("ปฏิเสธ");
            hintLabel.setText("กดทอยลูกเต๋า");
        } else {
            btnConfirm.setText("ตกลง");
            btnRefuse.setText("ปฏิเสธ");
            hintLabel.setText("จัดการครบแล้ว กดจบเทิร์น");
        }
    }
}
