package com.example.game

/**
 * INTEGRATION GUIDE - How to use Combat System
 * 
 * Add these functions to your RoguelikeMainActivity.kt
 */

// ========== ADD TO RoguelikeMainActivity CLASS ==========

/*
// Add these methods to RoguelikeMainActivity class

private fun combatRoom(random: Random) {
    tvStory.text = """
╔══════════════════════════════╗
║       ⚔️ Floor $floor         ║
║       COMBAT ENCOUNTER       ║
╚══════════════════════════════╝

คุณเจอศัตรู!

กำลังเตรียมการต่อสู้...
    """.trimIndent()
    
    setChoices("", "", "", "") { }
    
    // Generate enemy based on floor
    val enemy = if (floor % 10 == 0) {
        // Boss every 10 floors
        EnemyGenerator.generateBossEnemy(floor, currentSeed + floor, difficulty)
    } else {
        // Normal enemy
        EnemyGenerator.generateEnemy(floor, currentSeed + floor, difficulty)
    }
    
    // Create player data
    val player = Player(
        name = playerName,
        classData = playerClass,
        level = level,
        hp = hp,
        maxHp = maxHp,
        atk = atk,
        def = def,
        luck = luck,
        speed = 5 + luck, // Speed based on luck
        soulPoints = soulPoints
    )
    
    // Wait a moment then start combat
    Handler(Looper.getMainLooper()).postDelayed({
        startCombat(player, enemy)
    }, 1000)
}

private fun startCombat(player: Player, enemy: Enemy) {
    val combatManager = CombatManager(
        activity = this,
        player = player,
        enemy = enemy,
        onCombatEnd = { victory, rewards ->
            handleCombatEnd(victory, rewards, player)
        }
    )
    
    combatManager.startCombat()
}

private fun handleCombatEnd(
    victory: Boolean, 
    rewards: CombatManager.CombatRewards?,
    player: Player
) {
    // Update player stats from combat
    hp = player.hp
    soulPoints = player.soulPoints
    
    if (victory && rewards != null) {
        // Add rewards
        exp += rewards.exp
        gold += rewards.gold
        soulPoints += rewards.soul
        
        // Add items to inventory
        rewards.items.forEach { item ->
            addItemToInventory(item)
        }
        
        // Check level up
        checkLevelUp()
        
        // Update stats
        updateStats()
        
        // Show victory screen
        showCombatVictory(rewards)
    } else {
        // Player died or fled
        if (hp <= 0) {
            gameOver()
        } else {
            // Fled successfully - continue to next room
            continueAdventure()
        }
    }
}

private fun showCombatVictory(rewards: CombatManager.CombatRewards) {
    tvStory.text = """
╔══════════════════════════════╗
║        🎉 VICTORY! 🎉        ║
╚══════════════════════════════╝

คุณชนะการต่อสู้!

💰 REWARDS:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✨ EXP: +${rewards.exp}
💰 Gold: +${rewards.gold}
✨ Soul: +${rewards.soul}

${if (rewards.items.isNotEmpty()) {
    "🎁 LOOT:\n" + rewards.items.joinToString("\n") { 
        "${it.rarity.emoji} ${it.emoji} ${it.name}" 
    }
} else ""}

📊 สถิติปัจจุบัน:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Lv.$level (${exp}/${expToNext} EXP)
❤️ HP: $hp/$maxHp
✨ Soul: $soulPoints
💰 Gold: $gold

${if (levelUpPending) "🎊 LEVEL UP! สถิติเพิ่มขึ้น!" else ""}
    """.trimIndent()
    
    setChoices(
        "⬇️ ลงชั้นถัดไป",
        if (rewards.items.isNotEmpty()) "🎒 ดูไอเทม" else "",
        "💾 บันทึกเกม",
        "📊 ดูสถิติ",
        {
            floor++
            updateStats()
            enterFloor()
        },
        {
            if (rewards.items.isNotEmpty()) {
                showInventory()
            }
        },
        {
            saveGame()
            Toast.makeText(this, "💾 บันทึกแล้ว", Toast.LENGTH_SHORT).show()
        },
        {
            showCharacterSheet()
        }
    )
}

private var levelUpPending = false

private fun checkLevelUp() {
    levelUpPending = false
    
    while (exp >= expToNext) {
        levelUp()
        levelUpPending = true
    }
}

private fun levelUp() {
    level++
    exp -= expToNext
    expToNext = (expToNext * 1.5).roundToInt()
    
    // Stat increases
    val hpIncrease = 5 + Random.nextInt(-1, 3)
    val atkIncrease = 2 + Random.nextInt(-1, 2)
    val defIncrease = 1 + Random.nextInt(0, 2)
    
    maxHp += hpIncrease
    hp = maxHp // Full heal on level up
    atk += atkIncrease
    def += defIncrease
    
    // Class-specific bonuses
    when (playerClass) {
        CharacterClass.WARRIOR -> {
            maxHp += 3
            def += 1
        }
        CharacterClass.MAGE -> {
            soulPoints += 10
            atk += 1
        }
        CharacterClass.ROGUE -> {
            luck += 1
        }
        CharacterClass.CLERIC -> {
            soulPoints += 5
            maxHp += 2
        }
        CharacterClass.RANGER -> {
            luck += 1
            atk += 1
        }
    }
    
    hp = maxHp
    
    Toast.makeText(
        this,
        "🎊 LEVEL UP! Lv.$level\n❤️+$hpIncrease ⚔️+$atkIncrease 🛡️+$defIncrease",
        Toast.LENGTH_LONG
    ).show()
}

private fun addItemToInventory(item: Item) {
    if (inventory.size >= maxInventorySize) {
        Toast.makeText(this, "🎒 กระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
        return
    }
    
    // Check if stackable
    if (item.stackable) {
        val existing = inventory.find { it.id == item.id }
        if (existing != null) {
            existing.quantity++
            return
        }
    }
    
    inventory.add(item)
}

// ========== EXAMPLE: Modified enterFloor() ==========

private fun enterFloor() {
    // Generate floor based on seed
    val floorSeed = currentSeed + floor
    val random = Random(floorSeed)
    
    val eventType = random.nextInt(100)
    
    when {
        eventType < 30 -> normalRoom(random)      // 30% - Safe room
        eventType < 70 -> combatRoom(random)      // 40% - Combat
        eventType < 85 -> treasureRoom(random)    // 15% - Treasure
        eventType < 95 -> merchantRoom()          // 10% - Merchant
        else -> specialRoom(random)               // 5% - Special
    }
}

*/

