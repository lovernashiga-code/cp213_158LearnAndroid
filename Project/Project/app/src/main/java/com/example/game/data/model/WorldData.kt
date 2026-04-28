package com.example.game.data.model

object WorldData {

    // ─────────────────────────────────────────────
    // SKILLS
    // ─────────────────────────────────────────────

    val SKILLS = listOf(
        Skill(
            id = "shadow_strike",
            name = "Shadow Strike",
            emoji = "🌑",
            description = "A swift dark slash dealing heavy damage.",
            manaCost = 15,
            minDamage = 18,
            maxDamage = 30
        ),
        Skill(
            id = "fire_slash",
            name = "Fire Slash",
            emoji = "🔥",
            description = "Engulf your blade in flames.",
            manaCost = 10,
            minDamage = 12,
            maxDamage = 22
        ),
        Skill(
            id = "soul_pulse",
            name = "Soul Pulse",
            emoji = "💜",
            description = "Channel void energy for devastating damage.",
            manaCost = 0,
            staminaCost = 5,
            minDamage = 25,
            maxDamage = 40
        ),
        Skill(
            id = "mend",
            name = "Mend",
            emoji = "✨",
            description = "Restore 20 HP using inner energy.",
            manaCost = 12,
            healing = 20
        )
    )

    // ─────────────────────────────────────────────
    // ITEMS
    // ─────────────────────────────────────────────

    val SWORD = Item(
        id = "sword",
        name = "Iron Sword",
        emoji = "⚔️",
        description = "A reliable blade forged in shadow-iron.",
        type = ItemType.WEAPON,
        value = 50,
        atkBonus = 4
    )

    val SHIELD = Item(
        id = "shield",
        name = "Void Shield",
        emoji = "🛡️",
        description = "Absorbs dark energy, reducing damage.",
        type = ItemType.ARMOR,
        value = 30,
        defBonus = 3
    )

    val POTION = Item(
        id = "potion",
        name = "Health Potion",
        emoji = "🧪",
        description = "Restores 15 HP.",
        type = ItemType.CONSUMABLE,
        value = 20,
        hpRestore = 15
    )

    val MANA_POTION = Item(
        id = "mana_potion",
        name = "Mana Potion",
        emoji = "💧",
        description = "Restores 20 Mana.",
        type = ItemType.CONSUMABLE,
        value = 25,
        manaRestore = 20
    )

    val KEY = Item(
        id = "key",
        name = "Ancient Key",
        emoji = "🗝️",
        description = "Opens a sealed chamber.",
        type = ItemType.KEY_ITEM,
        value = 0
    )

    val AMULET = Item(
        id = "amulet",
        name = "Void Amulet",
        emoji = "🔮",
        description = "Increases luck and soul resonance.",
        type = ItemType.KEY_ITEM,
        value = 50,
        luckBonus = 4
    )

    // ─────────────────────────────────────────────
    // ENEMIES
    // ─────────────────────────────────────────────

    val SHADOW_GHOUL = EnemyState(
        id = "shadow_ghoul",
        name = "Shadow Ghoul",
        emoji = "👻",
        hp = 20, maxHp = 20,
        attack = 4, defense = 1,
        expReward = 30, goldReward = 10
    )

    val CURSED_SENTINEL = EnemyState(
        id = "cursed_sentinel",
        name = "Cursed Sentinel",
        emoji = "🗡️",
        hp = 35, maxHp = 35,
        attack = 6, defense = 2,
        expReward = 55, goldReward = 18
    )

    val WRAITH = EnemyState(
        id = "wraith",
        name = "Ethereal Wraith",
        emoji = "💀",
        hp = 50, maxHp = 50,
        attack = 8, defense = 3,
        expReward = 90, goldReward = 30
    )

    val VOID_LORD = EnemyState(
        id = "void_lord",
        name = "The Void Lord",
        emoji = "🌑",
        hp = 100, maxHp = 100,
        attack = 12, defense = 5,
        expReward = 500, goldReward = 200
    )

    // ─────────────────────────────────────────────
    // SHOP INVENTORIES
    // ─────────────────────────────────────────────

    val FLOOR_3_SHOP = listOf(
        ShopItem(SHIELD, 30),
        ShopItem(POTION.copy(quantity = 2), 35),
        ShopItem(AMULET, 50)
    )

