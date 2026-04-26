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
        // Steak: temporary ATK boost in combat
        if (name.contains("สเต็ก")) stats["atk"] = 5
        // Poison cure: clears status effects in combat
        if (name.contains("ล้างพิษ")) stats["purify"] = 1
        // Teleport scroll: skip to next floor
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

    private fun generateWeapon(floor: Int, rarity: ItemRarity, random: Random): Item {
        val weaponBases = listOf(
            Triple("ดาบ", "⚔️", "sword"),
            Triple("ขวาน", "🪓", "axe"),
            Triple("หอก", "🔱", "spear"),
            Triple("มีดสั้น", "🗡️", "dagger"),
            Triple("คทา", "🧙", "staff"),
            Triple("ธนู", "🏹", "bow"),
            Triple("กระบอง", "🔨", "mace"),
            Triple("เคียว", "☠️", "scythe"),
            Triple("ดาบยักษ์", "🗡️", "sword"),
            Triple("กิโยติน", "🪓", "axe"),
            Triple("สนับมือ", "🥊", "mace"),
            Triple("แส้", "⛓️", "mace"),
            Triple("ลูกตุ้มเหล็ก", "⛓️", "mace"),
            Triple("ไม้เท้าเทพเจ้า", "🦯", "staff"),
            Triple("คัมภีร์เวทย์", "📖", "grimoire"),
            Triple("มีดบิน", "🔪", "dagger"),
            Triple("คาตานะ", "🏮", "katana"),
            Triple("กรงเล็บปีศาจ", "🐾", "dagger"),
            Triple("โล่หนาม", "🛡️", "shield"),
            Triple("ค้อนศึก", "⚒️", "mace"),
            Triple("หน้าไม้", "🏹", "bow")
        )
        val (baseName, emoji, wType) = weaponBases.random(random)

        val adjectives = when (rarity) {
            ItemRarity.COMMON -> listOf("ธรรมดา", "เก่า", "สนิมเขรอะ")
            ItemRarity.UNCOMMON -> listOf("คมกริบ", "ทนทาน", "เหล็กกล้า")
            ItemRarity.RARE -> listOf("อาคม", "นักรบ", "เยือกเย็น")
            ItemRarity.EPIC -> listOf("มหากาพย์", "วิญญาณ", "เปลวเพลิง")
            ItemRarity.LEGENDARY -> listOf("ในตำนาน", "เทพเจ้า", "ศักดิ์สิทธิ์")
            ItemRarity.MYTHIC -> listOf("นิรันดร์", "ผู้สร้าง", "ทำลายล้าง")
        }

        val name = "${adjectives.random(random)}$baseName"
        val powerScale = (floor * 1.5).roundToInt().coerceAtLeast(1)
        val rarityMult = (rarity.ordinal + 1) * 1.2
        val atkBonus = (powerScale * rarityMult * (0.8 + random.nextDouble() * 0.4)).roundToInt()

        return Item(
            id = "wpn_${random.nextInt(1000000)}",
            name = name,
            emoji = emoji,
            description = "เพิ่มพลังโจมตี $atkBonus หน่วย",
            type = ItemType.WEAPON,
            stats = mapOf("atk" to atkBonus),
            weaponType = wType,
            rarity = rarity,
            stackable = false
        )
    }

    private fun generateArmor(floor: Int, rarity: ItemRarity, random: Random): Item {
        val armorBases = listOf(
            "ชุดเกราะ" to "🛡️", "เสื้อคลุม" to "🧥", "เกราะเบา" to "🛡️", "เกราะหนัก" to "⛓️"
        )
        val (baseName, emoji) = armorBases.random(random)

        val adjectives = when (rarity) {
            ItemRarity.COMMON -> listOf("ผ้าขาดๆ", "หนังเก่า", "ผุพัง")
            ItemRarity.UNCOMMON -> listOf("ชุบแข็ง", "ทนทาน", "เสริมเหล็ก")
            ItemRarity.RARE -> listOf("พิทักษ์", "อัศวิน", "ศิลา")
            ItemRarity.EPIC -> listOf("พรายแสง", "จันทรา", "มังกร")
            ItemRarity.LEGENDARY -> listOf("ศักดิ์สิทธิ์", "ราชา", "วีรบุรุษ")
            ItemRarity.MYTHIC -> listOf("อมตะ", "เทพนิยาย", "พระเจ้า")
        }

        val name = "${baseName}${adjectives.random(random)}"
        val powerScale = (floor * 1.2).roundToInt().coerceAtLeast(1)
        val rarityMult = (rarity.ordinal + 1) * 1.1
        val defBonus = (powerScale * rarityMult * (0.8 + random.nextDouble() * 0.4)).roundToInt()

        return Item(
            id = "arm_${random.nextInt(1000000)}",
            name = name,
            emoji = emoji,
            description = "เพิ่มพลังป้องกัน $defBonus หน่วย",
            type = ItemType.ARMOR,
            stats = mapOf("def" to defBonus),
            rarity = rarity,
            stackable = false
        )
    }

    private fun generateAccessory(floor: Int, rarity: ItemRarity, random: Random): Item {
        val names = listOf("แหวน", "สร้อยคอ", "เครื่องราง", "ต่างหู")
        val name = names.random(random)
        val luckBonus = (rarity.ordinal + 1) * 2 + random.nextInt(0, 5)

        return Item(
            id = "acc_${random.nextInt(1000000)}",
            name = name,
            emoji = "💍",
            description = "เพิ่มโชค $luckBonus หน่วย",
            type = ItemType.ACCESSORY,
            stats = mapOf("luck" to luckBonus),
            rarity = rarity,
            stackable = false
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
            else -> ItemRarity.COMMON
        }
    }

    fun generateMaterial(type: String, random: Random): Item {
        return Item(
            id = "mat_${type}_${random.nextInt(1000000)}",
            name = when(type) {
                "boss_soul" -> "วิญญาณบอส"
                "iron" -> "แร่เหล็ก"
                "gold" -> "ทองคำ"
                else -> "วัสดุแปลกๆ"
            },
            emoji = "💎",
            description = "ใช้สำหรับคราฟต์ไอเทม",
            type = ItemType.MATERIAL,
            rarity = if (type == "boss_soul") ItemRarity.LEGENDARY else ItemRarity.COMMON,
            stackable = true
        )
    }
}
