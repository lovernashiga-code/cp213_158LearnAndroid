package com.example.game

import android.content.SharedPreferences

// ── LP calculation ────────────────────────────────────────────────────────────

object MetaProgression {

    private const val PREFS_NAME   = "MetaProgression"
    private const val KEY_TOTAL_LP = "total_lp"
    private const val KEY_CLASSES  = "unlocked_classes"
    private const val KEY_RACES    = "unlocked_races"

    // Classes unlocked from the start (no LP needed)
    val defaultClasses = setOf("WARRIOR", "MAGE", "ROGUE", "CLERIC", "RANGER")
    val defaultRaces   = setOf("HUMAN", "ELF", "DWARF", "ORC", "DEMON")

    data class UnlockEntry(
        val id: String,
        val displayName: String,
        val emoji: String,
        val cost: Int,
        val hint: String
    )

    // ── Class unlock catalogue ────────────────────────────────────────────────
    val classUnlocks: List<UnlockEntry> = listOf(
        // Tier 1 — 50 LP
        UnlockEntry("PALADIN",     "อัศวินศักดิ์สิทธิ์", "🛡️",  50, "โล่แห่งแสง — HP+DEF สูงสุด รักษาตัวเองได้"),
        UnlockEntry("BERSERKER",   "เบอร์เซิร์กเกอร์",  "🪓",  50, "บ้าคลั่ง — ATK สูง ยิ่งเลือดน้อยยิ่งตีแรง"),
        UnlockEntry("BARD",        "กวี",              "🎶",  50, "บทเพลงชัยชนะ — Luck สูงสุด บัฟทีม"),
        UnlockEntry("DRUID",       "ดรูอิด",            "🌿",  50, "พลังธรรมชาติ — โจมตีและฟื้นฟูในตัว"),
        // Tier 2 — 100 LP
        UnlockEntry("NECROMANCER", "เนโครแมนเซอร์",    "💀", 100, "จ้าวมรณะ — ATK สูง ดูดพลังชีวิตศัตรู"),
        UnlockEntry("SAMURAI",     "ซามูไร",            "🏮", 100, "คมดาบสังหาร — ความเร็วและแม่นยำ"),
        UnlockEntry("SHAMAN",      "หมอผี",             "🎭", 100, "สื่อวิญญาณ — คำสาปและบัฟจากวิญญาณ"),
        UnlockEntry("MONK",        "นักพรต",            "👊", 100, "เหล็กไหล — ฟื้นฟู HP ทุกเทิร์น"),
        UnlockEntry("ALCHEMIST",   "นักเล่นแร่แปรธาตุ","⚗️", 100, "ปรมาจารย์ยา — เริ่มพร้อมยา ไอเทมมีประสิทธิภาพสูง"),
        UnlockEntry("PIRATE",      "โจรสลัด",           "🏴‍☠️",100, "ปล้นทะเล — ขโมยทองจากดาเมจที่สร้าง"),
        // Tier 3 — 150 LP
        UnlockEntry("NINJA",       "นินจา",             "🥷", 150, "เงาสังหาร — 8% สังหารทันที, Dodge สูง"),
        UnlockEntry("TEMPLAR",     "เทมพลาร์",          "⚜️", 150, "แสงศักดิ์สิทธิ์ — ดาเมจ +50% ต่อบอส"),
        UnlockEntry("SORCERER",    "จอมเวทย์",          "🌑", 150, "กระแสอาร์เคน — ทักษะทุกประเภท +30% ดาเมจ"),
        UnlockEntry("TINKERER",    "ช่างประดิษฐ์",      "🔧", 150, "ช่างดัดแปลง — ATK+DEF ต่อช่องอุปกรณ์"),
        UnlockEntry("GLADIATOR",   "กลาดิเอเตอร์",     "🏟️", 150, "นักสู้สังเวียน — ATK+1 ถาวรทุกชัยชนะ")
    )

