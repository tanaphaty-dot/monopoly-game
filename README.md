##  สมาชิกในกลุ่มและการแบ่งงาน 

การแบ่งขอบเขตความรับผิดชอบของสมาชิกทั้ง 4 คน

| สมาชิก | ฟังก์ชันการทำงาน & หน้า GUI ที่รับผิดชอบ | ออกแบบ Class Diagram และพัฒนา Class |
| :--- | :--- | :--- |
| **ฟิล์ม** | **ระบบกระดาน & อาณาเขต**<br>• พัฒนาอัลกอริทึมการเชื่อมต่อพื้นที่ <br>• ออกแบบหน้า UI แสดงผลกระดาน 24 ช่องและโบนัสอาณาเขตด้วย Figma/Swing | `Board`<br>`Tile`<br>`CityTile`<br>`EventTile`<br>`TileFactory` |
| **เดียร์** | **ระบบผู้เล่น & การเคลื่อนที่**<br>• พัฒนาระบบสถานะผู้เล่น สินทรัพย์ และการสุ่มเต๋า<br>• ออกแบบ UI แสดงข้อมูลผู้เล่น (Player Panel) และช่องสัมภาระ (Inventory) | `Player`<br>`Dice`<br>`Card`<br>`Inventory` |
| **เก๋า** | **ระบบการต่อสู้ & เหตุการณ์**<br>• พัฒนาสูตรคำนวณพลังตี/เกราะเมือง และผลลัพธ์การรบ<br>• ออกแบบ UI หน้าจอ Pop-up ช่วงตัดสินใจตีเมืองและผลลัพธ์การต่อสู้ | `SiegeSystem`<br>`BattleResult`<br>`EventCard` |
| **พีท** | **ตัวควบคุมเกม & หน้าจอหลัก**<br>• พัฒนาระบบ Turn Loop, State Management และสลับผู้เล่น (Hotseat)<br>• ออกแบบ Main GUI Framework และหน้าจอเมนูหลัก (Main Menu / Game Over) | `GameController`<br>`GameObserver`<br>`MainUI` |

## Monopoly & Zone Rush

เกมเศรษฐีแนวกลยุทธ์เชิงสงคราม (Tactical Strategy Board Game) พัฒนาด้วยภาษา **Java** สำหรับโปรเจกต์

---

## เกี่ยวกับโปรเจกต์ (Project Concept)

โปรเจกต์นี้ถูกออกแบบขึ้นเพื่อแก้ปัญหา (Pain Points) ของเกมเศรษฐีแบบดั้งเดิมที่พึ่งพาดวง มากเกินไป และมักจะมีช่วงท้ายเกมที่ยืดเยื้อน่าเบื่อ โดยการเพิ่ม 2 กลไกหลัก:

1. **ระบบตีเมือง (Siege System):** เมื่อเดินตกเมืองผู้อื่น ผู้เล่นสามารถเลือกได้ว่าจะ "จ่ายค่าผ่านทาง" หรือ "ประกาศสงคราม" เพื่อแย่งชิงเมือง
2. **ระบบขยายอาณาเขต (Territory Expansion):** ยิ่งครอบครองเมืองที่อยู่ติดกัน (Adjacency Tiles) จะยิ่งเพิ่มค่าผ่านทางและเพิ่มพลังป้องกัน (Defense Power) ให้กับอาณาจักร

---

##  Tech Stack & Environment

* **Language:** Java 17+
* **GUI Framework:** Java Swing (Standard Java Library)
* **UI Design Tool:** Figma
* **Version Control:** Git / GitHub
* **IDE Supported:** IntelliJ IDEA / Eclipse / NetBeans

---

##  แผนผังกระดาน 24 ช่อง (Board Layout)

กระดานแบ่งออกเป็น 4 โซนอาณาเขต (สีละ 3 เมือง) พร้อมจุดยุทธศาสตร์และช่องเหตุการณ์:
* **Zone Green (ช่อง 02, 03, 05):** โซนป่า - ยึดง่าย ราคาถูก เน้นเปิดเกม
* **Zone Blue (ช่อง 07, 09, 10):** โซนน้ำ - ค่าผ่านทางระดับกลาง
* **Zone Yellow (ช่อง 13, 14, 16):** โซนเหมืองเหล็ก - ค่าผ่านทางและพลังป้องกันสูง
* **Zone Red (ช่อง 19, 20, 22):** โซนเมืองหลวง - โซนยุทธศาสตร์แพงที่สุดในเกม
* **Special Tiles:** จุดเริ่มต้น (01), ค่ายพักพล (06), หอคอยสังเกตการณ์ (12), คุกกี้/คุกสงคราม (15), สนามซ้อมรบ (18) และช่องการ์ดแผนการสุ่มเหตุการณ์

---

##  โครงสร้างสถาปัตยกรรม (Design Patterns)

โปรเจกต์นี้มีการประยุกต์ใช้ **Software Design Patterns** เพื่อโครงสร้างโค้ดที่เป็นระเบียบตามหลัก OOP:

* **Factory Pattern (`TileFactory`):** ใช้สร้างวัตถุประเภท `Tile` บนกระดาน
* **Observer Pattern (`GameObserver`):** ใช้ในการเชื่อมต่อระหว่าง Game State Controller กับหน้าจอ Swing UI เพื่ออัปเดตข้อมูลแบบ Real-time
* **State Pattern (`TurnState`):** จัดการลำดับขั้นตอนในแต่ละเทิร์น (`START_TURN`, `ROLL_DICE`, `ACTION`, `SIEGE`, `END_TURN`)

---

## 📂 โครงสร้างโปรเจกต์ (Directory Structure)

```text
src/
├── controller/         # ควบคุม Logic การเล่นและ State ของเกม (GameController)
├── model/
│   ├── board/          # จัดการกระดาน, ช่อง (Tile, Board, CityTile, EventTile)
│   ├── entity/         # จัดการข้อมูลผู้เล่น และไอเทม (Player, Dice, Card)
│   └── combat/         # ระบบคำนวณการตีเมืองและผลลัพธ์ (SiegeSystem, BattleResult)
├── view/               # หน้าจอและส่วนประกอบ GUI (MainUI, BoardPanel, PlayerPanel)
├── pattern/            # Classes/Interfaces สื่อกลางสำหรับ Factory, Observer, State
└── Main.java           # จุดเริ่มต้นการทำงานของโปรแกรม (Entry Point)
