package model.board;

import model.entity.Player;

/**
 * (Class พ่อ / Abstract Base Class) สืบไป City กับ EventTile ต้องถูกเก็บเป็นลิสใน Board (List<Tile>)
 * Abstract Class สำหรับช่องทั้งหมดบนกระดาน   ***เขียนแบบนี้กันด้วย***
 * @author ฟิล์ม (feature/board-setup)
 */
public abstract class Tile { // Tile คือช่องในกระดานนะ
    private final int id;
    private final String name;

    public Tile(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; } // ส่งออก getId          } ให้ระบบอื่นแต่ต้องกำหนดให้คลาสลูกต้องเขียน 
    public String getName() { return name; } // ส่งออก getName }                                     Logic ใน onStep()

    // NOTICE (ถึง เดียร์): ใช้ Object player ชั่วคราวเพราะคลาส Player ยังไม่ได้สร้าง
    // ฝากเดียร์ช่วย Refactor เปลี่ยน Parameter จาก Object เป็น Player ด้้วย
    //                           |||
    //                           VVV
    public abstract void onStep(Player player);  //ใช้ abstract เพราะตอนผู้เล่นเดินตก (onStep) จะไม่เหมือนกัน แต่ชื่อกับidของคคน จะเหมือนเดิมตลอด
}