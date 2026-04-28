package com.example.game

/**
 * INTEGRATION GUIDE
 * Inventory & Equipment System + Skill Tree System
 * 
 * How to integrate these systems into RoguelikeMainActivity
 */

// ========== ADD TO RoguelikeMainActivity CLASS ==========

/*

// ========== NEW PROPERTIES ==========

// Inventory
private val inventory = mutableListOf<Item>()
private var equippedWeapon: Item? = null
private var equippedArmor: Item? = null
private var equippedAccessory: Item? = null
private val maxInventorySize = 20

// Skills
private var skillPoints = 0
private val unlockedSkills = mutableSetOf<String>()

// ========== MODIFY EXISTING FUNCTIONS ==========

// Update updateStats() to include equipment bonuses
private fun updateStats() {
    // Calculate equipment bonuses
    var equipBonus_atk = 0
    var equipBonus_def = 0
    var equipBonus_hp = 0
    var equipBonus_luck = 0
    var equipBonus_soul = 0
    
    listOf(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
        item?.stats?.forEach { (stat, value) ->
            when (stat) {
                "atk" -> equipBonus_atk += value
                "def" -> equipBonus_def += value
                "hp" -> equipBonus_hp += value
                "luck" -> equipBonus_luck += value
                "soul" -> equipBonus_soul += value
            }
        }
    }
    
    // Calculate skill bonuses
    val skillBonus_atk = calculateSkillBonus("atk_bonus")
    val skillBonus_def = calculateSkillBonus("def_bonus")
    val skillBonus_hp = calculateSkillBonus("hp_bonus")
    val skillBonus_luck = unlockedSkills.count { it.contains("luck") }
    
    // Apply all bonuses
    val totalAtk = baseAtk + equipBonus_atk + skillBonus_atk
    val totalDef = baseDef + equipBonus_def + skillBonus_def
    val totalMaxHp = baseMaxHp + equipBonus_hp + skillBonus_hp
    val totalLuck = baseLuck + equipBonus_luck + skillBonus_luck
    
    tvStats.text = buildString {
        append("Lv.$level ${playerClass.emoji} $playerName | ")
        append("❤️ $hp/$totalMaxHp | ")
        append("⚔️ $totalAtk | ")
        append("🛡️ $totalDef | ")
        append("🍀 $totalLuck | ")
        append("✨ $soulPoints | ")
        append("💰 $gold | ")
        append("🏰 $floor")
    }
    
    val hpPercent = (hp * 100) / totalMaxHp
    progressHP.progress = hpPercent
    
    val expPercent = (exp * 100) / expToNext
    progressEXP.progress = expPercent
    
    updateInventoryPreview()
    
    if (hp <= 0) {
        gameOver()
    }
}

// Add skill bonus calculator
private fun calculateSkillBonus(effectKey: String): Int {
    var total = 0
    unlockedSkills.forEach { skillId ->
        val skill = findSkillById(skillId)
        skill?.effects?.get(effectKey)?.let { value ->
            total += (value as? Int) ?: 0
        }
    }
    return total
}

// Update levelUp() to give skill points
private fun levelUp() {
    level++
    exp -= expToNext
    expToNext = (expToNext * 1.5).roundToInt()
    
    // Give skill point every level
    skillPoints++
    
    // Stat increases
    val hpIncrease = 5 + Random.nextInt(-1, 3)
    val atkIncrease = 2 + Random.nextInt(-1, 2)
    val defIncrease = 1 + Random.nextInt(0, 2)
    
    maxHp += hpIncrease
    hp = maxHp
    atk += atkIncrease
    def += defIncrease
    
    Toast.makeText(
        this,
        "🎊 LEVEL UP! Lv.$level\n❤️+$hpIncrease ⚔️+$atkIncrease 🛡️+$defIncrease\n💎 +1 Skill Point",
        Toast.LENGTH_LONG
    ).show()
}

// ========== NEW FUNCTIONS FOR INVENTORY ==========

fun showInventory() {
    val inventoryManager = InventoryManager(
        activity = this,
        inventory = inventory,
        equippedWeapon = equippedWeapon,
        equippedArmor = equippedArmor,
        equippedAccessory = equippedAccessory,
        maxInventorySize = maxInventorySize,
        onInventoryChanged = { weapon, armor, accessory ->
            equippedWeapon = weapon
            equippedArmor = armor
            equippedAccessory = accessory
            updateStats()
        }
    )
    
    inventoryManager.showInventory()
}

fun addItemToInventory(item: Item): Boolean {
    if (inventory.size >= maxInventorySize) {
        Toast.makeText(this, "🎒 Inventory Full!", Toast.LENGTH_SHORT).show()
        return false
    }
    
    // Check if stackable
    if (item.stackable) {
        val existing = inventory.find { it.id == item.id }
        if (existing != null) {
            existing.quantity += item.quantity
            Toast.makeText(this, "📦 ${item.emoji} ${item.name} x${item.quantity}", Toast.LENGTH_SHORT).show()
            return true
        }
    }
    
    inventory.add(item)
    Toast.makeText(this, "✨ Got: ${item.emoji} ${item.name}", Toast.LENGTH_SHORT).show()
    return true
}

private fun updateInventoryPreview() {
    val items = mutableListOf<String>()
    equippedWeapon?.let { items.add(it.emoji) }
    equippedArmor?.let { items.add(it.emoji) }
    equippedAccessory?.let { items.add(it.emoji) }
    
    tvInventoryPreview.text = buildString {
        append("🎒 ${inventory.size}/$maxInventorySize")
        if (items.isNotEmpty()) {
            append(" | ")
            append(items.joinToString(" "))
        }
    }
}

// ========== NEW FUNCTIONS FOR SKILL TREE ==========

fun showSkillTree() {
    val skillTreeManager = SkillTreeManager(
        activity = this,
        playerClass = playerClass,
        skillPoints = skillPoints,
        unlockedSkills = unlockedSkills,
        onSkillChanged = { points, skills ->
            skillPoints = points
            unlockedSkills.clear()
            unlockedSkills.addAll(skills)
            updateStats()
        }
    )
    
    skillTreeManager.showSkillTree()
}

private fun findSkillById(skillId: String): Skill? {
    // TODO: Implement skill lookup
    return null
}

// ========== MODIFY showCharacterSheet() ==========

private fun showCharacterSheet() {
    tvStory.text = """
╔══════════════════════════════╗
║       📊 Character Sheet     ║
╚══════════════════════════════╝

👤 $playerName
${playerClass.emoji} Lv.$level ${playerClass.displayName}

📊 BASE STATS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❤️  HP: $hp/$maxHp
⚔️  Attack: $atk
🛡️  Defense: $def
🍀 Luck: $luck
✨ Soul Points: $soulPoints
💰 Gold: $gold

⚔️ EQUIPMENT BONUS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
${getEquipmentBonusText()}

🌳 SKILL BONUS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
💎 Skill Points: $skillPoints
🔓 Unlocked: ${unlockedSkills.size}
${getSkillBonusText()}

📈 PROGRESS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🏰 Floor: $floor
🎯 EXP: $exp/$expToNext
⏰ Playtime: ${formatPlayTime(totalPlayTime)}

🎒 INVENTORY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Items: ${inventory.size}/$maxInventorySize
Weapon: ${equippedWeapon?.name ?: "None"}
Armor: ${equippedArmor?.name ?: "None"}
Accessory: ${equippedAccessory?.name ?: "None"}
    """.trimIndent()
    
    setChoices(
        "⏮️ Back",
        "🎒 Inventory",
        "🌳 Skills",
        "💾 Save",
        { continueAdventure() },
        { showInventory() },
        { showSkillTree() },
        {
            saveGame()
            Toast.makeText(this, "💾 Saved!", Toast.LENGTH_SHORT).show()
        }
    )
}

private fun getEquipmentBonusText(): String {
    var totalAtk = 0
    var totalDef = 0
    var totalHp = 0
    var totalLuck = 0
    
    listOf(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
        item?.stats?.forEach { (stat, value) ->
            when (stat) {
                "atk" -> totalAtk += value
                "def" -> totalDef += value
                "hp" -> totalHp += value
                "luck" -> totalLuck += value
            }
        }
    }
    
    val bonuses = mutableListOf<String>()
    if (totalAtk > 0) bonuses.add("⚔️ +$totalAtk")
    if (totalDef > 0) bonuses.add("🛡️ +$totalDef")
    if (totalHp > 0) bonuses.add("❤️ +$totalHp")
    if (totalLuck > 0) bonuses.add("🍀 +$totalLuck")
    
    return if (bonuses.isEmpty()) "None" else bonuses.joinToString(" ")
}

private fun getSkillBonusText(): String {
    if (unlockedSkills.isEmpty()) return "None"
    
    val bonuses = mutableListOf<String>()
    val atkBonus = calculateSkillBonus("atk_bonus")
    val defBonus = calculateSkillBonus("def_bonus")
    val hpBonus = calculateSkillBonus("hp_bonus")
    
    if (atkBonus > 0) bonuses.add("⚔️ +$atkBonus%")
    if (defBonus > 0) bonuses.add("🛡️ +$defBonus%")
    if (hpBonus > 0) bonuses.add("❤️ +$hpBonus%")
    
    return if (bonuses.isEmpty()) "See skill tree" else bonuses.joinToString(" ")
}

// ========== SAVE/LOAD SUPPORT ==========

private fun saveGame() {
    prefs.edit().apply {
        // ... existing saves ...
        
        // Save inventory
        putInt("inventory_size", inventory.size)
        inventory.forEachIndexed { index, item ->
            putString("inventory_$index", itemToJson(item))
        }
        
        // Save equipment
        equippedWeapon?.let { putString("equipped_weapon", itemToJson(it)) }
        equippedArmor?.let { putString("equipped_armor", itemToJson(it)) }
        equippedAccessory?.let { putString("equipped_accessory", itemToJson(it)) }
        
        // Save skills
        putInt("skill_points", skillPoints)
        putStringSet("unlocked_skills", unlockedSkills)
        
        apply()
    }
}

private fun loadGame() {
    // ... existing loads ...
    
    // Load inventory
    inventory.clear()
    val invSize = prefs.getInt("inventory_size", 0)
    repeat(invSize) { index ->
        prefs.getString("inventory_$index", null)?.let { json ->
            jsonToItem(json)?.let { item ->
                inventory.add(item)
            }
        }
    }
    
    // Load equipment
    equippedWeapon = prefs.getString("equipped_weapon", null)?.let { jsonToItem(it) }
    equippedArmor = prefs.getString("equipped_armor", null)?.let { jsonToItem(it) }
    equippedAccessory = prefs.getString("equipped_accessory", null)?.let { jsonToItem(it) }
    
    // Load skills
    skillPoints = prefs.getInt("skill_points", 0)
    prefs.getStringSet("unlocked_skills", emptySet())?.let {
        unlockedSkills.clear()
        unlockedSkills.addAll(it)
    }
    
    updateStats()
    continueAdventure()
}

private fun itemToJson(item: Item): String {
    return JSONObject().apply {
        put("id", item.id)
        put("name", item.name)
        put("emoji", item.emoji)
        put("type", item.type.name)
        put("description", item.description)
        put("rarity", item.rarity.name)
        put("quantity", item.quantity)
        
        val statsJson = JSONObject()
        item.stats.forEach { (key, value) ->
            statsJson.put(key, value)
        }
        put("stats", statsJson)
    }.toString()
}

private fun jsonToItem(json: String): Item? {
    return try {
        val obj = JSONObject(json)
        val stats = mutableMapOf<String, Int>()
        val statsJson = obj.getJSONObject("stats")
        statsJson.keys().forEach { key ->
            stats[key] = statsJson.getInt(key)
        }
        
        Item(
            id = obj.getString("id"),
            name = obj.getString("name"),
            emoji = obj.getString("emoji"),
            type = ItemType.valueOf(obj.getString("type")),
            description = obj.getString("description"),
            rarity = ItemRarity.valueOf(obj.getString("rarity")),
            stats = stats,
            quantity = obj.getInt("quantity")
        )
    } catch (e: Exception) {
        null
    }
}

*/

