package com.example.game.data.model

// ─────────────────────────────────────────────
// PLAYER
// ─────────────────────────────────────────────

data class PlayerStats(
    val hp: Int = 20,
    val maxHp: Int = 20,
    val mana: Int = 30,
    val maxMana: Int = 30,
    val stamina: Int = 10,
    val maxStamina: Int = 10,
    val level: Int = 1,
    val exp: Int = 0,
    val expToNext: Int = 100,
    val strength: Int = 5,
    val agility: Int = 3,
    val intelligence: Int = 4,
    val defense: Int = 3,
    val luck: Int = 5,
    val gold: Int = 0,
    val soulPoints: Int = 0
)

fun PlayerStats.gainExp(amount: Int): PlayerStats {
    val newExp = exp + amount
    return if (newExp >= expToNext) {
        copy(
            exp = newExp - expToNext,
            level = level + 1,
            expToNext = (expToNext * 1.5).toInt(),
            maxHp = maxHp + 5,
            hp = maxHp + 5,
            maxMana = maxMana + 5,
            mana = maxMana + 5,
            maxStamina = maxStamina + 2,
            stamina = maxStamina + 2,
            strength = strength + 1,
            defense = defense + 1,
            intelligence = intelligence + 1
        )
    } else {
        copy(exp = newExp)
    }
}

fun PlayerStats.heal(amount: Int) = copy(hp = minOf(hp + amount, maxHp))
fun PlayerStats.restoreMana(amount: Int) = copy(mana = minOf(mana + amount, maxMana))
fun PlayerStats.takeDamage(amount: Int) = copy(hp = maxOf(0, hp - amount))

// ─────────────────────────────────────────────
// ITEMS
// ─────────────────────────────────────────────

enum class ItemType { WEAPON, ARMOR, ACCESSORY, CONSUMABLE, MATERIAL, KEY_ITEM, SPECIAL }

enum class ItemRarity(val color: Int, val emoji: String) {
    COMMON(0xFF9E9E9E.toInt(), "⚪"),
    UNCOMMON(0xFF4CAF50.toInt(), "🟢"),
    RARE(0xFF2196F3.toInt(), "🔵"),
    EPIC(0xFF9C27B0.toInt(), "🟣"),
    LEGENDARY(0xFFFF9800.toInt(), "🟡"),
    MYTHIC(0xFFF44336.toInt(), "🔴")
}

data class Item(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val type: ItemType,
    val value: Int = 0,
    val atkBonus: Int = 0,
    val defBonus: Int = 0,
    val hpRestore: Int = 0,
    val manaRestore: Int = 0,
    val luckBonus: Int = 0,
    val quantity: Int = 1,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val stats: Map<String, Int> = emptyMap(),
    val weaponType: String? = null,
    val stackable: Boolean = true,
    val consumable: Boolean = false
) {
    fun getStat(key: String): Int {
        val k = key.lowercase()
        // Try direct map access or common aliases
        val mappedKey = when(k) {
            "atk", "attack", "atkbonus" -> "atk"
            "def", "defense", "defbonus" -> "def"
            "luck", "luk", "luckbonus" -> "luck"
            "hp", "maxhp", "hprestore" -> "hp"
            "mp", "mana", "manarestore" -> "mp"
            "soul", "soulpoints" -> "soul"
            else -> k
        }
        
        val fromMap = stats[mappedKey] ?: stats[k] ?: stats[key]
        if (fromMap != null) return fromMap

        // Fallback to legacy explicit fields
        return when (mappedKey) {
            "atk" -> atkBonus
            "def" -> defBonus
            "luck" -> luckBonus
            "hp" -> hpRestore
            "mp" -> manaRestore
            else -> 0
        }
    }

    fun getAllStats(): Map<String, Int> {
        val combined = stats.toMutableMap()
        if (atkBonus != 0 && !combined.containsKey("atk") && !combined.containsKey("attack")) {
            combined["atk"] = atkBonus
        }
        if (defBonus != 0 && !combined.containsKey("def") && !combined.containsKey("defense")) {
            combined["def"] = defBonus
        }
        if (luckBonus != 0 && !combined.containsKey("luck") && !combined.containsKey("luk")) {
            combined["luck"] = luckBonus
        }
        if (hpRestore != 0 && !combined.containsKey("hpRestore")) {
            combined["hpRestore"] = hpRestore
        }
        return combined
    }
}