    val FLOOR_10_SHOP = listOf(
        ShopItem(SWORD.copy(id = "silver_sword", name = "Silver Blade", atkBonus = 7), 80),
        ShopItem(SHIELD.copy(id = "dark_shield", name = "Dark Shield", defBonus = 5), 70),
        ShopItem(MANA_POTION.copy(quantity = 2), 40),
        ShopItem(POTION.copy(quantity = 3), 45),
        ShopItem(AMULET, 50)
    )

    // ─────────────────────────────────────────────
    // ISEKAI WORLDS
    // ─────────────────────────────────────────────

    val FANTASY_WORLDS = listOf(
        FantasyWorld(
            id = "arcadia",
            name = "โลกมหาเวทย์แห่งอาร์คาเดีย",
            emoji = "🔮",
            description = "โลกแห่งเวทมนตร์และสิ่งมีชีวิตลึกลับ",
            startingStory = "คุณถูกดูดเข้ามาในโลกแห่งเวทมนตร์ที่ชื่ออาร์คาเดีย ที่นี่มนุษย์และสิ่งมีชีวิตลึกลับอาศัยอยู่ร่วมกัน ระบบเวทมนตร์ 7 ธาตุเป็นพลังหลักของโลกนี้",
            playerRole = "นักเวทย์จากต่างโลก",
            specialFeatures = "ระบบเวทมนตร์ 7 ธาตุ, ดันเจี้ยนลึกลับ, กิลด์นักผจญภัย"
        ),
        FantasyWorld(
            id = "kingdom",
            name = "อาณาจักรดาบและเกียรติยศ",
            emoji = "⚔️",
            description = "อาณาจักรยุคกลางแห่งสงครามและเกียรติยศ",
            startingStory = "คุณปรากฏตัวในอาณาจักรกลางสงคราม ในฐานะนักรบปริศนาจากต่างโลก ทุกคนต้องการพลังของคุณ",
            playerRole = "อัศวินปริศนา",
            specialFeatures = "ระบบอัศวิน, สงครามระหว่างอาณาจักร, การแข่งขันดวล"
        ),
        FantasyWorld(
            id = "beast",
            name = "ดินแดนแห่งสัตว์ประหลาด",
            emoji = "🐉",
            description = "โลกที่เต็มไปด้วยสัตว์ประหลาดผู้ทรงพลัง",
            startingStory = "คุณตื่นขึ้นมาในป่าลึกที่เต็มไปด้วยสัตว์ประหลาดนานาชนิด พลังพิเศษที่คุณนำมาจากโลกเดิมทำให้คุณสามารถสื่อสารกับพวกมันได้",
            playerRole = "ผู้ฝึกสัตว์ในตำนาน",
            specialFeatures = "การจับและวิวัฒนาการสัตว์, สำรวจป่าดันเจี้ยน, ลีกการต่อสู้"
        ),
        FantasyWorld(
            id = "steampunk",
            name = "เมืองลอยฟ้าแห่งสตีมพังค์",
            emoji = "⚙️",
            description = "โลกอนาคตที่ผสมเวทมนตร์กับเทคโนโลยีไอน้ำ",
            startingStory = "คุณมาถึงเมืองลอยฟ้า Aethoria เมืองที่ขับเคลื่อนด้วยพลังไอน้ำและคริสตัลเวทมนตร์ การมาถึงของคุณไม่ใช่เรื่องบังเอิญ",
            playerRole = "วิศวกรปริศนา",
            specialFeatures = "ระบบสร้างสรรค์, เครื่องจักรเวทมนตร์, เมืองลอยฟ้า"
        ),
        FantasyWorld(
            id = "divine",
            name = "ดินแดนของเทพเจ้าและมนุษย์",
            emoji = "⚡",
            description = "โลกที่เทพเจ้าและมนุษย์ยังคงมีปฏิสัมพันธ์กัน",
            startingStory = "เทพเจ้าแห่งโชคชะตาเลือกคุณมาเป็นผู้พิทักษ์โลก Asgaria คุณได้รับพรจากเทพเจ้าและต้องเผชิญกับความชั่วร้ายที่คุกคามสามโลก",
            playerRole = "ผู้ถูกเลือกโดยเทพ",
            specialFeatures = "เวทมนตร์ศักดิ์สิทธิ์, พันธมิตรเทพเจ้า, ภารกิจศาสนา"
        )
    )
}
