package com.example.game

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import android.widget.FrameLayout

/**
 * Skill Tree System
 * Features: Passive & Active abilities, Skill points, Prerequisites, Visual tree
 */

class SkillTreeManager(
    private val activity: RoguelikeMainActivity,
    private val playerClass: RoguelikeMainActivity.CharacterClass,
    private val playerRace: RoguelikeMainActivity.Race,
    private var skillPoints: Int,
    private val unlockedSkills: MutableSet<String>,
    private val onSkillChanged: (skillPoints: Int, unlockedSkills: Set<String>) -> Unit
) {
    
    private lateinit var skillTreeLayout: LinearLayout
    private lateinit var tvSkillPoints: TextView
    
    // Skill data
    private val skillTrees = SkillTreeData.skillTrees
    
    private fun isSkillVisible(skill: Skill): Boolean {
        // If it's a Tier 1 skill (no prerequisites), always show it
        if (skill.prerequisites.isEmpty()) return true
        
        // If it's unlocked, show it
        if (skill.id in unlockedSkills) return true
        
        // Show if AT LEAST ONE prerequisite is unlocked (cascading reveal)
        // This makes it feel like a "tree" that grows
        return skill.prerequisites.any { it in unlockedSkills }
    }

    fun showSkillTree() {
        createSkillTreeUI()
    }
    
    private fun createSkillTreeUI() {
        skillTreeLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(activity.getThemeColor("background"))
        }
        
        // Header
        addHeader()
        
        // Tabs for different trees
        addTreeTabs()
        
        // Skill tree content
        addSkillTreeContent()
        
        // Bottom controls
        addBottomControls()
        
        activity.setContentView(skillTreeLayout)
    }
    
    private fun addHeader() {
        val headerCard = activity.createCard().apply {
            setCardBackgroundColor(activity.getThemeColor("primary"))
        }
        
        val headerLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(activity.dp(16), activity.dp(12), activity.dp(16), activity.dp(12))
        }
        
        val title = TextView(activity).apply {
            text = "🌳 ผังทักษะ (Skill Tree)"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        headerLayout.addView(title)
        
        val classText = TextView(activity).apply {
            text = "${playerClass.emoji} ${playerClass.displayName}"
            textSize = 16f
            setTextColor(activity.getThemeColor("textSecondary"))
            gravity = Gravity.CENTER
            setPadding(0, activity.dp(4), 0, 0)
        }
        headerLayout.addView(classText)
        
        tvSkillPoints = TextView(activity).apply {
            text = "💎 แต้มทักษะ: $skillPoints"
            textSize = 18f
            setTextColor(Color.parseColor("#f1c40f"))
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, activity.dp(8), 0, 0)
        }
        headerLayout.addView(tvSkillPoints)
        
        headerCard.addView(headerLayout)
        skillTreeLayout.addView(headerCard)
    }
    
    private var currentTreeType = SkillTreeType.COMBAT
    
    private fun addTreeTabs() {
        val tabContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(activity.dp(16), activity.dp(8), activity.dp(16), activity.dp(8))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val trees = skillTrees[playerClass] ?: emptyMap()

        SkillTreeType.values().forEach { treeType ->
            // Show class skill tabs if the class has them; always show RACE tab
            if (trees.containsKey(treeType) || treeType == SkillTreeType.RACE) {
                val tab = createTreeTab(treeType)
                tabContainer.addView(tab)
            }
        }

        skillTreeLayout.addView(tabContainer)
    }
    
    private fun createTreeTab(treeType: SkillTreeType): View {
        val isActive = currentTreeType == treeType
        
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(activity.dp(8), activity.dp(12), activity.dp(8), activity.dp(8))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            
            setOnClickListener {
                if (currentTreeType != treeType) {
                    currentTreeType = treeType
                    refreshSkillTree()
                }
            }
        }
        
        val label = TextView(activity).apply {
            text = treeType.displayName.uppercase()
            textSize = 12f
            setTextColor(if (isActive) Color.parseColor("#00d4ff") else Color.parseColor("#666666"))
            typeface = Typeface.create("sans-serif-medium", if (isActive) Typeface.BOLD else Typeface.NORMAL)
            gravity = Gravity.CENTER
        }
        layout.addView(label)
        
        // Modern underline indicator
        val indicator = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                activity.dp(3)
            ).apply { 
                topMargin = activity.dp(8)
                marginStart = activity.dp(4)
                marginEnd = activity.dp(4)
            }
            setBackgroundColor(if (isActive) Color.parseColor("#00d4ff") else Color.TRANSPARENT)
        }
        layout.addView(indicator)
        
        return layout
    }
    
    private fun addSkillTreeContent() {
        val scrollView = ScrollView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                topMargin = activity.dp(8)
                bottomMargin = activity.dp(8)
            }
        }
        
        val contentLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val p = activity.dp(12)
            setPadding(p, p, p, p)
        }
        
        val trees = skillTrees[playerClass] ?: emptyMap()
        val currentTree = if (currentTreeType == SkillTreeType.RACE)
            RaceSkillData.getRaceSkillTree(playerRace)
        else
            trees[currentTreeType]

        currentTree?.tiers?.forEach { tier ->
            // Tier header
            val tierHeader = TextView(activity).apply {
                text = "━━━ ${tier.name} (Tier ${tier.tierLevel}) ━━━"
                textSize = 14f
                setTextColor(activity.getThemeColor("accent"))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, activity.dp(16), 0, activity.dp(12))
            }
            contentLayout.addView(tierHeader)
            
            // Skills in this tier
            tier.skills.forEach { skill ->
                if (isSkillVisible(skill)) {
                    val skillCard = createSkillCard(skill, tier.tierLevel)
                    contentLayout.addView(skillCard)
                }
            }
        }
        
        scrollView.addView(contentLayout)
        skillTreeLayout.addView(scrollView)
    }
    
    private fun createSkillCard(skill: Skill, tierLevel: Int): CardView {
        val isUnlocked = skill.id in unlockedSkills
        val canUnlock = canUnlockSkill(skill)
        
        val card = CardView(activity).apply {
            radius = activity.dp(12).toFloat()
            cardElevation = if (isUnlocked) 4f else 0f
            
            // Dungeon Theme Card
            val bg = GradientDrawable().apply {
                cornerRadius = activity.dp(12).toFloat()
                if (isUnlocked) {
                    setColor(Color.parseColor("#1a1a1a"))
                    setStroke(activity.dp(2), when(currentTreeType) {
                        SkillTreeType.COMBAT -> Color.parseColor("#ff4444")
                        SkillTreeType.DEFENSE -> Color.parseColor("#4444ff")
                        SkillTreeType.UTILITY -> Color.parseColor("#ffcc00")
                        SkillTreeType.RACE -> Color.parseColor("#00cc88")
                    })
                } else if (canUnlock) {
                    setColor(Color.parseColor("#121212"))
                    setStroke(activity.dp(1), Color.parseColor("#444444"))
                } else {
                    setColor(Color.parseColor("#0d0d0d"))
                    setStroke(activity.dp(1), Color.parseColor("#222222"))
                }
            }
            background = bg

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = activity.dp(12)
            }
        }

        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val p = activity.dp(12)
            setPadding(p, p, p, p)
        }

        // Skill Header
        val headerLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Skill Icon with Glow logic
        val iconContainer = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(activity.dp(54), activity.dp(54))
            
            if (isUnlocked) {
                val glow = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    val glowColor = when (currentTreeType) {
                        SkillTreeType.COMBAT -> Color.parseColor("#441111")
                        SkillTreeType.DEFENSE -> Color.parseColor("#111144")
                        SkillTreeType.UTILITY -> Color.parseColor("#114411")
                        SkillTreeType.RACE -> Color.parseColor("#114433")
                    }
                    colors = intArrayOf(glowColor, Color.TRANSPARENT)
                    gradientType = GradientDrawable.RADIAL_GRADIENT
                    gradientRadius = activity.dp(30).toFloat()
                }
                background = glow
            }
        }

        val icon = TextView(activity).apply {
            text = skill.icon
            textSize = 28f
            gravity = Gravity.CENTER
            alpha = if (isUnlocked || canUnlock) 1.0f else 0.3f
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        iconContainer.addView(icon)
        headerLayout.addView(iconContainer)

        val nameLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(activity.dp(16), 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nameText = TextView(activity).apply {
            text = skill.name
            textSize = 16f
            setTextColor(if (isUnlocked) Color.WHITE else if (canUnlock) Color.LTGRAY else Color.DKGRAY)
            typeface = Typeface.DEFAULT_BOLD
        }
        nameLayout.addView(nameText)

        val typeLabel = TextView(activity).apply {
            text = "${if (skill.isPassive) "PASSIVE 📊" else "ACTIVE ⚡"} | Tier $tierLevel"
            textSize = 9f
            setTextColor(if (isUnlocked) (when(currentTreeType) {
                SkillTreeType.COMBAT -> Color.parseColor("#ff8888")
                SkillTreeType.DEFENSE -> Color.parseColor("#8888ff")
                SkillTreeType.UTILITY -> Color.parseColor("#ffff88")
                SkillTreeType.RACE -> Color.parseColor("#88ffcc")
            }) else Color.parseColor("#666666"))
            typeface = Typeface.MONOSPACE
            setPadding(0, activity.dp(2), 0, 0)
        }
        nameLayout.addView(typeLabel)
        headerLayout.addView(nameLayout)

        // Status indicator
        val statusText = TextView(activity).apply {
            text = when {
                isUnlocked -> "ปลดล็อค"
                canUnlock -> "${skill.cost} SP"
                else -> "LOCKED"
            }
            textSize = 11f
            setTextColor(when {
                isUnlocked -> Color.parseColor("#00ff88")
                canUnlock && skillPoints >= skill.cost -> Color.parseColor("#f1c40f")
                else -> Color.parseColor("#444444")
            })
            typeface = Typeface.MONOSPACE
            gravity = Gravity.END
        }
        headerLayout.addView(statusText)
        
        layout.addView(headerLayout)

        // Description with better styling
        val descText = TextView(activity).apply {
            text = skill.description
            textSize = 12f
            setTextColor(if (isUnlocked) Color.parseColor("#bbbbbb") else Color.parseColor("#666666"))
            setPadding(0, activity.dp(12), 0, 0)
            alpha = if (isUnlocked) 1f else 0.7f
        }
        layout.addView(descText)

        // Effects Section (Only if unlocked or available)
        if (skill.effects.isNotEmpty() && (isUnlocked || canUnlock)) {
            val effectsLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(activity.dp(8), activity.dp(8), activity.dp(8), activity.dp(8))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = activity.dp(8)
                }
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#0d0d0d"))
                    cornerRadius = activity.dp(6).toFloat()
                }
            }
            
            skill.effects.forEach { (key, value) ->
                val effectText = TextView(activity).apply {
                    text = "▶ ${formatEffect(key, value)}"
                    textSize = 11f
                    setTextColor(Color.parseColor("#00d4ff"))
                }
                effectsLayout.addView(effectText)
            }
            layout.addView(effectsLayout)
        }

        // Unlock Button or Prerequisite info
        if (!isUnlocked) {
            if (canUnlock) {
                if (skillPoints >= skill.cost) {
                    val btnUnlock = Button(activity).apply {
                        text = "UNLOCK SKILL"
                        textSize = 12f
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#27ae60"))
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, activity.dp(36)).apply {
                            topMargin = activity.dp(12)
                        }
                        setOnClickListener { unlockSkill(skill) }
                    }
                    layout.addView(btnUnlock)
                }
            } else {
                val prereqText = TextView(activity).apply {
                    val missing = skill.prerequisites.filter { it !in unlockedSkills }.map { getSkillName(it) }.joinToString(", ")
                    text = "ต้องการ: $missing"
                    textSize = 10f
                    setTextColor(Color.parseColor("#e74c3c"))
                    setPadding(0, activity.dp(8), 0, 0)
                    gravity = Gravity.CENTER
                }
                layout.addView(prereqText)
            }
        }

        card.addView(layout)
        return card
    }
    
    private fun canUnlockSkill(skill: Skill): Boolean {
        // Check prerequisites
        return skill.prerequisites.all { it in unlockedSkills }
    }
    
    private fun unlockSkill(skill: Skill) {
        if (skill.id in unlockedSkills) {
            return
        }

        if (skillPoints < skill.cost) {
            Toast.makeText(activity, "❌ แต้มทักษะไม่พอ!", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!canUnlockSkill(skill)) {
            Toast.makeText(activity, "❌ ยังไม่ผ่านเงื่อนไขการปลดล็อค!", Toast.LENGTH_SHORT).show()
            return
        }
        
        skillPoints -= skill.cost
        unlockedSkills.add(skill.id)
        
        Toast.makeText(
            activity,
            "✅ ปลดล็อคสำเร็จ: ${skill.name}!",
            Toast.LENGTH_LONG
        ).show()
        
        onSkillChanged(skillPoints, unlockedSkills)
        refreshSkillTree()
    }
    
    private fun showSkillDetails(skill: Skill, isUnlocked: Boolean, canUnlock: Boolean) {
        val details = buildString {
            appendLine("${skill.icon} ${skill.name}")
            appendLine()
            appendLine("ประเภท: ${if (skill.isPassive) "📊 Passive" else "⚡ Active"}")
            appendLine("ราคา: ${skill.cost} แต้มทักษะ")
            appendLine()
            appendLine(skill.description)
            
            if (skill.effects.isNotEmpty()) {
                appendLine()
                appendLine("ความสามารถ:")
                skill.effects.forEach { (key, value) ->
                    appendLine("• ${formatEffect(key, value)}")
                }
            }
            
            if (skill.prerequisites.isNotEmpty()) {
                appendLine()
                appendLine("ต้องการ:")
                skill.prerequisites.forEach { prereqId ->
                    val unlocked = prereqId in unlockedSkills
                    val emoji = if (unlocked) "✅" else "❌"
                    appendLine("$emoji ${getSkillName(prereqId)}")
                }
            }
        }
        
        val builder = AlertDialog.Builder(activity)
            .setTitle(if (isUnlocked) "✅ ปลดล็อคแล้ว" else "รายละเอียดทักษะ")
            .setMessage(details)
            .setNegativeButton("ปิด", null)
            
        if (!isUnlocked && canUnlock) {
            builder.setPositiveButton("🔓 ปลดล็อค") { _, _ ->
                unlockSkill(skill)
            }
        } else if (!isUnlocked && !canUnlock) {
            builder.setMessage(details + "\n\n⚠️ ยังไม่ผ่านเงื่อนไขการปลดล็อค")
        }
        
        builder.show()
    }
    
    private fun formatEffect(key: String, value: Any): String {
        return when (key) {
            "atk_bonus" -> "⚔️ พลังโจมตี +$value"
            "def_bonus" -> "🛡️ พลังป้องกัน +$value"
            "hp_bonus" -> "❤️ HP สูงสุด +$value"
            "luck_bonus" -> "🍀 โชค +$value"
            "crit_chance" -> "💥 โอกาสคริติคอล +$value%"
            "crit_bonus" -> "💥 พลังคริติคอล +$value%"
            "dodge_chance" -> "💨 โอกาสหลบหลีก +$value%"
            "soul_regen" -> "✨ ฟื้นฟูวิญญาณ +$value ต่อเทิร์น"
            "gold_bonus" -> "💰 ทองที่ได้รับ +$value%"
            "gold_multiplier" -> "💰 ทองที่ได้รับ ×$value"
            "exp_bonus" -> "📊 EXP ที่ได้รับ +$value%"
            "heal_bonus" -> "💚 การรักษา +$value%"
            "damage_reduction" -> "🛡️ ลดความเสียหายรับ $value หน่วย"
            "speed_bonus" -> "⚡ ความเร็ว +$value"
            "max_hp" -> "❤️ HP สูงสุด +$value"
            "max_soul" -> "✨ Soul Points เริ่มต้น +$value"
            "block_chance" -> "🛡️ โอกาสบล็อก $value%"
            "reflect" -> "⚔️ สะท้อนความเสียหาย $value%"
            "soul_per_kill" -> "👻 +$value Soul ต่อศัตรูที่ตาย"
            "low_hp_dmg_boost" -> "🩸 ดาเมจเพิ่มเมื่อ HP ต่ำ +$value%"
            "low_hp_def_boost" -> "🪨 ลดดาเมจรับเมื่อ HP ต่ำ $value%"
            "lifesteal_passive" -> "🩸 ดูดพลังชีวิตจากทุกการโจมตี $value%"
            "soul_burst" -> "💥 ระเบิดวิญญาณ (SP × $value = ดาเมจ)"
            "hp_regen" -> "💚 ฟื้นฟู HP +$value ต่อเทิร์น"
            "regen" -> "💚 ฟื้นฟู HP +$value ต่อเทิร์น (passive)"
            "self_damage_pct" -> "💔 เสียสละ HP ตัวเอง $value%"
            else -> "$key: $value"
        }
    }
    
    private fun getSkillName(skillId: String): String {
        return SkillTreeData.getAnySkillById(playerClass, playerRace, skillId)?.name ?: skillId
    }
    
    private fun addBottomControls() {
        val controlLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            val p = activity.dp(12)
            setPadding(p, p, p, p)
            setBackgroundColor(activity.getThemeColor("card"))
        }
        
        val btnBack = Button(activity).apply {
            text = "⬅️ กลับ"
            textSize = 14f
            setBackgroundColor(activity.getThemeColor("accent"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                0,
                activity.dp(50),
                1f
            ).apply { marginEnd = activity.dp(8) }
            
            setOnClickListener {
                activity.setContentView(activity.rootLayout)
                activity.continueAdventure()
            }
        }
        
        val btnReset = Button(activity).apply {
            text = "🔄 รีเซ็ต (100g)"
            textSize = 14f
            setBackgroundColor(Color.parseColor("#e74c3c"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                0,
                activity.dp(50),
                1f
            )
            
            setOnClickListener {
                if (activity.gold >= 100) {
                    confirmResetSkills()
                } else {
                    Toast.makeText(activity, "❌ ต้องใช้ 100 ทองเพื่อรีเซ็ต!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        controlLayout.addView(btnBack)
        controlLayout.addView(btnReset)
        
        skillTreeLayout.addView(controlLayout)
    }
    
    private fun confirmResetSkills() {
        val refundPoints = unlockedSkills.sumOf { skillId ->
            findSkillById(skillId)?.cost ?: 0
        }
        
        AlertDialog.Builder(activity)
            .setTitle("⚠️ รีเซ็ตทักษะทั้งหมด?")
            .setMessage("ราคา: 100 ทอง\nได้รับคืน: $refundPoints แต้มทักษะ\n\nทักษะทั้งหมดจะถูกยกเลิก!")
            .setPositiveButton("รีเซ็ต") { _, _ ->
                if (activity.gold >= 100) {
                    activity.gold -= 100
                    unlockedSkills.clear()
                    skillPoints += refundPoints
                    
                    onSkillChanged(skillPoints, unlockedSkills)
                    refreshSkillTree()

                    Toast.makeText(
                        activity,
                        "✅ รีเซ็ตทักษะแล้ว! +$refundPoints แต้ม",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(activity, "❌ ทองไม่พอ! (ต้องการ 100g)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
    
    private fun findSkillById(skillId: String): Skill? {
        return SkillTreeData.getSkillById(playerClass, skillId)
            ?: RaceSkillData.getRaceSkillById(playerRace, skillId)
    }
    
    private fun refreshSkillTree() {
        // Remove old content
        skillTreeLayout.removeAllViews()
        
        // Rebuild UI
        addHeader()
        addTreeTabs()
        addSkillTreeContent()
        addBottomControls()
    }
}

// ========== DATA AND PROVIDER ==========

object SkillTreeData {
    val skillTrees: Map<RoguelikeMainActivity.CharacterClass, Map<SkillTreeType, SkillTree>> by lazy {
        mapOf(
            RoguelikeMainActivity.CharacterClass.WARRIOR to createWarriorSkills(),
            RoguelikeMainActivity.CharacterClass.MAGE to createMageSkills(),
            RoguelikeMainActivity.CharacterClass.ROGUE to createRogueSkills(),
            RoguelikeMainActivity.CharacterClass.CLERIC to createClericSkills(),
            RoguelikeMainActivity.CharacterClass.RANGER to createRangerSkills(),
            RoguelikeMainActivity.CharacterClass.PALADIN to createPaladinSkills(),
            RoguelikeMainActivity.CharacterClass.NECROMANCER to createNecroSkills(),
            RoguelikeMainActivity.CharacterClass.BERSERKER to createBerserkerSkills(),
            RoguelikeMainActivity.CharacterClass.BARD to createBardSkills(),
            RoguelikeMainActivity.CharacterClass.DRUID to createDruidSkills(),
            RoguelikeMainActivity.CharacterClass.SAMURAI to createSamuraiSkills(),
            RoguelikeMainActivity.CharacterClass.SHAMAN to createShamanSkills(),
            RoguelikeMainActivity.CharacterClass.MONK to createMonkSkills(),
            RoguelikeMainActivity.CharacterClass.ALCHEMIST to createAlchemistSkills(),
            RoguelikeMainActivity.CharacterClass.PIRATE to createPirateSkills(),
            RoguelikeMainActivity.CharacterClass.NINJA to createNinjaSkills(),
            RoguelikeMainActivity.CharacterClass.TEMPLAR to createTemplarSkills(),
            RoguelikeMainActivity.CharacterClass.SORCERER to createSorcererSkills(),
            RoguelikeMainActivity.CharacterClass.TINKERER to createTinkererSkills(),
            RoguelikeMainActivity.CharacterClass.GLADIATOR to createGladiatorSkills()
        )
    }

    fun getSkillById(playerClass: RoguelikeMainActivity.CharacterClass, skillId: String): Skill? {
        val trees = skillTrees[playerClass] ?: return null
        trees.values.forEach { tree ->
            tree.tiers.forEach { tier ->
                tier.skills.find { it.id == skillId }?.let { return it }
            }
        }
        return null
    }

    fun getAnySkillById(
        playerClass: RoguelikeMainActivity.CharacterClass,
        playerRace: RoguelikeMainActivity.Race,
        skillId: String
    ): Skill? = getSkillById(playerClass, skillId) ?: RaceSkillData.getRaceSkillById(playerRace, skillId)

    private fun createWarriorSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree(
                name = "Combat",
                description = "Offensive abilities",
                tiers = listOf(
                    SkillTier(
                        tierLevel = 1,
                        name = "Basic Combat",
                        skills = listOf(
                            Skill("warrior_atk_1", "Power Strike", "⚔️", "Increase base attack damage", 1, true, effects = mapOf("atk_bonus" to 10)),
                            Skill("warrior_crit_1", "Battle Focus", "💥", "Increase critical hit chance", 1, true, effects = mapOf("crit_chance" to 5)),
                            Skill("warrior_cleave", "Cleave", "🪓", "Active: Deal 150% damage to all enemies", 1, false, effects = mapOf("aoe_damage" to 150))
                        )
                    ),
                    SkillTier(
                        tierLevel = 2,
                        name = "Advanced Combat",
                        skills = listOf(
                            Skill("warrior_atk_2", "Berserker Rage", "😡", "Greatly increase attack at cost of defense", 2, true, prerequisites = listOf("warrior_atk_1"), effects = mapOf("atk_bonus" to 25, "def_bonus" to -10)),
                            Skill("warrior_war_cry", "War Cry", "📢", "Active: Increase attack for 3 turns", 2, false, prerequisites = listOf("warrior_cleave"), effects = mapOf("atk_buff" to 20, "duration" to 3)),
                            Skill("warrior_shield_slam", "Shield Slam", "🔰", "Active: ใช้พลังป้องกันโจมตีศัตรู + สตัน 1 เทิร์น", 2, false, prerequisites = listOf("warrior_cleave"), effects = mapOf("def_scaling_damage" to 1.5, "stun" to 1))
                        )
                    ),
                    SkillTier(
                        tierLevel = 3,
                        name = "Master Combat",
                        skills = listOf(
                            Skill("warrior_ultimate", "Titan's Might", "👑", "Ultimate: Massive damage + stun", 3, false, prerequisites = listOf("warrior_atk_2", "warrior_cleave"), effects = mapOf("damage" to 300, "stun" to true)),
                            Skill("warrior_war_banner", "War Banner", "🚩", "Active: เพิ่ม ATK+DEF ตัวเองพร้อมกัน 4 เทิร์น", 3, false, prerequisites = listOf("warrior_war_cry"), effects = mapOf("atk_buff" to 15, "def_buff" to 20, "duration" to 4))
                        )
                    )
                )
            ),
            SkillTreeType.DEFENSE to SkillTree(
                name = "Defense",
                description = "Defensive abilities",
                tiers = listOf(
                    SkillTier(
                        tierLevel = 1,
                        name = "Basic Defense",
                        skills = listOf(
                            Skill("warrior_def_1", "Iron Skin", "🛡️", "Increase defense", 1, true, effects = mapOf("def_bonus" to 15)),
                            Skill("warrior_hp_1", "Vitality", "❤️", "Increase maximum HP", 1, true, effects = mapOf("hp_bonus" to 20))
                        )
                    ),
                    SkillTier(
                        tierLevel = 2,
                        name = "Advanced Defense",
                        skills = listOf(
                            Skill("warrior_block", "Shield Master", "🛡️", "Chance to block attacks completely", 2, true, prerequisites = listOf("warrior_def_1"), effects = mapOf("block_chance" to 15)),
                            Skill("warrior_regen", "Second Wind", "💚", "Regenerate HP each turn", 2, true, prerequisites = listOf("warrior_hp_1"), effects = mapOf("hp_regen" to 3)),
                            Skill("warrior_iron_will", "Iron Will", "🪨", "Passive: เมื่อ HP < 50% ลดความเสียหายที่รับลง 30%", 2, true, prerequisites = listOf("warrior_def_1"), effects = mapOf("low_hp_def_boost" to 30))
                        )
                    )
                )
            ),
            SkillTreeType.UTILITY to SkillTree(
                name = "Utility",
                description = "Support abilities",
                tiers = listOf(
                    SkillTier(
                        tierLevel = 1,
                        name = "Basic Utility",
                        skills = listOf(
                            Skill("warrior_gold", "Treasure Hunter", "💰", "Increase gold drops", 1, true, effects = mapOf("gold_bonus" to 10))
                        )
                    )
                )
            )
        )
    }

    private fun createMageSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Elemental Magic", "Offensive spells", listOf(
                SkillTier(1, "Basic Magic", listOf(
                    Skill("mage_atk_1", "Spell Power", "🔮", "Increase spell damage", 1, true, effects = mapOf("atk_bonus" to 15)),
                    Skill("mage_fireball", "Fireball", "🔥", "Deal 180% damage", 1, false, effects = mapOf("damage" to 180))
                )),
                SkillTier(2, "Advanced Magic", listOf(
                    Skill("mage_chain_lightning", "Chain Lightning", "⚡", "Deal 130% AOE damage", 2, false, prerequisites = listOf("mage_fireball"), effects = mapOf("aoe_damage" to 130)),
                    Skill("mage_blizzard", "Blizzard", "❄️", "Deal 100% damage & freeze", 2, false, prerequisites = listOf("mage_fireball"), effects = mapOf("damage" to 100, "freeze" to 1)),
                    Skill("mage_blaze", "Inferno Blaze", "🌋", "Active: ระเบิดเพลิงอัดแน่น 220% + ติดเผาไหม้", 2, false, prerequisites = listOf("mage_fireball"), effects = mapOf("damage" to 220, "POISON" to 8))
                )),
                SkillTier(3, "Arcane Mastery", listOf(
                    Skill("mage_time_stop", "Time Stop", "⏳", "Active: หยุดเวลา! ศัตรูทุกตัวมึนงง 2 เทิร์น", 3, false, prerequisites = listOf("mage_chain_lightning"), effects = mapOf("aoe_stun" to 2)),
                    Skill("mage_arcane_mind", "Arcane Mind", "🧠", "Passive: เพิ่ม ATK+Soul Regen จากการฝึกจิต", 2, true, prerequisites = listOf("mage_atk_1"), effects = mapOf("atk_bonus" to 20, "soul_regen" to 5))
                ))
            )),
            SkillTreeType.UTILITY to SkillTree("Mana Control", "Internal energy", listOf(
                SkillTier(1, "Mana Flow", listOf(
                    Skill("mage_soul_1", "Soul Well", "✨", "+5 Soul Regen per turn", 1, true, effects = mapOf("soul_regen" to 5))
                ))
            ))
        )
    }

    private fun createRogueSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Assassination", "Sneak and crit", listOf(
                SkillTier(1, "Beginner", listOf(
                    Skill("rogue_crit_1", "Deadly Aim", "🎯", "+10% Crit Chance", 1, true, effects = mapOf("crit_chance" to 10)),
                    Skill("rogue_backstab", "Backstab", "🔪", "Deal 140% damage", 1, false, effects = mapOf("damage" to 140))
                )),
                SkillTier(2, "Assassin", listOf(
                    Skill("rogue_poison", "Poison Blade", "🧪", "Damage & Poison enemy", 2, false, prerequisites = listOf("rogue_backstab"), effects = mapOf("damage" to 110, "poison" to 3)),
                    Skill("rogue_shadow", "Shadow Step", "👤", "+20% Dodge for 3 turns", 2, false, effects = mapOf("dodge_buff" to 20, "duration" to 3)),
                    Skill("rogue_heartseeker", "Heartseeker", "🗡️", "Active: แทงหัวใจ 200% — แม่นยำสูงสุด", 2, false, prerequisites = listOf("rogue_crit_1"), effects = mapOf("damage" to 200))
                )),
                SkillTier(3, "Shadow Master", listOf(
                    Skill("rogue_vanish", "Vanish", "🌑", "Active: หายตัว! หลบหลีก 50% + ATK พุ่ง 2 เทิร์น", 2, false, prerequisites = listOf("rogue_shadow"), effects = mapOf("dodge_buff" to 50, "atk_buff" to 30, "duration" to 2)),
                    Skill("rogue_death_mark", "Death Mark", "💀", "Active: ทำเครื่องหมายตาย — ลด ATK+DEF+ติดพิษพร้อมกัน", 3, false, prerequisites = listOf("rogue_poison"), effects = mapOf("debuff_atk" to 15, "debuff_def" to 15, "poison" to 5, "duration" to 3))
                ))
            ))
        )
    }

    private fun createClericSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Holy Arts", "Light power", listOf(
                SkillTier(1, "Acolyte", listOf(
                    Skill("cleric_heal", "Lesser Heal", "💖", "Heal 30% HP", 1, false, effects = mapOf("heal" to 30)),
                    Skill("cleric_def_1", "Faith", "✨", "+10 DEF", 1, true, effects = mapOf("def_bonus" to 10)),
                    Skill("cleric_blessing", "Blessing", "🙏", "Passive: พรของพระเจ้า — ฟื้นฟู HP + เพิ่มดวง", 1, true, effects = mapOf("regen" to 4, "luck_bonus" to 8))
                )),
                SkillTier(2, "Priest", listOf(
                    Skill("cleric_purify", "Purify", "🚿", "Heal & Remove status", 2, false, prerequisites = listOf("cleric_heal"), effects = mapOf("heal" to 20, "purify" to true)),
                    Skill("cleric_barrier", "Holy Barrier", "🛡️", "Reduce damage taken by 40%", 2, false, effects = mapOf("damage_reduction" to 40, "duration" to 2)),
                    Skill("cleric_divine_strike", "Divine Strike", "⚡", "Active: โจมตีศักดิ์สิทธิ์ที่รักษาผู้ใช้ด้วย", 2, false, prerequisites = listOf("cleric_def_1"), effects = mapOf("damage" to 130, "heal" to 0.2))
                )),
                SkillTier(3, "High Priest", listOf(
                    Skill("cleric_resurrection", "Resurrection", "✝️", "Active: ฟื้นฟู 80% HP + ล้างสถานะผิดปกติทั้งหมด", 3, false, prerequisites = listOf("cleric_purify"), effects = mapOf("heal" to 0.8, "purify" to true))
                ))
            ))
        )
    }

    private fun createRangerSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Archery", "Distance and precision", listOf(
                SkillTier(1, "Fledgling", listOf(
                    Skill("ranger_double_shot", "Double Shot", "🏹", "Deal 140% damage", 1, false, effects = mapOf("damage" to 140)),
                    Skill("ranger_atk_1", "Eagle Eye", "🎯", "+12 ATK passive", 1, true, effects = mapOf("atk_bonus" to 12)),
                    Skill("ranger_wild_instinct", "Wild Instinct", "🐺", "Passive: สัญชาตญาณป่า — ฟื้นฟู HP + เพิ่มดวง", 1, true, effects = mapOf("hp_regen" to 4, "luck_bonus" to 6))
                )),
                SkillTier(2, "Hunter", listOf(
                    Skill("ranger_arrow_rain", "Arrow Rain", "🌧️", "Deal 120% AOE damage", 2, false, prerequisites = listOf("ranger_double_shot"), effects = mapOf("aoe_damage" to 120)),
                    Skill("ranger_trap", "Bear Trap", "🪤", "Stun enemy for 2 turns", 2, false, effects = mapOf("stun" to 2)),
                    Skill("ranger_aimed_shot", "Aimed Shot", "🎯", "Active: เล็งนานแต่แม่น — โจมตีสูงสุด 200%", 2, false, prerequisites = listOf("ranger_atk_1"), effects = mapOf("damage" to 200))
                )),
                SkillTier(3, "Beastmaster", listOf(
                    Skill("ranger_poison_arrow", "Poison Arrow", "🧪", "Active: ลูกธนูพิษ — ดาเมจ + ติดพิษหนัก", 2, false, prerequisites = listOf("ranger_double_shot"), effects = mapOf("damage" to 110, "poison" to 6))
                ))
            ))
        )
    }

    private fun createPaladinSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Holy Power", "Light's wrath", listOf(
                SkillTier(1, "Novice", listOf(
                    Skill("paladin_smite", "Smite", "✨", "Deal 130% damage", 1, false, effects = mapOf("damage" to 130)),
                    Skill("pal_hp_1", "Holy Vitality", "❤️", "+20 Max HP", 1, true, effects = mapOf("max_hp" to 20)),
                    Skill("paladin_devotion", "Devotion", "🕊️", "Passive: ความศรัทธา — DEF + ดวงจากพระเจ้า", 1, true, effects = mapOf("def_bonus" to 15, "luck_bonus" to 5))
                )),
                SkillTier(2, "Knight", listOf(
                    Skill("paladin_judgment", "Judgment", "⚖️", "Damage based on DEF", 2, false, prerequisites = listOf("paladin_smite"), effects = mapOf("def_scaling_damage" to 2.0)),
                    Skill("paladin_aura", "Holy Aura", "☀️", "+15 DEF & +5 HP Regen passive", 2, true, effects = mapOf("def_bonus" to 15, "regen" to 5)),
                    Skill("paladin_consecrate", "Consecrate", "🌟", "Active: แสงศักดิ์สิทธิ์ AoE + รักษาตัวเอง 20%", 2, false, prerequisites = listOf("paladin_smite"), effects = mapOf("aoe_damage" to 120, "heal" to 0.2))
                )),
                SkillTier(3, "Holy Knight", listOf(
                    Skill("paladin_divine_shield", "Divine Shield", "🛡️", "Active: โล่พระเจ้า — DEF พุ่ง + รีเจน 3 เทิร์น", 3, false, prerequisites = listOf("paladin_judgment"), effects = mapOf("def_buff" to 60, "regen_buff" to 20, "duration" to 3))
                ))
            ))
        )
    }

    private fun createNecroSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Death Magic", "Dark arts", listOf(
                SkillTier(1, "Apprentice", listOf(
                    Skill("necro_drain", "Life Drain", "💀", "Damage & Heal 20%", 1, false, effects = mapOf("damage" to 110, "lifesteal" to 20)),
                    Skill("necro_soul_1", "Soul Reaper", "👻", "+10 Soul/Kill", 1, true, effects = mapOf("soul_per_kill" to 10)),
                    Skill("necro_undying", "Undying", "🩸", "Passive: กาฝากชีวิต — ดูดพลังชีวิต 10% จากทุกดาเมจที่ตี", 2, true, effects = mapOf("lifesteal_passive" to 10))
                )),
                SkillTier(2, "Necromancer", listOf(
                    Skill("necro_curse", "Wither", "🥀", "Reduce enemy ATK & DEF", 2, false, effects = mapOf("debuff_atk" to 10, "debuff_def" to 10, "duration" to 3)),
                    Skill("necro_raise", "Raise Dead", "🧟", "Summon minion to attack", 3, false, prerequisites = listOf("necro_drain"), effects = mapOf("summon" to "skeleton", "damage" to 80)),
                    Skill("necro_soul_burst", "Soul Explosion", "💥", "Active: ระเบิดวิญญาณ! แปลง SP ทั้งหมดเป็นดาเมจ (SP×3)", 3, false, prerequisites = listOf("necro_soul_1"), effects = mapOf("soul_burst" to 3))
                )),
                SkillTier(3, "Death Lord", listOf(
                    Skill("necro_bone_wall", "Bone Wall", "🦴", "Active: กำแพงกระดูก — DEF ตัวเองพุ่ง + ลด DEF ศัตรู", 2, false, prerequisites = listOf("necro_curse"), effects = mapOf("def_buff" to 40, "debuff_def" to 20, "duration" to 3))
                ))
            ))
        )
    }

    private fun createBerserkerSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Rage", "Pure fury", listOf(
                SkillTier(1, "Angry", listOf(
                    Skill("berserker_slash", "Heavy Slash", "🪓", "Deal 160% damage", 1, false, effects = mapOf("damage" to 160)),
                    Skill("ber_atk_1", "Inner Rage", "💢", "+15 ATK passive", 1, true, effects = mapOf("atk_bonus" to 15)),
                    Skill("berserker_primal_fury", "Primal Fury", "🔥", "Passive: ความโกรธดึกดำบรรพ์ — คริต + ดาเมจเพิ่มเมื่อ HP ต่ำ", 1, true, effects = mapOf("low_hp_dmg_boost" to 30, "crit_chance" to 8))
                )),
                SkillTier(2, "Furious", listOf(
                    Skill("berserker_blood", "Bloodlust", "🩸", "More DMG as HP drops", 2, true, effects = mapOf("low_hp_dmg_boost" to 50)),
                    Skill("berserker_execute", "Execute", "☠️", "Double DMG if enemy HP < 25%", 3, false, prerequisites = listOf("berserker_slash"), effects = mapOf("execute_threshold" to 25, "damage" to 200)),
                    Skill("berserker_rampage", "Rampage", "🌀", "Active: คลั่ง! โจมตี 3 ครั้งติดต่อกันสุ่มเป้าหมาย", 2, false, prerequisites = listOf("ber_atk_1"), effects = mapOf("multi_hit" to 3, "damage" to 130))
                )),
                SkillTier(3, "Incarnate", listOf(
                    Skill("berserker_war_stomp", "War Stomp", "🦶", "Active: กระทืบสนามรบ — AoE + สตันศัตรูทุกตัว", 3, false, prerequisites = listOf("berserker_slash"), effects = mapOf("aoe_damage" to 130, "aoe_stun" to 1))
                ))
            ))
        )
    }

    private fun createBardSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Music", "Sonic waves", listOf(
                SkillTier(1, "Performer", listOf(
                    Skill("bard_song", "Dissonance", "🎶", "Damage & Stun", 1, false, effects = mapOf("damage" to 100, "stun" to 1)),
                    Skill("bard_luck_1", "Good Vibes", "🍀", "+10 Luck passive", 1, true, effects = mapOf("luck_bonus" to 10))
                )),
                SkillTier(2, "Troubadour", listOf(
                    Skill("bard_buff", "Heroic Ballad", "🎺", "Buff all stats for 5 turns", 2, false, effects = mapOf("all_stats_buff" to 10, "duration" to 5)),
                    Skill("bard_wealth", "Golden Melody", "💰", "+50% Gold from battle", 2, true, effects = mapOf("gold_multiplier" to 1.5)),
                    Skill("bard_requiem", "Requiem", "🎻", "Active: บทเพลงมรณะ — ลด ATK+DEF + สตัน 1 เทิร์น", 2, false, prerequisites = listOf("bard_song"), effects = mapOf("debuff_atk" to 15, "debuff_def" to 15, "stun" to 1))
                )),
                SkillTier(3, "Maestro", listOf(
                    Skill("bard_encore", "Soul Encore", "🎤", "Passive: เพลงอมตะ — Soul Regen สูง + ดวงโชค", 2, true, prerequisites = listOf("bard_luck_1"), effects = mapOf("soul_regen" to 8, "luck_bonus" to 8)),
                    Skill("bard_war_anthem", "War Anthem", "📯", "Active: เพลงสงครามสูงสุด — All Stats สูงมาก 6 เทิร์น", 3, false, prerequisites = listOf("bard_buff"), effects = mapOf("all_stats_buff" to 20, "duration" to 6))
                ))
            ))
        )
    }

    private fun createDruidSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Nature", "Wild power", listOf(
                SkillTier(1, "Seedling", listOf(
                    Skill("druid_vine", "Vine Whip", "🌿", "Deal 120% damage", 1, false, effects = mapOf("damage" to 120)),
                    Skill("druid_regen_1", "Natural Growth", "🌱", "+5 HP Regen passive", 1, true, effects = mapOf("regen" to 5)),
                    Skill("druid_barkskin", "Barkskin", "🪵", "Passive: หนังเปลือกไม้ — DEF + MaxHP เพิ่มขึ้น", 1, true, effects = mapOf("def_bonus" to 20, "hp_bonus" to 15))
                )),
                SkillTier(2, "Warden", listOf(
                    Skill("druid_form", "Bear Form", "🐻", "+50% HP & DEF, -20% ATK", 2, false, effects = mapOf("hp_buff" to 50, "def_buff" to 50, "atk_debuff" to 20, "duration" to 3)),
                    Skill("druid_entangle", "Entangle", "🌳", "Root all enemies (Skip turn)", 3, false, prerequisites = listOf("druid_vine"), effects = mapOf("aoe_stun" to 1)),
                    Skill("druid_regrowth", "Regrowth", "🌺", "Active: ฟื้นฟูพลังธรรมชาติ — ฮีลทันที + รีเจนยาวๆ", 2, false, prerequisites = listOf("druid_regen_1"), effects = mapOf("hp_buff" to 30, "regen_buff" to 20, "duration" to 5))
                )),
                SkillTier(3, "Archdruid", listOf(
                    Skill("druid_storm", "Nature's Wrath", "⛈️", "Active: พายุธรรมชาติ — AoE 150% + สถานะสุ่มทุกตัว", 3, false, prerequisites = listOf("druid_vine"), effects = mapOf("aoe_damage" to 150, "random_status" to true))
                ))
            ))
        )
    }

    private fun createSamuraiSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Bushido", "Way of the warrior", listOf(
                SkillTier(1, "Ronin", listOf(
                    Skill("samurai_slash", "Quick Draw", "🏮", "Deal 150% damage", 1, false, effects = mapOf("damage" to 150)),
                    Skill("samurai_crit_1", "Focus", "🎯", "+10% Crit passive", 1, true, effects = mapOf("crit_bonus" to 10))
                )),
                SkillTier(2, "Samurai", listOf(
                    Skill("samurai_counter", "Counter", "⚔️", "Reflect 50% damage passive", 2, true, effects = mapOf("reflect" to 50)),
                    Skill("samurai_ultimate", "Omnislash", "🌌", "7 hits on random enemies", 3, false, prerequisites = listOf("samurai_slash"), effects = mapOf("multi_hit" to 7, "damage" to 40)),
                    Skill("samurai_void_slash", "Void Slash", "⚫", "Active: กระบี่จากความว่างเปล่า — โจมตีรุนแรง 220%", 2, false, prerequisites = listOf("samurai_slash"), effects = mapOf("damage" to 220)),
                    Skill("samurai_death_walk", "Death Walk", "💀", "Passive: เดินสู่ความตาย — เมื่อ HP ต่ำ ATK+Reflect เพิ่ม", 2, true, prerequisites = listOf("samurai_counter"), effects = mapOf("low_hp_dmg_boost" to 40, "reflect" to 25))
                )),
                SkillTier(3, "Kensei", listOf(
                    Skill("samurai_blade_storm", "Blade Storm", "🌀", "Active: พายุใบมีด — 10 ครั้งติดต่อกัน!", 3, false, prerequisites = listOf("samurai_ultimate"), effects = mapOf("multi_hit" to 10, "damage" to 25))
                ))
            ))
        )
    }

    private fun createShamanSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Spirits", "Old ways", listOf(
                SkillTier(1, "Initiate", listOf(
                    Skill("shaman_bolt", "Spirit Bolt", "🎭", "Deal 125% damage", 1, false, effects = mapOf("damage" to 125)),
                    Skill("shaman_soul_1", "Ancestors", "🧿", "+50 Max Soul passive", 1, true, effects = mapOf("max_soul" to 50)),
                    Skill("shaman_wolf_spirit", "Wolf Spirit", "🐺", "Passive: วิญญาณหมาป่า — ATK + ดวงโชคเพิ่ม", 1, true, effects = mapOf("atk_bonus" to 15, "luck_bonus" to 8))
                )),
                SkillTier(2, "Elder", listOf(
                    Skill("shaman_totem", "Healing Totem", "🗿", "+15 HP Regen for 5 turns", 2, false, effects = mapOf("regen_buff" to 15, "duration" to 5)),
                    Skill("shaman_elemental", "Spirit Storm", "🌪️", "Random Elemental AOE", 2, false, prerequisites = listOf("shaman_bolt"), effects = mapOf("aoe_damage" to 110, "random_status" to true)),
                    Skill("shaman_blood_pact", "Blood Pact", "🩸", "Active: สัญญาเลือด — ATK พุ่งสูงสุด 3 เทิร์น", 2, false, prerequisites = listOf("shaman_bolt"), effects = mapOf("atk_buff" to 40, "duration" to 3))
                )),
                SkillTier(3, "High Shaman", listOf(
                    Skill("shaman_earthquake", "Earthquake", "🌍", "Active: แผ่นดินไหว — AoE 160% + สตันศัตรูทั้งหมด", 3, false, prerequisites = listOf("shaman_elemental"), effects = mapOf("aoe_damage" to 160, "aoe_stun" to 1))
                ))
            ))
        )
    }

    private fun createMonkSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Iron Body", "Fists of discipline", listOf(
                SkillTier(1, "Disciple", listOf(
                    Skill("monk_inner_peace", "Inner Peace", "🧘", "Active: สมาธิใจ — ฟื้นฟู HP 20% + ATK ขึ้น 3 เทิร์น", 1, false, effects = mapOf("heal" to 0.2, "atk_buff" to 15, "duration" to 3)),
                    Skill("monk_iron_skin", "Iron Skin", "💪", "Passive: ผิวเหล็ก — DEF +15 & HP Regen passive", 1, true, effects = mapOf("def_bonus" to 15, "regen" to 4))
                )),
                SkillTier(2, "Monk", listOf(
                    Skill("monk_combo_strike", "Combo Strike", "👊", "Active: ต่อยชุด — โจมตี 4 ครั้ง 80% ต่อครั้ง", 2, false, prerequisites = listOf("monk_inner_peace"), effects = mapOf("multi_hit" to 4, "damage" to 80)),
                    Skill("monk_mind_over_body", "Mind Over Body", "🧠", "Passive: จิตเหนือกาย — HP สูง + DEF ลด Soul Cost", 2, true, prerequisites = listOf("monk_iron_skin"), effects = mapOf("hp_bonus" to 30, "def_bonus" to 10))
                )),
                SkillTier(3, "Grandmaster", listOf(
                    Skill("monk_transcendence", "Transcendence", "☯️", "Active: พ้นโลก! AoE + DEF buff ใหญ่ 3 เทิร์น", 3, false, prerequisites = listOf("monk_combo_strike"), effects = mapOf("aoe_damage" to 140, "def_buff" to 40, "duration" to 3))
                ))
            ))
        )
    }

    private fun createAlchemistSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Transmutation", "Brew and concoct", listOf(
                SkillTier(1, "Apprentice", listOf(
                    Skill("alchemist_transmute", "Transmute", "⚗️", "Active: แปรธาตุ — ฮีล HP 25% + ล้างสถานะทั้งหมด", 1, false, effects = mapOf("heal" to 0.25, "purify" to true)),
                    Skill("alchemist_reagent_mastery", "Reagent Mastery", "🔬", "Passive: เชี่ยวชาญสารเคมี — Luck +12 & Gold bonus +15%", 1, true, effects = mapOf("luck_bonus" to 12, "gold_bonus" to 15))
                )),
                SkillTier(2, "Alchemist", listOf(
                    Skill("alchemist_acid_bomb", "Acid Bomb", "💣", "Active: ระเบิดกรด — AoE 120% + ติดพิษทุกตัว", 2, false, prerequisites = listOf("alchemist_transmute"), effects = mapOf("aoe_damage" to 120, "poison" to 5)),
                    Skill("alchemist_fortify", "Fortify Elixir", "🧪", "Active: ยาเสริมกำลัง — ATK+DEF ขึ้นพร้อมกัน 4 เทิร์น", 2, false, prerequisites = listOf("alchemist_reagent_mastery"), effects = mapOf("atk_buff" to 20, "def_buff" to 20, "duration" to 4))
                )),
                SkillTier(3, "Grand Alchemist", listOf(
                    Skill("alchemist_philosophers_stone", "Philosopher's Stone", "💎", "Passive: หินนักปราชญ์ — Luck สูงสุด + Gold bonus ใหญ่", 2, true, prerequisites = listOf("alchemist_acid_bomb"), effects = mapOf("luck_bonus" to 20, "gold_bonus" to 30))
                ))
            ))
        )
    }

    private fun createPirateSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Plunder", "Riches from battle", listOf(
                SkillTier(1, "Buccaneer", listOf(
                    Skill("pirate_plunder", "Plunder", "🏴‍☠️", "Active: ปล้น! โจมตี 140% + ขโมยทองจากศัตรู", 1, false, effects = mapOf("damage" to 140)),
                    Skill("pirate_sea_legs", "Sea Legs", "⚓", "Passive: ขาทะเล — Dodge +8% & Luck +10", 1, true, effects = mapOf("dodge_bonus" to 8, "luck_bonus" to 10))
                )),
                SkillTier(2, "Corsair", listOf(
                    Skill("pirate_cannon_blast", "Cannon Blast", "💥", "Active: ยิงปืนใหญ่ — AoE 150% + สตัน 1 เทิร์น", 2, false, prerequisites = listOf("pirate_plunder"), effects = mapOf("aoe_damage" to 150, "stun" to 1)),
                    Skill("pirate_loaded", "Loaded", "💰", "Passive: มีเงินมาก — ATK +15 จากความร่ำรวย", 2, true, prerequisites = listOf("pirate_sea_legs"), effects = mapOf("atk_bonus" to 15))
                )),
                SkillTier(3, "Pirate King", listOf(
                    Skill("pirate_broadside", "Broadside Volley", "🚢", "Active: ระดมยิง — โจมตี 5 ครั้งสุ่มเป้า 100% ต่อครั้ง", 3, false, prerequisites = listOf("pirate_cannon_blast"), effects = mapOf("multi_hit" to 5, "damage" to 100))
                ))
            ))
        )
    }

    private fun createNinjaSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Shadow Arts", "Speed and stealth", listOf(
                SkillTier(1, "Initiate", listOf(
                    Skill("ninja_shadow_step", "Shadow Step", "🥷", "Active: ก้าวเงา — Dodge +30% & ATK ขึ้น 3 เทิร์น", 1, false, effects = mapOf("dodge_buff" to 30, "atk_buff" to 20, "duration" to 3)),
                    Skill("ninja_shuriken", "Shuriken Barrage", "⭐", "Active: ดาวกระจาย — โจมตี 3 ครั้ง 90% ต่อครั้ง", 1, false, effects = mapOf("multi_hit" to 3, "damage" to 90))
                )),
                SkillTier(2, "Shadow", listOf(
                    Skill("ninja_smoke_bomb", "Smoke Bomb", "💨", "Active: ระเบิดควัน — สตันทุกศัตรู 1 เทิร์น + Dodge พุ่ง", 2, false, prerequisites = listOf("ninja_shadow_step"), effects = mapOf("aoe_stun" to 1, "dodge_buff" to 25, "duration" to 2)),
                    Skill("ninja_blade_dance", "Blade Dance", "🗡️", "Active: เต้นใบมีด — โจมตี 5 ครั้ง 90% ต่อครั้ง", 2, false, prerequisites = listOf("ninja_shuriken"), effects = mapOf("multi_hit" to 5, "damage" to 90))
                )),
                SkillTier(3, "Shadow Master", listOf(
                    Skill("ninja_death_lotus", "Death Lotus", "🌸", "Active: ดอกไม้มรณะ — AoE 160% + ติดพิษทุกตัว + Dodge", 3, false, prerequisites = listOf("ninja_blade_dance", "ninja_smoke_bomb"), effects = mapOf("aoe_damage" to 160, "poison" to 6, "dodge_buff" to 40, "duration" to 2))
                ))
            ))
        )
    }

    private fun createTemplarSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Holy Crusade", "Light's judgment", listOf(
                SkillTier(1, "Initiate", listOf(
                    Skill("templar_holy_strike", "Holy Strike", "⚜️", "Active: โจมตีศักดิ์สิทธิ์ — ดาเมจ 160% + บอสได้รับเพิ่มขึ้น", 1, false, effects = mapOf("damage" to 160)),
                    Skill("templar_divine_armor", "Divine Armor", "🛡️", "Passive: เกราะพระเจ้า — DEF +18 & MaxHP +20", 1, true, effects = mapOf("def_bonus" to 18, "hp_bonus" to 20))
                )),
                SkillTier(2, "Crusader", listOf(
                    Skill("templar_consecration", "Consecration", "✝️", "Active: ศักดิ์สิทธิ์สถาน — AoE 140% + ฮีลตัวเอง 15%", 2, false, prerequisites = listOf("templar_holy_strike"), effects = mapOf("aoe_damage" to 140, "heal" to 0.15)),
                    Skill("templar_shield_faith", "Shield of Faith", "🙏", "Passive: โล่ศรัทธา — MaxHP สูง + Luck จากพระเจ้า", 2, true, prerequisites = listOf("templar_divine_armor"), effects = mapOf("hp_bonus" to 30, "luck_bonus" to 10))
                )),
                SkillTier(3, "Holy Avenger", listOf(
                    Skill("templar_holy_wrath", "Holy Wrath", "☀️", "Active: ความพิโรธศักดิ์สิทธิ์ — ดาเมจ 280% vs บอส หรือ 200% ปกติ", 3, false, prerequisites = listOf("templar_consecration"), effects = mapOf("damage" to 280, "execute_threshold" to 50))
                ))
            ))
        )
    }

    private fun createSorcererSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Arcane Power", "Raw magic unleashed", listOf(
                SkillTier(1, "Apprentice", listOf(
                    Skill("sorcerer_void_bolt", "Void Bolt", "🌑", "Active: ลูกกระสุนอาร์เคน — ดาเมจ 180% ทะลุป้องกัน", 1, false, effects = mapOf("damage" to 180)),
                    Skill("sorcerer_arcane_mastery", "Arcane Mastery", "🔮", "Passive: เชี่ยวชาญอาร์เคน — ATK +18 & Soul Regen +5", 1, true, effects = mapOf("atk_bonus" to 18, "soul_regen" to 5))
                )),
                SkillTier(2, "Sorcerer", listOf(
                    Skill("sorcerer_meteor", "Meteor", "☄️", "Active: อุกกาบาต — AoE 170% ทุกตัว", 2, false, prerequisites = listOf("sorcerer_void_bolt"), effects = mapOf("aoe_damage" to 170)),
                    Skill("sorcerer_mind_shatter", "Mind Shatter", "💀", "Active: ทำลายจิตใจ — ดาเมจ 140% + ลด DEF ศัตรูใหญ่", 2, false, prerequisites = listOf("sorcerer_arcane_mastery"), effects = mapOf("damage" to 140, "debuff_def" to 25, "duration" to 3))
                )),
                SkillTier(3, "Archmage", listOf(
                    Skill("sorcerer_arcane_apocalypse", "Arcane Apocalypse", "🌌", "Active: หายนะอาร์เคน — AoE 220% ทำลายล้างทุกสิ่ง!", 3, false, prerequisites = listOf("sorcerer_meteor"), effects = mapOf("aoe_damage" to 220, "aoe_stun" to 1))
                ))
            )),
            SkillTreeType.UTILITY to SkillTree("Soul Mastery", "Energy control", listOf(
                SkillTier(1, "Flow", listOf(
                    Skill("sorcerer_soul_surge", "Soul Surge", "✨", "Passive: กระแสวิญญาณ — Soul Regen สูงมาก +8", 1, true, effects = mapOf("soul_regen" to 8))
                ))
            ))
        )
    }

    private fun createTinkererSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Gadgetry", "Mechanical ingenuity", listOf(
                SkillTier(1, "Inventor", listOf(
                    Skill("tinkerer_gadget", "Gadget Strike", "🔧", "Active: โจมตีด้วยอุปกรณ์ — ดาเมจ 130% + สตัน 1 เทิร์น", 1, false, effects = mapOf("damage" to 130, "stun" to 1)),
                    Skill("tinkerer_equipment_synergy", "Equipment Synergy", "⚙️", "Passive: ซินเนอร์จี้อุปกรณ์ — ATK+DEF ตาม slot ที่ใส่", 1, true, effects = mapOf("atk_bonus" to 8, "def_bonus" to 8))
                )),
                SkillTier(2, "Engineer", listOf(
                    Skill("tinkerer_turret", "Auto Turret", "🤖", "Active: ป้อมปืนอัตโนมัติ — โจมตี 4 ครั้ง 100% ต่อครั้ง", 2, false, prerequisites = listOf("tinkerer_gadget"), effects = mapOf("multi_hit" to 4, "damage" to 100)),
                    Skill("tinkerer_shield_module", "Shield Module", "🔩", "Passive: โมดูลเกราะ — DEF +18 & โอกาสบล็อก +10%", 2, true, prerequisites = listOf("tinkerer_equipment_synergy"), effects = mapOf("def_bonus" to 18, "block_chance" to 10))
                )),
                SkillTier(3, "Master Tinkerer", listOf(
                    Skill("tinkerer_mech_suit", "Mech Suit", "🦾", "Active: ชุดกลไก — ATK+DEF พุ่งสูงมาก 4 เทิร์น", 3, false, prerequisites = listOf("tinkerer_turret"), effects = mapOf("atk_buff" to 35, "def_buff" to 35, "duration" to 4))
                ))
            ))
        )
    }

    private fun createGladiatorSkills(): Map<SkillTreeType, SkillTree> {
        return mapOf(
            SkillTreeType.COMBAT to SkillTree("Arena Glory", "Fight for the crowd", listOf(
                SkillTier(1, "Fighter", listOf(
                    Skill("gladiator_arena_rush", "Arena Rush", "🏟️", "Active: บุกเข้าสังเวียน — โจมตีรุนแรง 165%", 1, false, effects = mapOf("damage" to 165)),
                    Skill("gladiator_battle_hardened", "Battle Hardened", "⚔️", "Passive: ชินกับสนามรบ — ATK +15 จากการต่อสู้", 1, true, effects = mapOf("atk_bonus" to 15))
                )),
                SkillTier(2, "Veteran", listOf(
                    Skill("gladiator_net_throw", "Net Throw", "🕸️", "Active: ขว้างตาข่าย — สตันศัตรู 2 เทิร์น", 2, false, prerequisites = listOf("gladiator_arena_rush"), effects = mapOf("stun" to 2)),
                    Skill("gladiator_crowd_pleaser", "Crowd Pleaser", "👑", "Active: เอาใจฝูงชน — AoE 145% + ATK buff ตัวเอง", 2, false, prerequisites = listOf("gladiator_battle_hardened"), effects = mapOf("aoe_damage" to 145, "atk_buff" to 20, "duration" to 3))
                )),
                SkillTier(3, "Champion", listOf(
                    Skill("gladiator_champion_spirit", "Champion Spirit", "🏆", "Passive: จิตใจแชมป์ — ATK+DEF+HP สูงสุดจากชัยชนะ", 2, true, prerequisites = listOf("gladiator_crowd_pleaser", "gladiator_net_throw"), effects = mapOf("atk_bonus" to 20, "def_bonus" to 15, "hp_bonus" to 25))
                ))
            ))
        )
    }
}

