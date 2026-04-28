package com.example.game

import kotlin.random.Random

enum class RunModifierType(val emoji: String, val label: String, val color: String) {
    BLESSING("✨", "พร",    "#4CAF50"),
    CURSE   ("💀", "สาป",  "#F44336"),
    MIXED   ("⚖️",  "ผสม",  "#FF9800")
}

data class RunModifier(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val flavorText: String = "",
    val type: RunModifierType,
    val effects: Map<String, Any> = emptyMap()
)

object RunModifierCatalog {

    val all: List<RunModifier> = listOf(

        // ── BLESSINGS ───────────────────────────────────────────────────────
        RunModifier(
            id = "golden_touch", name = "มือทอง", emoji = "🪙",
            description = "Gold ที่ได้รับเพิ่มขึ้น 60% ตลอด Run",
            flavorText = "\"ทุกสิ่งที่แตะต้องกลายเป็นทอง\"",
            type = RunModifierType.BLESSING,
            effects = mapOf("gold_mult" to 1.60f)
        ),
        RunModifier(
            id = "wise_scholar", name = "นักวิชาการ", emoji = "📚",
            description = "EXP ที่ได้รับเพิ่มขึ้น 40% ตลอด Run",
            flavorText = "\"ความรู้คือพลังที่แท้จริง\"",
            type = RunModifierType.BLESSING,
            effects = mapOf("exp_mult" to 1.40f)
        ),
        RunModifier(
            id = "titan_blood", name = "เลือดยักษ์", emoji = "🩸",
            description = "Max HP +50 ตั้งแต่เริ่มต้น",
            flavorText = "\"เลือดของผู้ยิ่งใหญ่ไหลเวียนอยู่ในร่าง\"",
            type = RunModifierType.BLESSING,
            effects = mapOf("max_hp_bonus" to 50)
        ),
        RunModifier(
            id = "fortunate_soul", name = "ดวงดี", emoji = "🍀",
            description = "Luck +12 ถาวรตลอด Run",
            flavorText = "\"ดาวนำทางส่องสว่างเส้นทางของคุณ\"",
            type = RunModifierType.BLESSING,
            effects = mapOf("luck_bonus" to 12)
        ),
        RunModifier(
            id = "soul_fountain", name = "น้ำพุวิญญาณ", emoji = "💎",
            description = "เริ่มต้นด้วย Soul Points +100",
            flavorText = "\"วิญญาณนับไม่ถ้วนถูกกลั่นมาเป็นพลัง\"",
            type = RunModifierType.BLESSING,
            effects = mapOf("start_soul" to 100)
        ),
        RunModifier(
            id = "divine_protection", name = "พรสวรรค์", emoji = "😇",
            description = "ฟื้นคืนชีพ 1 ครั้งต่อ Run ด้วย 30% HP เมื่อ HP เป็น 0",
            flavorText = "\"เทวดาเฝ้าดูแลคุณอยู่เสมอ\"",
            type = RunModifierType.BLESSING,
            effects = mapOf("revive_once" to true, "revive_hp_pct" to 30)
        ),

        // ── CURSES ──────────────────────────────────────────────────────────
        RunModifier(
            id = "cursed_wealth", name = "สาปทรัพย์", emoji = "💸",
            description = "Gold ที่ได้รับลดลง 40% ตลอด Run",
            flavorText = "\"ของมีค่าละลายหายไปเหมือนทรายในมือ\"",
            type = RunModifierType.CURSE,
            effects = mapOf("gold_mult" to 0.60f)
        ),
        RunModifier(
            id = "brittle_bones", name = "กระดูกเปราะ", emoji = "🦴",
            description = "Max HP -30 ตลอด Run",
            flavorText = "\"ร่างกายที่ทรุดโทรมไม่สามารถแบกรับโชคชะตาได้\"",
            type = RunModifierType.CURSE,
            effects = mapOf("max_hp_bonus" to -30)
        ),
        RunModifier(
            id = "bad_luck", name = "ดวงตก", emoji = "🌑",
            description = "Luck -10 ตลอด Run",
            flavorText = "\"โชคชะตาหันหลังให้เจ้าแล้ว\"",
            type = RunModifierType.CURSE,
            effects = mapOf("luck_bonus" to -10)
        ),
        RunModifier(
            id = "bleeding_dungeon", name = "ดันเจี้ยนเลือดไหล", emoji = "🩹",
            description = "เสียHP เพิ่ม 10 หน่วยทุกครั้งที่เข้าห้องใหม่",
            flavorText = "\"กำแพงนี้มีชีวิต และมันกำลังดูดพลังชีวิตของคุณ\"",
            type = RunModifierType.CURSE,
            effects = mapOf("room_damage" to 10)
        ),
        RunModifier(
            id = "empowered_monsters", name = "สัตว์ประหลาดพลัง", emoji = "👹",
            description = "ศัตรูทุกตัวมี ATK เพิ่มขึ้น 25%",
            flavorText = "\"พวกมันกินพลังงานจากดันเจี้ยนและแข็งแกร่งขึ้นเรื่อยๆ\"",
            type = RunModifierType.CURSE,
            effects = mapOf("enemy_atk_mult" to 1.25f)
        ),
        RunModifier(
            id = "slow_exp", name = "เรียนรู้ช้า", emoji = "🐌",
            description = "EXP ที่ได้รับลดลง 30% ตลอด Run",
            flavorText = "\"บางครั้งสมองก็ปฏิเสธที่จะเรียนรู้\"",
            type = RunModifierType.CURSE,
            effects = mapOf("exp_mult" to 0.70f)
        ),

        // ── MIXED ───────────────────────────────────────────────────────────
        RunModifier(
            id = "glass_cannon", name = "เมจแก้ว", emoji = "⚡",
            description = "ATK +25 แต่ Max HP -40",
            flavorText = "\"พลังมหาศาล แต่ร่างกายนั้นเปราะบางยิ่ง\"",
            type = RunModifierType.MIXED,
            effects = mapOf("atk_bonus" to 25, "max_hp_bonus" to -40)
        ),
        RunModifier(
            id = "ancient_pact", name = "สัญญาโบราณ", emoji = "📜",
            description = "ศัตรู HP +50% แต่ EXP +70%",
            flavorText = "\"ยิ่งยากยิ่งได้มาก — นี่คือกฎของดันเจี้ยน\"",
            type = RunModifierType.MIXED,
            effects = mapOf("enemy_hp_mult" to 1.50f, "exp_mult" to 1.70f)
        ),
        RunModifier(
            id = "blood_pact", name = "สัญญาเลือด", emoji = "❤️",
            description = "Gold x2 แต่เสีย HP เพิ่ม 15 ทุกห้อง",
            flavorText = "\"ความมั่งคั่งมาพร้อมกับราคาที่ต้องจ่ายเป็นเลือด\"",
            type = RunModifierType.MIXED,
            effects = mapOf("gold_mult" to 2.0f, "room_damage" to 15)
        ),
        RunModifier(
            id = "scholar_curse", name = "สาปนักวิชาการ", emoji = "🔄",
            description = "EXP +60% แต่ Gold -50%",
            flavorText = "\"ความรู้หรือทรัพย์สิน — เลือกได้แค่หนึ่งอย่าง\"",
            type = RunModifierType.MIXED,
            effects = mapOf("exp_mult" to 1.60f, "gold_mult" to 0.50f)
        ),
        RunModifier(
            id = "savage_world", name = "โลกดุร้าย", emoji = "🌍",
            description = "ศัตรูทุกตัว ATK +15% และ HP +30% แต่ Gold +50%",
            flavorText = "\"ดันเจี้ยนแห่งนี้โหดร้ายกว่าที่เคย... แต่ผลตอบแทนก็สูงกว่าด้วย\"",
            type = RunModifierType.MIXED,
            effects = mapOf("enemy_atk_mult" to 1.15f, "enemy_hp_mult" to 1.30f, "gold_mult" to 1.50f)
        )
    )

    fun getRandomModifiers(count: Int, random: Random = Random): List<RunModifier> {
        // Guarantee at least 1 blessing and 1 curse in a 3-modifier run
        val blessings = all.filter { it.type == RunModifierType.BLESSING }.shuffled(random)
        val curses    = all.filter { it.type == RunModifierType.CURSE    }.shuffled(random)
        val mixed     = all.filter { it.type == RunModifierType.MIXED    }.shuffled(random)

        val result = mutableListOf<RunModifier>()
        if (count >= 3) {
            result.add(blessings.first())
            result.add(curses.first())
            val remaining = (mixed + blessings.drop(1) + curses.drop(1)).shuffled(random)
            result.addAll(remaining.take(count - 2))
        } else {
            result.addAll(all.shuffled(random).take(count))
        }
        return result.shuffled(random) // shuffle so order doesn't reveal category
    }
}
