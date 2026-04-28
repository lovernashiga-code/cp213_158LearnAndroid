package com.example.game

import com.example.game.data.model.Item
import com.example.game.data.model.ItemRarity
import com.example.game.data.model.ItemType
import kotlin.random.Random
import kotlin.math.roundToInt

object ItemGenerator {

    fun generateConsumable(id: String? = null, random: Random = Random): Item {
        val consumables = mutableListOf(
            Triple("ยาเพิ่มพลังกาย (เล็ก)", "🧪", "ฟื้นฟู 20 HP"),
            Triple("ยาเพิ่มพลังกาย (กลาง)", "🧪", "ฟื้นฟู 50 HP"),
            Triple("ยาเพิ่มพลังกาย (ใหญ่)", "🧪", "ฟื้นฟู 100 HP"),
            Triple("สเต็กเนื้อย่าง", "🥩", "ฟื้นฟู 30 HP และเพิ่มพลังโจมตีชั่วคราว"),
            Triple("ขนมปังแข็งๆ", "🍞", "ฟื้นฟู 10 HP"),
            Triple("ยาล้างพิษ", "🧪", "ล้างสถานะผิดปกติ"),
            Triple("ม้วนคัมภีร์เทเลพอร์ต", "📜", "พาคุณไปยังชั้นถัดไปทันที")
        )

        // Rare chance for Golden Apple
        if (random.nextInt(100) < 5) {
            consumables.add(Triple("แอปเปิ้ลสีทอง", "🍎", "ฟื้นฟู HP ทั้งหมด"))
        }

        val (name, emoji, desc) = consumables.random(random)
        val hpRestore = when {
            desc.contains("20 HP") -> 20
            desc.contains("50 HP") -> 50
            desc.contains("100 HP") -> 100
            desc.contains("30 HP") -> 30
            desc.contains("10 HP") -> 10
            desc.contains("ทั้งหมด") -> 999
            else -> 0
        }

        val stats = mutableMapOf<String, Int>()
        if (hpRestore > 0) stats["hpRestore"] = hpRestore
        if (name.contains("สเต็ก")) stats["atk"] = 5
        if (name.contains("ล้างพิษ")) stats["purify"] = 1
        if (name.contains("เทเลพอร์ต")) stats["teleport"] = 1

        return Item(
            id = id ?: "cons_${random.nextInt(1000000)}",
            name = name,
            emoji = emoji,
            description = desc,
            type = ItemType.CONSUMABLE,
            stats = stats,
            stackable = true,
            consumable = true,
            rarity = if (name.contains("สีทอง") || name.contains("เทเลพอร์ต")) ItemRarity.RARE else ItemRarity.COMMON
        )
    }

    fun generateEquipment(floor: Int, rarity: ItemRarity? = null, random: Random = Random): Item {
        val targetRarity = rarity ?: pickRarity(floor, random)
        val type = listOf(ItemType.WEAPON, ItemType.ARMOR, ItemType.ACCESSORY).random(random)

        return when (type) {
            ItemType.WEAPON -> generateWeapon(floor, targetRarity, random)
            ItemType.ARMOR -> generateArmor(floor, targetRarity, random)
            ItemType.ACCESSORY -> generateAccessory(floor, targetRarity, random)
            else -> generateWeapon(floor, targetRarity, random)
        }
    }

    // ── Weapon archetype definitions ─────────────────────────────────────────
    private data class WeaponArchetype(
        val baseName: String,
        val emoji: String,
        val wType: String,
        val atkMult: Float,       // multiplier on base ATK power
        val specialKey: String,   // extra stat key (empty = none)
        val specialBase: Int,     // base value of extra stat at floor 1
        val specialRarityScale: Float // extra per rarity tier
    )

