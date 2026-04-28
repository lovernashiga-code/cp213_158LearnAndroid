package com.example.game.domain

import com.example.game.data.model.*

object WorldManager {

    data class FloorContent(
        val intro: String,
        val choices: List<Choice>,
        val immediateEnemy: EnemyState? = null
    )

    data class ActionResult(
        val message: String,
        val hpDelta: Int = 0,
        val manaDelta: Int = 0,
        val goldDelta: Int = 0,
        val soulDelta: Int = 0,
        val expGain: Int = 0,
        val itemsGained: List<Item> = emptyList(),
        val flagsSet: Map<String, Boolean> = emptyMap(),
        val advanceFloor: Boolean = false,
        val triggerEnemy: EnemyState? = null,
        val openShop: List<ShopItem>? = null,
        val strDelta: Int = 0,
        val defDelta: Int = 0,
        val luckDelta: Int = 0,
        val intDelta: Int = 0,
        val maxHpDelta: Int = 0
    )

    fun getFloorContent(floor: Int, flags: Map<String, Boolean>): FloorContent = when (floor) {

        1 -> FloorContent(
            intro = """
╔══════════════════════════════╗
║    วิหารแห่งเงามืด   ║
╚══════════════════════════════╝

คุณยืนอยู่ที่ทางเข้าของวิหารโบราณ
อากาศหนาวเย็นและหนักอึ้ง...
ประตูขนาดใหญ่อยู่ตรงหน้าคุณ
ตะเกียงลึกลับส่องแสงสลัวอยู่ข้างๆ

🗺️ ชั้น 1 — ทางเข้าวิหาร
            """.trimIndent(),
            choices = listOf(
                Choice("🚪 ผลักประตูเข้าไป", "f1_door"),
                Choice("🕯️ สัมผัสตะเกียง", "f1_lamp"),
                Choice("🧘 นั่งสมาธิ", "f1_meditate"),
                Choice("⬇️ เดินต่อไป", "f1_advance")
            )
        )

        2 -> FloorContent(
            intro = """
🌫️ ชั้น 2 — โถงแห่งเงา

คุณก้าวเข้าสู่โถงที่มืดมิด
เสียงกรีดร้องดังขึ้นจากความมืด
เงาดำรูปร่างบิดเบี้ยวปรากฏขึ้น...

💀 Shadow Ghoul ปรากฏตัว!
            """.trimIndent(),
            choices = listOf(
                Choice("⚔️ ต่อสู้!", "f2_fight"),
                Choice("💨 หลบหนี", "f2_flee_pre")
            ),
            immediateEnemy = WorldData.SHADOW_GHOUL
        )

        3 -> FloorContent(
            intro = """
🏪 ชั้น 3 — ตลาดมืด

แสงไฟสีส้มจากร้านค้าลึกลับ
พ่อค้าร่างผอมยิ้มต้อนรับคุณ
"ยินดีต้อนรับ นักเดินทาง..."

💰 Gold: มีสินค้าน่าสนใจ
            """.trimIndent(),
            choices = listOf(
                Choice("🛍️ ดูสินค้า", "f3_shop"),
                Choice("⬇️ เดินต่อไป", "f3_advance")
            )
        )

        4 -> FloorContent(
            intro = """
📦 ชั้น 4 — ห้องสมบัติ

หีบสมบัติขนาดใหญ่ตั้งตระหง่านอยู่
มีรูปสลักแปลกๆ อยู่รอบๆ
คุณรู้สึกว่ามีบางอย่างผิดปกติ...

⚠️ ระวัง — อาจมีกับดัก!
            """.trimIndent(),
            choices = listOf(
                Choice("🔓 เปิดหีบตรงๆ", "f4_open"),
                Choice("🔍 ตรวจสอบก่อน", "f4_inspect"),
                Choice("⬇️ ข้ามไป", "f4_skip")
            )
        )

        5 -> FloorContent(
            intro = """
👻 ชั้น 5 — ห้องวิญญาณ

วิญญาณสีขาวลอยอยู่กลางห้อง
มันร้องไห้อย่างเจ็บปวด
"ช่วยฉันด้วย... โปรดปลดปล่อยฉัน..."

💠 คุณจะทำอย่างไร?
            """.trimIndent(),
            choices = listOf(
                Choice("✨ ปลดปล่อยวิญญาณ", "f5_free"),
                Choice("⚡ ดูดซับพลังงาน", "f5_absorb"),
                Choice("🚶 เดินผ่านไป", "f5_ignore")
            )
        )

        6 -> FloorContent(
            intro = """
💧 ชั้น 6 — ห้องพักผ่อน

ห้องที่เงียบสงบกับน้ำพุลึกลับ
แสงอ่อนๆ ส่องจากผนัง
คุณรู้สึกว่าสถานที่นี้ปลอดภัย

🌿 ฟื้นฟูพลังงาน
            """.trimIndent(),
            choices = listOf(
                Choice("💧 ดื่มน้ำจากน้ำพุ", "f6_drink"),
                Choice("🧘 นั่งสมาธิลึก", "f6_meditate"),
                Choice("📚 อ่านหนังสือโบราณ", "f6_read"),
                Choice("⬇️ เดินต่อไป", "f6_advance")
            )
        )

        7 -> FloorContent(
            intro = """
💀 ชั้น 7 — ห้องบอสย่อย

อากาศเย็นยะเยือกอย่างรวดเร็ว
รูปร่างกึ่งโปร่งใสล่องลอยมาหาคุณ
ดวงตาสีม่วงเรืองแสงในความมืด

⚠️ Ethereal Wraith — มินิบอส!
            """.trimIndent(),
            choices = listOf(
                Choice("⚔️ เผชิญหน้า!", "f7_fight")
            ),
            immediateEnemy = WorldData.WRAITH
        )

        8 -> FloorContent(
            intro = """
🔱 ชั้น 8 — ทางแยกแห่งโชคชะตา

ทางสามแพร่งอยู่ตรงหน้าคุณ
🔴 ทางแดง — อันตราย แต่รางวัลมาก
🟡 ทางทอง — สมดุลระหว่างความเสี่ยงและรางวัล
🔵 ทางน้ำเงิน — ปลอดภัย แต่รางวัลน้อย

คุณจะเลือกทางไหน?
            """.trimIndent(),
            choices = listOf(
                Choice("🔴 ทางแดง — สูงเสี่ยงสูงรางวัล", "f8_red"),
                Choice("🟡 ทางทอง — สมดุล", "f8_gold"),
                Choice("🔵 ทางน้ำเงิน — ปลอดภัย", "f8_blue")
            )
        )

        9 -> FloorContent(
            intro = """
🪞 ชั้น 9 — กระจกแห่งความจริง

กระจกขนาดใหญ่สะท้อนภาพคุณ
แต่ภาพสะท้อนนั้น... พูดได้!
"ข้าจะมอบพลังให้เจ้า
เลือกสิ่งที่เจ้าต้องการ..."

⭐ เลือกพลังพิเศษ!
            """.trimIndent(),
            choices = listOf(
                Choice("💪 เพิ่ม STR +3", "f9_str"),
                Choice("🛡️ เพิ่ม DEF +3", "f9_def"),
                Choice("🧠 เพิ่ม INT +3", "f9_int"),
                Choice("🍀 เพิ่ม LUCK +5", "f9_luck")
            )
        )

        10 -> FloorContent(
            intro = """
🏪 ชั้น 10 — ร้านค้าสุดท้าย

พ่อค้าลึกลับรอคุณอยู่
"นักเดินทาง... ฉันรู้ว่าเจ้ากำลังจะพบกับ
ความมืดสูงสุด ฉันมีของวิเศษสำหรับเจ้า"

⚠️ เตรียมตัวให้พร้อมก่อนบอสสุดท้าย!
            """.trimIndent(),
            choices = listOf(
                Choice("🛍️ ซื้อของ", "f10_shop"),
                Choice("⬇️ เดินต่อไป", "f10_advance")
            )
        )

        11 -> FloorContent(
            intro = """
⚔️ ชั้น 11 — ห้องเตรียมพร้อม

ประตูขนาดมหึมาอยู่ตรงหน้า
เสียงคำรามดังมาจากด้านหลัง
คุณรู้สึกว่านี่คือจุดสิ้นสุด...

💜 Soul Points จะทำให้คุณแข็งแกร่ง
🧪 ใช้ยาฟื้นฟูก่อนสู้
            """.trimIndent(),
            choices = listOf(
                Choice("🧪 ใช้ยาฟื้นฟู HP", "f11_heal"),
                Choice("💜 นั่งสมาธิ (ฟื้น Mana)", "f11_meditate"),
                Choice("⚔️ เข้าสู่ห้องบอส!", "f11_enter_boss")
            )
        )

        12 -> FloorContent(
            intro = """
🌑 ชั้น 12 — ห้องบอสสุดท้าย

╔════════════════════════════╗
║   THE VOID LORD AWAITS    ║
╚════════════════════════════╝

ความมืดสมบูรณ์ปกคลุมทั่วห้อง
สิ่งมีชีวิตขนาดมหึมายืนตระหง่าน
ดวงตาสีม่วงเรืองแสงมองมาที่คุณ...

"เจ้า... กล้าดีมาถึงที่นี่..."

🌑 The Void Lord — บอสสุดท้าย!
            """.trimIndent(),
            choices = listOf(
                Choice("⚔️ สู้กับ Void Lord!", "f12_fight")
            ),
            immediateEnemy = WorldData.VOID_LORD
        )

        else -> FloorContent(
            intro = "🏆 คุณผ่านวิหารแห่งเงามืดแล้ว!",
            choices = emptyList()
        )
    }

