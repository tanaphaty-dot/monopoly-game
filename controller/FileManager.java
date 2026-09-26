package controller;

import java.io.*;

/**
 * คลาสจัดการการอ่าน/เขียนไฟล์เพื่อเซฟและโหลดสถานะเกมลงไฟล์ savegame.txt
 * @author พีท (feature/ui-system)
 */
public class FileManager {
    private static final String SAVE_FILE_PATH = "savegame.txt";

    /**
     * บันทึกข้อมูลข้อความสถานะเกมลงในไฟล์ savegame.txt
     */
    public static boolean saveGame(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE_PATH))) {
            writer.write(data);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
            return false;
        }
    }

    /**
     * อ่านข้อมูลสถานะเกมจากไฟล์ savegame.txt
     */
    public static String loadGame() {
        File file = new File(SAVE_FILE_PATH);
        if (!file.exists()) {
            return null;
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            System.err.println("Error loading game: " + e.getMessage());
            return null;
        }
    }
}