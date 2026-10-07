package view;

import java.awt.Font;

/**
 * จัดการฟอนต์ภาษาไทยสำหรับ Java Swing
 * @author พีท (feature/ui-system)
 */
public class FontUtil {
    // กำหนด Font ภาษาไทยมาตรฐานของระบบ
    private static final String DEFAULT_FONT_NAME = "Tahoma";

    // ดึง Font ตัวปกติ (PLAIN) ตามขนาดที่ต้องการ
    public static Font getThaiFont(float size) {
        return new Font(DEFAULT_FONT_NAME, Font.PLAIN, (int) size);
    }

    // ดึง Font ตัวหนา (BOLD) ตามขนาดที่ต้องการ
    public static Font getThaiFontBold(float size) {
        return new Font(DEFAULT_FONT_NAME, Font.BOLD, (int) size);
    }
}