// ========== USAGE EXAMPLES ==========

/**
 * EXAMPLE 1: Giving items as rewards
 */
/*
private fun handleCombatEnd(victory: Boolean, rewards: CombatManager.CombatRewards?, player: Player) {
    if (victory && rewards != null) {
        // Add items to inventory
        rewards.items.forEach { item ->
            addItemToInventory(item)
        }
        
        // ... rest of code
    }
}
*/

/**
 * EXAMPLE 2: Treasure room with loot
 */
/*
private fun treasureRoom(random: Random) {
    val item = ItemGenerator.generateEquipment(
        floor = floor,
        rarity = when (random.nextInt(100)) {
            in 0..49 -> ItemRarity.COMMON
            in 50..74 -> ItemRarity.UNCOMMON
            in 75..89 -> ItemRarity.RARE
            in 90..97 -> ItemRarity.EPIC
            else -> ItemRarity.LEGENDARY
        },
        random = random
    )
    
    tvStory.text = """
╔══════════════════════════════╗
║       💎 Floor $floor         ║
║       TREASURE ROOM          ║
╚══════════════════════════════╝

คุณพบหีบสมบัติ!

${item.rarity.emoji} ${item.emoji} ${item.name}
${item.rarity.name}

${item.description}

เก็บหรือไม่?
    """.trimIndent()
    
    setChoices(
        "✨ เก็บ",
        "❌ ทิ้ง",
        "📊 ตรวจสอบ",
        "",
        {
            if (addItemToInventory(item)) {
                floor++
                updateStats()
                enterFloor()
            }
        },
        {
            Toast.makeText(this, "ทิ้งไอเทม", Toast.LENGTH_SHORT).show()
            floor++
            updateStats()
            enterFloor()
        },
        {
            showItemInfo(item)
        },
        {}
    )
}
*/