    private val weaponArchetypes = listOf(
        // Sword/Greatsword — balanced ATK + crit
        WeaponArchetype("ดาบ",         "⚔️",  "sword",     1.0f,  "crit",       5,  2.5f),
        WeaponArchetype("ดาบยักษ์",    "🗡️",  "sword",     1.15f, "crit",       4,  2.0f),
        WeaponArchetype("คาตานะ",      "🏮",  "katana",    1.1f,  "crit",       7,  3.0f),
        WeaponArchetype("หอก",         "🔱",  "spear",     1.05f, "crit",       4,  2.0f),
        // Dagger — low ATK, high crit + dodge + speed
        WeaponArchetype("มีดสั้น",     "🗡️",  "dagger",    0.75f, "crit",       10, 4.0f),
        WeaponArchetype("มีดบิน",      "🔪",  "dagger",    0.70f, "crit",       12, 4.5f),
        WeaponArchetype("กรงเล็บปีศาจ","🐾",  "dagger",    0.80f, "crit",       8,  3.5f),
        // Axe — high ATK + armor penetration
        WeaponArchetype("ขวาน",        "🪓",  "axe",       1.2f,  "armor_pen",  10, 4.0f),
        WeaponArchetype("กิโยติน",     "🪓",  "axe",       1.25f, "armor_pen",  12, 5.0f),
        // Staff/Grimoire — normal ATK + soul regen per turn
        WeaponArchetype("คทา",         "🧙",  "staff",     1.0f,  "soul_regen", 1,  0.5f),
        WeaponArchetype("ไม้เท้าเทพเจ้า","🦯","staff",    1.05f, "soul_regen", 1,  0.8f),
        WeaponArchetype("คัมภีร์เวทย์", "📖", "grimoire",  1.0f,  "soul_regen", 2,  1.0f),
        // Bow — normal ATK + speed + dodge
        WeaponArchetype("ธนู",         "🏹",  "bow",       0.95f, "dodge",      5,  2.0f),
        WeaponArchetype("หน้าไม้",     "🏹",  "bow",       1.0f,  "dodge",      6,  2.5f),
        // Mace/Hammer — ATK + stun chance
        WeaponArchetype("กระบอง",      "🔨",  "mace",      1.05f, "stun_chance",10, 4.0f),
        WeaponArchetype("สนับมือ",     "🥊",  "mace",      0.95f, "stun_chance",8,  3.5f),
        WeaponArchetype("แส้",         "⛓️",  "mace",      0.90f, "stun_chance",8,  3.0f),
        WeaponArchetype("ลูกตุ้มเหล็ก","⛓️", "mace",      1.0f,  "stun_chance",12, 4.5f),
        WeaponArchetype("ค้อนศึก",     "⚒️",  "mace",      1.1f,  "stun_chance",10, 4.0f),
        // Scythe — high ATK + lifesteal
        WeaponArchetype("เคียว",       "☠️",  "scythe",    1.1f,  "lifesteal",  5,  2.5f),
        // Shield (off-hand) — DEF + HP
        WeaponArchetype("โล่หนาม",    "🛡️",  "shield",    0.3f,  "def",        8,  3.0f)
    )

    private fun generateWeapon(floor: Int, rarity: ItemRarity, random: Random): Item {
        val arch = weaponArchetypes.random(random)

        val adjectives = when (rarity) {
            ItemRarity.COMMON    -> listOf("ธรรมดา", "เก่า", "สนิมเขรอะ")
            ItemRarity.UNCOMMON  -> listOf("คมกริบ", "ทนทาน", "เหล็กกล้า")
            ItemRarity.RARE      -> listOf("อาคม", "นักรบ", "เยือกเย็น")
            ItemRarity.EPIC      -> listOf("มหากาพย์", "วิญญาณ", "เปลวเพลิง")
            ItemRarity.LEGENDARY -> listOf("ในตำนาน", "เทพเจ้า", "ศักดิ์สิทธิ์")
            ItemRarity.MYTHIC    -> listOf("นิรันดร์", "ผู้สร้าง", "ทำลายล้าง")
        }

        val name = "${adjectives.random(random)}${arch.baseName}"
        val powerScale = (floor * 1.5).roundToInt().coerceAtLeast(1)
        val rarityMult = (rarity.ordinal + 1) * 1.2
        val atkBonus = (powerScale * rarityMult * arch.atkMult * (0.8 + random.nextDouble() * 0.4)).roundToInt()

        val stats = mutableMapOf<String, Int>()

        // Shield: give DEF + HP instead of ATK
        if (arch.wType == "shield") {
            val defVal = (powerScale * rarityMult * 0.9 * (0.8 + random.nextDouble() * 0.4)).roundToInt()
            val hpVal  = (arch.specialBase + rarity.ordinal * arch.specialRarityScale * 3).roundToInt()
                .coerceAtLeast(5)
            stats["atk"] = atkBonus
            stats["def"] = defVal
            stats["hp"]  = hpVal
            return Item(
                id = "wpn_${random.nextInt(1000000)}",
                name = name, emoji = arch.emoji,
                description = "ATK +$atkBonus | DEF +$defVal | HP +$hpVal",
                type = ItemType.WEAPON, stats = stats,
                weaponType = arch.wType, rarity = rarity, stackable = false
            )
        }

        stats["atk"] = atkBonus

        // Dagger: also give spd + dodge on top of crit
        if (arch.wType == "dagger") {
            val spdVal   = (3 + rarity.ordinal).coerceAtMost(10)
            val dodgeVal = (3 + rarity.ordinal).coerceAtMost(10)
            stats["spd"]   = spdVal
            stats["dodge"] = dodgeVal
        }

        // Bow: also give spd on top of dodge
        if (arch.wType == "bow") {
            val spdVal = (3 + rarity.ordinal).coerceAtMost(10)
            stats["spd"] = spdVal
        }

        // Special stat
        if (arch.specialKey.isNotEmpty()) {
            val specialVal = (arch.specialBase + rarity.ordinal * arch.specialRarityScale +
                    random.nextDouble() * arch.specialRarityScale).roundToInt()
                .coerceAtLeast(1)
            stats[arch.specialKey] = specialVal
        }

        val descParts = mutableListOf("ATK +$atkBonus")
        if (stats.containsKey("crit"))        descParts.add("Crit +${stats["crit"]}%")
        if (stats.containsKey("dodge"))       descParts.add("Dodge +${stats["dodge"]}%")
        if (stats.containsKey("spd"))         descParts.add("SPD +${stats["spd"]}")
        if (stats.containsKey("armor_pen"))   descParts.add("ArmorPen ${stats["armor_pen"]}%")
        if (stats.containsKey("soul_regen"))  descParts.add("SP/Turn +${stats["soul_regen"]}")
        if (stats.containsKey("stun_chance")) descParts.add("Stun ${stats["stun_chance"]}%")
        if (stats.containsKey("lifesteal"))   descParts.add("Lifesteal ${stats["lifesteal"]}%")

        return Item(
            id = "wpn_${random.nextInt(1000000)}",
            name = name, emoji = arch.emoji,
            description = descParts.joinToString(" | "),
            type = ItemType.WEAPON, stats = stats,
            weaponType = arch.wType, rarity = rarity, stackable = false
        )
    }

