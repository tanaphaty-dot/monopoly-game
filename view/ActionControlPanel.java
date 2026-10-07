package view;

import javax.swing.*;
import java.awt.*;
import controller.GameController;
import model.entity.PendingAction;

/**
 * รวมปุ่มทอยเต๋า ปุ่มยืนยัน/ปฏิเสธแอ็กชัน และปุ่มเปลี่ยนเทิร์น
 * @author พีท (feature/ui-system)
 */
public class ActionControlPanel extends JPanel {
    private final GameController controller;
    private final MainUI mainUI;

    private final JButton btnRoll;
    private final JButton btnConfirm;
    private final JButton btnRefuse;
    private final JButton btnEndTurn;
    private final JButton btnSave;
    private final JButton btnExit;

    public ActionControlPanel(GameController controller, MainUI mainUI) {
        this.controller = controller;
        this.mainUI = mainUI;

        setLayout(new GridLayout(3, 2, 6, 6)); // จัดวางปุ่มเป็นตาราง 3 แถว 2 คอลัมน์

        // 1. สร้างปุ่มคำสั่งต่างๆ
        btnRoll = new JButton("ทอยลูกเต๋า");
        btnConfirm = new JButton("ตกลง / บุกยึด");
        btnRefuse = new JButton("ปฏิเสธ / จ่ายค่าเช่า");
        btnEndTurn = new JButton("จบเทิร์น");
        btnSave = new JButton("บันทึกเกม");
        btnExit = new JButton("ออกเกม (Pause & Save)");

        // 2. ตั้งค่า Font ภาษาไทยให้ปุ่มกด
        Font font = FontUtil.getThaiFont(12f);
        btnRoll.setFont(font);
        btnConfirm.setFont(font);
        btnRefuse.setFont(font);
        btnEndTurn.setFont(font);
        btnSave.setFont(font);
        btnExit.setFont(font);

        // 3. เชื่อม Event ปุ่มกดเข้ากับ GameController
        btnRoll.addActionListener(e -> controller.rollDice());
        btnConfirm.addActionListener(e -> controller.processPendingAction(true));
        btnRefuse.addActionListener(e -> controller.processPendingAction(false));
        btnEndTurn.addActionListener(e -> controller.endTurn());
        btnSave.addActionListener(e -> controller.saveGame());

        // ปุ่มออกเกม: เซฟสถานะแล้วพักกลับไปหน้าเมนูหลัก
        btnExit.addActionListener(e -> {
        // ห่อข้อความด้วย HTML และระบุ Font 'Tahoma' เพื่อป้องกันภาษาไทยกลายเป็นกล่องสี่เหลี่ยม
        String message = "<html><font face='Tahoma' size='4'>ระบบจะบันทึกเกม ณ จุดนี้ไว้ก่อนออก แล้วกลับไปหน้าเมนูหลัก ยืนยันหรือไม่?</font></html>";
        int confirm = JOptionPane.showConfirmDialog(this,message,"ออกเกม / บันทึกและพัก",JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
        mainUI.pauseAndReturnToMenu();
        }
        });

        // 4. เพิ่มปุ่มลงใน Panel
        add(btnRoll);
        add(btnEndTurn);
        add(btnConfirm);
        add(btnRefuse);
        add(btnSave);
        add(btnExit);
    }

    // เปิด-ปิดการใช้งานปุ่มกดให้สอดคล้องกับสถานะของเกม
    public void updateControlState() {
        boolean hasRolled = controller.hasRolledThisTurn();
        PendingAction pending = controller.getPendingAction();
        boolean isGameOver = controller.isGameOver();

        // ถ้าเกมจบแล้ว ปิดปุ่มควบคุมหลักทั้งหมด
        if (isGameOver) {
            btnRoll.setEnabled(false);
            btnConfirm.setEnabled(false);
            btnRefuse.setEnabled(false);
            btnEndTurn.setEnabled(false);
            return;
        }

        // ปุ่มทอยเต๋า: เปิดเฉพาะตอนยังไม่ได้ทอยในเทิร์นนั้น
        btnRoll.setEnabled(!hasRolled);

        // ปุ่มแอ็กชัน: เปิดเมื่อมี Action รอยืนยัน (เช่น ตกเมือง)
        boolean hasAction = (pending != PendingAction.NONE);
        btnConfirm.setEnabled(hasAction);
        btnRefuse.setEnabled(hasAction);

        // ปุ่มจบเทิร์น: เปิดเมื่อทอยเต๋าแล้วและจัดการ Action เรียบร้อยแล้วเท่านั้น
        btnEndTurn.setEnabled(hasRolled && !hasAction);
    }
}