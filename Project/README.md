# ⚔️ Dark Abyss — Roguelike RPG

> เกม Roguelike RPG บน Android ที่พาคุณตะลุยดันเจี้ยนอันมืดมิด ต่อสู้กับศัตรูนับร้อย และสร้างตำนานของตัวเอง

---

## 📖 อธิบายโปรเจ็กต์

**Dark Abyss** คือเกมแนว **Roguelike RPG** สำหรับ Android ที่เน้นการดำเนินเนื้อเรื่องและการต่อสู้แบบ **Turn-based** (สลับเทิร์น)

ผู้เล่นเลือกอาชีพ แล้วตะลุยดันเจี้ยนชั้นแล้วชั้นเล่า โดยแต่ละชั้นจะสุ่มสร้างเหตุการณ์จากกลุ่มเหตุการณ์มากกว่า **25 ประเภท** ทั้ง:

- ⚔️ **Combat Encounter** — ต่อสู้กับมอนสเตอร์ทั่วไปหรือ Boss ทุก 10 ชั้น  
- 💰 **Treasure / Merchant Room** — ห้องสมบัติและร้านค้าอัปเกรดตัวละคร  
- 🎲 **Random Events** — เหตุการณ์สุ่มที่เปลี่ยนสถานการณ์การเล่นทุกรอบ

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + XML View System |
| **Navigation** | Navigation Compose |
| **State Management** | ViewModel + LiveData |
| **Async** | Kotlin Coroutines |
| **Storage** | DataStore (Preferences) |
| **AI** | Google Generative AI SDK |
| **UI Components** | Material 3, CardView, AppCompat |
| **Build System** | Gradle (Kotlin DSL) |
| **Min SDK** | API 30 (Android 11) |
| **Target SDK** | API 35 (Android 15) |

---

## ✨ Features

### ⚔️ ระบบการต่อสู้ (Combat System)
- ระบบเทิร์นที่กำหนดโดย **Speed** ของตัวละครและศัตรู
- **Status Effects**: Burn 🔥, Poison ☠️, Stun ⚡
- ความสามารถพิเศษเฉพาะอาชีพ (Class-specific Abilities)
- Critical Hit System ตามค่า Luck

### 🧙 ระบบอาชีพ (Character Classes) — 20+ Classes

<details>
<summary>ดูรายชื่ออาชีพทั้งหมด</summary>

| Class | Emoji | Special Ability | จุดเด่น |
|---|---|---|---|
| นักรบ | ⚔️ | Shield Bash | HP & DEF สูงสุด |
| นักเวทย์ | 🔮 | Fireball | ATK เวทย์สูง, Soul Power มหาศาล |
| โจร | 🗡️ | Critical Strike | Crit Rate สูง, หลบกับดักเก่ง |
| นักบวช | ✨ | Divine Heal | ฟื้นฟู HP, ต้านคำสาป |
| นักล่า | 🏹 | Multi-Shot | ตรวจจับกับดัก, หาทองเพิ่ม |
| อัศวินศักดิ์สิทธิ์ | 🛡️ | Holy Shield | DEF สูงสุด, รักษาตัวเองได้ |
| เนโครแมนเซอร์ | 💀 | Raise Dead | เวทย์มนต์ดำ, ดูดพลังชีวิต |
| เบอร์เซิร์กเกอร์ | 🪓 | Blood Rage | ATK มหาศาล, ยิ่งเลือดน้อยยิ่งแรง |
| กวี | 🎶 | Battle Song | บัฟทีม, โชคสูง |
| ดรูอิด | 🌿 | Nature's Wrath | พลังธรรมชาติ, โจมตี+ฟื้นฟู |
| ซามูไร | 🏮 | Quick Draw | ความเร็วและแม่นยำสูง |
| หมอผี | 🎭 | Spirit Totem | คำสาปศัตรู, วิญญาณช่วยรบ |
| นักพรต | 👊 | Iron Body | ฟื้น HP ทุกเทิร์น, แข็งขึ้นเมื่อโดนตี |
| นักเล่นแร่แปรธาตุ | ⚗️ | Double Brew | ยา 3 ขวดเริ่มต้น, ไอเทมแรงขึ้น |
| โจรสลัด | 🏴‍☠️ | Plunder | ขโมยทองจากดาเมจ |
| นินจา | 🥷 | Shadow Kill | โอกาส One-Hit Kill, หลบสูง |
| เทมพลาร์ | ⚜️ | Holy Strike | ดาเมจ +50% ต่อบอส |
| จอมเวทย์ | 🌑 | Arcane Surge | ทักษะทุกอย่าง +30% ดาเมจ |
| ช่างประดิษฐ์ | 🔧 | Overclock | ATK+DEF ต่อชิ้นอุปกรณ์ที่สวมใส่ |
| กลาดิเอเตอร์ | 🏟️ | Arena Rush | ATK +1 ถาวรทุกชัยชนะ |