/**
 * EXAMPLE 3: Skill point rewards
 */
/*
private fun specialEventRoom() {
    tvStory.text = """
╔══════════════════════════════╗
║       ✨ Special Event       ║
╚══════════════════════════════╝

คุณพบ Shrine ลึกลับ
แสงสว่างส่องออกมา...

"ผู้กล้า รับพลังนี้ไว้"

ได้รับ 2 Skill Points!
    """.trimIndent()
    
    skillPoints += 2
    
    setChoices(
        "🙏 ขอบคุณ",
        "🌳 เปิด Skill Tree",
        "",
        "",
        {
            floor++
            updateStats()
            enterFloor()
        },
        {
            showSkillTree()
        },
        {},
        {}
    )
}
*/

/**
 * SUMMARY OF FEATURES:
 * 
 * ✅ INVENTORY SYSTEM:
 * - Grid layout with filters (All, Weapon, Armor, etc.)
 * - Sorting (Rarity, Type, Name, Newest)
 * - Equipment slots (Weapon, Armor, Accessory)
 * - Stack items
 * - Compare equipment
 * - Use consumables
 * - Discard items
 * - Auto-sort
 * - Stat bonuses from equipment
 * 
 * ✅ SKILL TREE SYSTEM:
 * - Multiple tree types (Combat, Defense, Utility)
 * - Tiered progression (Tier 1, 2, 3)
 * - Prerequisites system
 * - Passive & Active skills
 * - Skill points from leveling
 * - Reset skills (costs gold)
 * - Stat bonuses from skills
 * - Visual tree display
 * 
 * ✅ INTEGRATION:
 * - Save/Load support
 * - Combat integration
 * - Stat calculations
 * - Reward systems
 * 
 * NEXT STEPS:
 * 1. Add all skills for all classes (currently only Warrior has full tree)
 * 2. Implement active skill usage in combat
 * 3. Add more item types (potions, scrolls, materials)
 * 4. Implement crafting system
 * 5. Add set bonuses for equipment
 */