// ========== HELPER EXTENSIONS ==========

// Add these to MainActivity if not already present

/*
import android.os.Handler
import android.os.Looper
import kotlin.math.roundToInt

fun RoguelikeMainActivity.createGradientDrawable(
    startColor: Int, 
    endColor: Int
): android.graphics.drawable.Drawable {
    return android.graphics.drawable.GradientDrawable(
        android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(startColor, endColor)
    ).apply {
        cornerRadius = dp(4).toFloat()
    }
}

fun RoguelikeMainActivity.dp(value: Int): Int {
    return (value * resources.displayMetrics.density).toInt()
}

fun RoguelikeMainActivity.getThemeColor(type: String): Int {
    return when (colorTheme) {
        "Dark" -> when (type) {
            "background" -> Color.parseColor("#0a0e27")
            "card" -> Color.parseColor("#1a1a2e")
            "primary" -> Color.parseColor("#16213e")
            "accent" -> Color.parseColor("#0f3460")
            "text" -> Color.parseColor("#ecf0f1")
            "textSecondary" -> Color.parseColor("#bdc3c7")
            else -> Color.GRAY
        }
        "Light" -> when (type) {
            "background" -> Color.parseColor("#f5f6fa")
            "card" -> Color.WHITE
            "primary" -> Color.parseColor("#3498db")
            "accent" -> Color.parseColor("#2ecc71")
            "text" -> Color.parseColor("#2c3e50")
            "textSecondary" -> Color.parseColor("#7f8c8d")
            else -> Color.GRAY
        }
        else -> Color.GRAY
    }
}

fun RoguelikeMainActivity.createCard(): CardView {
    return CardView(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dp(12), dp(6), dp(12), dp(6))
        }
        radius = dp(12).toFloat()
        cardElevation = dp(4).toFloat()
        setCardBackgroundColor(getThemeColor("card"))
    }
}
*/

// ========== COMBAT SYSTEM USAGE SUMMARY ==========

/**
 * QUICK START GUIDE:
 * 
 * 1. Copy CombatSystem.kt และ EnemyGenerator.kt ไปยังโปรเจค
 * 
 * 2. เพิ่ม functions ด้านบนไปยัง RoguelikeMainActivity.kt
 * 
 * 3. แก้ไข combatRoom() function ใน enterFloor() ให้เรียกใช้ Combat System
 * 
 * 4. ระบบจะทำงานอัตโนมัติ:
 *    - สร้างศัตรูตามชั้นและ difficulty
 *    - แสดง Combat UI
 *    - จัดการเทิร์นการต่อสู้
 *    - คำนวณ damage, status effects
 *    - ให้รางวัลเมื่อชนะ
 *    - จัดการ Level Up
 * 
 * FEATURES:
 * ✅ Turn-based combat
 * ✅ Special abilities for each class
 * ✅ Status effects (Burn, Poison, Stun, etc.)
 * ✅ Enemy AI with skills
 * ✅ Combat animations
 * ✅ Loot system
 * ✅ EXP and Level up
 * ✅ Boss fights every 10 floors
 * ✅ Difficulty scaling
 * ✅ Combat stats tracking
 * 
 * NEXT STEPS:
 * - Inventory & Equipment System (ต่อไป)
 * - Skill Tree System
 * - Crafting System
 */