// ========== RACE SKILL DATA ==========

object RaceSkillData {
    private val raceSkillTrees: Map<RoguelikeMainActivity.Race, SkillTree> by lazy {
        mapOf(
            RoguelikeMainActivity.Race.HUMAN to SkillTree("Human Traits", "Tenacity of Mankind", listOf(
                SkillTier(1, "Adaptable", listOf(
                    Skill("human_resolve", "Unwavering Resolve", "💪", "Passive: สมดุลมนุษย์ — ATK+DEF+Luck เพิ่มขึ้น", 1, true, effects = mapOf("atk_bonus" to 5, "def_bonus" to 5, "luck_bonus" to 5)),
                    Skill("human_determination", "Determination", "🔥", "Active: ความมุ่งมั่น — ฟื้นฟู HP + บัฟ ATK 3 เทิร์น", 1, false, effects = mapOf("hp_buff" to 25, "atk_buff" to 15, "duration" to 3))
                )),
                SkillTier(2, "Human Mastery", listOf(
                    Skill("human_adaptability", "Adaptability", "🌟", "Passive: ปรับตัวเก่ง — Gold bonus + Luck สูง", 2, true, prerequisites = listOf("human_resolve"), effects = mapOf("gold_bonus" to 15, "luck_bonus" to 8))
                ))
            )),
            RoguelikeMainActivity.Race.ELF to SkillTree("Elf Traits", "Ancient Arcana", listOf(
                SkillTier(1, "Arcane Born", listOf(
                    Skill("elf_soul_flow", "Soul Flow", "✨", "Passive: กระแสวิญญาณ — Soul Regen สูงมาก", 1, true, effects = mapOf("soul_regen" to 10)),
                    Skill("elf_grace", "Elven Grace", "🌙", "Passive: ความสง่างามเอลฟ์ — Crit + Luck สูง", 1, true, effects = mapOf("crit_chance" to 12, "luck_bonus" to 6))
                )),
                SkillTier(2, "Ancient Power", listOf(
                    Skill("elf_ancient_arrow", "Ancient Arrow", "🏹", "Active: ลูกศรโบราณ — ดาเมจ 175% + สตัน 1 เทิร์น", 2, false, prerequisites = listOf("elf_soul_flow"), effects = mapOf("damage" to 175, "stun" to 1))
                ))
            )),
            RoguelikeMainActivity.Race.DWARF to SkillTree("Dwarf Traits", "Iron Blood", listOf(
                SkillTier(1, "Stone-born", listOf(
                    Skill("dwarf_stonehide", "Stone Hide", "🪨", "Passive: หนังหิน — DEF + HP สูง", 1, true, effects = mapOf("def_bonus" to 18, "hp_bonus" to 20))
                )),
                SkillTier(2, "Iron Will", listOf(
                    Skill("dwarf_stubbornness", "Dwarven Stubbornness", "⚒️", "Passive: ดื้อรั้นแบบคนแคระ — ลดดาเมจที่รับเมื่อ HP ต่ำ 40%", 2, true, prerequisites = listOf("dwarf_stonehide"), effects = mapOf("low_hp_def_boost" to 40)),
                    Skill("dwarf_battle_axe", "Battle Axe Fury", "🪓", "Active: ขวานรบแห่งความโกรธ — AoE ดาเมจหนัก 160%", 2, false, effects = mapOf("aoe_damage" to 160))
                ))
            )),
            RoguelikeMainActivity.Race.ORC to SkillTree("Orc Traits", "Savage Power", listOf(
                SkillTier(1, "Born Fighter", listOf(
                    Skill("orc_primal_rage", "Primal Rage", "👹", "Passive: ความโกรธดึกดำบรรพ์ — ดาเมจพุ่งเมื่อ HP ต่ำ +45%", 1, true, effects = mapOf("low_hp_dmg_boost" to 45))
                )),
                SkillTier(2, "Warchief", listOf(
                    Skill("orc_warchief_roar", "Warchief's Roar", "📢", "Active: คำรามหัวหน้าสงคราม — ATK พุ่ง + ลด ATK ศัตรู", 2, false, effects = mapOf("atk_buff" to 35, "debuff_atk" to 15, "duration" to 3)),
                    Skill("orc_skull_crush", "Skull Crush", "💥", "Active: บดขยี้กะโหลก — AoE 140% + สตันทุกตัว", 2, false, prerequisites = listOf("orc_warchief_roar"), effects = mapOf("aoe_damage" to 140, "aoe_stun" to 1))
                ))
            )),
            RoguelikeMainActivity.Race.DEMON to SkillTree("Demon Traits", "Soul Contract", listOf(
                SkillTier(1, "Soul Collector", listOf(
                    Skill("demon_soul_harvest", "Soul Harvest", "👿", "Passive: เก็บเกี่ยววิญญาณ — Soul ต่อการสังหาร +20", 1, true, effects = mapOf("soul_per_kill" to 20))
                )),
                SkillTier(2, "Hell's Power", listOf(
                    Skill("demon_hellfire", "Hellfire", "🔥", "Active: ไฟนรก — AoE ดาเมจ + ติดเผาไหม้ทุกตัว", 2, false, effects = mapOf("aoe_damage" to 150, "POISON" to 6)),
                    Skill("demon_sacrifice", "Devil's Contract", "😈", "Active: สัญญาปีศาจ — ดาเมจ 280% แลกด้วย HP ตัวเอง 20%", 3, false, prerequisites = listOf("demon_soul_harvest"), effects = mapOf("damage" to 280, "self_damage_pct" to 20))
                ))
            )),
            RoguelikeMainActivity.Race.ANGEL to SkillTree("Angel Traits", "Divine Grace", listOf(
                SkillTier(1, "Blessed", listOf(
                    Skill("angel_holy_blessing", "Holy Blessing", "🕊️", "Passive: พรศักดิ์สิทธิ์ — ฟื้นฟู HP passive + Luck", 1, true, effects = mapOf("regen" to 6, "luck_bonus" to 8))
                )),
                SkillTier(2, "Angel Power", listOf(
                    Skill("angel_radiance", "Holy Radiance", "☀️", "Active: แสงพระเจ้า — โจมตีพร้อมรักษา HP ตัวเอง 30%", 2, false, effects = mapOf("damage" to 120, "heal" to 0.3)),
                    Skill("angel_divine_barrier", "Divine Barrier", "🛡️", "Active: กำแพงศักดิ์สิทธิ์ — ลดดาเมจ 50% + รีเจน 4 เทิร์น", 2, false, prerequisites = listOf("angel_holy_blessing"), effects = mapOf("damage_reduction" to 50, "regen_buff" to 15, "duration" to 4))
                ))
            )),
            RoguelikeMainActivity.Race.DRAGONKIN to SkillTree("Dragonkin Traits", "Dragon Heritage", listOf(
                SkillTier(1, "Dragon Blood", listOf(
                    Skill("dragon_scales_passive", "Dragon Scales", "🐲", "Passive: เกล็ดมังกร — DEF + HP สูง", 1, true, effects = mapOf("def_bonus" to 15, "hp_bonus" to 25))
                )),
                SkillTier(2, "Dragon Power", listOf(
                    Skill("dragon_breath_fire", "Dragon Breath", "🔥", "Active: ลมหายใจมังกร — AoE ไฟขนาดใหญ่ 180%", 2, false, effects = mapOf("aoe_damage" to 180, "POISON" to 5)),
                    Skill("dragon_roar_skill", "Dragon Roar", "😤", "Active: คำรามมังกร — ลด ATK+DEF ศัตรูพร้อมกัน 25%", 2, false, prerequisites = listOf("dragon_scales_passive"), effects = mapOf("debuff_atk" to 25, "debuff_def" to 25, "duration" to 4))
                ))
            )),
            RoguelikeMainActivity.Race.UNDEAD to SkillTree("Undead Traits", "Death's Persistence", listOf(
                SkillTier(1, "Undying", listOf(
                    Skill("undead_resilience_passive", "Undead Resilience", "💀", "Passive: ความทนทานมรณะ — HP Regen + ลดดาเมจ HP ต่ำ", 1, true, effects = mapOf("hp_regen" to 5, "low_hp_def_boost" to 25))
                )),
                SkillTier(2, "Death Magic", listOf(
                    Skill("undead_death_touch", "Death Touch", "🖤", "Active: สัมผัสมรณะ — ดาเมจ 175% + ดูดชีวิต 30%", 2, false, effects = mapOf("damage" to 175, "lifesteal" to 30)),
                    Skill("undead_curse_skill", "Undead Curse", "☠️", "Active: คำสาปอันเดด — ติดพิษ + ลด ATK+DEF ศัตรู", 2, false, prerequisites = listOf("undead_resilience_passive"), effects = mapOf("poison" to 7, "debuff_atk" to 12, "debuff_def" to 12, "duration" to 3))
                ))
            )),
            RoguelikeMainActivity.Race.VAMPIRE to SkillTree("Vampire Traits", "Blood Magic", listOf(
                SkillTier(1, "Blood Born", listOf(
                    Skill("vampire_blood_thirst", "Blood Thirst", "🧛", "Passive: กระหายเลือด — Lifesteal passive 15% ทุกโจมตี", 1, true, effects = mapOf("lifesteal_passive" to 15)),
                    Skill("vampire_night_power", "Night Power", "🌑", "Passive: พลังราตรี — ATK + Crit สูง", 1, true, effects = mapOf("atk_bonus" to 12, "crit_chance" to 10))
                )),
                SkillTier(2, "Vampire Lord", listOf(
                    Skill("vampire_mist_form", "Mist Form", "💨", "Active: แปลงเป็นหมอก — Dodge 40% + โจมตี 3 ครั้ง", 2, false, prerequisites = listOf("vampire_blood_thirst"), effects = mapOf("dodge_buff" to 40, "multi_hit" to 3, "damage" to 80, "duration" to 2))
                ))
            )),
            RoguelikeMainActivity.Race.FAIRY to SkillTree("Fairy Traits", "Stardust Magic", listOf(
                SkillTier(1, "Lucky", listOf(
                    Skill("fairy_stardust_fortune", "Stardust Fortune", "⭐", "Passive: โชคดาวดวง — Luck +15 & Gold bonus +20%", 1, true, effects = mapOf("luck_bonus" to 15, "gold_bonus" to 20))
                )),
                SkillTier(2, "Fairy Magic", listOf(
                    Skill("fairy_pixie_heal", "Pixie Heal", "✨", "Active: ฮีลแฟรี่ — ฟื้นฟู HP 35% + ล้างสถานะทั้งหมด", 2, false, effects = mapOf("heal" to 0.35, "purify" to true)),
                    Skill("fairy_chaos_swarm", "Chaos Swarm", "🦋", "Active: ฝูงแฟรี่ — AoE 100% + สถานะสุ่มทุกตัว", 2, false, prerequisites = listOf("fairy_stardust_fortune"), effects = mapOf("aoe_damage" to 100, "random_status" to true))
                ))
            )),
            RoguelikeMainActivity.Race.CYBORG to SkillTree("Cyborg Traits", "Overclock Protocol", listOf(
                SkillTier(1, "Iron Body", listOf(
                    Skill("cyborg_armor_plating", "Armor Plating", "🔩", "Passive: เกราะโลหะ — DEF +15 & โอกาสบล็อก 10%", 1, true, effects = mapOf("def_bonus" to 15, "block_chance" to 10)),
                    Skill("cyborg_tactical_scan", "Tactical Scan", "🔭", "Passive: สแกนยุทธศาสตร์ — Crit Chance +10 & Crit Power +15%", 1, true, effects = mapOf("crit_chance" to 10, "crit_bonus" to 15))
                )),
                SkillTier(2, "Overclock", listOf(
                    Skill("cyborg_overclock", "Overclock", "⚡", "Active: โอเวอร์โหลดระบบ — ATK พุ่ง +50 / 2 เทิร์น แลกด้วย HP 15%", 2, false, effects = mapOf("atk_buff" to 50, "duration" to 2, "self_damage_pct" to 15))
                ))
            )),
            RoguelikeMainActivity.Race.SHADOW to SkillTree("Shadow Traits", "Void Walker", listOf(
                SkillTier(1, "Void Born", listOf(
                    Skill("shadow_dark_aura", "Dark Aura", "👥", "Passive: ออร่าความมืด — ATK +18 & ดาเมจเพิ่มเมื่อ HP ต่ำ", 1, true, effects = mapOf("atk_bonus" to 18, "low_hp_dmg_boost" to 25))
                )),
                SkillTier(2, "Shadow Master", listOf(
                    Skill("shadow_phase", "Phase Shift", "🌑", "Active: เฟสร่างผ่านความจริง — Dodge 70% เป็นเวลา 2 เทิร์น", 2, false, effects = mapOf("dodge_buff" to 70, "duration" to 2)),
                    Skill("shadow_void_strike", "Void Strike", "⚫", "Active: ฟันจากความว่าง — โจมตี 4 ครั้งสุ่มเป้า 120% ต่อครั้ง", 2, false, prerequisites = listOf("shadow_dark_aura"), effects = mapOf("multi_hit" to 4, "damage" to 120))
                ))
            )),
            RoguelikeMainActivity.Race.WEREWOLF to SkillTree("Werewolf Traits", "Blood Frenzy", listOf(
                SkillTier(1, "Feral", listOf(
                    Skill("werewolf_frenzy", "Feral Frenzy", "🐺", "Passive: ความดุร้ายสัตว์ — ATK +15 & Crit +12% เมื่อโจมตีต่อเนื่อง", 1, true, effects = mapOf("atk_bonus" to 15, "crit_chance" to 12)),
                    Skill("werewolf_howl", "Battle Howl", "🌕", "Active: คำรามสงคราม — ATK buff สูง 3 เทิร์น", 1, false, effects = mapOf("atk_buff" to 30, "duration" to 3))
                )),
                SkillTier(2, "Wolf Lord", listOf(
                    Skill("werewolf_pack_hunt", "Pack Hunt", "🐾", "Active: ล่าเป็นฝูง — โจมตี 3 ครั้ง 110% ต่อครั้ง", 2, false, prerequisites = listOf("werewolf_howl"), effects = mapOf("multi_hit" to 3, "damage" to 110))
                ))
            )),
            RoguelikeMainActivity.Race.MERMAID to SkillTree("Mermaid Traits", "Ocean's Flow", listOf(
                SkillTier(1, "Sea Born", listOf(
                    Skill("mermaid_current", "Tidal Current", "🌊", "Passive: กระแสทะเล — Soul Regen +6 & Luck +8 ทุกเทิร์น", 1, true, effects = mapOf("soul_regen" to 6, "luck_bonus" to 8))
                )),
                SkillTier(2, "Deep Sea", listOf(
                    Skill("mermaid_whirlpool", "Whirlpool", "🌀", "Active: กระแสน้ำวน — AoE 130% + สตัน 1 เทิร์น", 2, false, prerequisites = listOf("mermaid_current"), effects = mapOf("aoe_damage" to 130, "stun" to 1)),
                    Skill("mermaid_siren_song", "Siren Song", "🎵", "Active: เพลงไซเรน — ลด ATK+DEF ศัตรูทุกตัว 20%", 2, false, effects = mapOf("debuff_atk" to 20, "debuff_def" to 20, "duration" to 3))
                ))
            )),
            RoguelikeMainActivity.Race.GOLEM to SkillTree("Golem Traits", "Stone Fortress", listOf(
                SkillTier(1, "Stone-born", listOf(
                    Skill("golem_stone_wall", "Stone Wall", "🗿", "Passive: กำแพงหิน — DEF +20 & HP +30 ความแข็งแกร่งสูงสุด", 1, true, effects = mapOf("def_bonus" to 20, "hp_bonus" to 30))
                )),
                SkillTier(2, "Rock Fortress", listOf(
                    Skill("golem_quake_slam", "Quake Slam", "💥", "Active: กระแทกพื้น — AoE 150% + สตันทุกตัว 1 เทิร์น", 2, false, prerequisites = listOf("golem_stone_wall"), effects = mapOf("aoe_damage" to 150, "aoe_stun" to 1)),
                    Skill("golem_petrify", "Petrify", "🪨", "Active: กลายเป็นหิน — DEF +60 สูงสุด 2 เทิร์น", 2, false, effects = mapOf("def_buff" to 60, "duration" to 2))
                ))
            )),
            RoguelikeMainActivity.Race.PHOENIX to SkillTree("Phoenix Traits", "Flame Rebirth", listOf(
                SkillTier(1, "Ember", listOf(
                    Skill("phoenix_rebirth_flame", "Rebirth Flame", "🔥", "Passive: เปลวแห่งการเกิดใหม่ — ATK+Luck สูง & ฟื้นฟูได้เร็ว", 1, true, effects = mapOf("atk_bonus" to 12, "luck_bonus" to 8))
                )),
                SkillTier(2, "Blazing Phoenix", listOf(
                    Skill("phoenix_inferno_wings", "Inferno Wings", "🌋", "Active: ปีกไฟนรก — AoE 160% + ติดเผาไหม้ทุกตัว", 2, false, prerequisites = listOf("phoenix_rebirth_flame"), effects = mapOf("aoe_damage" to 160, "POISON" to 6)),
                    Skill("phoenix_solar_flare", "Solar Flare", "☀️", "Active: แสงดวงอาทิตย์ — ดาเมจ 200% ทะลุทุกสิ่ง", 2, false, effects = mapOf("damage" to 200))
                ))
            )),
            RoguelikeMainActivity.Race.WITCH to SkillTree("Witch Traits", "Hex Mastery", listOf(
                SkillTier(1, "Hex Caster", listOf(
                    Skill("witch_hex_passive", "Cursed Hex", "🧙", "Passive: คำสาปสูงสุด — Luck +12 & โอกาสลด DEF ศัตรูเพิ่ม", 1, true, effects = mapOf("luck_bonus" to 12, "crit_chance" to 8))
                )),
                SkillTier(2, "Witch", listOf(
                    Skill("witch_cauldron_blast", "Cauldron Blast", "🫧", "Active: ระเบิดหม้อต้ม — AoE 130% + ติดพิษทุกตัว", 2, false, prerequisites = listOf("witch_hex_passive"), effects = mapOf("aoe_damage" to 130, "poison" to 4)),
                    Skill("witch_polymorph", "Dark Polymorph", "🐸", "Active: สาบให้กลายร่าง — ลด ATK+DEF ศัตรู 30% 3 เทิร์น", 2, false, effects = mapOf("debuff_atk" to 30, "debuff_def" to 30, "duration" to 3))
                ))
            )),
            RoguelikeMainActivity.Race.TITAN to SkillTree("Titan Traits", "Giant's Might", listOf(
                SkillTier(1, "Colossus", listOf(
                    Skill("titan_colossus", "Colossus Frame", "🦣", "Passive: โครงร่างยักษ์ — HP +40 & ดาเมจเพิ่มตาม MaxHP", 1, true, effects = mapOf("hp_bonus" to 40, "atk_bonus" to 10))
                )),
                SkillTier(2, "Titan", listOf(
                    Skill("titan_ground_smash", "Ground Smash", "🌍", "Active: กระแทกพื้น — AoE ดาเมจ 180% จากความหนักของร่างกาย", 2, false, prerequisites = listOf("titan_colossus"), effects = mapOf("aoe_damage" to 180)),
                    Skill("titan_immovable", "Immovable Force", "⚓", "Passive: พลังที่เคลื่อนย้ายไม่ได้ — DEF +15 & HP Regen passive", 2, true, effects = mapOf("def_bonus" to 15, "regen" to 5))
                ))
            )),
            RoguelikeMainActivity.Race.SPECTER to SkillTree("Specter Traits", "Ghost Phase", listOf(
                SkillTier(1, "Wraith", listOf(
                    Skill("specter_phase_passive", "Ghostly Phase", "👻", "Passive: ร่างผี — Dodge +12% & ภูมิคุ้มกันโจมตีแรก passive", 1, true, effects = mapOf("dodge_bonus" to 12, "luck_bonus" to 10))
                )),
                SkillTier(2, "Specter", listOf(
                    Skill("specter_haunt", "Haunt", "💀", "Active: หลอกหลอน — ดาเมจ 150% + ลด ATK ศัตรู 20% 3 เทิร์น", 2, false, prerequisites = listOf("specter_phase_passive"), effects = mapOf("damage" to 150, "debuff_atk" to 20, "duration" to 3)),
                    Skill("specter_soul_drain", "Soul Drain", "🌑", "Active: ดูดดาวิญญาณ — ดาเมจ + ดูดชีวิต 35%", 2, false, effects = mapOf("damage" to 130, "lifesteal" to 35))
                ))
            )),
            RoguelikeMainActivity.Race.BEASTKIN to SkillTree("Beastkin Traits", "Hunter's Instinct", listOf(
                SkillTier(1, "Pack Hunter", listOf(
                    Skill("beastkin_predator", "Predator Instinct", "🐯", "Passive: สัญชาตญาณนักล่า — Luck +12 & EXP+Gold bonus", 1, true, effects = mapOf("luck_bonus" to 12, "gold_bonus" to 15))
                )),
                SkillTier(2, "Alpha", listOf(
                    Skill("beastkin_savage_pounce", "Savage Pounce", "🦁", "Active: โจนสวาปาม — ดาเมจ 170% + สตัน 1 เทิร์น", 2, false, prerequisites = listOf("beastkin_predator"), effects = mapOf("damage" to 170, "stun" to 1)),
                    Skill("beastkin_pack_roar", "Pack Roar", "📢", "Active: คำรามฝูง — AoE 130% + ลด ATK ศัตรูทุกตัว 15%", 2, false, effects = mapOf("aoe_damage" to 130, "debuff_atk" to 15, "duration" to 3))
                ))
            ))
        )
    }

    fun getRaceSkillTree(race: RoguelikeMainActivity.Race): SkillTree? = raceSkillTrees[race]

    fun getRaceSkillById(race: RoguelikeMainActivity.Race, skillId: String): Skill? {
        val tree = raceSkillTrees[race] ?: return null
        tree.tiers.forEach { tier ->
            tier.skills.find { it.id == skillId }?.let { return it }
        }
        return null
    }
}

// ========== DATA CLASSES ==========

enum class SkillTreeType(val emoji: String, val displayName: String) {
    COMBAT("⚔️", "การต่อสู้"),
    DEFENSE("🛡️", "การป้องกัน"),
    UTILITY("⚙️", "อรรถประโยชน์"),
    RACE("🧬", "เผ่าพันธุ์")
}

data class SkillTree(
    val name: String,
    val description: String,
    val tiers: List<SkillTier>
)

data class SkillTier(
    val tierLevel: Int,
    val name: String,
    val skills: List<Skill>
)

data class Skill(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val cost: Int,
    val isPassive: Boolean,
    val prerequisites: List<String> = emptyList(),
    val effects: Map<String, Any> = emptyMap()
)