</details>

### 👾 ระบบศัตรู (Enemy System)
- สุ่มสร้างศัตรูตามชั้นและระดับความยาก
- **Boss Fight** ทุก 10 ชั้น พร้อม mechanics พิเศษ

### 📈 ระบบ Progression
- เก็บ **EXP → Level Up** พร้อมการสุ่มเพิ่ม Stat (ATK / DEF / HP / Luck)
- ระบบ **Soul Points** — พลังงานสำหรับใช้ทักษะพิเศษ
- **Inventory System** — เก็บและจัดการไอเทมจากศัตรูและห้องสมบัติ
- **Relic System** — ของสะสมพิเศษที่ให้ Passive Buff

### 🎲 Procedural Generation
- ใช้ **Seed-based Room Generation** ทุกรอบการเล่นมีเส้นทางแตกต่างกัน แต่ยังคงความสามารถในการควบคุมได้

### 🏆 Achievements
- ระบบ Achievement หลายสิบรายการ (เช่น First Blood, นักสู้ 10 ดาว ฯลฯ)
- ติดตามสถิติการเล่นสะสมทุก Run

### ⚙️ Settings ที่ครอบคลุม
- ระดับความยาก: Easy / Normal / Hard / Nightmare
- ควบคุมเสียงเพลงและเสียงเอฟเฟกต์
- ปรับขนาดตัวอักษรและความเร็ว Animation
- Color Theme: Dark / Light / AMOLED / Cyberpunk
- Auto-Save

---

## 🗺️ หน้าจอในแอป (Screens)

```
Main Menu
├── เริ่มเผชิญไพ (New Game)   → เลือกอาชีพ → ดันเจี้ยน
├── เล่นต่อ (Continue)        → โหลด Save → ดันเจี้ยน
├── ตั้งค่า (Settings)
├── Achievements
└── วิธีเล่น (How to Play)
```

---

## 🚀 วิธีติดตั้งและรันโปรเจ็กต์

```bash
# Clone repository
git clone https://github.com/lovernashiga-code/cp213_158LearnAndroid.git

# เปิดด้วย Android Studio
# File → Open → เลือกโฟลเดอร์ Project/Project

# Build & Run บน Emulator หรือ Device จริง
# Run → Run 'app'
```

**Requirements:**
- Android Studio Hedgehog หรือใหม่กว่า
- JDK 11+
- Android SDK API 30+

---

## 📂 โครงสร้างโปรเจ็กต์

```
Project/
├── app/src/main/
├── java/com/example/game/
├── RoguelikeMainActivity.kt   # Activity หลัก + Game Logic
├── CombatSystem.kt            # ระบบต่อสู้
│── EnemyGenerator.kt          # สุ่มสร้างศัตรู
├── InventorySystem.kt         # ระบบ Inventory
├── SkillTreeSystem.kt         # Skill Tree
├── MetaProgressionSystem.kt   # ระบบ Progression ข้าม Run
├── RelicSystem.kt             # ระบบของสะสม
├── ModernUI.kt                # UI Components
```

---

## 📱 Wireframe / UI Desig
<img width="1414" height="2000" alt="ดีไซน์ที่ยังไม่ได้ตั้งชื่อ" src="https://github.com/user-attachments/assets/191e87f4-f6c2-4e17-8638-084fefa5aa6a" />
