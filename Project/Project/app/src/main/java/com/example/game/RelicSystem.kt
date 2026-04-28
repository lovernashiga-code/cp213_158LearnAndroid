package com.example.game

import kotlin.random.Random

enum class RelicRarity(val colorHex: String, val emoji: String, val displayName: String) {
    COMMON   ("#9E9E9E", "⚪", "ธรรมดา"),
    UNCOMMON ("#4CAF50", "🟢", "ไม่ธรรมดา"),
    RARE     ("#2196F3", "🔵", "หายาก"),
    EPIC     ("#9C27B0", "🟣", "มหากาพย์"),
    LEGENDARY("#FF9800", "🟡", "ในตำนาน")
}

data class Relic(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val flavorText: String = "",
    val rarity: RelicRarity,
    val effects: Map<String, Any> = emptyMap()
)

object RelicCatalog {

    val all: List<Relic> = listOf(

        // ─── COMMON ──────────────────────────────────────────────────────────
        Relic("gold_sack",       "ถุงทองโบราณ",        "🪙",
            "Gold ที่ได้รับเพิ่มขึ้น 30%",
            "\"หนักมาก... แต่คุ้มค่า\"",
            RelicRarity.COMMON, mapOf("gold_bonus" to 30)),

        Relic("rabbit_foot",     "เท้ากระต่ายนำโชค",   "🍀",
            "Luck +8",
            "\"ของมันต้องมี\"",
            RelicRarity.COMMON, mapOf("luck_bonus" to 8)),

        Relic("old_shield_frag", "แผ่นโล่เก่า",        "🛡️",
            "DEF +6 ในการต่อสู้",
            "\"สึกกร่อนแต่ยังใช้ได้\"",
            RelicRarity.COMMON, mapOf("def_bonus" to 6)),

        Relic("emergency_vial",  "ขวดน้ำยาฉุกเฉิน",    "💊",
            "ครั้งแรกที่ HP จะเป็น 0 ฟื้นคืน 25 HP แทน (ครั้งเดียว/การต่อสู้)",
            "\"ใช้ได้แค่ครั้งเดียว\"",
            RelicRarity.COMMON, mapOf("death_protection" to 25)),

        Relic("warrior_journal", "บันทึกนักรบ",         "📖",
            "EXP ที่ได้รับเพิ่มขึ้น 25%",
            "\"ความรู้จากนักรบผู้ยิ่งใหญ่\"",
            RelicRarity.COMMON, mapOf("exp_bonus" to 25)),

        // ─── UNCOMMON ────────────────────────────────────────────────────────
        Relic("killer_ring",     "แหวนนักฆ่า",          "🩸",
            "ทุก 3 ศัตรูที่สังหาร: ATK +2 ถาวร (ใน run นี้)",
            "\"ย้อมด้วยเลือดศัตรู\"",
            RelicRarity.UNCOMMON, mapOf("kill_atk_stacks" to 2, "kill_atk_every" to 3)),

        Relic("thunder_heart",   "หัวใจสายฟ้า",          "⚡",
            "ATK +12 ในการต่อสู้",
            "\"เต้นระรัวไม่หยุด\"",
            RelicRarity.UNCOMMON, mapOf("atk_bonus" to 12)),

        Relic("cracked_skull",   "กะโหลกหักสาม",        "💀",
            "เมื่อ HP < 50%: ATK +15 เพิ่มเติมในการต่อสู้",
            "\"ความเจ็บปวดคือพลัง\"",
            RelicRarity.UNCOMMON, mapOf("low_hp_atk_bonus" to 15)),

        Relic("soul_crystal",    "ผลึกวิญญาณ",           "🔮",
            "เริ่มต้นทุกการต่อสู้ด้วย Soul Points +80",
            "\"บรรจุวิญญาณนับไม่ถ้วน\"",
            RelicRarity.UNCOMMON, mapOf("sp_start" to 80)),

        Relic("jungle_leaf",     "ใบยาป่า",              "🌿",
            "ฟื้นฟู 4 HP ทุกเทิร์นในสนามรบ",
            "\"กลิ่นป่าหลังฝน\"",
            RelicRarity.UNCOMMON, mapOf("regen" to 4)),

        // ─── RARE ────────────────────────────────────────────────────────────
        Relic("dragon_bone",     "กระดูกมังกร",          "🩻",
            "DEF +15 และลดความเสียหายที่ได้รับ 15%",
            "\"ทนทานเหนือธรรมชาติ\"",
            RelicRarity.RARE, mapOf("def_bonus" to 15, "damage_reduction" to 15)),

        Relic("meteor_stone",    "หินอุกกาบาต",          "☄️",
            "ทุก 5 ศัตรูที่สังหาร: Max HP +10",
            "\"พลังจากนอกโลก\"",
            RelicRarity.RARE, mapOf("kill_hp_stacks" to 10, "kill_hp_every" to 5)),

        Relic("shadow_destroyer","เงาผู้ทำลาย",           "🌑",
            "ATK +22 แต่ DEF -10",
            "\"พลังที่มาพร้อมต้นทุน\"",
            RelicRarity.RARE, mapOf("atk_bonus" to 22, "def_bonus" to -10)),

        Relic("immortal_heart",  "หัวใจอมตะ",            "🫀",
            "ฟื้นฟู 8 HP ทุกเทิร์นในสนามรบ",
            "\"เต้นไม่หยุดแม้แทบจะตาย\"",
            RelicRarity.RARE, mapOf("regen" to 8)),

        Relic("eternal_flame",   "เปลวเพลิงนิรันดร์",    "🔥",
            "การโจมตีพื้นฐานมีโอกาส 25% ติด BURN บนศัตรู",
            "\"ไฟที่ไม่มีวันดับ\"",
            RelicRarity.RARE, mapOf("burn_on_attack" to 25)),

        // ─── EPIC ────────────────────────────────────────────────────────────
        Relic("god_eye",         "ตาเทพเจ้า",            "👁️",
            "HP ไม่สามารถลดต่ำกว่า 10% ของ Max HP ได้",
            "\"มองเห็นขอบเขตของตัวเอง\"",
            RelicRarity.EPIC, mapOf("min_hp_pct" to 10)),

        Relic("death_card",      "ไพ่มรณะ",             "🎴",
            "เมื่อ HP < 25%: ค่าใช้จ่าย Soul Points ของทุกสกิลเป็น 0",
            "\"เดิมพันชีวิต\"",
            RelicRarity.EPIC, mapOf("low_hp_free_skills" to 25)),

        Relic("fate_sword",      "ดาบคู่โชคชะตา",        "⚔️",
            "ATK +25 และ Luck +15",
            "\"หล่อหลอมจากแสงดาว\"",
            RelicRarity.EPIC, mapOf("atk_bonus" to 25, "luck_bonus" to 15)),

        Relic("guiding_light",   "แสงนำทาง",            "🌟",
            "EXP +50% และ Gold +50%",
            "\"เส้นทางสู่ความยิ่งใหญ่\"",
            RelicRarity.EPIC, mapOf("exp_bonus" to 50, "gold_bonus" to 50)),

        // ─── LEGENDARY ───────────────────────────────────────────────────────
        Relic("destiny_star",    "ดาวแห่งโชคชะตา",       "💫",
            "ATK, DEF และ Luck ทั้งหมด +15",
            "\"เกิดมาเพื่อสิ่งนี้\"",
            RelicRarity.LEGENDARY, mapOf("atk_bonus" to 15, "def_bonus" to 15, "luck_bonus" to 15)),

        Relic("divine_dragon_scale", "เกล็ดมังกรเทพ",   "🐉",
            "Max HP +50 และลดความเสียหายทั้งหมด 25%",
            "\"ป้องกันได้แม้จากเทพ\"",
            RelicRarity.LEGENDARY, mapOf("hp_bonus" to 50, "damage_reduction" to 25)),

        Relic("death_ring",      "แหวนจ้าวมรณะ",        "💍",
            "20% โอกาสสังหารทันทีศัตรูที่ HP เหลือต่ำกว่า 15%",
            "\"ความตายมาอยู่ในมือคุณแล้ว\"",
            RelicRarity.LEGENDARY, mapOf("enemy_execute_chance" to 20))
    )

    fun getById(id: String): Relic? = all.firstOrNull { it.id == id }

    fun getRandomRelics(count: Int, excludeIds: Set<String> = emptySet(), random: Random = Random): List<Relic> {
        val available = all.filter { it.id !in excludeIds }
        if (available.isEmpty()) return emptyList()

        // Weighted pool by rarity
        val pool = mutableListOf<Relic>()
        available.forEach { r ->
            val w = when (r.rarity) {
                RelicRarity.COMMON    -> 40
                RelicRarity.UNCOMMON  -> 25
                RelicRarity.RARE      -> 15
                RelicRarity.EPIC      -> 8
                RelicRarity.LEGENDARY -> 3
            }
            repeat(w) { pool.add(r) }
        }

        val result = mutableListOf<Relic>()
        val usedIds = excludeIds.toMutableSet()
        repeat(count) {
            val filtered = pool.filter { it.id !in usedIds }
            if (filtered.isNotEmpty()) {
                val picked = filtered.random(random)
                result.add(picked)
                usedIds.add(picked.id)
            }
        }
        return result
    }
}
