package model.board;

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

    public int getId() { return id; }
    public String getName() { return name; }
}