    // ── Race unlock catalogue ─────────────────────────────────────────────────
    val raceUnlocks: List<UnlockEntry> = listOf(
        // Tier 1 — 40 LP
        UnlockEntry("ANGEL",     "เทวทูต",      "😇",  40, "10% ฟื้นฟู HP เมื่อเข้าห้องใหม่"),
        UnlockEntry("UNDEAD",    "อันเดด",      "💀",  40, "15% คืนชีพด้วย 1 HP เมื่อตาย"),
        UnlockEntry("VAMPIRE",   "แวมไพร์",     "🧛",  40, "ฟื้นฟู HP 2 หน่วยทุกครั้งที่โจมตีโดน"),
        UnlockEntry("WEREWOLF",  "มนุษย์หมาป่า","🐺",  40, "โจมตีครั้งที่ 3 ติดต่อกัน = Crit 100%"),
        // Tier 2 — 80 LP
        UnlockEntry("DRAGONKIN", "เผ่ามังกร",   "🐲",  80, "ลดดาเมจกายภาพ, HP+30 ATK+8 DEF+8"),
        UnlockEntry("FAIRY",     "แฟรี่",       "🧚",  80, "Luck +20 มหาศาล — หีบสมบัติหายากมากขึ้น"),
        UnlockEntry("CYBORG",    "ออโตมาตรอน", "🤖",  80, "HP<30% → ATK เพิ่ม 2 เท่า"),
        UnlockEntry("SHADOW",    "เงามืด",      "👥",  80, "20% หลบการโจมตีทุกประเภท"),
        UnlockEntry("MERMAID",   "เงือก",       "🧜",  80, "ฟื้นฟู Soul +3 ต่อเทิร์นอัตโนมัติ"),
        UnlockEntry("WITCH",     "แม่มด",       "🧙",  80, "25% ลด DEF ศัตรูทุกโจมตี 2 เทิร์น"),
        // Tier 3 — 120 LP
        UnlockEntry("GOLEM",     "โกเลม",       "🗿", 120, "HP+40 DEF+10, สะท้อน 15% ดาเมจกายภาพ"),
        UnlockEntry("PHOENIX",   "ฟีนิกซ์",     "🔥", 120, "ฟื้นคืนมาด้วย HP 30% ครั้งแรกต่อชั้น"),
        UnlockEntry("TITAN",     "ไททัน",       "🦣", 120, "HP+60, ดาเมจ+1 ต่อทุก 10 MaxHP"),
        UnlockEntry("SPECTER",   "ผี",          "👻", 120, "ภูมิคุ้มกันการโจมตีครั้งแรกทุกต่อสู้"),
        UnlockEntry("BEASTKIN",  "มนุษย์เสือ",  "🐯", 120, "EXP+Gold จากสังหาร +25%")
    )

    // ── Prefs helpers ─────────────────────────────────────────────────────────

    fun getPrefs(activity: android.app.Activity): SharedPreferences =
        activity.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)

    fun getTotalLP(prefs: SharedPreferences): Int = prefs.getInt(KEY_TOTAL_LP, 0)

    fun addLP(prefs: SharedPreferences, amount: Int) {
        prefs.edit().putInt(KEY_TOTAL_LP, getTotalLP(prefs) + amount).apply()
    }

    fun getUnlockedClasses(prefs: SharedPreferences): Set<String> {
        val saved = prefs.getStringSet(KEY_CLASSES, null)
        return if (saved == null) defaultClasses else defaultClasses + saved
    }

    fun getUnlockedRaces(prefs: SharedPreferences): Set<String> {
        val saved = prefs.getStringSet(KEY_RACES, null)
        return if (saved == null) defaultRaces else defaultRaces + saved
    }

    fun isClassUnlocked(prefs: SharedPreferences, id: String): Boolean =
        id in defaultClasses || id in (prefs.getStringSet(KEY_CLASSES, emptySet()) ?: emptySet())

    fun isRaceUnlocked(prefs: SharedPreferences, id: String): Boolean =
        id in defaultRaces || id in (prefs.getStringSet(KEY_RACES, emptySet()) ?: emptySet())

    fun unlockClass(prefs: SharedPreferences, id: String, cost: Int): Boolean {
        if (getTotalLP(prefs) < cost) return false
        val current = prefs.getStringSet(KEY_CLASSES, mutableSetOf()) ?: mutableSetOf()
        prefs.edit()
            .putInt(KEY_TOTAL_LP, getTotalLP(prefs) - cost)
            .putStringSet(KEY_CLASSES, current + id)
            .apply()
        return true
    }

    fun unlockRace(prefs: SharedPreferences, id: String, cost: Int): Boolean {
        if (getTotalLP(prefs) < cost) return false
        val current = prefs.getStringSet(KEY_RACES, mutableSetOf()) ?: mutableSetOf()
        prefs.edit()
            .putInt(KEY_TOTAL_LP, getTotalLP(prefs) - cost)
            .putStringSet(KEY_RACES, current + id)
            .apply()
        return true
    }

    // ── LP formula ────────────────────────────────────────────────────────────

    fun calculateRunLP(
        floor: Int,
        level: Int,
        enemiesKilled: Int,
        bossesKilled: Int,
        modifiers: List<RunModifier>
    ): Int {
        val base        = floor * 5 + level * 10 + enemiesKilled * 2 + bossesKilled * 20
        val curseBonus  = modifiers.count { it.type == RunModifierType.CURSE  } * 15
        val mixedBonus  = modifiers.count { it.type == RunModifierType.MIXED  } * 8
        return (base + curseBonus + mixedBonus).coerceAtLeast(10) // minimum 10 LP per run
    }
}
