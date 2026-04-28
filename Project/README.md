1. อธิบายโปรเจกต์คร่าวๆ
เกมนี้เป็นเกมแนว Roguelike RPG บน Android ที่เน้นการดำเนินเนื้อเรื่องและการต่อสู้แบบ Turn-based (สลับเทิร์น) โดยมีระบบการเล่นหลักคือการตะลุยไปในแต่ละชั้น (Floors) ของดันเจี้ยน ซึ่งในแต่ละชั้นผู้เล่นจะต้องสุ่มเจอกับเหตุการณ์ต่างๆได้มากถึง 25 เหตุการณ์ เช่น:
Combat Encounter: การต่อสู้กับศัตรูทั่วไปหรือ Boss ทุกๆ 10 ชั้น
Treasure/Merchant Room: ห้องสมบัติหรือร้านค้าเพื่ออัปเกรดตัวละคร
ตัวเกมมีระบบการพัฒนาตัวละครที่ชัดเจน ทั้งการเก็บเลเวล (EXP), การเพิ่มสเตตัส (ATK, DEF, HP, Luck), และระบบสายอาชีพ (Classes)

2. Tech Stack ที่ใช้:
Language: Kotlin (ภาษาหลักในการพัฒนา)
UI Framework:
Jetpack Compose: (ใน dependencies) ใช้สำหรับหน้าจอ UI
XML / View System: (ใน CardView, TextView, LinearLayout) ใช้จัดการ Layout หลักใน RoguelikeMainActivity
Architecture & Components:
Navigation Compose: สำหรับจัดการหน้าจอต่างๆ
Lifecycle & ViewModel: จัดการ State และข้อมูลของเกมให้คงอยู่แม้เปลี่ยนหน้าจอ
Coroutines: จัดการงานเบื้องหลังและการ Delay (เช่น ตอนเริ่มการต่อสู้)
Data Storage:
DataStore (Preferences): ใช้สำหรับบันทึกข้อมูลเกม (Save Game), เลเวล, หรือการตั้งค่าต่างๆ
UI Library: Google Material Components และ CardView เพื่อความสวยงามของ Card สเตตัส

3. Features เด่นของเกมในปัจจุบัน:
ระบบการต่อสู้ (Combat System):
มีระบบความเร็ว (Speed) เพื่อกำหนดลำดับเทิร์น
มี Status Effects (Burn, Poison, Stun)
มีระบบ Class-specific abilities (ความสามารถเฉพาะสายอาชีพ)
ระบบศัตรู (Enemy Generation):
ระบบสุ่มศัตรูตามความยากและชั้นที่ผู้เล่นอยู่
ระบบ Boss Fight ทุก 10 ชั้น
ระบบอาชีพ (Character Classes):
มีอย่างน้อย 12 อาชีพ : นักรบ นักเวทย์ โจร นักล่า เป็นต้น
แต่ละอาชีพจะได้โบนัสสเตตัสตอน Level Up ต่างกัน
ระบบ Progression:
การเก็บ EXP และ Level Up (พร้อมระบบสุ่มเพิ่มสเตตัส)
ระบบ Soul Points (ค่าพลังงาน)
ระบบ Inventory & Loot (การเก็บไอเทมจากศัตรู)
เเละอื่นๆ 
ระบบ Procedural Generation (เบื้องต้น):
◦
ใช้ Seed ในการสุ่มห้อง (Enter Floor) เพื่อให้การเล่นแต่ละรอบมีความหลากหลายแต่ยังสามารถควบคุมได้
