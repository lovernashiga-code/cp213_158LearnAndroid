package com.example.game

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.card.MaterialCardView
import androidx.core.graphics.ColorUtils
import com.example.game.data.model.Item
import com.example.game.data.model.ItemType
import com.example.game.data.model.ItemRarity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Advanced Combat System for Roguelike RPG
 * Features: Turn-based, Elemental damage, Status effects, Combos, Special abilities
 */

// ========== COMBAT DATA CLASSES ==========

data class Enemy(
    val id: String,
    val name: String,
    val emoji: String,
    val level: Int,
    var hp: Int,
    val maxHp: Int,
    val atk: Int,
    val def: Int,
    val speed: Int,
    val expReward: Int,
    val goldReward: Int,
    val soulReward: Int,
    val skills: List<EnemySkill>,
    val weaknesses: List<ElementType> = emptyList(),
    val resistances: List<ElementType> = emptyList(),
    val lootTable: List<LootDrop> = emptyList(),
    val description: String = "",
    val isBoss: Boolean = false,
    var phase: Int = 1,
    val aiType: EnemyAIType = EnemyAIType.NORMAL,
    var rageStacks: Int = 0
)

data class EnemySkill(
    val name: String,
    val emoji: String,
    val damage: IntRange,
    val effect: StatusEffect? = null,
    val secondaryEffect: StatusEffect? = null,
    val isSelfTarget: Boolean = false,
    val targetsAllAllies: Boolean = false,
    val cooldown: Int = 0,
    var currentCooldown: Int = 0,
    val lifeSteal: Boolean = false,
    val ignoreShield: Boolean = false,
    val executionBonus: Boolean = false
)

data class LootDrop(
    val item: Item,
    val dropChance: Float // 0.0 to 1.0
)

enum class ElementType(val emoji: String, val color: Int) {
    PHYSICAL("⚔️", Color.parseColor("#95a5a6")),
    FIRE("🔥", Color.parseColor("#e74c3c")),
    ICE("❄️", Color.parseColor("#3498db")),
    LIGHTNING("⚡", Color.parseColor("#f1c40f")),
    POISON("☠️", Color.parseColor("#9b59b6")),
    HOLY("✨", Color.parseColor("#ecf0f1")),
    DARK("🌑", Color.parseColor("#34495e"))
}

enum class StatusEffect(val emoji: String, val displayName: String) {
    BURN("🔥", "เผาไหม้"),
    FREEZE("❄️", "แช่แข็ง"),
    STUN("💫", "มึนงง"),
    POISON("☠️", "ติดพิษ"),
    BLEED("💉", "เลือดไหล"),
    WEAK("⬇️", "อ่อนแอ"),
    DEFENSE_DOWN("🛡️⬇️", "ลดเกราะ"),
    REGEN("💚", "ฟื้นฟู"),
    SHIELD("🛡️", "โล่"),
    ATK_UP("⚔️⬆️", "เพิ่มโจมตี")
}

data class ActiveStatus(
    val effect: StatusEffect,
    var duration: Int,
    val power: Int
)

data class CombatAction(
    val type: ActionType,
    val name: String,
    val emoji: String,
    val damage: Int = 0,
    val element: ElementType = ElementType.PHYSICAL,
    val soulCost: Int = 0,
    val statusEffect: StatusEffect? = null,
    val secondaryEffect: StatusEffect? = null,
    val statusDuration: Int = 0,
    val hits: Int = 1,
    val description: String = "",
    val isSelfTarget: Boolean = false,
    val targetsAllAllies: Boolean = false,
    val lifeSteal: Boolean = false,
    val ignoreShield: Boolean = false,
    val executionBonus: Boolean = false
)

enum class ActionType {
    ATTACK,
    DEFEND,
    SPECIAL_ABILITY,
    ITEM,
    FLEE
}

enum class EnemyAIType {
    NORMAL,      // สุ่มโจมตี - พฤติกรรมพื้นฐาน
    AGGRESSIVE,  // รุกทันที - เลือกสกิลดาเมจสูงสุดเสมอ
    BERSERKER,   // สะสม Rage ทุกเทิร์น - ATK เพิ่มขึ้นเรื่อยๆ
    TACTICAL,    // เดบัฟก่อน แล้วถล่ม
    SUPPORT,     // รักษาเพื่อน บัฟทีม แล้วค่อยโจมตี
    PACK         // แข็งแกร่งขึ้นเมื่อมีพรรคพวก ถ้าเป็นตัวสุดท้ายจะ enrage
}

data class CombatLog(
    val message: String,
    val color: Int = Color.WHITE,
    val isImportant: Boolean = false
)

// ========== COMBAT MANAGER ==========

data class CombatRewards(
    val exp: Int,
    val gold: Int,
    val soulPoints: Int,
    val items: List<Item> = emptyList()
)

data class Player(
    val name: String,
    val classData: RoguelikeMainActivity.CharacterClass,
    val playerRace: RoguelikeMainActivity.Race = RoguelikeMainActivity.Race.HUMAN,
    var level: Int,
    var hp: Int,
    var maxHp: Int,
    var atk: Int,
    var def: Int,
    var luck: Int,
    var speed: Int,
    var soulPoints: Int,
    val unlockedSkills: Set<String> = emptySet()
)