    // ── Armor archetypes ──────────────────────────────────────────────────────
    private data class ArmorArchetype(
        val baseName: String,
        val emoji: String,
        val defMult: Float,
        val bonusKey: String,  // extra stat (empty = none)
        val bonusBase: Int
    )

    private val armorArchetypes = listOf(
        // Heavy armor: high DEF + small HP
        ArmorArchetype("เกราะหนัก",    "⛓️", 1.2f,  "hp",    15),
        ArmorArchetype("ชุดเกราะ",     "🛡️", 1.1f,  "hp",    10),
        // Light armor: medium DEF + dodge
        ArmorArchetype("เกราะเบา",     "🛡️", 0.85f, "dodge", 4),
        ArmorArchetype("หนังนักรบ",    "🧥", 0.80f, "dodge", 5),
        // Robe: lower DEF + soul regen
        ArmorArchetype("เสื้อคลุม",    "🧥", 0.70f, "soul_regen", 1),
        ArmorArchetype("ชุดจ้าวเวทย์", "🧙", 0.65f, "soul_regen", 2),
        // Cloak: DEF + speed
        ArmorArchetype("เสื้อคลุมเร็ว","🌪️", 0.75f, "spd",   4),
        ArmorArchetype("ชุดเงา",       "🌑", 0.78f, "spd",   5)
    )

    private fun generateArmor(floor: Int, rarity: ItemRarity, random: Random): Item {
        val arch = armorArchetypes.random(random)

        val adjectives = when (rarity) {
            ItemRarity.COMMON    -> listOf("ผ้าขาดๆ", "หนังเก่า", "ผุพัง")
            ItemRarity.UNCOMMON  -> listOf("ชุบแข็ง", "ทนทาน", "เสริมเหล็ก")
            ItemRarity.RARE      -> listOf("พิทักษ์", "อัศวิน", "ศิลา")
            ItemRarity.EPIC      -> listOf("พรายแสง", "จันทรา", "มังกร")
            ItemRarity.LEGENDARY -> listOf("ศักดิ์สิทธิ์", "ราชา", "วีรบุรุษ")
            ItemRarity.MYTHIC    -> listOf("อมตะ", "เทพนิยาย", "พระเจ้า")
        }

        val name = "${arch.baseName}${adjectives.random(random)}"
        val powerScale = (floor * 1.2).roundToInt().coerceAtLeast(1)
        val rarityMult = (rarity.ordinal + 1) * 1.1
        val defVal = (powerScale * rarityMult * arch.defMult * (0.8 + random.nextDouble() * 0.4)).roundToInt()

        val stats = mutableMapOf("def" to defVal)
        val descParts = mutableListOf("DEF +$defVal")

        if (arch.bonusKey.isNotEmpty()) {
            val bonusVal = (arch.bonusBase + rarity.ordinal * 1.5 +
                    random.nextDouble() * 2).roundToInt().coerceAtLeast(1)
            stats[arch.bonusKey] = bonusVal
            when (arch.bonusKey) {
                "hp"         -> descParts.add("HP +$bonusVal")
                "dodge"      -> descParts.add("Dodge +$bonusVal%")
                "soul_regen" -> descParts.add("SP/Turn +$bonusVal")
                "spd"        -> descParts.add("SPD +$bonusVal")
            }
        }

        return Item(
            id = "arm_${random.nextInt(1000000)}",
            name = name, emoji = arch.emoji,
            description = descParts.joinToString(" | "),
            type = ItemType.ARMOR, stats = stats,
            rarity = rarity, stackable = false
        )
    }