    fun processAction(
        floor: Int,
        actionId: String,
        player: PlayerStats,
        inventory: Inventory,
        flags: Map<String, Boolean>
    ): ActionResult = when (actionId) {

        // ── Floor 1 ──────────────────────────────────────

        "f1_door" -> ActionResult(
            message = "🚪 คุณผลักประตูหนักเข้าไป...\nหีบไม้เก่าซ่อนอยู่ข้างหลัง\nคุณพบยาฟื้นฟู HP!",
            itemsGained = listOf(WorldData.POTION)
        )

        "f1_lamp" -> ActionResult(
            message = "🕯️ คุณสัมผัสตะเกียงลึกลับ\nพลังงานลึกลับไหลเข้าสู่ร่างกาย!\n+15 Soul Points",
            soulDelta = 15
        )

        "f1_meditate" -> ActionResult(
            message = "🧘 คุณนั่งสมาธิ ปรับจิตใจให้ว่าง\nพลังงานภายในฟื้นตัว\n+5 Mana, +1 STR",
            manaDelta = 5,
            strDelta = 1
        )

        "f1_advance" -> ActionResult(
            message = "⬇️ คุณเดินลงบันไดสู่ชั้นถัดไป...",
            advanceFloor = true
        )

        // ── Floor 2 ──────────────────────────────────────

        "f2_fight" -> ActionResult(
            message = "⚔️ คุณชักดาบออกมา!\nพร้อมต่อสู้กับ Shadow Ghoul!",
            triggerEnemy = WorldData.SHADOW_GHOUL
        )

        "f2_flee_pre" -> ActionResult(
            message = "💨 คุณวิ่งหนีกลับขึ้นบันได...\nแต่ Shadow Ghoul ไล่ตาม!\nคุณได้รับความเสียหาย -5 HP",
            hpDelta = -5,
            advanceFloor = false
        )

        // ── Floor 3 ──────────────────────────────────────

        "f3_shop" -> ActionResult(
            message = "🛍️ พ่อค้ายิ้มต้อนรับ\n\"ยินดีต้อนรับสู่ร้านของข้า!\"",
            openShop = WorldData.FLOOR_3_SHOP
        )

        "f3_advance" -> ActionResult(
            message = "⬇️ คุณออกจากตลาดและเดินต่อไป",
            advanceFloor = true
        )

        // ── Floor 4 ──────────────────────────────────────

        "f4_open" -> {
            val hasSoul = player.soulPoints >= 20
            if (hasSoul) {
                ActionResult(
                    message = "🔓 คุณใช้พลัง Soul เพื่อป้องกันกับดัก!\nหีบเปิดออก... ภายในมีดาบเก่า!",
                    itemsGained = listOf(WorldData.SWORD),
                    soulDelta = -20
                )
            } else {
                ActionResult(
                    message = "💥 กับดักถูกเปิดใช้งาน!\nหนามพิษแทงเข้าที่มือคุณ\n-8 HP, -5 Mana",
                    hpDelta = -8,
                    manaDelta = -5
                )
            }
        }

        "f4_inspect" -> ActionResult(
            message = "🔍 คุณตรวจสอบรอบๆ อย่างระมัดระวัง\nพบกลไกกับดักซ่อนอยู่!\nคุณปิดมันได้และเปิดหีบอย่างปลอดภัย",
            itemsGained = listOf(WorldData.KEY),
            goldDelta = 15
        )

        "f4_skip" -> ActionResult(
            message = "🚶 คุณเดินผ่านหีบไปอย่างระวัง\nฉลาดดี — ไม่มีอะไรสูญเสีย",
            advanceFloor = true
        )

        // ── Floor 5 ──────────────────────────────────────

        "f5_free" -> ActionResult(
            message = "✨ คุณใช้พลังงานปลดปล่อยวิญญาณ\nวิญญาณหายไปพร้อมแสงสีขาว\n\"ขอบคุณ... ข้าจะมอบพรให้เจ้า\"\n+3 LUCK, +10 EXP",
            luckDelta = 3,
            expGain = 10,
            flagsSet = mapOf("savedSpirit" to true),
            advanceFloor = true
        )

        "f5_absorb" -> ActionResult(
            message = "⚡ คุณดูดซับพลังงานวิญญาณ!\nพลัง Soul Points เพิ่มขึ้นมาก\n+25 Soul Points, +2 INT",
            soulDelta = 25,
            intDelta = 2,
            advanceFloor = true
        )

        "f5_ignore" -> ActionResult(
            message = "🚶 คุณเดินผ่านวิญญาณไปโดยไม่สนใจ\nเสียงร้องไห้ดังลั่นหลังหลัง...",
            advanceFloor = true
        )

        // ── Floor 6 ──────────────────────────────────────

        "f6_drink" -> ActionResult(
            message = "💧 น้ำพุรสหวานชุ่มคอ\nร่างกายฟื้นตัวอย่างรวดเร็ว!\n+15 HP, +10 Mana",
            hpDelta = 15,
            manaDelta = 10
        )

        "f6_meditate" -> ActionResult(
            message = "🧘 คุณนั่งสมาธิอย่างลึก ประมาณ 1 ชั่วโมง\nMana ฟื้นตัวเต็ม\n+Full Mana, +5 Soul Points",
            manaDelta = player.maxMana,
            soulDelta = 5
        )

        "f6_read" -> ActionResult(
            message = "📚 หนังสือโบราณเผยสูตรเวทมนตร์ใหม่!\nคุณเรียนรู้เทคนิคการต่อสู้\n+2 STR, +2 INT",
            strDelta = 2,
            intDelta = 2
        )

        "f6_advance" -> ActionResult(
            message = "⬇️ คุณพักผ่อนเพียงพอแล้ว เดินต่อไป",
            advanceFloor = true
        )

        // ── Floor 7 ──────────────────────────────────────

        "f7_fight" -> ActionResult(
            message = "⚔️ คุณเผชิญหน้ากับ Ethereal Wraith!\nนี่คือการต่อสู้ที่ยากที่สุดจนถึงตอนนี้!",
            triggerEnemy = WorldData.WRAITH
        )

        // ── Floor 8 ──────────────────────────────────────

        "f8_red" -> ActionResult(
            message = "🔴 ทางแดง — เต็มไปด้วยอันตราย!\nคุณเจอกับดักพิษ... -10 HP\nแต่พบสมบัติซ่อนอยู่! +40 Gold, +1 Sword",
            hpDelta = -10,
            goldDelta = 40,
            itemsGained = listOf(WorldData.SWORD),
            advanceFloor = true
        )

        "f8_gold" -> ActionResult(
            message = "🟡 ทางทอง — สมดุลที่ดี!\nคุณเดินทางอย่างปลอดภัยและพบสมบัติ\n+20 Gold, +5 EXP",
            goldDelta = 20,
            expGain = 5,
            advanceFloor = true
        )

        "f8_blue" -> ActionResult(
            message = "🔵 ทางน้ำเงิน — ปลอดภัยที่สุด\nคุณเดินทางถึงชั้นถัดไปอย่างสบาย\n+5 Gold",
            goldDelta = 5,
            advanceFloor = true
        )

        // ── Floor 9 ──────────────────────────────────────

        "f9_str" -> ActionResult(
            message = "💪 กระจกมอบพลังร่างกายให้คุณ!\n+3 STR — การโจมตีของคุณรุนแรงขึ้น!",
            strDelta = 3,
            advanceFloor = true
        )

        "f9_def" -> ActionResult(
            message = "🛡️ กระจกมอบพลังป้องกันให้คุณ!\n+3 DEF — คุณแข็งแกร่งขึ้นมาก!",
            defDelta = 3,
            advanceFloor = true
        )

        "f9_int" -> ActionResult(
            message = "🧠 กระจกมอบสติปัญญาให้คุณ!\n+3 INT, +20 Mana Max — เวทมนตร์ทรงพลังขึ้น!",
            intDelta = 3,
            maxHpDelta = 20,
            advanceFloor = true
        )

        "f9_luck" -> ActionResult(
            message = "🍀 กระจกมอบโชคให้คุณ!\n+5 LUCK — วิกฤตโจมตีสูงขึ้นมาก!",
            luckDelta = 5,
            advanceFloor = true
        )

        // ── Floor 10 ─────────────────────────────────────

        "f10_shop" -> ActionResult(
            message = "🛍️ \"สินค้าพิเศษสำหรับนักรบผู้กล้าหาญ!\"",
            openShop = WorldData.FLOOR_10_SHOP
        )

        "f10_advance" -> ActionResult(
            message = "⬇️ คุณเตรียมพร้อมและเดินต่อไป...",
            advanceFloor = true
        )

        // ── Floor 11 ─────────────────────────────────────

        "f11_heal" -> {
            val potions = inventory.consumables.filter { it.id == "potion" }
            if (potions.isNotEmpty()) {
                ActionResult(
                    message = "🧪 คุณดื่มยาฟื้นฟู\n+15 HP — เตรียมพร้อมแล้ว!",
                    hpDelta = 15
                )
            } else {
                ActionResult(message = "❌ คุณไม่มียาฟื้นฟู!")
            }
        }

        "f11_meditate" -> ActionResult(
            message = "💜 คุณนั่งสมาธิ ปรับจิตใจให้สงบ\n+15 Mana ฟื้นตัว",
            manaDelta = 15
        )

        "f11_enter_boss" -> ActionResult(
            message = "⚔️ คุณเดินเข้าสู่ห้องบอสสุดท้าย...\nโชคดีนะ นักรบ!",
            advanceFloor = true
        )

        // ── Floor 12 ─────────────────────────────────────

        "f12_fight" -> ActionResult(
            message = "🌑 \"เจ้าจะสิ้นชีวิตที่นี่!\"\nการต่อสู้สุดท้ายเริ่มต้นขึ้น!",
            triggerEnemy = WorldData.VOID_LORD
        )

        // ── Shop purchase ─────────────────────────────────

        else -> ActionResult(message = "❓ ไม่รู้จักคำสั่งนี้")
    }
}