// ─────────────────────────────────────────────
// SKILLS
// ─────────────────────────────────────────────

data class Skill(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val manaCost: Int,
    val staminaCost: Int = 0,
    val minDamage: Int = 0,
    val maxDamage: Int = 0,
    val healing: Int = 0,
    val effect: SkillEffect = SkillEffect.NONE
)

enum class SkillEffect { NONE, STUN, POISON, BUFF_ATK, BUFF_DEF }

// ─────────────────────────────────────────────
// ENEMIES
// ─────────────────────────────────────────────

data class EnemyState(
    val id: String,
    val name: String,
    val emoji: String,
    val hp: Int,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val expReward: Int,
    val goldReward: Int
)

// ─────────────────────────────────────────────
// NARRATIVE / CHOICES
// ─────────────────────────────────────────────

enum class StoryEntryType { NARRATION, PLAYER_ACTION, COMBAT_LOG, SYSTEM, AI_STORY }

data class StoryEntry(
    val text: String,
    val type: StoryEntryType = StoryEntryType.NARRATION,
    val id: Long = System.currentTimeMillis() + (Math.random() * 1000).toLong()
)

data class Choice(
    val label: String,
    val actionId: String,
    val icon: String = "▶"
)

// ─────────────────────────────────────────────
// SHOP
// ─────────────────────────────────────────────

data class ShopItem(
    val item: Item,
    val price: Int
)

// ─────────────────────────────────────────────
// INVENTORY
// ─────────────────────────────────────────────

data class Inventory(
    val items: List<Item> = emptyList()
) {
    val equippedWeapon: Item? get() = items.firstOrNull { it.type == ItemType.WEAPON }
    val equippedArmor: Item? get() = items.firstOrNull { it.type == ItemType.ARMOR }
    val consumables: List<Item> get() = items.filter { it.type == ItemType.CONSUMABLE }

    fun totalAtk(): Int = items.filter { it.type == ItemType.WEAPON }.sumOf { it.atkBonus }
    fun totalDef(): Int = items.filter { it.type == ItemType.ARMOR }.sumOf { it.defBonus }
    fun totalLuck(): Int = items.sumOf { it.luckBonus }

    fun add(item: Item): Inventory {
        val existing = items.indexOfFirst { it.id == item.id && it.type == ItemType.CONSUMABLE }
        return if (existing >= 0) {
            val updated = items[existing].copy(quantity = items[existing].quantity + item.quantity)
            copy(items = items.toMutableList().also { it[existing] = updated })
        } else {
            copy(items = items + item)
        }
    }

    fun remove(itemId: String, qty: Int = 1): Inventory {
        val idx = items.indexOfFirst { it.id == itemId }
        if (idx < 0) return this
        val item = items[idx]
        return if (item.quantity <= qty) {
            copy(items = items.filterIndexed { i, _ -> i != idx })
        } else {
            copy(items = items.toMutableList().also { it[idx] = item.copy(quantity = item.quantity - qty) })
        }
    }
}

// ─────────────────────────────────────────────
// GAME STATE
// ─────────────────────────────────────────────

enum class GameMode { STORY, COMBAT, SHOPPING, GAME_OVER, VICTORY }

data class GameState(
    val player: PlayerStats = PlayerStats(),
    val inventory: Inventory = Inventory(),
    val skills: List<Skill> = emptyList(),
    val currentFloor: Int = 1,
    val maxFloor: Int = 12,
    val mode: GameMode = GameMode.STORY,
    val storyLog: List<StoryEntry> = emptyList(),
    val choices: List<Choice> = emptyList(),
    val currentEnemy: EnemyState? = null,
    val shopItems: List<ShopItem> = emptyList(),
    val isLoading: Boolean = false,
    val flags: Map<String, Boolean> = emptyMap()
)

// ─────────────────────────────────────────────
// CHAT / AI
// ─────────────────────────────────────────────

data class ChatMessage(
    val id: Long = System.currentTimeMillis() + (Math.random() * 9999).toLong(),
    val role: String,
    val content: String,
    val isLoading: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class FantasyWorld(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val startingStory: String,
    val playerRole: String,
    val specialFeatures: String
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val currentWorld: FantasyWorld? = null,
    val isLoading: Boolean = false,
    val inputEnabled: Boolean = true,
    val worldSummary: String = "",
    val messageCount: Int = 0
)
