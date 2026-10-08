package controller;

import java.io.*;

/**
 * จัดการการอ่านและบันทึกไฟล์สถานะเกม
 * @author พีท (feature/ui-system)
 */
public class FileManager {
    private static final String DEFAULT_FILE = "savegame.txt";

    public static void saveGame(String data, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveGame(String data) {
        saveGame(data, DEFAULT_FILE);
    }

    public static String loadGame(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return null;

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String loadGame() {
        return loadGame(DEFAULT_FILE);
    }

    // เพิ่มใหม่: เช็กว่ามีไฟล์เซฟอยู่มั้ย ใช้เปิด/ปิดปุ่ม "ดำเนินเกมต่อ" ในเมนูหลัก
    public static boolean saveExists(String filePath) {
        return new File(filePath).exists();
    }

    public static boolean saveExists() {
        return saveExists(DEFAULT_FILE);
    }
}