class CombatManager(
    private val activity: RoguelikeMainActivity,
    private val player: Player,
    private val enemies: List<Enemy>,
    private val initialInventory: List<Item> = emptyList(),
    private val relics: List<Relic> = emptyList(),
    private val onCombatEnd: (Boolean, CombatRewards?) -> Unit
) {

    // Combat state
    private var turn = 0
    private var playerTurn = true
    private var combatEnded = false
    private var selectedEnemyIndex = 0
    private var deathProtectionUsed = false
    
    // Status effects
    private val playerStatuses = mutableListOf<ActiveStatus>()
    private val enemyStatusList = List(enemies.size) { mutableListOf<ActiveStatus>() }
    
    // Player items for combat
    private var playerCombatInventory = mutableListOf<Item>()
    
    // Combat stats
    private var damageDealt = 0
    private var damageTaken = 0
    private var turnCount = 0
    private var criticalHits = 0
    private var perfectDefends = 0
    
    // Combat UI
    private lateinit var combatLayout: LinearLayout
    private lateinit var logContainer: LinearLayout
    private lateinit var logScrollView: ScrollView
    private lateinit var playerHpBar: ProgressBar
    private val enemyHpBars = mutableListOf<ProgressBar>()
    private val tvEnemyHps = mutableListOf<TextView>()
    private val enemyStatusContainers = mutableListOf<LinearLayout>()
    private val enemyCards = mutableListOf<MaterialCardView>()
    private lateinit var tvPlayerHp: TextView
    private lateinit var actionButtonsContainer: LinearLayout
    private lateinit var tvTurnInfo: TextView
    private lateinit var tvPlayerSoul: TextView

    init {
        playerCombatInventory.addAll(initialInventory)
    }

    fun startCombat() {
        setupCombatUI()
        applyPassiveCombatEffects()
        val enemyNames = enemies.joinToString(", ") { it.name }
        addLog("เผชิญหน้ากับ $enemyNames!", Color.WHITE, true)
        
        // Check for surprise attack or first move
        val maxEnemySpeed = enemies.maxOfOrNull { it.speed } ?: 0
        if (player.speed >= maxEnemySpeed) {
            addLog("คุณเริ่มก่อน!", Color.LTGRAY)
            playerTurn = true
            showActionButtons()
        } else {
            addLog("ศัตรูเริ่มก่อน!", Color.LTGRAY)
            playerTurn = false
            Handler(Looper.getMainLooper()).postDelayed({
                executeEnemyTurn()
            }, 1000)
        }
    }
    
    private fun setupCombatUI() {
        combatLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            // Dynamic background variation based on floor or boss
            val isBossFight = enemies.any { it.isBoss }
            val bgColor = if (isBossFight) "#080000" else "#0a0a0a" 
            setBackgroundColor(Color.parseColor(bgColor))
            setPadding(activity.dp(16), activity.dp(24), activity.dp(16), activity.dp(16))
        }
        
        // Enemies Section
        val enemiesScroll = HorizontalScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = activity.dp(20) }
            isHorizontalScrollBarEnabled = false
        }
        
        val enemiesContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Calculate width based on number of enemies
        // If 1 enemy: full width or large card
        // If many: distribute width
        val enemyCount = enemies.size
        
        enemies.forEachIndexed { index, enemy ->
            val enemyCard = MaterialCardView(activity).apply {
                radius = activity.dp(12).toFloat()
                
                // Color based on rarity/boss status
                val cardColor = if (enemy.isBoss) "#1a0505" else "#141414"
                setCardBackgroundColor(Color.parseColor(cardColor))
                
                strokeWidth = activity.dp(if (enemy.isBoss) 2 else 1)
                strokeColor = Color.parseColor(if (enemy.isBoss) "#800000" else "#2a2a2a")
                elevation = if (enemy.isBoss) 8f else 2f
                
                // Dynamic weight/width based on enemy count
                val cardWidth = when (enemyCount) {
                    1 -> activity.dp(220)
                    2 -> activity.dp(160)
                    3 -> activity.dp(110)
                    else -> activity.dp(85) // 4 or more
                }
                
                layoutParams = LinearLayout.LayoutParams(
                    cardWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { 
                    marginEnd = if (index < enemies.size - 1) activity.dp(8) else 0 
                }
                
                setOnClickListener {
                    if (playerTurn && !combatEnded && enemy.hp > 0) {
                        selectEnemy(index)
                    }
                }
            }
            
            val enemyLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(activity.dp(8), activity.dp(10), activity.dp(8), activity.dp(10))
            }
            
            val tvEnemyName = TextView(activity).apply {
                text = "${enemy.emoji} ${enemy.name}"
                setTextColor(Color.WHITE)
                textSize = if (enemyCount > 2) 12f else 14f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            
            val hpBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = enemy.maxHp
                progress = enemy.hp
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    activity.dp(if (enemyCount > 2) 6 else 10)
                ).apply { topMargin = activity.dp(8) }
                val layers = arrayOf(
                    GradientDrawable().apply { cornerRadius = activity.dp(6).toFloat(); setColor(Color.parseColor("#1a1a1a")) },
                    android.graphics.drawable.ClipDrawable(
                        GradientDrawable().apply { 
                            cornerRadius = activity.dp(6).toFloat()
                            colors = intArrayOf(Color.parseColor("#4a0e0e"), Color.parseColor("#8b0000")) // Deep, desaturated red
                            orientation = GradientDrawable.Orientation.LEFT_RIGHT
                        },
                        Gravity.START,
                        android.graphics.drawable.ClipDrawable.HORIZONTAL
                    )
                )
                val layerDrawable = android.graphics.drawable.LayerDrawable(layers)
                layerDrawable.setId(0, android.R.id.background)
                layerDrawable.setId(1, android.R.id.progress)
                progressDrawable = layerDrawable
                
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    activity.dp(10)
                ).apply { topMargin = activity.dp(10); bottomMargin = activity.dp(4) }
            }
            
            val tvHp = TextView(activity).apply {
                text = "${enemy.hp}/${enemy.maxHp}"
                setTextColor(Color.parseColor("#bbbbbb"))
                textSize = if (enemyCount > 2) 9f else 11f
                typeface = Typeface.MONOSPACE
                gravity = if (enemyCount > 2) Gravity.CENTER else Gravity.END
            }

            val statusContainer = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    activity.dp(20)
                ).apply { topMargin = activity.dp(4) }
            }
            
            enemyLayout.addView(tvEnemyName)
            enemyLayout.addView(hpBar)
            enemyLayout.addView(tvHp)
            enemyLayout.addView(statusContainer)
            enemyCard.addView(enemyLayout)
            
            enemyCards.add(enemyCard)
            enemyHpBars.add(hpBar)
            tvEnemyHps.add(tvHp)
            enemyStatusContainers.add(statusContainer)
            enemiesContainer.addView(enemyCard)
        }
        
        enemiesScroll.addView(enemiesContainer)
        combatLayout.addView(enemiesScroll)
        
        // Highlight first enemy
        selectEnemy(0)
        
        // Log Section
        logScrollView = ScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply { 
                bottomMargin = activity.dp(20)
                topMargin = activity.dp(8)
            }
            isVerticalScrollBarEnabled = false
            setPadding(activity.dp(8), 0, activity.dp(8), 0)
        }
        
        logContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        logScrollView.addView(logContainer)
        combatLayout.addView(logScrollView)
        
        // Player Section
        val playerCard = MaterialCardView(activity).apply {
            radius = activity.dp(12).toFloat()
            setCardBackgroundColor(Color.parseColor("#141414"))
            strokeWidth = activity.dp(1)
            strokeColor = Color.parseColor("#2a2a2a")
            elevation = 4f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = activity.dp(16) }
        }
        
        val playerLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(activity.dp(16), activity.dp(16), activity.dp(16), activity.dp(16))
        }
        
        val headerLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        
        val tvPlayerName = TextView(activity).apply {
            text = player.name
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        tvPlayerSoul = TextView(activity).apply {
            text = "SP: ${player.soulPoints}"
            setTextColor(Color.parseColor("#5d3f6a")) // Desaturated purple
            textSize = 16f
            typeface = Typeface.MONOSPACE
        }
        
        headerLayout.addView(tvPlayerName)
        headerLayout.addView(tvPlayerSoul)
        
        playerHpBar = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = player.maxHp
            progress = player.hp
            
            val layers = arrayOf(
                GradientDrawable().apply { cornerRadius = activity.dp(4).toFloat(); setColor(Color.parseColor("#1a1a1a")) },
                android.graphics.drawable.ClipDrawable(
                    GradientDrawable().apply { 
                        cornerRadius = activity.dp(4).toFloat()
                        colors = intArrayOf(Color.parseColor("#1a3c1a"), Color.parseColor("#2d5a27")) // Deep, desaturated green
                        orientation = GradientDrawable.Orientation.LEFT_RIGHT
                    },
                    Gravity.START,
                    android.graphics.drawable.ClipDrawable.HORIZONTAL
                )
            )
            val layerDrawable = android.graphics.drawable.LayerDrawable(layers)
            layerDrawable.setId(0, android.R.id.background)
            layerDrawable.setId(1, android.R.id.progress)
            progressDrawable = layerDrawable
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.dp(12)
            ).apply { topMargin = activity.dp(8); bottomMargin = activity.dp(4) }
        }
        
        tvPlayerHp = TextView(activity).apply {
            text = "HP: ${player.hp}/${player.maxHp}"
            setTextColor(Color.LTGRAY)
            textSize = 14f
            gravity = Gravity.END
        }
        
        playerLayout.addView(headerLayout)
        playerLayout.addView(playerHpBar)
        playerLayout.addView(tvPlayerHp)
        playerCard.addView(playerLayout)
        combatLayout.addView(playerCard)
        
        // Actions Section
        actionButtonsContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }
        combatLayout.addView(actionButtonsContainer)
        
        activity.setContentView(combatLayout)
    }
    
    // Apply permanent passive effects at combat start (regen, hp_regen)
    private fun relicInt(key: String) = relics.sumOf { (it.effects[key] as? Int) ?: 0 }

    private fun applyRelicDeathProtection(): Boolean {
        if (deathProtectionUsed || player.hp > 0) return false
        val protection = relicInt("death_protection")
        if (protection <= 0) return false
        deathProtectionUsed = true
        player.hp = protection
        addLog("💊 ขวดน้ำยาฉุกเฉิน! ฟื้นคืน $protection HP!", Color.parseColor("#2ecc71"), true)
        updatePlayerHp()
        return true
    }

    private fun applyMinHpRelic() {
        val minPct = relicInt("min_hp_pct")
        if (minPct <= 0) return
        val minHp = max(1, (player.maxHp * minPct / 100f).roundToInt())
        if (player.hp < minHp) {
            player.hp = minHp
            addLog("👁️ ตาเทพเจ้า! HP จะไม่ต่ำกว่า $minPct%!", Color.parseColor("#9b59b6"))
            updatePlayerHp()
        }
    }

    private fun applyPassiveCombatEffects() {
        var passiveRegen = 0
        player.unlockedSkills.forEach { id ->
            val skill = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@forEach
            if (!skill.isPassive) return@forEach
            passiveRegen += (skill.effects["regen"] as? Int) ?: 0
            passiveRegen += (skill.effects["hp_regen"] as? Int) ?: 0
        }
        passiveRegen += relicInt("regen")
        if (passiveRegen > 0) {
            playerStatuses.add(ActiveStatus(StatusEffect.REGEN, 999, passiveRegen))
        }
        val spStart = relicInt("sp_start")
        if (spStart > 0) {
            player.soulPoints += spStart
            tvPlayerSoul.text = "SP: ${player.soulPoints}"
            addLog("🔮 ผลึกวิญญาณ! เริ่มต้น +$spStart SP!", Color.parseColor("#9b59b6"))
        }
    }

    private fun addLog(message: String, color: Int = Color.WHITE, isImportant: Boolean = false) {
        val isCritical = message.contains("CRITICAL", ignoreCase = true) || message.contains("รุนแรง", ignoreCase = true)
        
        val spannable = SpannableStringBuilder(message).apply {
            setSpan(ForegroundColorSpan(color), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (isCritical || isImportant) {
                setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(RelativeSizeSpan(if (isCritical) 1.2f else 1.1f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        
        val logView = TextView(activity).apply {
            text = spannable
            textSize = if (isCritical) 16f else if (isImportant) 15f else 14f
            setPadding(0, activity.dp(2), 0, activity.dp(2))
            alpha = 0f
        }
        
        logContainer.addView(logView)
        logView.animate().alpha(1f).setDuration(300).start()
        
        logScrollView.post {
            logScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    private fun showActionButtons() {
        if (combatEnded) return
        
        actionButtonsContainer.removeAllViews()
        actionButtonsContainer.visibility = View.VISIBLE
        
        val grid1 = LinearLayout(activity).apply { 
            orientation = LinearLayout.HORIZONTAL 
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, activity.dp(55))
        }
        val grid2 = LinearLayout(activity).apply { 
            orientation = LinearLayout.HORIZONTAL 
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, activity.dp(55)).apply {
                topMargin = activity.dp(4)
            }
        }
        
        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(activity.dp(4), 0, activity.dp(4), 0)
        }
        
        grid1.addView(createActionButton("โจมตี", Color.parseColor("#2a0a0a"), "⚔️") { executePlayerAttack() }, params)
        grid1.addView(createActionButton("ป้องกัน", Color.parseColor("#141414"), "🛡️") { executePlayerDefend() }, params)
        
        grid2.addView(createActionButton("ทักษะ", Color.parseColor("#141414"), "✨") { showSkillMenu() }, params)
        grid2.addView(createActionButton("ไอเทม", Color.parseColor("#141414"), "🎒") { showItemMenu() }, params)
        
        actionButtonsContainer.addView(grid1)
        actionButtonsContainer.addView(grid2)
    }

    private fun hideActionButtons() {
        actionButtonsContainer.visibility = View.GONE
    }

    private fun createActionButton(text: String, color: Int, icon: String = "", ignoreCombatEnd: Boolean = false, onClick: () -> Unit): View {
        return com.google.android.material.button.MaterialButton(activity).apply {
            this.text = if (icon.isNotEmpty()) "$icon $text" else text
            setBackgroundColor(color)
            setTextColor(Color.WHITE)
            textSize = 14f
            cornerRadius = activity.dp(4)
            insetTop = 0
            insetBottom = 0
            typeface = Typeface.create("serif", Typeface.BOLD)
            setOnClickListener { 
                if (!combatEnded || ignoreCombatEnd) {
                    onClick()
                }
            }
        }
    }
    
    private fun selectEnemy(index: Int) {
        if (index < 0 || index >= enemies.size || enemies[index].hp <= 0) return
        
        // Remove highlight from old
        if (selectedEnemyIndex < enemyCards.size) {
            enemyCards[selectedEnemyIndex].apply {
                strokeWidth = 0
                setCardBackgroundColor(Color.parseColor("#141414"))
            }
        }
        
        selectedEnemyIndex = index
        
        // Add highlight to new
        enemyCards[selectedEnemyIndex].apply {
            strokeWidth = activity.dp(2)
            setStrokeColor(Color.parseColor("#444444")) // Desaturated highlight
            setCardBackgroundColor(Color.parseColor("#1e1e1e"))
        }
    }

    private fun getSelectedEnemy() = enemies[selectedEnemyIndex]
    private fun getSelectedEnemyStatuses() = enemyStatusList[selectedEnemyIndex]

    private fun executePlayerAttack() {
        turnCount++
        hideActionButtons()
        
        val enemy = getSelectedEnemy()
        val enemyStatuses = getSelectedEnemyStatuses()
        
        // Calculate Crit — include passive crit_chance / crit_bonus from skills
        val skillCritBonus = player.unlockedSkills.sumOf { id ->
            val skill = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
            if (skill.isPassive)
                ((skill.effects["crit_chance"] as? Int) ?: 0) + ((skill.effects["crit_bonus"] as? Int) ?: 0)
            else 0
        }
        val isCrit = Random.nextFloat() < (player.luck * 0.05f + skillCritBonus / 100f)
        var damage = calculateDamage(player.atk, ElementType.PHYSICAL, enemy.def, playerStatuses, enemy)
        
        if (isCrit) {
            damage = (damage * 1.5f).roundToInt()
            criticalHits++
            addLog("⚡ คริติคอล!", Color.parseColor("#f1c40f"), true)
        }

        // Passive low_hp_dmg_boost (Bloodlust - Berserker, ORC race etc.)
        val lowHpBoost = player.unlockedSkills.sumOf { id ->
            val skill = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
            if (skill.isPassive) (skill.effects["low_hp_dmg_boost"] as? Int) ?: 0 else 0
        }
        if (lowHpBoost > 0 && player.hp.toFloat() / player.maxHp < 0.5f) {
            damage = (damage * (1f + lowHpBoost / 100f)).roundToInt()
            addLog("🩸 เลือดน้อยยิ่งตีแรง! ดาเมจเพิ่ม $lowHpBoost%", Color.parseColor("#c0392b"))
        }

        enemy.hp = max(0, enemy.hp - damage)
        damageDealt += damage

        // Passive lifesteal (Necromancer Undying)
        val lifeStealPassive = player.unlockedSkills.sumOf { id ->
            val s = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
            if (s.isPassive) (s.effects["lifesteal_passive"] as? Int) ?: 0 else 0
        }
        if (lifeStealPassive > 0 && damage > 0) {
            val healed = max(1, damage * lifeStealPassive / 100)
            player.hp = min(player.maxHp, player.hp + healed)
            updatePlayerHp()
            addLog("🩸 ดูดพลังชีวิต +$healed HP", Color.parseColor("#2ecc71"))
        }

        addLog("คุณโจมตี ${enemy.name} สร้างความเสียหาย $damage หน่วย", Color.WHITE)

        updateEnemyHp(selectedEnemyIndex)
        
        Handler(Looper.getMainLooper()).postDelayed({
            checkCombatEnd()
        }, 1000)
    }
    
    private fun executePlayerDefend() {
        turnCount++
        hideActionButtons()
        
        playerStatuses.add(ActiveStatus(StatusEffect.SHIELD, 1, player.def))
        addLog("คุณตั้งท่าป้องกัน! พลังป้องกันเพิ่มขึ้นในเทิร์นนี้", Color.LTGRAY)
        
        // Recover a bit more SP when defending
        val spGain = 10 + player.luck
        player.soulPoints += spGain
        tvPlayerSoul.text = "SP: ${player.soulPoints}"
        
        Handler(Looper.getMainLooper()).postDelayed({
            checkCombatEnd()
        }, 1000)
    }
    
    private fun showSkillMenu() {
        val skills = getUnlockedActiveSkills()
        if (skills.isEmpty()) {
            addLog("คุณยังไม่มีทักษะที่ใช้งานได้", Color.LTGRAY)
            return
        }
        
        showModernSelectionDialog(
            title = "เลือกทักษะ",
            items = skills,
            itemLabel = { it.name },
            itemSubLabel = { "${it.cost * 10} SP" },
            itemIcon = { it.icon },
            onSelected = { executeActiveSkill(it) }
        )
    }
    
    private fun showItemMenu() {
        val usableItems = playerCombatInventory.filter { it.consumable }
        if (usableItems.isEmpty()) {
            addLog("ไม่มีไอเทมที่ใช้งานได้", Color.LTGRAY)
            return
        }
        
        showModernSelectionDialog(
            title = "ใช้ไอเทม",
            items = usableItems,
            itemLabel = { it.name },
            itemSubLabel = { "คงเหลือ: ${it.quantity}" },
            itemIcon = { it.emoji },
            onSelected = { useItem(it) }
        )
    }

    private fun <T> showModernSelectionDialog(
        title: String,
        items: List<T>,
        itemLabel: (T) -> String,
        itemSubLabel: (T) -> String = { "" },
        itemIcon: (T) -> String = { "" },
        onSelected: (T) -> Unit
    ) {
        val dialogView = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(activity.dp(20), activity.dp(20), activity.dp(20), activity.dp(20))
        }

        val tvTitle = TextView(activity).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(0, 0, 0, activity.dp(16))
        }
        dialogView.addView(tvTitle)

        val scrollItems = ScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.dp(350)
            )
            isVerticalScrollBarEnabled = false
        }
        
        val itemsContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        val dialog = AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
            .create()

        items.forEach { item ->
            val itemCard = MaterialCardView(activity).apply {
                radius = activity.dp(16).toFloat()
                setCardBackgroundColor(Color.parseColor("#1e1e1e"))
                strokeWidth = activity.dp(1)
                strokeColor = Color.parseColor("#333333")
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = activity.dp(10) }
                isClickable = true
                isFocusable = true
                
                rippleColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#44ffffff"))
                
                setOnClickListener {
                    onSelected(item)
                    dialog.dismiss()
                }
            }

            val itemLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(activity.dp(16), activity.dp(16), activity.dp(16), activity.dp(16))
            }

            val tvIcon = TextView(activity).apply {
                text = itemIcon(item)
                textSize = 22f
                setPadding(0, 0, activity.dp(16), 0)
            }

            val textInfoLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvName = TextView(activity).apply {
                text = itemLabel(item)
                setTextColor(Color.WHITE)
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
            }

            val tvSub = TextView(activity).apply {
                text = itemSubLabel(item)
                setTextColor(Color.parseColor("#00d4ff")) // Cyan accent for SP/Qty
                textSize = 13f
                typeface = Typeface.MONOSPACE
            }

            textInfoLayout.addView(tvName)
            textInfoLayout.addView(tvSub)
            
            itemLayout.addView(tvIcon)
            itemLayout.addView(textInfoLayout)
            
            val tvArrow = TextView(activity).apply {
                text = "→"
                setTextColor(Color.parseColor("#555555"))
                textSize = 18f
            }
            itemLayout.addView(tvArrow)
            
            itemCard.addView(itemLayout)
            itemsContainer.addView(itemCard)
        }

        scrollItems.addView(itemsContainer)
        dialogView.addView(scrollItems)

        val btnCancel = Button(activity).apply {
            text = "ยกเลิก"
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                cornerRadius = activity.dp(12).toFloat()
                setColor(Color.parseColor("#cf352e")) // Dark red cancel button
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.dp(48)
            ).apply { topMargin = activity.dp(16) }
            setOnClickListener { dialog.dismiss() }
        }
        dialogView.addView(btnCancel)

        dialog.setView(dialogView)
        dialog.window?.setBackgroundDrawable(GradientDrawable().apply {
            setColor(Color.parseColor("#121212"))
            cornerRadius = activity.dp(24).toFloat()
        })
        dialog.show()
        
        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun useItem(item: Item) {
        addLog("ใช้ ${item.name}!", Color.parseColor("#f1c40f"))
        
        val itemStats = item.getAllStats()
        var isTeleport = false
        itemStats.forEach { (stat, value) ->
            when (stat.lowercase()) {
                "hp", "hprestore" -> {
                    player.hp = min(player.maxHp, player.hp + value)
                    addLog("ฟื้นฟู HP $value หน่วย", Color.parseColor("#2ecc71"))
                }
                "sp", "soul", "manarestore" -> {
                    player.soulPoints += value
                    addLog("ฟื้นฟู Soul Points $value หน่วย", Color.parseColor("#9b59b6"))
                }
                "atk", "attack" -> {
                    playerStatuses.add(ActiveStatus(StatusEffect.ATK_UP, 3, value))
                    addLog("เพิ่มพลังโจมตี $value หน่วย เป็นเวลา 3 เทิร์น", Color.parseColor("#e74c3c"))
                }
                "purify" -> {
                    playerStatuses.removeAll { it.effect == StatusEffect.POISON || it.effect == StatusEffect.BURN || it.effect == StatusEffect.BLEED }
                    addLog("ล้างสถานะผิดปกติทั้งหมด!", Color.parseColor("#ecf0f1"))
                }
                "teleport" -> {
                    addLog("✨ ม้วนคัมภีร์เทเลพอร์ต! หายตัวออกจากการต่อสู้!", Color.parseColor("#9b59b6"))
                    isTeleport = true
                }
            }
        }

        if (isTeleport) {
            removeItemFromCombatInventory(item)
            combatEnded = true
            Handler(Looper.getMainLooper()).postDelayed({ onCombatEnd(false, null) }, 1500)
            return
        }

        removeItemFromCombatInventory(item)
        
        updatePlayerHp()
        tvPlayerSoul.text = "SP: ${player.soulPoints}"
        
        Handler(Looper.getMainLooper()).postDelayed({
            checkCombatEnd()
        }, 1000)
    }
    
    private var enemyTurnIndex = 0

    private fun executeEnemyTurn() {
        if (combatEnded || playerTurn) return

        val livingEnemies = enemies.filter { it.hp > 0 }
        if (livingEnemies.isEmpty()) { victory(); return }

        if (enemyTurnIndex >= enemies.size) {
            enemyTurnIndex = 0
            checkCombatEnd()
            return
        }

        val currentEnemy = enemies[enemyTurnIndex]
        if (currentEnemy.hp <= 0) { enemyTurnIndex++; executeEnemyTurn(); return }

        // Tick down all skill cooldowns for this enemy
        currentEnemy.skills.forEach { if (it.currentCooldown > 0) it.currentCooldown-- }

        // Boss phase change
        if (currentEnemy.isBoss && currentEnemy.phase == 1 && currentEnemy.hp < currentEnemy.maxHp * 0.5f) {
            currentEnemy.phase = 2
            addLog("🚨 ${currentEnemy.name} เข้าสู่เฟส 2! ปลดพลังอสูรออกมา!", Color.YELLOW, true)
            enemyStatusList[enemyTurnIndex].add(ActiveStatus(StatusEffect.ATK_UP, 999, currentEnemy.atk / 3))
            enemyStatusList[enemyTurnIndex].add(ActiveStatus(StatusEffect.REGEN, 6, currentEnemy.level * 3))
            Handler(Looper.getMainLooper()).postDelayed({ enemyTurnIndex++; executeEnemyTurn() }, 1500)
            return
        }

        addLog("เทิร์นของ ${currentEnemy.emoji} ${currentEnemy.name}...", Color.LTGRAY)

        val idx = enemyTurnIndex
        val usableSkills = currentEnemy.skills.filter { it.currentCooldown <= 0 }

        when (currentEnemy.aiType) {
            EnemyAIType.AGGRESSIVE -> executeAggressiveAI(currentEnemy, usableSkills, idx)
            EnemyAIType.BERSERKER  -> executeBerserkerAI(currentEnemy, usableSkills, idx)
            EnemyAIType.TACTICAL   -> executeTacticalAI(currentEnemy, usableSkills, idx)
            EnemyAIType.SUPPORT    -> executeSupportAI(currentEnemy, usableSkills, idx)
            EnemyAIType.PACK       -> executePackAI(currentEnemy, usableSkills, idx)
            EnemyAIType.NORMAL     -> executeNormalAI(currentEnemy, usableSkills, idx)
        }
    }

    // ─── AI TYPES ───────────────────────────────────────────────────────────

    private fun executeNormalAI(enemy: Enemy, usableSkills: List<EnemySkill>, idx: Int) {
        if (enemy.isBoss && enemy.phase == 2) {
            val ultimate = usableSkills.filter { !it.isSelfTarget }.maxByOrNull { it.damage.last }
            if (ultimate != null) { useEnemySkill(enemy, ultimate, idx); return }
        }
        val heal = usableSkills.find { it.isSelfTarget && it.effect == StatusEffect.REGEN }
        if (heal != null && enemy.hp < enemy.maxHp * 0.5f) { useEnemySkill(enemy, heal, idx); return }
        val shield = usableSkills.find { it.isSelfTarget && it.effect == StatusEffect.SHIELD }
        if (shield != null && Random.nextFloat() < 0.4f) { useEnemySkill(enemy, shield, idx); return }
        val off = usableSkills.filter { !it.isSelfTarget && !it.targetsAllAllies }.randomOrNull()
        if (off != null && Random.nextFloat() < 0.55f) { useEnemySkill(enemy, off, idx); return }
        basicEnemyAttack(enemy, idx)
    }

    private fun executeAggressiveAI(enemy: Enemy, usableSkills: List<EnemySkill>, idx: Int) {
        // Always picks highest-damage offensive skill available
        val best = usableSkills.filter { !it.isSelfTarget && !it.targetsAllAllies }.maxByOrNull { it.damage.last }
        if (best != null && Random.nextFloat() < 0.85f) { useEnemySkill(enemy, best, idx); return }
        basicEnemyAttack(enemy, idx)
    }

    private fun executeBerserkerAI(enemy: Enemy, usableSkills: List<EnemySkill>, idx: Int) {
        // Accumulates rage every turn — ATK grows each round
        enemy.rageStacks = minOf(enemy.rageStacks + 1, 10)
        val bonus = enemy.rageStacks * 5
        if (enemy.rageStacks >= 2) addLog("🔥 ${enemy.name} เดือดพล่าน! Rage ${enemy.rageStacks}/10 (+$bonus ATK)", Color.parseColor("#e74c3c"))
        // Enrage skill when low HP
        if (enemy.hp.toFloat() / enemy.maxHp < 0.35f) {
            val enrage = usableSkills.find { it.isSelfTarget && it.effect == StatusEffect.ATK_UP }
            if (enrage != null) { useEnemySkill(enemy, enrage, idx); return }
        }
        val off = usableSkills.filter { !it.isSelfTarget && !it.targetsAllAllies }.randomOrNull()
        if (off != null) { useEnemySkill(enemy, off, idx); return }
        basicEnemyAttack(enemy, idx, bonusAtk = bonus)
    }

    private fun executeTacticalAI(enemy: Enemy, usableSkills: List<EnemySkill>, idx: Int) {
        val hasWeak    = playerStatuses.any { it.effect == StatusEffect.WEAK }
        val hasDefDown = playerStatuses.any { it.effect == StatusEffect.DEFENSE_DOWN }
        // Step 1: weaken player if not already weakened
        if (!hasWeak) {
            val weakSkill = usableSkills.find { it.effect == StatusEffect.WEAK && !it.isSelfTarget }
            if (weakSkill != null) { useEnemySkill(enemy, weakSkill, idx); return }
        }
        // Step 2: break armor if not broken
        if (!hasDefDown) {
            val armorBreak = usableSkills.find { it.effect == StatusEffect.DEFENSE_DOWN && !it.isSelfTarget }
            if (armorBreak != null) { useEnemySkill(enemy, armorBreak, idx); return }
        }
        // Step 3: exploit debuffs with big hit
        if (hasWeak || hasDefDown) {
            val big = usableSkills.filter { !it.isSelfTarget && !it.targetsAllAllies }.maxByOrNull { it.damage.last }
            if (big != null) { useEnemySkill(enemy, big, idx); return }
        }
        // Step 4: stun player
        val stun = usableSkills.find { it.effect == StatusEffect.STUN && !it.isSelfTarget }
        if (stun != null && Random.nextFloat() < 0.55f) { useEnemySkill(enemy, stun, idx); return }
        basicEnemyAttack(enemy, idx)
    }

    private fun executeSupportAI(enemy: Enemy, usableSkills: List<EnemySkill>, idx: Int) {
        // Step 1: heal most-wounded ally (including self)
        val weakestIdx = enemies.indices.filter { enemies[it].hp > 0 }
            .minByOrNull { enemies[it].hp.toFloat() / enemies[it].maxHp }
        val healSkill = usableSkills.find { it.isSelfTarget && it.effect == StatusEffect.REGEN }
        if (healSkill != null && weakestIdx != null && enemies[weakestIdx].hp.toFloat() / enemies[weakestIdx].maxHp < 0.5f) {
            val amt = (healSkill.damage.average() + enemy.level * 3).roundToInt()
            enemies[weakestIdx].hp = min(enemies[weakestIdx].maxHp, enemies[weakestIdx].hp + amt)
            healSkill.currentCooldown = healSkill.cooldown
            addLog("💚 ${enemy.name} รักษา ${enemies[weakestIdx].name}! +$amt HP", Color.parseColor("#2ecc71"), true)
            updateEnemyHp(weakestIdx)
            Handler(Looper.getMainLooper()).postDelayed({ enemyTurnIndex++; executeEnemyTurn() }, 1200)
            return
        }
        // Step 2: rally cry — buff all allies
        val rally = usableSkills.find { it.targetsAllAllies }
        if (rally != null) { useEnemySkill(enemy, rally, idx); return }
        // Step 3: debuff player
        val debuff = usableSkills.find { !it.isSelfTarget && !it.targetsAllAllies &&
            (it.effect == StatusEffect.WEAK || it.effect == StatusEffect.DEFENSE_DOWN) }
        if (debuff != null && Random.nextFloat() < 0.65f) { useEnemySkill(enemy, debuff, idx); return }
        basicEnemyAttack(enemy, idx)
    }

    private fun executePackAI(enemy: Enemy, usableSkills: List<EnemySkill>, idx: Int) {
        val aliveCount = enemies.count { it.hp > 0 }
        val packBonus = (aliveCount - 1) * 7
        // Last survivor: one-time enrage
        if (aliveCount == 1 && enemy.rageStacks == 0) {
            enemy.rageStacks = 1
            addLog("😤 ${enemy.name} เป็นตัวสุดท้ายของฝูง! พลังแค้นระเบิดออกมา!", Color.parseColor("#e74c3c"), true)
            enemyStatusList[idx].add(ActiveStatus(StatusEffect.ATK_UP, 999, enemy.atk / 2))
        }
        if (packBonus > 0) addLog("🐺 รุมโจมตีเป็นฝูง! (+$packBonus ATK)", Color.parseColor("#f39c12"))
        val off = usableSkills.filter { !it.isSelfTarget && !it.targetsAllAllies }.randomOrNull()
        if (off != null && Random.nextFloat() < 0.65f) { useEnemySkill(enemy, off, idx); return }
        basicEnemyAttack(enemy, idx, bonusAtk = packBonus)
    }

    private fun basicEnemyAttack(enemy: Enemy, enemyIndex: Int, bonusAtk: Int = 0) {
        val rawDamage = max(1, calculateDamage(enemy.atk + bonusAtk, ElementType.PHYSICAL, player.def, enemyStatusList[enemyIndex], enemy))
        val blockChance = player.unlockedSkills.sumOf { id ->
            val s = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
            if (s.isPassive) (s.effects["block_chance"] as? Int) ?: 0 else 0
        }
        val blocked = blockChance > 0 && Random.nextInt(100) < blockChance
        val lowHpDefBoost = player.unlockedSkills.sumOf { id ->
            val s = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
            if (s.isPassive) (s.effects["low_hp_def_boost"] as? Int) ?: 0 else 0
        }
        val isLowHp = player.hp.toFloat() / player.maxHp < 0.5f
        val afterIronWill = if (isLowHp && lowHpDefBoost > 0) max(1, (rawDamage * (1f - lowHpDefBoost / 100f)).roundToInt()) else rawDamage
        val dmgReduction = relicInt("damage_reduction")
        val reduced = if (dmgReduction > 0) max(1, (afterIronWill * (1f - dmgReduction / 100f)).roundToInt()) else afterIronWill
        val damage = if (blocked) 0 else reduced
        if (blocked) {
            addLog("🛡️ บล็อก! ป้องกันการโจมตีของ ${enemy.name}!", Color.parseColor("#3498db"))
        } else {
            if (isLowHp && lowHpDefBoost > 0) addLog("🪨 Iron Will! ลดความเสียหาย $lowHpDefBoost%", Color.parseColor("#95a5a6"))
            if (dmgReduction > 0 && damage > 0) addLog("🛡️ Relic ลดความเสียหาย $dmgReduction%!", Color.parseColor("#3498db"))
            player.hp = max(0, player.hp - damage)
            damageTaken += damage
            addLog("${enemy.name} โจมตี สร้างความเสียหาย $damage หน่วย", Color.parseColor("#FF3333"))
            val reflectPct = player.unlockedSkills.sumOf { id ->
                val s = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
                if (s.isPassive) (s.effects["reflect"] as? Int) ?: 0 else 0
            }
            if (reflectPct > 0) {
                val reflectDmg = max(1, damage * reflectPct / 100)
                enemy.hp = max(0, enemy.hp - reflectDmg)
                addLog("⚔️ สะท้อนความเสียหาย $reflectDmg หน่วยกลับ!", Color.parseColor("#f1c40f"))
                updateEnemyHp(enemyIndex)
            }
        }
        if (applyRelicDeathProtection()) { /* saved by relic */ }
        else applyMinHpRelic()
        updatePlayerHp()
        Handler(Looper.getMainLooper()).postDelayed({
            if (player.hp <= 0 && !combatEnded) defeat()
            else { enemyTurnIndex++; executeEnemyTurn() }
        }, 1000)
    }

    private fun useEnemySkill(enemy: Enemy, skill: EnemySkill, enemyIndex: Int) {
        skill.currentCooldown = skill.cooldown
        
        val skillDamage = try {
            if (skill.damage.isEmpty() || skill.damage.first > skill.damage.last) {
                if (skill.damage.isEmpty()) 0 else skill.damage.first
            } else {
                skill.damage.random()
            }
        } catch (e: Exception) {
            skill.damage.first
        }

        val combatAction = CombatAction(
            type = ActionType.ATTACK,
            name = skill.name,
            emoji = skill.emoji,
            damage = skillDamage,
            statusEffect = skill.effect,
            secondaryEffect = skill.secondaryEffect,
            isSelfTarget = skill.isSelfTarget,
            targetsAllAllies = skill.targetsAllAllies,
            lifeSteal = skill.lifeSteal,
            ignoreShield = skill.ignoreShield,
            executionBonus = skill.executionBonus
        )
        executeEnemySkill(enemy, combatAction, enemyIndex)
    }

    private fun executeEnemySkill(enemy: Enemy, action: CombatAction, enemyIndex: Int) {
        addLog("${enemy.emoji} ${enemy.name} ใช้ทักษะ ${action.emoji} ${action.name}!", Color.parseColor("#9b59b6"), true)

        // ─── targetsAllAllies: rally cry / group buff ───────────────────────
        if (action.targetsAllAllies) {
            action.statusEffect?.let { effect ->
                var count = 0
                enemies.forEachIndexed { i, e ->
                    if (e.hp > 0) {
                        enemyStatusList[i].add(ActiveStatus(effect, 3, maxOf(action.damage, 8)))
                        count++
                    }
                }
                addLog("📣 ${enemy.name} ปลุกพลังทีม! $count ตัวได้รับ ${effect.displayName}!", Color.parseColor("#f1c40f"), true)
                updateEnemyStatus()
            }
            Handler(Looper.getMainLooper()).postDelayed({ enemyTurnIndex++; executeEnemyTurn() }, 1200)
            return
        }

        // ─── isSelfTarget: healing / self-buff ─────────────────────────────
        if (action.isSelfTarget) {
            action.statusEffect?.let { effect ->
                enemyStatusList[enemyIndex].add(ActiveStatus(effect, 3, maxOf(action.damage / 2, 5)))
                addLog("${enemy.name} ได้รับผลจาก ${effect.displayName}!", Color.parseColor("#2ecc71"))
                if (effect == StatusEffect.REGEN) {
                    val healAmount = action.damage + (enemy.level * 2)
                    enemy.hp = min(enemy.maxHp, enemy.hp + healAmount)
                    addLog("${enemy.name} ฟื้นฟู HP $healAmount หน่วย", Color.parseColor("#2ecc71"))
                    updateEnemyHp(enemyIndex)
                }
            }
            updatePlayerHp()
            Handler(Looper.getMainLooper()).postDelayed({
                if (player.hp <= 0 && !combatEnded) defeat() else { enemyTurnIndex++; executeEnemyTurn() }
            }, 1500)
            return
        }

        // ─── offensive: damage the player ──────────────────────────────────
        var baseDmgValue = action.damage
        // Execution bonus: +80% dmg when player is low HP
        if (action.executionBonus && player.hp.toFloat() / player.maxHp < 0.3f) {
            baseDmgValue = (baseDmgValue * 1.8f).roundToInt()
            addLog("☠️ ประหาร! เป้าหมาย HP ต่ำ — ดาเมจเพิ่ม 80%!", Color.parseColor("#c0392b"), true)
        }
        // If ignoreShield, skip player SHIELD statuses in defence calculation
        val effectiveDef = if (action.ignoreShield) {
            val shieldTotal = playerStatuses.filter { it.effect == StatusEffect.SHIELD }.sumOf { it.power }
            max(0, player.def - shieldTotal)
        } else player.def

        val rawDmg = max(if (baseDmgValue > 0) 1 else 0, calculateDamage(baseDmgValue, ElementType.PHYSICAL, effectiveDef, playerStatuses, enemy))
        if (action.ignoreShield && rawDmg > 0) addLog("🎯 ฟันทะลุโล่! ดาเมจเจาะเกราะ!", Color.parseColor("#e74c3c"))

        val blockChance = player.unlockedSkills.sumOf { id ->
            val s = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
            if (s.isPassive) (s.effects["block_chance"] as? Int) ?: 0 else 0
        }
        val blocked = !action.ignoreShield && blockChance > 0 && Random.nextInt(100) < blockChance
        val lowHpDefBoost = player.unlockedSkills.sumOf { id ->
            val s = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
            if (s.isPassive) (s.effects["low_hp_def_boost"] as? Int) ?: 0 else 0
        }
        val isLowHp = player.hp.toFloat() / player.maxHp < 0.5f
        val reducedDmg = if (isLowHp && lowHpDefBoost > 0) max(1, (rawDmg * (1f - lowHpDefBoost / 100f)).roundToInt()) else rawDmg
        val damage = if (blocked) 0 else reducedDmg

        if (blocked) {
            addLog("🛡️ บล็อก! ป้องกันการโจมตีของ ${enemy.name}!", Color.parseColor("#3498db"))
        } else {
            if (isLowHp && lowHpDefBoost > 0 && damage > 0) addLog("🪨 Iron Will! ลดความเสียหาย $lowHpDefBoost%", Color.parseColor("#95a5a6"))
            if (damage > 0) {
                player.hp = max(0, player.hp - damage)
                damageTaken += damage
                addLog("ได้รับความเสียหาย $damage หน่วย!", Color.parseColor("#FF3333"))
                // Life steal: enemy recovers % of damage dealt
                if (action.lifeSteal) {
                    val steal = max(1, (damage * 0.4f).roundToInt())
                    enemy.hp = min(enemy.maxHp, enemy.hp + steal)
                    addLog("🩸 ${enemy.name} ดูดพลังชีวิต +$steal HP!", Color.parseColor("#c0392b"))
                    updateEnemyHp(enemyIndex)
                }
                val reflectPct = player.unlockedSkills.sumOf { id ->
                    val s = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
                    if (s.isPassive) (s.effects["reflect"] as? Int) ?: 0 else 0
                }
                if (reflectPct > 0) {
                    val reflectDmg = max(1, damage * reflectPct / 100)
                    val enemyIdx = enemies.indexOf(enemy)
                    if (enemyIdx != -1) {
                        enemy.hp = max(0, enemy.hp - reflectDmg)
                        addLog("⚔️ สะท้อนความเสียหาย $reflectDmg หน่วยกลับ!", Color.parseColor("#f1c40f"))
                        updateEnemyHp(enemyIdx)
                    }
                }
            }
            action.statusEffect?.let { effect ->
                val statusPower = maxOf(damage / 4, 3)
                playerStatuses.add(ActiveStatus(effect, 2, statusPower))
                addLog("⚠️ คุณติดสถานะ ${effect.displayName}! ($statusPower/เทิร์น)", Color.parseColor("#e67e22"))
            }
            action.secondaryEffect?.let { effect ->
                val statusPower = maxOf(damage / 5, 2)
                playerStatuses.add(ActiveStatus(effect, 2, statusPower))
                addLog("⚠️ สถานะเพิ่มเติม ${effect.displayName}! ($statusPower/เทิร์น)", Color.parseColor("#c0392b"))
            }
        }

        updatePlayerHp()
        Handler(Looper.getMainLooper()).postDelayed({
            if (player.hp <= 0 && !combatEnded) defeat() else { enemyTurnIndex++; executeEnemyTurn() }
        }, 1500)
    }
    
    private fun checkCombatEnd() {
        if (combatEnded) return
        
        // Priority check for enemy death first
        val livingEnemies = enemies.filter { it.hp > 0 }
        if (livingEnemies.isEmpty()) {
            victory()
            return
        }
        
        if (player.hp <= 0) {
            defeat()
            return
        }

        // Switch turns
        playerTurn = !playerTurn
        
        if (playerTurn) {
            // Player's turn: process statuses first
            processStatusEffects()
            
            // Re-check after status damage
            if (player.hp <= 0) {
                defeat()
                return
            }
            if (enemies.all { it.hp <= 0 }) {
                victory()
                return
            }
            
            showActionButtons()
        } else {
            // Enemy's turn
            enemyTurnIndex = 0
            executeEnemyTurn()
        }
    }
    
    private fun processStatusEffects() {
        // Player statuses
        val playerItr = playerStatuses.iterator()
        while (playerItr.hasNext()) {
            val status = playerItr.next()
            when (status.effect) {
                StatusEffect.POISON, StatusEffect.BURN, StatusEffect.BLEED -> {
                    player.hp = max(1, player.hp - status.power)
                    addLog("ความเสียหายจาก ${status.effect.displayName}: ${status.power}", Color.parseColor("#e74c3c"))
                    updatePlayerHp()
                }
                StatusEffect.REGEN -> {
                    player.hp = min(player.maxHp, player.hp + status.power)
                    addLog("ฟื้นฟูจาก ${status.effect.displayName}: ${status.power}", Color.parseColor("#2ecc71"))
                    updatePlayerHp()
                }
                else -> {}
            }
            status.duration--
            if (status.duration <= 0) playerItr.remove()
        }
        
        // Re-check player death after status damage
        if (player.hp <= 0 && !combatEnded) {
            defeat()
            return
        }
        
        // Enemy statuses for each enemy
        enemies.forEachIndexed { index, enemy ->
            if (enemy.hp > 0) {
                val enemyItr = enemyStatusList[index].iterator()
                while (enemyItr.hasNext()) {
                    val status = enemyItr.next()
                    when (status.effect) {
                        StatusEffect.POISON, StatusEffect.BURN, StatusEffect.BLEED -> {
                            enemy.hp = max(0, enemy.hp - status.power)
                            addLog("${enemy.name} เสีย HP จาก ${status.effect.displayName}: ${status.power}", Color.parseColor("#e74c3c"))
                            updateEnemyHp(index)
                        }
                        else -> {}
                    }
                    status.duration--
                    if (status.duration <= 0) enemyItr.remove()
                }
            }
        }
        
        // Passive soul_regen (Mage Soul Well etc.)
        var soulRegen = 0
        player.unlockedSkills.forEach { id ->
            val skill = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@forEach
            if (skill.isPassive) soulRegen += (skill.effects["soul_regen"] as? Int) ?: 0
        }
        if (soulRegen > 0) {
            player.soulPoints += soulRegen
            tvPlayerSoul.text = "SP: ${player.soulPoints}"
        }

        updatePlayerHp()

        // Check if player died from status effects
        if (player.hp <= 0 && !combatEnded) {
            defeat()
            return
        }
        
        // Check if all enemies died from status effects
        if (enemies.all { it.hp <= 0 } && !combatEnded) {
            victory()
        }
    }
    
    private fun victory() {
        if (combatEnded) return
        combatEnded = true
        
        addLog("ชัยชนะ!", Color.parseColor("#f1c40f"), true)
        
        // Calculate total rewards from all enemies
        val totalExp = enemies.sumOf { it.expReward }
        val totalGold = enemies.sumOf { it.goldReward }
        val soulPerKill = player.unlockedSkills.sumOf { id ->
            val s = SkillTreeData.getAnySkillById(player.classData, player.playerRace, id) ?: return@sumOf 0
            if (s.isPassive) (s.effects["soul_per_kill"] as? Int) ?: 0 else 0
        }
        val totalSoul = enemies.sumOf { it.soulReward } + enemies.size * soulPerKill
        val allLoot = enemies.flatMap { e -> e.lootTable.filter { Random.nextFloat() < it.dropChance }.map { it.item } }

        val rewards = CombatRewards(
            exp = totalExp,
            gold = totalGold,
            soulPoints = totalSoul,
            items = allLoot
        )
        
        addLog("ได้รับ EXP: ${rewards.exp}", Color.LTGRAY)
        addLog("ได้รับ Gold: ${rewards.gold}", Color.LTGRAY)
        addLog("ได้รับ Soul Points: ${rewards.soulPoints}", Color.parseColor("#9b59b6"))
        
        // Continue button
        val btnContinue = createActionButton(
            "ไปต่อ",
            Color.WHITE,
            ignoreCombatEnd = true
        ) {
            onCombatEnd(true, rewards)
        }
        
        actionButtonsContainer.removeAllViews()
        actionButtonsContainer.addView(
            btnContinue,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.dp(60)
            )
        )
        actionButtonsContainer.visibility = View.VISIBLE
    }
    
    private fun defeat() {
        if (combatEnded) return
        combatEnded = true
        
        addLog("คุณสิ้นชีพในการต่อสู้...", Color.parseColor("#c0392b"), true)
        
        // Disable any interaction by clearing views
        actionButtonsContainer.removeAllViews()
        
        // Respawn button (Accept Death)
        val btnRespawn = createActionButton(
            "ยอมรับความตาย",
            Color.WHITE,
            ignoreCombatEnd = true
        ) {
            onCombatEnd(false, null)
        }
        
        actionButtonsContainer.addView(
            btnRespawn,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                activity.dp(60)
            )
        )
        actionButtonsContainer.visibility = View.VISIBLE
        
        // Force the UI to show the defeat state immediately
        tvPlayerHp.text = "HP: 0/${player.maxHp}"
        playerHpBar.progress = 0
    }
    
    // ========== HELPER FUNCTIONS ==========
    
    private fun getUnlockedActiveSkills(): List<Skill> {
        val activeSkills = mutableListOf<Skill>()
        player.unlockedSkills.forEach { skillId ->
            SkillTreeData.getAnySkillById(player.classData, player.playerRace, skillId)?.let { skill ->
                if (!skill.isPassive) {
                    activeSkills.add(skill)
                }
            }
        }
        return activeSkills
    }

    private fun executeActiveSkill(skill: Skill) {
        val soulCost = skill.cost * 10
        if (player.soulPoints < soulCost) {
            addLog("❌ Soul Points ไม่พอ!", Color.parseColor("#e74c3c"))
            return
        }

        turnCount++
        hideActionButtons()
        
        player.soulPoints -= soulCost
        tvPlayerSoul.text = "SP: ${player.soulPoints}"
        addLog("ใช้ทักษะ ${skill.name}!", Color.parseColor("#9b59b6"), true)
        
        // Visual effect for skill usage
        animateSkillActivation(skill)

        // Basic dynamic skill execution based on effects map
        val effects = skill.effects
        var handled = false
        
        val enemy = getSelectedEnemy()
        val enemyStatuses = getSelectedEnemyStatuses()

        if (effects.containsKey("aoe_damage") || effects.containsKey("damage")) {
            val damageValue = effects["aoe_damage"] ?: effects["damage"]
            var damageMultiplier = when(damageValue) {
                is Int -> damageValue / 100f
                is Float -> damageValue
                is Double -> damageValue.toFloat()
                else -> 1.0f
            }
            
            // Gimmick: Execute (Berserker)
            if (effects.containsKey("execute_threshold")) {
                val threshold = (effects["execute_threshold"] as? Int) ?: 25
                if (enemy.hp.toFloat() / enemy.maxHp * 100 <= threshold) {
                    damageMultiplier *= 2.0f
                    addLog("‼️ EXECUTE! ดาเมจเพิ่มขึ้นเป็น 2 เท่า!", Color.parseColor("#ff0000"))
                }
            }

            // Gimmick: DEF Scaling (Paladin)
            if (effects.containsKey("def_scaling_damage")) {
                val scaling = (effects["def_scaling_damage"] as? Double)?.toFloat() ?: 1.0f
                val extraDmg = (player.def * scaling).toInt()
                val damage = calculateDamage(extraDmg, ElementType.PHYSICAL, enemy.def, enemyStatuses, enemy)
                enemy.hp = max(0, enemy.hp - damage)
                addLog("⚖️ ตัดสินด้วยพลังป้องกัน! สร้างความเสียหาย $damage", Color.parseColor("#3498db"))
                handled = true
            } else if (effects.containsKey("aoe_damage")) {
                // Hit all living enemies
                enemies.forEachIndexed { index, e ->
                    if (e.hp > 0) {
                        val baseDmg = (player.atk * damageMultiplier).roundToInt()
                        val damage = calculateDamage(baseDmg, ElementType.PHYSICAL, e.def, enemyStatusList[index], e)
                        e.hp = max(0, e.hp - damage)
                        updateEnemyHp(index)
                    }
                }
                addLog("💥 ใช้ท่าโจมตีหมู่!", Color.parseColor("#e74c3c"))
            } else {
                // Hit selected enemy — playerStatuses so ATK_UP buffs apply
                val baseDmg = (player.atk * damageMultiplier).roundToInt()
                val damage = calculateDamage(baseDmg, ElementType.PHYSICAL, enemy.def, playerStatuses, enemy)
                enemy.hp = max(0, enemy.hp - damage)
                
                // Gimmick: Life Drain (Necromancer)
                if (effects.containsKey("lifesteal")) {
                    val lifestealPercent = (effects["lifesteal"] as? Int) ?: 0
                    val heal = (damage * (lifestealPercent / 100f)).toInt()
                    player.hp = min(player.maxHp, player.hp + heal)
                    addLog("💀 ดูดพลังชีวิต $heal หน่วย!", Color.parseColor("#2ecc71"))
                    updatePlayerHp()
                }
                
                addLog("💥 จู่โจมอย่างรุนแรง! สร้างความเสียหาย $damage", Color.parseColor("#e74c3c"))
            }
            handled = true
        }

        // Gimmick: Multi-hit (Samurai)
        if (effects.containsKey("multi_hit")) {
            val hits = (effects["multi_hit"] as? Int) ?: 1
            // Get damage percentage per hit (e.g., 100 means 100% of ATK per hit)
            val damageMult = (effects["damage"] as? Int)?.toFloat()?.div(100f) ?: 1.0f
            
            var totalDmg = 0
            var hitCount = 0
            
            repeat(hits) {
                val livingEnemiesIndices = enemies.indices.filter { enemies[it].hp > 0 }
                if (livingEnemiesIndices.isEmpty()) return@repeat
                
                val targetIndex = livingEnemiesIndices.random()
                val target = enemies[targetIndex]
                
                // Calculate full damage for EACH hit
                val baseDmg = (player.atk * damageMult).roundToInt()
                val damage = calculateDamage(baseDmg, ElementType.PHYSICAL, target.def, enemyStatusList[targetIndex], target)
                
                target.hp = max(0, target.hp - damage)
                totalDmg += damage
                hitCount++
                updateEnemyHp(targetIndex)
            }
            
            addLog("🌌 ${skill.name}! จู่โจมต่อเนื่อง $hitCount ครั้ง!", Color.parseColor("#9b59b6"), true)
            addLog("สร้างความเสียหายรวมทั้งหมด $totalDmg หน่วย", Color.parseColor("#f1c40f"), true)
            handled = true
        }

        // Gimmick: Summon Skeleton (Necromancer)
        if (effects.containsKey("summon")) {
            addLog("🧟 อัญเชิญลูกสมุนออกมาช่วยสู้!", Color.parseColor("#7f8c8d"))
        }

        // Soul Burst (Necromancer) — consume all SP, deal SP * ratio damage
        if (effects.containsKey("soul_burst")) {
            val ratio = (effects["soul_burst"] as? Int) ?: 1
            val spConsumed = player.soulPoints
            if (spConsumed > 0) {
                val burstDamage = calculateDamage(spConsumed * ratio, ElementType.PHYSICAL, enemy.def, playerStatuses, enemy)
                enemy.hp = max(0, enemy.hp - burstDamage)
                player.soulPoints = 0
                tvPlayerSoul.text = "SP: 0"
                addLog("💥 Soul Explosion! ใช้ $spConsumed SP สร้างความเสียหาย $burstDamage!", Color.parseColor("#9b59b6"), true)
                updateEnemyHp(selectedEnemyIndex)
            } else {
                addLog("❌ Soul Points ไม่มี!", Color.parseColor("#e74c3c"))
            }
            handled = true
        }

        // Gimmick: Freeze (Mage)
        if (effects.containsKey("freeze")) {
            enemyStatuses.add(ActiveStatus(StatusEffect.STUN, (effects["freeze"] as? Int) ?: 1, 0))
            addLog("❄️ ศัตรูถูกแช่แข็ง!", Color.parseColor("#3498db"))
            handled = true
        }

        // Gimmick: Poison (Rogue)
        if (effects.containsKey("poison")) {
            val power = (effects["poison"] as? Int) ?: 3
            enemyStatuses.add(ActiveStatus(StatusEffect.POISON, 3, power))
            addLog("🧪 ศัตรูติดพิษจากใบมีด!", Color.parseColor("#27ae60"))
            handled = true
        }

        // Gimmick: Purify (Cleric)
        if (effects.containsKey("purify")) {
            playerStatuses.clear()
            addLog("🚿 ชำระล้างสถานะผิดปกติทั้งหมด!", Color.parseColor("#ecf0f1"))
            handled = true
        }


        if (effects.containsKey("heal")) {
            val healValue = effects["heal"]
            val healAmount = when(healValue) {
                is Int -> healValue
                is Float -> (player.maxHp * healValue).roundToInt()
                is Double -> (player.maxHp * healValue).roundToInt()
                else -> 0
            }
            player.hp = min(player.maxHp, player.hp + healAmount)
            
            addLog("ฟื้นฟู $healAmount HP!", Color.parseColor("#2ecc71"))
            updatePlayerHp()
            handled = true
        }

        if (effects.containsKey("stun") || effects.containsKey("STUN")) {
            enemyStatuses.add(ActiveStatus(StatusEffect.STUN, 1, 1))
            addLog("ศัตรูมึนงงจนขยับไม่ได้!", Color.parseColor("#f1c40f"))
            handled = true
        }

        if (effects.containsKey("atk_buff") || effects.containsKey("BUFF_ATK") || effects.containsKey("atk_bonus")) {
            val buffValue = effects["atk_buff"] ?: effects["BUFF_ATK"] ?: effects["atk_bonus"]
            val power = when(buffValue) {
                is Int -> buffValue
                is Float -> (player.atk * buffValue).roundToInt()
                is Double -> (player.atk * buffValue).roundToInt()
                else -> 5
            }
            val atkDur = (effects["duration"] as? Int) ?: 3
            playerStatuses.add(ActiveStatus(StatusEffect.ATK_UP, atkDur, power))
            addLog("บ้าคลั่ง! พลังโจมตีเพิ่มขึ้น $power เป็นเวลา $atkDur เทิร์น", Color.parseColor("#e67e22"))
            updatePlayerStatus()
            handled = true
        }

        if (effects.containsKey("def_buff") || effects.containsKey("BUFF_DEF") || effects.containsKey("def_bonus")) {
            val buffValue = effects["def_buff"] ?: effects["BUFF_DEF"] ?: effects["def_bonus"]
            val power = when(buffValue) {
                is Int -> buffValue
                is Float -> (player.def * buffValue).roundToInt()
                is Double -> (player.def * buffValue).roundToInt()
                else -> 5
            }
            val defDur = (effects["duration"] as? Int) ?: 3
            playerStatuses.add(ActiveStatus(StatusEffect.SHIELD, defDur, power))
            addLog("ตั้งการ์ดแน่นหนา! ป้องกันเพิ่มขึ้น $power", Color.parseColor("#3498db"))
            updatePlayerStatus()
            handled = true
        }

        if (effects.containsKey("POISON")) {
            val power = (effects["POISON"] as? Int) ?: 5
            enemyStatuses.add(ActiveStatus(StatusEffect.POISON, 3, power))
            addLog("ศัตรูติดพิษ! จะสูญเสีย HP ต่อเนื่อง", Color.parseColor("#27ae60"))
            handled = true
        }

        // Dodge Buff → add SHIELD to represent evasion (Shadow Step - Rogue)
        if (effects.containsKey("dodge_buff")) {
            val value = (effects["dodge_buff"] as? Int) ?: 0
            val dur = (effects["duration"] as? Int) ?: 3
            playerStatuses.add(ActiveStatus(StatusEffect.SHIELD, dur, value))
            addLog("💨 ร่างกายว่องไว! ป้องกันเพิ่ม $value เป็นเวลา $dur เทิร์น", Color.parseColor("#00d4ff"))
            updatePlayerStatus()
            handled = true
        }

        // HP Buff — heal a % of maxHp (Bear Form - Druid)
        if (effects.containsKey("hp_buff")) {
            val percent = (effects["hp_buff"] as? Int) ?: 0
            val healAmount = (player.maxHp * percent / 100f).roundToInt()
            player.hp = min(player.maxHp, player.hp + healAmount)
            addLog("🐻 แปลงร่าง! ฟื้นฟู HP $healAmount หน่วย", Color.parseColor("#2ecc71"))
            updatePlayerHp()
            handled = true
        }

        // ATK self-debuff (Bear Form - Druid)
        if (effects.containsKey("atk_debuff")) {
            val percent = (effects["atk_debuff"] as? Int) ?: 0
            val loss = (player.atk * percent / 100f).roundToInt()
            player.atk = max(1, player.atk - loss)
            addLog("🐻 แปลงร่าง! พลังโจมตีลดลง $loss หน่วย", Color.parseColor("#e67e22"))
            handled = true
        }

        // All Stats Buff (Heroic Ballad - Bard)
        if (effects.containsKey("all_stats_buff")) {
            val value = (effects["all_stats_buff"] as? Int) ?: 0
            val dur = (effects["duration"] as? Int) ?: 3
            playerStatuses.add(ActiveStatus(StatusEffect.ATK_UP, dur, value))
            playerStatuses.add(ActiveStatus(StatusEffect.SHIELD, dur, value))
            addLog("🎺 เพลงวีรบุรุษ! ATK และ DEF เพิ่มขึ้น $value หน่วย $dur เทิร์น!", Color.parseColor("#f1c40f"))
            updatePlayerStatus()
            handled = true
        }

        // Enemy ATK Debuff — WEAK status (Wither - Necromancer)
        if (effects.containsKey("debuff_atk")) {
            val value = (effects["debuff_atk"] as? Int) ?: 0
            val dur = (effects["duration"] as? Int) ?: 3
            enemyStatuses.add(ActiveStatus(StatusEffect.WEAK, dur, value))
            addLog("🥀 ศัตรูอ่อนแอ! พลังโจมตีลดลง $value หน่วย $dur เทิร์น", Color.parseColor("#9b59b6"))
            handled = true
        }

        // Enemy DEF Debuff — DEFENSE_DOWN status (Wither - Necromancer)
        if (effects.containsKey("debuff_def")) {
            val value = (effects["debuff_def"] as? Int) ?: 0
            val dur = (effects["duration"] as? Int) ?: 3
            enemyStatuses.add(ActiveStatus(StatusEffect.DEFENSE_DOWN, dur, value))
            addLog("🥀 เกราะศัตรูแตกร้าว! ป้องกันลดลง $value หน่วย $dur เทิร์น", Color.parseColor("#9b59b6"))
            handled = true
        }

        // Regen Buff (Healing Totem - Shaman, Holy Aura - Paladin)
        if (effects.containsKey("regen_buff") || effects.containsKey("regen")) {
            val rawVal = effects["regen_buff"] ?: effects["regen"]
            val value = (rawVal as? Int) ?: 0
            val dur = (effects["duration"] as? Int) ?: 3
            playerStatuses.add(ActiveStatus(StatusEffect.REGEN, dur, value))
            addLog("💚 ฟื้นฟู $value HP ต่อเทิร์น เป็นเวลา $dur เทิร์น!", Color.parseColor("#2ecc71"))
            handled = true
        }

        // Damage Reduction → SHIELD status (Holy Barrier - Cleric)
        if (effects.containsKey("damage_reduction")) {
            val value = (effects["damage_reduction"] as? Int) ?: 0
            val dur = (effects["duration"] as? Int) ?: 2
            playerStatuses.add(ActiveStatus(StatusEffect.SHIELD, dur, value))
            addLog("🛡️ โล่ศักดิ์สิทธิ์! ลดความเสียหาย $value หน่วย เป็นเวลา $dur เทิร์น", Color.parseColor("#ecf0f1"))
            handled = true
        }

        // AOE Stun (Entangle - Druid)
        if (effects.containsKey("aoe_stun")) {
            val dur = (effects["aoe_stun"] as? Int) ?: 1
            enemies.forEachIndexed { index, e ->
                if (e.hp > 0) enemyStatusList[index].add(ActiveStatus(StatusEffect.STUN, dur, 0))
            }
            addLog("🌳 เถาวัลย์รัดแน่น! ศัตรูทุกตัวมึนงง $dur เทิร์น!", Color.parseColor("#27ae60"))
            updateEnemyStatus()
            handled = true
        }

        // Random Status Effect on all enemies (Spirit Storm - Shaman)
        if (effects.containsKey("random_status")) {
            val randomPool = listOf(StatusEffect.BURN, StatusEffect.FREEZE, StatusEffect.POISON, StatusEffect.BLEED)
            enemies.forEachIndexed { index, e ->
                if (e.hp > 0) {
                    val effect = randomPool.random()
                    enemyStatusList[index].add(ActiveStatus(effect, 2, player.level * 2))
                    addLog("🌪️ ${e.name} ติดสถานะ ${effect.displayName}!", Color.parseColor("#9b59b6"))
                }
            }
            updateEnemyStatus()
            handled = true
        }

        // Self-Damage (Demon/Cyborg sacrifice gimmick) — runs after all damage effects
        if (effects.containsKey("self_damage_pct")) {
            val pct = (effects["self_damage_pct"] as? Int) ?: 0
            if (pct > 0) {
                val sacrifice = max(1, player.maxHp * pct / 100)
                player.hp = max(1, player.hp - sacrifice)
                updatePlayerHp()
                addLog("💔 เสียสละ HP $sacrifice หน่วยเพื่อพลัง!", Color.parseColor("#e74c3c"))
            }
        }

        if (!handled) {
            addLog("ทักษะทำงาน!", Color.parseColor("#3498db"))
        }

        updateEnemyHp(selectedEnemyIndex)
        updateEnemyStatus()
        
        Handler(Looper.getMainLooper()).postDelayed({
            checkCombatEnd()
        }, 1000)
    }

    private fun removeItemFromCombatInventory(item: Item) {
        val idx = playerCombatInventory.indexOf(item)
        if (idx == -1) return
        val updated = item.copy(quantity = item.quantity - 1)
        if (updated.quantity <= 0) playerCombatInventory.removeAt(idx)
        else playerCombatInventory[idx] = updated
    }

    private fun updatePlayerHp() {
        playerHpBar.progress = player.hp
        tvPlayerHp.text = "HP: ${player.hp}/${player.maxHp}"
    }
    
    private fun updateEnemyHp(index: Int) {
        if (index < 0 || index >= enemies.size) return
        enemyHpBars[index].progress = enemies[index].hp
        tvEnemyHps[index].text = "${enemies[index].hp}/${enemies[index].maxHp}"
        
        if (enemies[index].hp <= 0) {
            enemyCards[index].alpha = 0.5f
            enemyCards[index].isEnabled = false
        }
    }
    
    private fun updatePlayerStatus() {
        // Implement status icon update if needed
    }
    
    private fun updateEnemyStatus() {
        enemies.forEachIndexed { index, _ ->
            val container = enemyStatusContainers[index]
            container.removeAllViews()
            
            enemyStatusList[index].forEach { activeStatus ->
                val statusIcon = TextView(activity).apply {
                    text = activeStatus.effect.emoji
                    textSize = 10f
                    setPadding(activity.dp(2), 0, activity.dp(2), 0)
                }
                container.addView(statusIcon)
            }
        }
    }

    private fun calculateDamage(
        baseDamage: Int,
        element: ElementType,
        defense: Int,
        statuses: List<ActiveStatus>,
        targetEnemy: Enemy? = null
    ): Int {
        var damage = baseDamage
        
        // Element Multipliers
        targetEnemy?.let { target ->
            if (target.weaknesses.contains(element)) {
                damage = (damage * 1.5).roundToInt()
                addLog("🎯 จุดอ่อน! (x1.5)", Color.parseColor("#f1c40f"))
            }
            if (target.resistances.contains(element)) {
                damage = (damage * 0.5).roundToInt()
                addLog("🛡️ ต้านทาน! (x0.5)", Color.parseColor("#95a5a6"))
            }
        }

        // Apply Status Modifiers (Buffs/Debuffs)
        var bonusAtk = 0
        statuses.forEach {
            when (it.effect) {
                StatusEffect.ATK_UP -> bonusAtk += it.power
                StatusEffect.WEAK   -> bonusAtk -= it.power  // WEAK reduces attacker's damage
                else -> {}
            }
        }
        
        var targetDef = defense
        if (playerTurn) {
            targetEnemy?.let { target ->
                val targetIndex = enemies.indexOf(target)
                if (targetIndex != -1) {
                    enemyStatusList[targetIndex].forEach { if (it.effect == StatusEffect.DEFENSE_DOWN) targetDef -= it.power }
                }
            }
        } else {
            playerStatuses.forEach { if (it.effect == StatusEffect.SHIELD) targetDef += it.power }
        }

        val finalDamage = max(1, (damage + bonusAtk) - (targetDef / 2))
        return finalDamage
    }

    private fun animateSkillActivation(skill: Skill) {
        // Simple scale animation for the whole combat layout
        combatLayout.animate().scaleX(1.02f).scaleY(1.02f).setDuration(150).withEndAction {
            combatLayout.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }.start()
    }
    
    private fun animateDamage(view: View, currentVal: Int, maxVal: Int) {
        // Could implement progress bar animation here if desired
    }
}