    // ── Accessory archetypes ──────────────────────────────────────────────────
    private data class AccessoryArchetype(
        val baseName: String,
        val emoji: String,
        val primaryKey: String,
        val secondaryKey: String
    )

    private val accessoryArchetypes = listOf(
        AccessoryArchetype("แหวนนักรบ",    "💍", "luck", "crit"),
        AccessoryArchetype("แหวนนักล่า",   "💍", "luck", "dodge"),
        AccessoryArchetype("สร้อยคอเวทย์", "📿", "luck", "soul_regen"),
        AccessoryArchetype("สร้อยแห่งชีวิต","📿","luck", "lifesteal"),
        AccessoryArchetype("เครื่องรางโชค", "🧿", "luck", "crit"),
        AccessoryArchetype("เครื่องรางพิษ","🧿", "luck", "stun_chance"),
        AccessoryArchetype("ต่างหูสายลม",  "✨", "luck", "spd"),
        AccessoryArchetype("ต่างหูนักรบ",  "✨", "luck", "armor_pen"),
        AccessoryArchetype("แหวนวิญญาณ",   "💍", "luck", "soul_regen"),
        AccessoryArchetype("เครื่องรางมรณะ","🧿","luck", "lifesteal")
    )

    private fun generateAccessory(floor: Int, rarity: ItemRarity, random: Random): Item {
        val arch = accessoryArchetypes.random(random)
        val luckVal = (rarity.ordinal + 1) * 2 + random.nextInt(0, 5)

        val secondaryVal = when (arch.secondaryKey) {
            "crit", "dodge", "stun_chance", "armor_pen" ->
                ((rarity.ordinal + 1) * 3 + random.nextInt(0, 4)).coerceAtMost(30)
            "soul_regen" ->
                (rarity.ordinal / 2 + 1).coerceAtMost(5)
            "lifesteal" ->
                ((rarity.ordinal + 1) * 2 + random.nextInt(0, 3)).coerceAtMost(20)
            "spd" ->
                ((rarity.ordinal + 1) * 2 + random.nextInt(0, 4)).coerceAtMost(15)
            else -> 0
        }

        val stats = mutableMapOf("luck" to luckVal, arch.secondaryKey to secondaryVal)
        val descSecondary = when (arch.secondaryKey) {
            "crit"        -> "Crit +$secondaryVal%"
            "dodge"       -> "Dodge +$secondaryVal%"
            "soul_regen"  -> "SP/Turn +$secondaryVal"
            "lifesteal"   -> "Lifesteal $secondaryVal%"
            "stun_chance" -> "Stun $secondaryVal%"
            "spd"         -> "SPD +$secondaryVal"
            "armor_pen"   -> "ArmorPen $secondaryVal%"
            else          -> ""
        }

        return Item(
            id = "acc_${random.nextInt(1000000)}",
            name = arch.baseName, emoji = arch.emoji,
            description = "Luck +$luckVal | $descSecondary",
            type = ItemType.ACCESSORY, stats = stats,
            rarity = rarity, stackable = false
        )
    }

    private fun pickRarity(floor: Int, random: Random): ItemRarity {
        val roll = random.nextInt(100)
        val floorBonus = floor / 5
        return when {
            roll + floorBonus >= 98 -> ItemRarity.MYTHIC
            roll + floorBonus >= 92 -> ItemRarity.LEGENDARY
            roll + floorBonus >= 80 -> ItemRarity.EPIC
            roll + floorBonus >= 60 -> ItemRarity.RARE
            roll + floorBonus >= 30 -> ItemRarity.UNCOMMON
            else                    -> ItemRarity.COMMON
        }
    }

    fun generateMaterial(type: String, random: Random): Item {
        return Item(
            id = "mat_${type}_${random.nextInt(1000000)}",
            name = when(type) {
                "boss_soul" -> "วิญญาณบอส"
                "iron"      -> "แร่เหล็ก"
                "gold"      -> "ทองคำ"
                else        -> "วัสดุแปลกๆ"
            },
            emoji = "💎",
            description = "ใช้สำหรับคราฟต์ไอเทม",
            type = ItemType.MATERIAL,
            rarity = if (type == "boss_soul") ItemRarity.LEGENDARY else ItemRarity.COMMON,
            stackable = true
        )
    }
}
