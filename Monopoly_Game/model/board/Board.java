package model.board;

import java.util.*;

import model.entity.Player;

/**
 * (คลาสจัดการกระดานหลัก)
 * 
 * จัดการกระดาน 24 ช่อง การเชื่อมต่อโหนด และอัลกอริทึม BFS อาณาเขต
 * การเชือมต่อโหนดคือ กระดาน 24 ช่องไม่ได้ถูกมองเป็นเพียง(Array) แบบมิติเดียวธรรมดา แต่มองเป็น โครงสร้างข้อมูลแบบกราฟนะจ้ะ
 * 
 * Breadth-First Search (BFS) อัลกอริทึมการท่องไปในกราฟแบบ แนวกว้างโดยเริ่มจากโหนดจุดเริ่มต้น 
 * แล้วขยายค้นหาโหนดรอบข้าง ทีละระดับชั้น โดยอาศัย คิว (Queue) และ เซตเก็บโหนดที่เคยไปแล้ว 
 * ใช้เพราะถ้้ามันจะเจอช่องเจ้าของเชื่อมกันในระบบจะให้พลังป้องกันสูงขึ้น
 * 
 * @author ฟิล์ม (feature/board-setup)
 */
public class Board {
    private final List<Tile> tiles = new ArrayList<>(); // Tile ที่บอกว่าเก็บเป็นลิส
    private final Map<Integer, List<Integer>> adjacencyMap = new HashMap<>();

    public Board() {
        initBoard();
    }

    private void initBoard() {
        // สร้างกระดาน 24 ช่อง (0-23) (TileFactory)
        for (int i = 0; i < 24; i++) {
            if (i == 1 || i == 2 || i == 4) {
                tiles.add(TileFactory.createCityTile(i, "Forest " + i, 100, 20, "Green"));
            } else if (i == 6 || i == 8 || i == 9) {
                tiles.add(TileFactory.createCityTile(i, "Water " + i, 200, 40, "Blue"));
            } else if (i == 12 || i == 13 || i == 15) {
                tiles.add(TileFactory.createCityTile(i, "Mine " + i, 300, 60, "Yellow"));
            } else if (i == 18 || i == 19 || i == 21) {
                tiles.add(TileFactory.createCityTile(i, "Capital " + i, 400, 80, "Red"));
            } else {
                tiles.add(TileFactory.createEventTile(i, "Event " + i, "CHANCE"));
            }

            adjacencyMap.put(i, new ArrayList<>());
        }

        // เชื่อมโหนดเป็นวงกลม Graph (Next & Prev)
        for (int i = 0; i < 24; i++) {
            int next = (i + 1) % 24;
            int prev = (i - 1 + 24) % 24;
            adjacencyMap.get(i).add(next);
            adjacencyMap.get(i).add(prev);
        }
    }

    public Tile getTile(int index) {
        return tiles.get(index);
    }

    public List<Tile> getAllTiles() {
        return tiles;
    }

    // NOTICE (ถึง เก๋า & เดียร์): อัลกอริทึม BFS สำหรับคำนวณจำนวนเมืองที่เชื่อมต่อกัน
    // ใช้ Object targetOwner ชั่วคราว ฝากเดียร์กับเก๋าช่วยแก้ เป็น Player targetOwner

    //รับ ID ช่องที่ตก และ ผู้เล่นเจ้าของเมือง
    public int calculateConnectedTerritory(int startTileId, Player targetOwner) {
        if (targetOwner == null) return 0;
        
        Tile startTile = tiles.get(startTileId);
        if (!(startTile instanceof CityTile) || ((CityTile) startTile).getOwner() != targetOwner) {
            return 0;
        }// instanceof คือไว้ตรวจว่า startTile มาจากคลาส CityTile เป็นคลาสลูกของ CityTile มั้ย

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.add(startTileId);
        visited.add(startTileId);

        int connectedCount = 0;

        while (!queue.isEmpty()) {
            int currentId = queue.poll();
            connectedCount++;

            for (int neighborId : adjacencyMap.getOrDefault(currentId, Collections.emptyList())) {
                if (!visited.contains(neighborId)) {
                    Tile neighborTile = tiles.get(neighborId);
                    if (neighborTile instanceof CityTile) {
                        CityTile city = (CityTile) neighborTile;

                        // ตรวจสอบว่าเป็นเจ้าของเดียวกันมั้ย
                        if (city.getOwner() == targetOwner) {
                            visited.add(neighborId);
                            queue.add(neighborId);
                        }
                    }
                }
            }
        }
        return connectedCount;
    }
}