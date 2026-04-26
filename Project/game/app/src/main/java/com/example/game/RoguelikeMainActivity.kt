package com.example.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import com.example.game.data.model.Item
import com.example.game.data.model.ItemRarity
import com.example.game.data.model.ItemType
import kotlin.math.min
import kotlin.random.Random

/**
 * Modern Roguelike RPG - Main Activity
 * Features: Procedural dungeons, character classes, modern UI, extensive settings
 */
class RoguelikeMainActivity : AppCompatActivity() {

    // ========== GAME SETTINGS ==========
    private lateinit var prefs: SharedPreferences
    private var difficulty = "Normal" // Easy, Normal, Hard, Nightmare
    private var musicEnabled = true
    private var soundEnabled = true
    private var vibrationEnabled = true
    private var musicVolume = 0.5f
    private var autoSaveEnabled = true
    private var animationSpeed = 1.0f // 0.5x to 2.0x
    private var textSize = 16f // 12f to 20f
    private var colorTheme = "Dark" // Dark, Light, AMOLED, Cyberpunk

    // ========== CHARACTER CLASSES ==========
    enum class CharacterClass(
        val displayName: String,
        val emoji: String,
        val baseHP: Int,
        val baseATK: Int,
        val baseDEF: Int,
        val baseLuck: Int,
        val specialAbility: String,
        val description: String,
        val allowedWeaponTypes: List<String>
    ) {
        WARRIOR("นักรบ", "⚔️", 60, 10, 8, 3, "Shield Bash", "ถึกทน! มี HP และ DEF สูงสุด เหมาะกับการปะทะซึ่งหน้า", 
            listOf("sword", "axe", "mace", "spear", "shield")),
        MAGE("นักเวทย์", "🔮", 35, 15, 3, 5, "Fireball", "พลังเวทย์รุนแรง! โจมตีสูงมากและเริ่มต้นด้วย Soul Power มหาศาล", 
            listOf("staff", "grimoire", "dagger")),
        ROGUE("โจร", "🗡️", 45, 12, 4, 10, "Critical Strike", "ว่องไวและอันตราย! มีโอกาสติดคริติคอลสูงและหลบหลีกกับดักได้เก่ง", 
            listOf("dagger", "bow", "sword")),
        CLERIC("นักบวช", "✨", 50, 8, 6, 6, "Divine Heal", "ผู้ศรัทธา! สามารถฟื้นฟู HP ได้ และมีต้านทานคำสาปเบื้องต้น", 
            listOf("mace", "shield", "staff")),
        RANGER("นักล่า", "🏹", 48, 11, 5, 8, "Multi-Shot", "เฉลียวฉลาด! ตรวจจับกับดักได้แม่นยำและหาทองได้มากกว่าอาชีพอื่น", 
            listOf("bow", "spear", "dagger")),
        PALADIN("อัศวินศักดิ์สิทธิ์", "🛡️", 70, 9, 12, 4, "Holy Shield", "โล่แห่งแสง! พลังป้องกันสูงสุดและสามารถรักษาตัวเองได้", 
            listOf("sword", "shield", "mace")),
        NECROMANCER("เนโครแมนเซอร์", "💀", 40, 16, 3, 4, "Raise Dead", "จ้าวแห่งความตาย! พลังเวทย์มนต์ดำที่รุนแรงและดูดพลังชีวิตศัตรู", 
            listOf("staff", "scythe", "grimoire")),
        BERSERKER("เบอร์เซิร์กเกอร์", "🪓", 55, 18, 2, 7, "Blood Rage", "บ้าคลั่ง! พลังโจมตีมหาศาล ยิ่งเลือดน้อยยิ่งตีแรง แต่เปราะบาง", 
            listOf("axe", "sword", "mace")),
        BARD("กวี", "🎶", 42, 9, 5, 12, "Battle Song", "บทเพลงแห่งชัยชนะ! เน้นการบัฟและเพิ่มโชคลาภในการหาไอเทม", 
            listOf("dagger", "bow")),
        DRUID("ดรูอิด", "🌿", 52, 10, 7, 6, "Nature's Wrath", "สมดุลแห่งธรรมชาติ! ใช้พลังธรรมชาติในการโจมตีและฟื้นฟู", 
            listOf("staff", "spear", "grimoire")),
        SAMURAI("ซามูไร", "🏮", 46, 14, 6, 9, "Quick Draw", "คมดาบสังหาร! เน้นความรวดเร็วและแม่นยำในการโจมตีจุดตาย", 
            listOf("katana", "sword", "spear")),
        SHAMAN("หมอผี", "🎭", 44, 13, 5, 7, "Spirit Totem", "สื่อวิญญาณ! ใช้คำสาปและวิญญาณในการรบกวนศัตรู", 
            listOf("staff", "mace", "dagger"))
    }

    enum class Race(
        val displayName: String,
        val emoji: String,
        val hpBonus: Int,
        val atkBonus: Int,
        val defBonus: Int,
        val luckBonus: Int,
        val trait: String,
        val allowedWeaponTypes: List<String> = listOf("sword", "axe", "staff", "bow", "dagger", "mace", "spear", "grimoire", "scythe", "shield", "katana")
    ) {
        HUMAN("มนุษย์", "🧑", 8, 3, 3, 3, "เรียนรู้ง่าย: ได้รับ EXP เพิ่มขึ้น 10%"), 
        ELF("เอลฟ์", "🧝", 2, 4, 1, 6, "สัมผัสมานา: เริ่มต้นด้วย Soul Power +50"),
        DWARF("คนแคระ", "🧔", 20, 3, 6, -1, "เลือดเหล็ก: เพิ่มประสิทธิภาพการป้องกันจากโล่ 20%"),
        ORC("ออร์ค", "👹", 25, 7, -2, -2, "บ้าเลือด: พลังโจมตีเพิ่มขึ้นเมื่อ HP ต่ำลง"),
        DEMON("ปีศาจ", "😈", 12, 6, 2, -1, "สัญญาปีศาจ: ได้ Soul Power ทุกครั้งที่สังหารศัตรู"),
        ANGEL("เทวทูต", "😇", 10, 3, 4, 7, "พรจากสวรรค์: มีโอกาส 10% ที่จะฟื้นฟู HP เมื่อเข้าห้องใหม่"),
        DRAGONKIN("เผ่ามังกร", "🐲", 30, 8, 8, 0, "เกล็ดมังกร: ลดความเสียหายทางกายภาพที่ได้รับลง 5 หน่วย"),
        UNDEAD("อันเดด", "💀", 5, 4, 2, 8, "คืนชีพ: มีโอกาส 15% ที่จะฟื้นคืนชีพด้วย 1 HP เมื่อตาย"),
        VAMPIRE("แวมไพร์", "🧛", 15, 6, 2, 5, "กระหายเลือด: ฟื้นฟู HP 2 หน่วยทุกครั้งที่โจมตีโดนศัตรู"),
        FAIRY("แฟรี่", "🧚", -10, 3, 1, 20, "ละอองดาว: โชคดีมหาศาล! มีโอกาสพบหีบสมบัติหายากมากขึ้น"),
        CYBORG("ไซบอร์ก", "🤖", 20, 10, 5, -5, "โอเวอร์โหลด: เมื่อ HP ต่ำกว่า 30% พลังโจมตีจะเพิ่มขึ้น 2 เท่า"),
        SHADOW("เงามืด", "👥", 0, 12, -5, 10, "ร่างไร้ตัวตน: มีโอกาส 20% ที่จะหลบการโจมตีได้ทุกประเภท")
    }

    // ========== PLAYER STATS ==========
    private var nextRooms = mutableListOf<String>()
    internal var playerClass: CharacterClass = CharacterClass.WARRIOR
    internal var playerRace: Race = Race.HUMAN
    private var playerName = "Hero"
    private var level = 1
    private var exp = 0
    private var expToNext = 100
    
    internal var hp = 30
    internal var maxHp = 30
    internal var atk = 8
    internal var def = 5
    internal var luck = 3
    internal var soulPoints = 0
    internal var gold = 0
    
    internal var floor = 1
    private var roomsCompleted = 0
    private val roomsPerFloor = 5
    
    // ========== INVENTORY ==========
    internal val inventory = mutableListOf<Item>()
    internal var equippedWeapon: Item? = null
    internal var equippedArmor: Item? = null
    internal var equippedAccessory: Item? = null
    internal val maxInventorySize = 20
    
    // Skills
    private var skillPoints = 0
    private val unlockedSkills = mutableSetOf<String>()
    
    // Background Music
    private var mediaPlayer: MediaPlayer? = null
    
    // ========== GAME STATE ==========
    private var currentSeed = 0L
    private var enemiesKilled = 0
    private var bossesKilled = 0
    private var deathCount = 0
    private var totalPlayTime = 0L
    private var sessionStartTime = 0L

    // ========== ACHIEVEMENTS ==========
    data class Achievement(
        val id: String,
        val name: String,
        val description: String,
        val icon: String,
        val category: String,       // "combat" | "explore" | "wealth" | "special"
        val rewardDesc: String = "",
        val rewardGold: Int = 0,
        val rewardSP: Int = 0,
        val unlocked: Boolean = false
    )

    private val achievements = mutableListOf<Achievement>()
    private var currentAchievementFilter = "all"
    private var arenaFightActive = false

    // ========== FLOOR BIOME ==========
    data class FloorBiome(
        val name: String,
        val emoji: String,
        val description: String,
        val effects: String,
        val roomEntryDamage: Int = 0,
        val roomEntryHeal: Int = 0,
        val combatAtkBonus: Int = 0,
        val combatDefBonus: Int = 0,
        val expMultiplier: Float = 1.0f,
        val goldMultiplier: Float = 1.0f
    )

    private val currentBiome: FloorBiome get() = getBiomeForFloor(floor)

    // ========== UI ELEMENTS ==========
    internal lateinit var rootLayout: FrameLayout
    private lateinit var mainContainer: LinearLayout
    private var ivLogo: ImageView? = null
    private var ivBackground: ImageView? = null
    private var logoTapCount = 0
    private var lastTapTime: Long = 0
    
    private lateinit var tvStory: TextView
    private lateinit var storyScrollView: ScrollView
    private lateinit var storyContainer: FrameLayout
    private lateinit var glassCard: FrameLayout
    private lateinit var choiceContainer: LinearLayout
    private lateinit var headerContainer: LinearLayout
    private lateinit var tvStats: TextView
    private lateinit var tvInventoryPreview: TextView
    
    private lateinit var btnChoice1: Button
    private lateinit var btnChoice2: Button
    private lateinit var btnChoice3: Button
    private lateinit var btnChoice4: Button
    
    private lateinit var buttonRow1: LinearLayout
    private lateinit var buttonRow2: LinearLayout
    
    private lateinit var progressHP: ProgressBar
    private lateinit var progressEXP: ProgressBar
    private lateinit var progressFloor: ProgressBar

    private var isStoryModeUnlocked = false
    
    // ========== LIFECYCLE ==========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefs = getSharedPreferences("RoguelikeRPG", MODE_PRIVATE)
        loadSettings()
        isStoryModeUnlocked = prefs.getBoolean("story_mode_unlocked", false)
        
        startBackgroundMusic()
        
        createModernUI()
        
        loadAchievements()
        
        if (hasSavedGame()) {
            loadGame()
        } else {
            showMainMenu()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (autoSaveEnabled && floor > 1) {
            autoSave()
        }
        updatePlayTime()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // If we are not showing the main root layout, return to it
        val currentContent = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        if (currentContent != rootLayout) {
            setContentView(rootLayout)
            continueAdventure()
            return
        }

        // Find current context and navigate back logically
        val currentText = tvStory.text.toString()

        when {
            currentText.contains("SETTINGS") || currentText.contains("ตั้งค่า") -> {
                if (currentText.contains("การเล่น") || currentText.contains("เสียง") || currentText.contains("DEVELOPER")) {
                    showSettings()
                } else {
                    showMainMenu()
                }
            }
            currentText.contains("Character Sheet") || currentText.contains("📊 ตัวละคร") -> {
                continueAdventure()
            }
            isInventoryShowing() -> {
                showCharacterSheet()
            }
            currentText.contains("🌳 Skills") || currentText.contains("Skills Tree") -> {
                showCharacterSheet()
            }
            currentText.contains("ACHIEVEMENTS") || currentText.contains("วิธีเล่น") || ivLogo?.visibility == View.VISIBLE -> {
                AlertDialog.Builder(this)
                    .setTitle("🚪 ออกจากเกม")
                    .setMessage("คุณต้องการปิดเกมใช่หรือไม่?")
                    .setPositiveButton("ใช่") { _, _ -> finish() }
                    .setNegativeButton("ไม่", null)
                    .show()
            }
            else -> {
                // If in a room, try to return to Map or show Character Sheet
                @Suppress("DEPRECATION")
                super.onBackPressed()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        sessionStartTime = System.currentTimeMillis()
        if (musicEnabled && mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        } else if (musicEnabled && mediaPlayer == null) {
            startBackgroundMusic()
        }
    }
    
    override fun onStop() {
        super.onStop()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundMusic()
    }
    
    private var isMusicStarting = false
    private fun startBackgroundMusic() {
        if (!musicEnabled || isMusicStarting) return
        isMusicStarting = true
        
        Thread {
            try {
                if (mediaPlayer == null) {
                    val afd = try { resources.openRawResourceFd(R.raw.candle_iron) } catch (e: Exception) { null }
                    if (afd == null) {
                        isMusicStarting = false
                        return@Thread
                    }
                    
                    val mp = MediaPlayer()
                    mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    mp.setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    mp.isLooping = true
                    mp.setOnErrorListener { mpInternal, _, _ ->
                        isMusicStarting = false
                        try { mpInternal.release() } catch (e: Exception) {}
                        mediaPlayer = null
                        true
                    }
                    mp.prepare() 
                    mp.setVolume(musicVolume, musicVolume)
                    mp.start()
                    mediaPlayer = mp
                } else if (!mediaPlayer!!.isPlaying) {
                    mediaPlayer?.start()
                }
            } catch (e: Exception) {
                android.util.Log.e("AudioError", "Music Error: ${e.message}")
            } finally {
                isMusicStarting = false
            }
        }.start()
    }

    private fun stopBackgroundMusic() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (_: Exception) {}
            mediaPlayer = null
        }
    }
    
    // ========== MODERN UI CREATION ==========
    private fun createModernUI() {
        rootLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
        }

        // Main Background (Centered with Overlay effect)
        ivBackground = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 0.5f
            // โหลดภาพแบบดีเลย์มากขึ้นและเช็คไม่ให้โหลดซ้ำ
            postDelayed({
                try {
                    if (ivBackground?.drawable == null) {
                        setImageResource(R.drawable.background)
                    }
                } catch (e: Exception) {
                    setBackgroundColor(Color.parseColor("#0A0A0A"))
                }
            }, 500)
        }
        rootLayout.addView(ivBackground, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // Content Container
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(0, 0, 0, 0)
        }

        // Add Animated Logo
        ivLogo = ImageView(this).apply {
            setImageResource(R.drawable.logo_main)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                dp(180)
            ).apply {
                setMargins(dp(20), dp(20), dp(20), 0)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener { handleLogoClick() }
            visibility = View.GONE
        }
        mainContainer.addView(ivLogo)
        
        // Ensure UI components are created in correct order and initialized
        createHeader()
        createStoryArea()
        
        rootLayout.addView(mainContainer)
        setContentView(rootLayout)
    }

    private fun handleLogoClick() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < 500) {
            logoTapCount++
        } else {
            logoTapCount = 1
        }
        lastTapTime = currentTime

        ivLogo?.let { view ->
            // Shake effect
            val intensity = (logoTapCount * 5f).coerceAtMost(60f)
            val shake = ObjectAnimator.ofFloat(view, "translationX", 0f, intensity, -intensity, intensity, 0f)
            shake.duration = 100
            shake.start()

            // Visual "Breaking" effect - tilting more as it gets hit
            view.rotation = (logoTapCount * 2f) * (if (logoTapCount % 2 == 0) 1f else -1f)

            // Unlock condition: 15 rapid taps
            if (logoTapCount >= 15 && !isStoryModeUnlocked) {
                isStoryModeUnlocked = true
                prefs.edit().putBoolean("story_mode_unlocked", true).apply()
                triggerLogoFallAnimation(view)
                showMainMenu() // Refresh menu to show Story Mode button
            }
        }
    }

    private fun triggerLogoFallAnimation(view: View) {
        // 1. Logo falls down and fades out
        val fall = ObjectAnimator.ofFloat(view, "translationY", 0f, 1000f)
        val rotate = ObjectAnimator.ofFloat(view, "rotation", view.rotation, 45f)
        val fade = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        
        AnimatorSet().apply {
            playTogether(fall, rotate, fade)
            duration = 1000
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    showStoryModeButton()
                }
            })
            start()
        }

        // 2. Play a "break" sound if available
        if (soundEnabled) vibrate()
    }

    private fun showStoryModeButton() {
        // Create an image-based button using the new story.png asset
        val storyBtn = createImageButton(R.drawable.story) {
            startStoryMode()
        }
        
        val lp = LinearLayout.LayoutParams(
            dp(400), // Match the size of other menu buttons
            dp(170)
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setMargins(0, dp(40), 0, dp(-20)) // Increased top margin, kept tight bottom
        }
        
        // Add to mainContainer at the top (where logo was)
        mainContainer.addView(storyBtn, 0, lp)
        
        // Animation to pop up the new button
        storyBtn.scaleX = 0f
        storyBtn.scaleY = 0f
        storyBtn.animate()
            .scaleX(1.1f).scaleY(1.1f)
            .setDuration(400)
            .withEndAction {
                storyBtn.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            .start()
    }

    private fun startStoryMode() {
        headerContainer.visibility = View.GONE
        ivLogo?.visibility = View.GONE
        
        val unlockedCount = prefs.getInt("lore_unlocked_count", 1).coerceAtLeast(1)
        
        val fullLore = StringBuilder()
        fullLore.append("""
╔══════════════════════════════╗
║        📖 ตำนานแห่งหอคอยทมิฬ       ║
╚══════════════════════════════╝

กาลครั้งหนึ่งนานมาแล้ว... ก่อนที่ยุคสมัยแห่งอาณาจักรจะรุ่งเรือง โลกนี้เคยถูกปกครองด้วย "พลังบรรพกาล" ที่ไม่มีใครรู้จัก หอคอยแห่งนี้ไม่ได้ถูกสร้างขึ้นโดยมนุษย์ แต่มันงอกเงยออกมาจากหัวใจของผืนดินที่เจ็บปวด

📜 บันทึกที่ 1: การล่มสลายของแสง
"ข้าได้เห็นท้องฟ้าเปลี่ยนเป็นสีเลือด และดวงอาทิตย์ก็ไม่เคยขึ้นมาอีกเลย..." นักบวชผู้ถูกลืมเขียนไว้ในซากปรักหักพัง 
เหล่าเทพเจ้าไม่ได้ทอดทิ้งพวกเรา แต่พวกเขา "พ่ายแพ้" ให้กับสิ่งที่อยู่ใต้หอคอยนี้ สิ่งที่ข้าเรียกว่า 'ความว่างเปล่า' (The Abyss)
        """.trimIndent())

        if (unlockedCount >= 2) {
            fullLore.append("\n\n")
            fullLore.append("""
💀 บันทึกที่ 2: ความลับของวิญญาณ
ทำไมพวกเราถึงต้องเก็บสะสม Soul Points? แท้จริงแล้วมันไม่ใช่พลังงาน... แต่มันคือ "เศษเสี้ยวความทรงจำ" ของผู้ที่ล่วงลับไป 
ยิ่งเจ้ามี Soul มากเท่าไหร่ เจ้าก็จะยิ่งเข้าใกล้ความจริงของหอคอยนี้มากขึ้นเท่านั้น แต่ระวังไว้เถิด... เมื่อเจ้าจำทุกอย่างได้ เจ้าอาจจะไม่เหลือความเป็นมนุษย์อีกต่อไป
            """.trimIndent())
        }

        if (unlockedCount >= 3) {
            fullLore.append("\n\n")
            fullLore.append("""
🛡️ บันทึกที่ 3: หอคอยคือสิ่งมีชีวิต
เจ้ารู้สึกไหม? ทุกครั้งที่เจ้าลงไปในชั้นที่ลึกขึ้น ผนังหอคอยจะเต้นตุบๆ เหมือนหัวใจ ทุกๆ ห้องที่เจ้าผ่านไปคืออวัยวะ และศัตรูที่เจ้าฆ่าคือภูมิคุ้มกันของมัน 

เจ้าไม่ได้กำลังพิชิตหอคอย... แต่เจ้ากำลังถูกมัน "ย่อย" อย่างช้าๆ...
            """.trimIndent())
        }
        
        if (unlockedCount >= 4) {
            fullLore.append("\n\n")
            fullLore.append("""
✨ บันทึกที่ 4: ชุดเกราะแห่งนิรันดร์ (Eternal)
ว่ากันว่าชุดเกราะเหล่านี้ไม่ได้ทำจากเหล็ก แต่ทำจาก "เวลา" ที่หยุดนิ่ง ผู้ที่สวมใส่มันจะไม่แก่เฒ่าและไม่มีวันตาย... จนกว่าหอคอยจะตัดสินใจเรียกเก็บ 'ค่าเช่า' ด้วยวิญญาณทั้งหมดที่เจ้าสะสมมา
            """.trimIndent())
        }
        
        if (unlockedCount >= 5) {
            fullLore.append("\n\n")
            fullLore.append("""
🔥 บันทึกที่ 5: คำสาปบรรพกาล (Ancient Curse)
ไอเทมที่เจ้าถืออยู่นั้น เคยเป็นของเหล่าวีรบุรุษที่พยายามท้าทายหอคอยก่อนหน้าเจ้า ทุกคนต่างคิดว่าตนเองคือ "ผู้ถูกเลือก" แต่สุดท้ายพวกเขาก็กลายเป็นเพียงส่วนหนึ่งของคลังสมบัติที่ไร้จุดจบนี้
            """.trimIndent())
        }

        if (unlockedCount < 5) {
            fullLore.append("\n\n(เนื้อเรื่องจะปลดล็อกเพิ่มเมื่อพบไอเทมระดับตำนานชิ้นถัดไป)")
        } else {
            fullLore.append("\n\n(ตำนานบทหลักถูกเปิดเผยจนสิ้นสุดแล้ว... หรือนี่ยังเป็นเพียงจุดเริ่มต้น?)")
        }

        tvStory.gravity = Gravity.START
        tvStory.text = fullLore.toString()
        
        setChoices(
            "🔙 ย้อนกลับ",
            "",
            "",
            "",
            { showMainMenu() },
            {}, {}, {}
        )
    }

    private fun checkStoryUnlock(item: Item) {
        val loreKeywords = listOf("Ancient", "Cursed", "Eternal", "โบราณ", "ต้องสาป", "นิรันดร์")
        val hasKeyword = loreKeywords.any { item.name.contains(it) || item.description.contains(it) }
        
        if (hasKeyword) {
            val unlockedCount = prefs.getInt("lore_unlocked_count", 0)
            val newCount = unlockedCount + 1
            prefs.edit().putInt("lore_unlocked_count", newCount).apply()

            if (!isStoryModeUnlocked) {
                isStoryModeUnlocked = true
                prefs.edit().putBoolean("story_mode_unlocked", true).apply()
                
                AlertDialog.Builder(this)
                    .setTitle("📖 ปลดล็อกเนื้อเรื่อง!")
                    .setMessage("คุณได้พบไอเทมที่มีพลังวิญญาณรุนแรง... 'โหมดเนื้อเรื่อง' ถูกปลดล็อกแล้วในเมนูหลัก")
                    .setPositiveButton("รับทราบ", null)
                    .show()
            } else {
                // Show a small toast or notification that more lore is available
                Toast.makeText(this, "🖋️ บันทึกตำนานบทใหม่ถูกเพิ่มเข้าไปแล้ว...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createHeader() {
        headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
            visibility = View.GONE
        }

        tvStats = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
            typeface = Typeface.DEFAULT_BOLD
            visibility = View.GONE
        }
        headerContainer.addView(tvStats)

        val progressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            weightSum = 3f
            visibility = View.GONE
        }

        progressHP = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(12), 1f).apply { setMargins(dp(4), 0, dp(4), 0) }
            progressDrawable = createProgressDrawable(Color.parseColor("#444444"), Color.parseColor("#121212")) // Dark Gray
        }
        
        progressEXP = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(12), 1f).apply { setMargins(dp(4), 0, dp(4), 0) }
            progressDrawable = createProgressDrawable(Color.parseColor("#333333"), Color.parseColor("#0A0A0A")) // Even Darker Gray
        }

        progressFloor = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(12), 1f).apply { setMargins(dp(4), 0, dp(4), 0) }
            progressDrawable = createProgressDrawable(Color.parseColor("#222222"), Color.parseColor("#050505")) // Blackish Gray
            max = 50
        }

        progressContainer.addView(progressHP)
        progressContainer.addView(progressEXP)
        progressContainer.addView(progressFloor)
        headerContainer.addView(progressContainer)

        tvInventoryPreview = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#AAAAAA"))
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(8))
            visibility = View.GONE
        }
        headerContainer.addView(tvInventoryPreview)

        mainContainer.addView(headerContainer, 1)
    }

    private fun createProgressDrawable(foreground: Int, background: Int): GradientDrawable {
        val shape = GradientDrawable()
        shape.cornerRadius = dp(6).toFloat()
        shape.setColor(foreground)
        return shape 
    }
    
    private fun createStoryArea() {
        storyContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                // Increased top margin from dp(10) to dp(60) to provide space from the top
                setMargins(dp(16), dp(60), dp(16), dp(10))
            }
        }

        glassCard = ModernUI.createModernCard(this, ModernUI.ColorPalette.gradientDark, 12f, 0.4f)
        
        val innerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        storyScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            isVerticalScrollBarEnabled = false
        }
        
        tvStory = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setLineSpacing(0f, 1.4f)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        
        storyScrollView.addView(tvStory)
        innerLayout.addView(storyScrollView)

        choiceContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        buttonRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        buttonRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        choiceContainer.addView(buttonRow1)
        choiceContainer.addView(buttonRow2)
        innerLayout.addView(choiceContainer)
        
        glassCard.addView(innerLayout)
        storyContainer.addView(glassCard)
        mainContainer.addView(storyContainer)

        // Initialize choice buttons to avoid UninitializedPropertyAccessException
        btnChoice1 = Button(this)
        btnChoice2 = Button(this)
        btnChoice3 = Button(this)
        btnChoice4 = Button(this)
    }
    
    // ========== UI HELPERS ==========
    internal fun createCard(): CardView {
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
    
    internal fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
    
    internal fun getThemeColor(type: String): Int {
        return when (colorTheme) {
            "Dark" -> when (type) {
                "background" -> "#0a0e27".toColorInt()
                "card" -> "#1a1a2e".toColorInt()
                "primary" -> "#16213e".toColorInt()
                "accent" -> "#0f3460".toColorInt()
                "text" -> "#ecf0f1".toColorInt()
                "textSecondary" -> "#bdc3c7".toColorInt()
                else -> Color.GRAY
            }
            "Light" -> when (type) {
                "background" -> "#f5f6fa".toColorInt()
                "card" -> Color.WHITE
                "primary" -> "#3498db".toColorInt()
                "accent" -> "#2ecc71".toColorInt()
                "text" -> "#2c3e50".toColorInt()
                "textSecondary" -> "#7f8c8d".toColorInt()
                else -> Color.GRAY
            }
            "AMOLED" -> when (type) {
                "background" -> Color.BLACK
                "card" -> "#0d0d0d".toColorInt()
                "primary" -> "#1a1a1a".toColorInt()
                "accent" -> "#00bcd4".toColorInt()
                "text" -> Color.WHITE
                "textSecondary" -> "#9e9e9e".toColorInt()
                else -> Color.GRAY
            }
            "Cyberpunk" -> when (type) {
                "background" -> "#0a0e27".toColorInt()
                "card" -> "#1a1a2e".toColorInt()
                "primary" -> "#ff006e".toColorInt()
                "accent" -> "#00f5ff".toColorInt()
                "text" -> "#00f5ff".toColorInt()
                "textSecondary" -> "#ff006e".toColorInt()
                else -> Color.GRAY
            }
            else -> Color.GRAY
        }
    }
    
    private fun vibrate() {
        // Implement vibration if needed
    }
    
    fun updateStats() {
        if (isFinishing) return

        // Calculate equipment bonuses
        var bonusHp = 0
        var bonusAtk = 0
        var bonusDef = 0
        var bonusLuck = 0
        var bonusSoul = 0
        
        listOf(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
            if (item != null) {
                bonusHp += item.getStat("hp")
                bonusAtk += item.getStat("atk")
                bonusDef += item.getStat("def")
                bonusLuck += item.getStat("luck")
                bonusSoul += item.getStat("soul")
            }
        }

        // Calculate skill bonuses
        val skillBonusAtk = calculateSkillBonus("atk_bonus")
        val skillBonusDef = calculateSkillBonus("def_bonus")
        val skillBonusHp = calculateSkillBonus("hp_bonus") + calculateSkillBonus("max_hp")
        val skillBonusLuck = calculateSkillBonus("luck_bonus")
        
        val totalBonusHp = bonusHp + skillBonusHp
        val totalBonusAtk = bonusAtk + skillBonusAtk
        val totalBonusDef = bonusDef + skillBonusDef
        val totalBonusLuck = bonusLuck + skillBonusLuck

        val displayMaxHp = maxHp + totalBonusHp
        val displayAtk = atk + totalBonusAtk
        val displayDef = def + totalBonusDef
        val displayLuck = luck + totalBonusLuck

        // Cleaned up statsText - although it's mostly hidden now, we keep it consistent
        val statsText = buildString {
            append("Lv.$level $playerName | ")
            append("HP $hp/$displayMaxHp ($maxHp ${if(totalBonusHp >= 0) "+" else ""}$totalBonusHp) | ")
            append("ATK $displayAtk ($atk ${if(totalBonusAtk >= 0) "+" else ""}$totalBonusAtk) | ")
            append("DEF $displayDef ($def ${if(totalBonusDef >= 0) "+" else ""}$totalBonusDef) | ")
            append("LUCK $displayLuck ($luck ${if(totalBonusLuck >= 0) "+" else ""}$totalBonusLuck)")
        }
        
        if (::tvStats.isInitialized) {
            tvStats.text = statsText
        }
        
        if (::progressHP.isInitialized) {
            progressHP.max = displayMaxHp
            progressHP.progress = hp
        }
        if (::progressEXP.isInitialized) {
            progressEXP.max = expToNext
            progressEXP.progress = exp
        }
        if (::progressFloor.isInitialized) {
            progressFloor.progress = floor
        }
        
        val invText = "Soul: $soulPoints | Gold: $gold | Inventory: ${inventory.size}/$maxInventorySize | Floor: $floor"
        if (::tvInventoryPreview.isInitialized) {
            tvInventoryPreview.text = invText
        }
        
        // Update Progress Bars
        if (::progressHP.isInitialized) {
            progressHP.max = displayMaxHp
            progressHP.progress = hp.coerceIn(0, displayMaxHp)
        }
        
        if (::progressEXP.isInitialized) {
            progressEXP.max = expToNext
            progressEXP.progress = exp.coerceIn(0, expToNext)
        }
        
        if (hp <= 0) {
            gameOver()
        }
    }

    internal fun calculateSkillBonus(effectKey: String): Int {
        var total = 0
        unlockedSkills.forEach { skillId ->
            val skill = SkillTreeData.getAnySkillById(playerClass, playerRace, skillId)
            if (skill != null) {
                val value = skill.effects[effectKey]
                if (value is Int) total += value
            }
        }
        return total
    }
    
    private fun updateInventoryPreview() {
        val items = mutableListOf<String>()
        // No emojis
        
        tvInventoryPreview.text = buildString {
            append("Inventory: ${inventory.size}/$maxInventorySize")
            if (items.isNotEmpty()) {
                append(" | ")
                append(items.joinToString(" "))
            }
        }
    }
    
    // ========== MAIN MENU ==========
    private fun showMainMenu() {
        floor = 1 // Reset floor state when returning to main menu
        roomsCompleted = 0
        if (::tvStory.isInitialized) tvStory.text = "" 
        if (::headerContainer.isInitialized) headerContainer.visibility = View.GONE
        ivLogo?.visibility = View.VISIBLE
        ivLogo?.alpha = 1.0f
        
        // Ensure mainContainer occupies full screen for centering
        (mainContainer.layoutParams as? FrameLayout.LayoutParams)?.height = ViewGroup.LayoutParams.MATCH_PARENT
        
        if (hasSavedGame()) {
            setChoices(
                "เริ่มเกมใหม่",
                "เล่นต่อ",
                "ตั้งค่า",
                "วิธีเล่น",
                { confirmNewGame() },
                { loadGame() },
                { showSettings() },
                { showHowToPlay() }
            )
        } else {
            setChoices(
                "เริ่มเกมใหม่",
                "ตั้งค่า",
                "Achievements",
                "วิธีเล่น",
                { showClassSelection() },
                { showSettings() },
                { showAchievements() },
                { showHowToPlay() }
            )
        }
    }
    
    private fun showWelcomeWithContinue() {
        showMainMenu()
    }
    
    // ========== CLASS SELECTION ==========
    private var selectedClassIndex = 0
    private var selectedRaceIndex = 0
    private var selectedWeaponIndex = 0

    private fun showClassSelection() {
        updateStats() // Ensure stats are updated before showing
        headerContainer.visibility = View.GONE // Keep header hidden during selection
        val classes = CharacterClass.entries
        val charClass = classes[selectedClassIndex]
        
        tvStory.text = """
╔═══════════════════════════════════╗
   ขั้นตอนที่ 1: เลือกอาชีพ (Class)
╚═══════════════════════════════════╝

${charClass.displayName}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
HP: ${charClass.baseHP} | ATK: ${charClass.baseATK}
DEF: ${charClass.baseDEF} | LUCK: ${charClass.baseLuck}

🎯 Skill: ${charClass.specialAbility}
📖 ${charClass.description}

[ เลือกอาชีพเพื่อไปยังขั้นตอนถัดไป ]
        """.trimIndent()
        
        setChoices(
            "ก่อนหน้า",
            "ถัดไป",
            "เลือกอาชีพนี้",
            "เมนูหลัก",
            { 
                selectedClassIndex = (selectedClassIndex - 1 + classes.size) % classes.size
                showClassSelection()
            },
            { 
                selectedClassIndex = (selectedClassIndex + 1) % classes.size
                showClassSelection()
            },
            { 
                playerClass = CharacterClass.entries[selectedClassIndex]
                showRaceSelection() 
            },
            { showMainMenu() }
        )
    }

    private fun showRaceSelection() {
        headerContainer.visibility = View.GONE
        val races = Race.entries
        val race = races[selectedRaceIndex]
        
        tvStory.text = """
╔═══════════════════════════════════╗
   ขั้นตอนที่ 2: เลือกเผ่าพันธุ์ (Race)
╚═══════════════════════════════════╝

เผ่า${race.displayName}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
➕ โบนัสค่าสถานะ:
HP: +${race.hpBonus} | ATK: +${race.atkBonus}
DEF: +${race.defBonus} | LUCK: +${race.luckBonus}

✨ จุดเด่น: ${race.trait}

[ เลือกเผ่าพันธุ์เพื่อไปยังขั้นตอนถัดไป ]
        """.trimIndent()

        setChoices(
            "ก่อนหน้า",
            "ถัดไป",
            "เลือกเผ่านี",
            "ย้อนกลับ",
            {
                selectedRaceIndex = (selectedRaceIndex - 1 + races.size) % races.size
                showRaceSelection()
            },
            {
                selectedRaceIndex = (selectedRaceIndex + 1) % races.size
                showRaceSelection()
            },
            { 
                playerRace = Race.values()[selectedRaceIndex]
                showWeaponSelection() 
            },
            { showClassSelection() }
        )
    }

    private fun showWeaponSelection() {
        headerContainer.visibility = View.GONE
        val allWeapons = listOf(
            Item("w_katana", "คาตานะเก่า", "🏮", "ดาบคาตานะสะท้อนแสงจันทร์", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 7, "luck" to 3), weaponType = "katana"),
            Item("w_sword", "ดาบเหล็ก", "⚔️", "ดาบมาตรฐาน สมดุลทุกด้าน", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 5), weaponType = "sword"),
            Item("w_axe", "ขวานศึก", "🪓", "โจมตีหนักแต่ช้า", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 8), weaponType = "axe"),
            Item("w_staff", "ไม้เท้าเวทย์", "🧙", "เพิ่มพลังวิญญาณ", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 3, "soul" to 20), weaponType = "staff"),
            Item("w_bow", "ธนูไม้", "🏹", "โจมตีไกลและแม่นยำ", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 4, "luck" to 2), weaponType = "bow"),
            Item("w_dagger", "มีดสั้น", "🗡️", "เน้นคริติคอล", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 2, "luck" to 5), weaponType = "dagger"),
            Item("w_mace", "กระบองหนาม", "🔨", "ทำลายเกราะ", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 6, "def" to 2), weaponType = "mace"),
            Item("w_spear", "หอกยาว", "🔱", "เพิ่มระยะโจมตี", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 5, "def" to 1), weaponType = "spear"),
            Item("w_grimoire", "ตำรามนตรา", "📖", "เน้นพลังเวทย์ล้วน", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 1, "soul" to 40), weaponType = "grimoire"),
            Item("w_scythe", "เคียวมรณะ", "☠️", "พลังทำลายวิญญาณ", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("atk" to 10, "luck" to -2), weaponType = "scythe"),
            Item("w_shield", "โล่ศักดิ์สิทธิ์", "🛡️", "เน้นป้องกันสูงสุด", ItemType.WEAPON, rarity = ItemRarity.COMMON, stats = mapOf("def" to 10, "atk" to 1), weaponType = "shield")
        )
        
        // Filter weapons based on Class and Race
        val weapons = allWeapons.filter { 
            (playerClass.allowedWeaponTypes.contains(it.weaponType)) && 
            (playerRace.allowedWeaponTypes.contains(it.weaponType))
        }

        if (weapons.isEmpty()) {
            // Fallback if no weapons match (shouldn't happen with Human, but for safety)
            Toast.makeText(this, "ไม่พบอาวุธที่เหมาะกับคุณ! สุ่มอาวุธพื้นฐานให้", Toast.LENGTH_SHORT).show()
            finalizeCharacter(allWeapons[0])
            return
        }

        val weapon = if (selectedWeaponIndex < weapons.size) weapons[selectedWeaponIndex] else weapons[0]

        tvStory.text = """
╔═══════════════════════════════════╗
   ขั้นตอนที่ 3: เลือกอาวุธเริ่มต้น
╚═══════════════════════════════════╝

${weapon.name}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 คุณสมบัติ:
${weapon.stats.entries.joinToString("\n") { "• ${it.key.uppercase()}: +${it.value}" }}

📖 ${weapon.description}

[ เลือกอาวุธเพื่อเริ่มการเดินทาง ]
        """.trimIndent()

        setChoices(
            "ก่อนหน้า",
            "ถัดไป",
            "เลือกอาวุธนี้",
            "ย้อนกลับ",
            {
                selectedWeaponIndex = (selectedWeaponIndex - 1 + weapons.size) % weapons.size
                showWeaponSelection()
            },
            {
                selectedWeaponIndex = (selectedWeaponIndex + 1) % weapons.size
                showWeaponSelection()
            },
            { 
                playerRace = Race.entries[selectedRaceIndex]
                val selectedWeapon = weapons[selectedWeaponIndex]
                finalizeCharacter(selectedWeapon) 
            },
            { showRaceSelection() }
        )
    }

    private fun finalizeCharacter(startingWeapon: Item) {
        val charClass = CharacterClass.entries[selectedClassIndex]
        playerClass = charClass
        
        // Calculate final stats
        maxHp = charClass.baseHP + playerRace.hpBonus
        hp = maxHp
        atk = charClass.baseATK + playerRace.atkBonus
        def = charClass.baseDEF + playerRace.defBonus
        luck = charClass.baseLuck + playerRace.luckBonus
        
        // Clear inventory and equip starting weapon
        inventory.clear()
        equippedWeapon = startingWeapon
        equippedArmor = null
        equippedAccessory = null
        
        // Ensure starting weapon is NOT in inventory
        inventory.removeIf { it.id == startingWeapon.id }
        
        // Race Unique Traits Implementation
        when (playerRace) {
            Race.ELF -> soulPoints += 50
            Race.VAMPIRE -> {} // Logic in CombatManager/Lifesteal
            Race.HUMAN -> {} // 10% EXP boost in checkLevelUp
            else -> {}
        }
        
        // Class starting bonuses
        when (charClass) {
            CharacterClass.MAGE -> soulPoints += 100
            CharacterClass.CLERIC -> {
                soulPoints += 50
                maxHp += 10
            }
            CharacterClass.RANGER -> gold += 150
            CharacterClass.WARRIOR -> {
                maxHp += 20
                def += 5
            }
            CharacterClass.ROGUE -> luck += 15
            CharacterClass.PALADIN -> {
                maxHp += 30
                def += 10
            }
            CharacterClass.NECROMANCER -> soulPoints += 150
            CharacterClass.BERSERKER -> atk += 10
            CharacterClass.BARD -> {
                luck += 10
                gold += 100
            }
            CharacterClass.DRUID -> {
                maxHp += 15
                soulPoints += 40
            }
            CharacterClass.SAMURAI -> {
                atk += 5
                luck += 5
            }
            CharacterClass.SHAMAN -> soulPoints += 80
        }
        hp = maxHp

        // No longer applying weapon effects directly to base stats to avoid double counting
        // Weapon stats will be calculated in updateStats and Combat initialization

        // Add to inventory and equip
        inventory.add(startingWeapon)
        equippedWeapon = startingWeapon
        
        // Add initial Tier 1 skill based on class
        when (charClass) {
            CharacterClass.WARRIOR -> unlockedSkills.add("warrior_cleave")
            CharacterClass.MAGE -> unlockedSkills.add("mage_fireball")
            CharacterClass.ROGUE -> unlockedSkills.add("rogue_backstab")
            CharacterClass.CLERIC -> unlockedSkills.add("cleric_heal")
            CharacterClass.RANGER -> unlockedSkills.add("ranger_double_shot")
            CharacterClass.PALADIN -> unlockedSkills.add("paladin_smite")
            CharacterClass.NECROMANCER -> unlockedSkills.add("necro_drain")
            CharacterClass.BERSERKER -> unlockedSkills.add("berserker_slash")
            CharacterClass.BARD -> unlockedSkills.add("bard_song")
            CharacterClass.DRUID -> unlockedSkills.add("druid_vine")
            CharacterClass.SAMURAI -> unlockedSkills.add("samurai_slash")
            CharacterClass.SHAMAN -> unlockedSkills.add("shaman_bolt")
        }
        
        showNameInput()
    }
    
    private fun showNameInput() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(24))
            setBackgroundColor("#1A1A2E".toColorInt())
        }

        val title = TextView(this).apply {
            text = "📝 ตั้งชื่อตัวละคร"
            textSize = 22f
            setTextColor(ModernUI.ColorPalette.neonGold)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
        }

        val inputFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(60)
            )
            background = GradientDrawable().apply {
                setColor("#33FFFFFF".toColorInt())
                cornerRadius = dp(12).toFloat()
                setStroke(2, ModernUI.ColorPalette.neonBlue)
            }
            setPadding(dp(16), 0, dp(16), 0)
        }

        val input = EditText(this).apply {
            hint = "ชื่อนักผจญภัย..."
            setHintTextColor(Color.LTGRAY)
            setText(playerName)
            setTextColor(Color.WHITE)
            textSize = 18f
            background = null
            gravity = Gravity.CENTER_VERTICAL
            isSingleLine = true
        }
        inputFrame.addView(input)

        container.addView(title)
        container.addView(inputFrame)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(20), 0, 0)
            weightSum = 2f
        }

        val btnCancel = ModernUI.createNeonButton(this, "ยกเลิก", Color.RED) {
            dialog.dismiss()
            showClassSelection()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(50), 1f).apply {
                marginEnd = dp(8)
            }
        }

        val btnStart = ModernUI.createNeonButton(this, "เริ่มผจญภัย", ModernUI.ColorPalette.neonGreen) {
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                playerName = name
                dialog.dismiss()
                startNewGame()
            } else {
                input.error = "กรุณาใส่ชื่อ"
            }
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(50), 1f).apply {
                marginStart = dp(8)
            }
        }

        buttonLayout.addView(btnCancel)
        buttonLayout.addView(btnStart)
        container.addView(buttonLayout)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
    
    // ========== SETTINGS ==========
    private fun showSettings() {
        headerContainer.visibility = View.GONE
        ivLogo?.visibility = View.GONE
        
        tvStory.text = """
╔═══════════════════════════════════╗
             ⚙️ ตั้งค่า (Settings)
╚═══════════════════════════════════╝

🎮 ระบบการเล่น
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🏆 ระดับความยาก: $difficulty
💾 บันทึกอัตโนมัติ: ${if (autoSaveEnabled) "เปิด" else "ปิด"}

🔊 ระบบเสียง
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎵 ระดับเสียง: ${(musicVolume * 100).toInt()}%
🔊 เสียงเอฟเฟกต์: ${if (soundEnabled) "เปิด" else "ปิด"}
        """.trimIndent()
        
        tvStory.gravity = Gravity.CENTER
        
        setChoices(
            "🎮 ตั้งค่าเกม",
            "🔊 ตั้งค่าเสียง",
            "🛠️ เมนูผู้พัฒนา",
            "✅ บันทึกและกลับ",
            { showGameplaySettings() },
            { showAudioSettings() },
            { showDevMenu() },
            {
                saveSettings()
                showMainMenu()
            }
        )
    }

    private fun showDevMenu() {
        tvStory.text = """
╔══════════════════════════════╗
║      🛠️ DEVELOPER MENU       ║
╚══════════════════════════════╝

ใช้สำหรับทดสอบระบบเท่านั้น

💰 Gold: $gold
💎 Skill Points: $skillPoints
✨ Soul Points: $soulPoints
🏰 Floor: $floor
📖 Story Unlocked: $isStoryModeUnlocked
        """.trimIndent()
        
        setChoices(
            "💰 +1000 Gold",
            "💎 +5 Skill Pts",
            "📖 Unlock Story",
            "⏮️ Back",
            { 
                gold += 1000
                Toast.makeText(this, "Added 1000 Gold", Toast.LENGTH_SHORT).show()
                showDevMenu()
            },
            { 
                skillPoints += 5
                Toast.makeText(this, "Added 5 Skill Points", Toast.LENGTH_SHORT).show()
                showDevMenu()
            },
            { 
                isStoryModeUnlocked = true
                prefs.edit().putBoolean("story_mode_unlocked", true).apply()
                Toast.makeText(this, "Story Mode Unlocked", Toast.LENGTH_SHORT).show()
                showDevMenu()
            },
            { showSettings() }
        )
    }

    private fun showGameplaySettings() {
        headerContainer.visibility = View.GONE
        ivLogo?.visibility = View.GONE
        
        tvStory.text = """
╔═══════════════════════════════════╗
            🎮 ตั้งค่าการเล่น
╚═══════════════════════════════════╝

ระดับความยากปัจจุบัน: $difficulty
การบันทึกอัตโนมัติ: ${if (autoSaveEnabled) "เปิด" else "ปิด"}

[ เลือกเพื่อเปลี่ยนการตั้งค่า ]
        """.trimIndent()
        
        tvStory.gravity = Gravity.CENTER
        
        setChoices(
            "🏆 ความยาก: $difficulty",
            "💾 บันทึกอัตโนมัติ: ${if (autoSaveEnabled) "ON" else "OFF"}",
            "⏮️ ย้อนกลับ",
            "",
            {
                val difficulties = arrayOf("Easy", "Normal", "Hard", "Nightmare")
                val currentIndex = difficulties.indexOf(difficulty)
                difficulty = difficulties[(currentIndex + 1) % difficulties.size]
                showGameplaySettings()
            },
            {
                autoSaveEnabled = !autoSaveEnabled
                showGameplaySettings()
            },
            { showSettings() },
            {}
        )
    }

    private fun showAudioSettings() {
        headerContainer.visibility = View.GONE
        ivLogo?.visibility = View.GONE
        
        tvStory.text = """
╔═══════════════════════════════════╗
            🔊 ตั้งค่าเสียง
╚═══════════════════════════════════╝

ปรับระดับเสียงและระบบสั่น
        """.trimIndent()
        tvStory.gravity = Gravity.CENTER
        
        choiceContainer.removeAllViews()
        
        // Volume Slider
        val musicLabel = TextView(this).apply {
            text = "🎵 ระดับเสียงเพลง: ${(musicVolume * 100).toInt()}%"
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(15), 0, dp(5))
        }
        
        val musicSeekBar = SeekBar(this).apply {
            max = 100
            progress = (musicVolume * 100).toInt()
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(40)).apply {
                setMargins(dp(40), 0, dp(40), dp(20))
            }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    musicVolume = progress / 100f
                    musicLabel.text = "🎵 ระดับเสียงเพลง: $progress%"
                    mediaPlayer?.setVolume(musicVolume, musicVolume)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        
        // Fix for "Hissing" sound: Restart music if it was problematic
        val btnFixSound = ModernUI.createNeonButton(this, "🔧 รีเซ็ตระบบเสียง", ModernUI.ColorPalette.neonGold) {
            stopBackgroundMusic()
            startBackgroundMusic()
            Toast.makeText(this, "🔄 รีเซ็ตระบบเสียงแล้ว", Toast.LENGTH_SHORT).show()
        }
        
        // Toggle Buttons
        val btnSound = ModernUI.createNeonButton(this, "🔊 เสียง: ${if (soundEnabled) "ON" else "OFF"}", ModernUI.ColorPalette.neonBlue) {
            soundEnabled = !soundEnabled
            showAudioSettings()
        }
        
        val btnVibrate = ModernUI.createNeonButton(this, "📳 สั่น: ${if (vibrationEnabled) "ON" else "OFF"}", ModernUI.ColorPalette.neonPink) {
            vibrationEnabled = !vibrationEnabled
            showAudioSettings()
        }
        
        val btnBack = ModernUI.createNeonButton(this, "⏮️ ย้อนกลับ", ModernUI.ColorPalette.neonPurple) {
            showSettings()
        }
        
        choiceContainer.addView(musicLabel)
        choiceContainer.addView(musicSeekBar)
        choiceContainer.addView(btnFixSound, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(55)).apply { setMargins(dp(20), 0, dp(20), dp(10)) })
        
        val row1 = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        row1.addView(btnSound, LinearLayout.LayoutParams(0, dp(55), 1f))
        row1.addView(btnVibrate, LinearLayout.LayoutParams(0, dp(55), 1f))
        
        choiceContainer.addView(row1)
        choiceContainer.addView(btnBack, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(55)))
    }
    
    // ========== SAVE/LOAD ==========
    private fun saveSettings() {
        prefs.edit().apply {
            putString("difficulty", difficulty)
            putBoolean("music", musicEnabled)
            putBoolean("sound", soundEnabled)
            putBoolean("vibration", vibrationEnabled)
            putFloat("music_volume", musicVolume)
            putBoolean("autosave", autoSaveEnabled)
            putFloat("animation_speed", animationSpeed)
            putFloat("text_size", textSize)
            putString("theme", colorTheme)
            apply()
        }
        
        Toast.makeText(this, "✅ บันทึกการตั้งค่าแล้ว", Toast.LENGTH_SHORT).show()
    }
    
    private fun loadSettings() {
        difficulty = prefs.getString("difficulty", "Normal") ?: "Normal"
        musicEnabled = prefs.getBoolean("music", true)
        soundEnabled = prefs.getBoolean("sound", true)
        vibrationEnabled = prefs.getBoolean("vibration", true)
        musicVolume = prefs.getFloat("music_volume", 0.5f)
        autoSaveEnabled = prefs.getBoolean("autosave", true)
        animationSpeed = prefs.getFloat("animation_speed", 1.0f)
        textSize = prefs.getFloat("text_size", 16f)
        colorTheme = prefs.getString("theme", "Dark") ?: "Dark"
    }
    
    private fun hasSavedGame(): Boolean {
        // If saved_floor exists and is at least 1, we have a valid game
        return prefs.contains("saved_floor") && prefs.getInt("saved_floor", 0) >= 1
    }
    
    private fun autoSave() {
        // Implementation for auto-save
        saveGame()
    }
    
    private fun saveGame() {
        prefs.edit().apply {
            putInt("saved_floor", floor)
            putInt("saved_rooms_completed", roomsCompleted)
            putString("saved_class", playerClass.name)
            putString("saved_name", playerName)
            putInt("saved_level", level)
            putInt("saved_hp", hp)
            putInt("saved_max_hp", maxHp)
            putInt("saved_atk", atk)
            putInt("saved_def", def)
            putInt("saved_luck", luck)
            putInt("saved_soul", soulPoints)
            putInt("saved_gold", gold)
            putInt("saved_exp", exp)
            putLong("total_playtime", totalPlayTime)
            putInt("stat_kills", enemiesKilled)
            putInt("stat_bosses", bossesKilled)
            apply()
        }
    }
    
    private fun loadGame() {
        floor = prefs.getInt("saved_floor", 1)
        roomsCompleted = prefs.getInt("saved_rooms_completed", 0)
        playerClass = CharacterClass.valueOf(prefs.getString("saved_class", "WARRIOR") ?: "WARRIOR")
        playerName = prefs.getString("saved_name", "Hero") ?: "Hero"
        level = prefs.getInt("saved_level", 1)
        hp = prefs.getInt("saved_hp", 30)
        maxHp = prefs.getInt("saved_max_hp", 30)
        atk = prefs.getInt("saved_atk", 8)
        def = prefs.getInt("saved_def", 5)
        luck = prefs.getInt("saved_luck", 3)
        soulPoints = prefs.getInt("saved_soul", 0)
        gold = prefs.getInt("saved_gold", 0)
        exp = prefs.getInt("saved_exp", 0)
        totalPlayTime  = prefs.getLong("total_playtime", 0)
        enemiesKilled  = prefs.getInt("stat_kills", 0)
        bossesKilled   = prefs.getInt("stat_bosses", 0)

        updateStats()
        continueAdventure()
    }
    
    private fun confirmNewGame() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ เริ่มเกมใหม่?")
            .setMessage("เกมที่บันทึกไว้จะถูกลบ!\nคุณแน่ใจหรือไม่?")
            .setPositiveButton("ใช่, เริ่มใหม่") { _, _ ->
                clearSaveData()
                showClassSelection()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
    
    private fun clearSaveData() {
        prefs.edit().apply {
            remove("saved_floor")
            remove("saved_class")
            remove("saved_name")
            apply()
        }
    }
    
    // ========== HELPER FUNCTIONS ==========
    private fun setChoices(
        t1: String, t2: String, t3: String, t4: String,
        a1: () -> Unit, a2: () -> Unit, a3: () -> Unit, a4: () -> Unit
    ) {
        // Detect if we are in Main Menu (Initial screen)
        val isMainMenu = t1.contains("เริ่ม") || t1.contains("เล่นต่อ")

        // Automatically hide logo if it's not the main menu state
        if (!isMainMenu) {
            ivLogo?.visibility = View.GONE
            // Expand mainContainer to fill screen when logo is gone
            (mainContainer.layoutParams as? FrameLayout.LayoutParams)?.height = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ivLogo?.visibility = View.VISIBLE
            (mainContainer.layoutParams as? FrameLayout.LayoutParams)?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        choiceContainer.removeAllViews()
        buttonRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        buttonRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        // Image-based buttons for Main Menu
        if (isMainMenu) {
            // 1. Hide unwanted elements
            if (::headerContainer.isInitialized) headerContainer.visibility = View.GONE
            ivBackground?.alpha = 1.0f // Full background brightness for menu
            glassCard.background = null
            glassCard.elevation = 0f
            storyScrollView.visibility = View.GONE
            
            // 2. Adjust Logo for better spacing
            ivLogo?.layoutParams = (ivLogo?.layoutParams as LinearLayout.LayoutParams).apply {
                height = dp(240) // Slightly larger logo
                setMargins(dp(20), dp(40), dp(20), dp(10))
            }

            // 3. Reset Container for vertical center stacking
            storyContainer.layoutParams = (storyContainer.layoutParams as LinearLayout.LayoutParams).apply {
                setMargins(0, 0, 0, 0)
                weight = 1f
            }
            
            choiceContainer.layoutParams = (choiceContainer.layoutParams as LinearLayout.LayoutParams).apply {
                height = ViewGroup.LayoutParams.MATCH_PARENT
                weight = 0f
                setMargins(dp(10), 0, dp(10), dp(60))
            }
            choiceContainer.gravity = Gravity.CENTER

            // 4. Extra Large, closely-packed buttons
            val btnW = dp(400) // Much wider
            val btnH = dp(160) // Slightly smaller height to prevent overlap
            
            // Helper to determine image from button text
            fun getButtonImage(text: String): Int {
                return when {
                    text.contains("เล่นต่อ") || text.contains("Continue") -> R.drawable.btn_continue
                    text.contains("เริ่มเกม") || text.contains("เริ่มใหม่") || text.contains("Start") -> R.drawable.start
                    text.contains("ตั้งค่า") || text.contains("Settings") -> R.drawable.setting
                    text.contains("Achievement") || text.contains("ผลงาน") || text.contains("ความสำเร็จ") || text.contains("Achievements") -> R.drawable.achievement
                    text.contains("วิธีเล่น") || text.contains("How to") -> R.drawable.howto
                    else -> R.drawable.start
                }
            }
            
            val buttonList = mutableListOf<Pair<String, () -> Unit>>()
            
            // Fixed ordering based on request: Start -> Continue (if any) -> Settings -> HowTo
            // Start Game / New Game
            if (t1.contains("เริ่ม")) buttonList.add(t1 to a1)
            else if (t2.contains("เริ่ม")) buttonList.add(t2 to a2)

            // Continue (เล่นต่อ)
            if (t1.contains("เล่นต่อ")) buttonList.add(t1 to a1)
            else if (t2.contains("เล่นต่อ")) buttonList.add(t2 to a2)
            
            // Settings
            if (t2.contains("ตั้งค่า")) buttonList.add(t2 to a2)
            else if (t3.contains("ตั้งค่า")) buttonList.add(t3 to a3)
            
            // HowTo / Achievements
            if (t3.contains("Achievements") || t3.contains("ผลงาน")) buttonList.add(t3 to a3)
            else if (t4.contains("Achievements") || t4.contains("ผลงาน")) buttonList.add(t4 to a4)
            
            if (t4.contains("วิธีเล่น")) buttonList.add(t4 to a4)

            val itemLp = LinearLayout.LayoutParams(btnW, btnH).apply { 
                gravity = Gravity.CENTER
                setMargins(0, dp(-25), 0, dp(-25)) 
            }

            // Clear and add in new order
            choiceContainer.removeAllViews()
            buttonList.take(4).forEach { (text, action) ->
                choiceContainer.addView(createImageButton(getButtonImage(text), action), itemLp)
            }

            return
        }

        // Standard RPG Mode (Story / Combat)
        // Header stays hidden during gameplay as per new UI design
        if (::headerContainer.isInitialized) {
            headerContainer.visibility = View.GONE
        }
        ivBackground?.alpha = 0.4f // Dim background more for dark theme
        ivLogo?.layoutParams = (ivLogo?.layoutParams as LinearLayout.LayoutParams).apply {
            height = dp(180)
            setMargins(dp(20), dp(20), dp(20), 0)
        }
        glassCard.background = ModernUI.createModernCard(this, ModernUI.ColorPalette.gradientDark, 12f, 0.7f).background
        glassCard.elevation = 8f * resources.displayMetrics.density
        storyContainer.layoutParams = (storyContainer.layoutParams as LinearLayout.LayoutParams).apply {
            setMargins(dp(16), dp(10), dp(16), dp(10))
            weight = 1f
        }
        storyScrollView.visibility = View.VISIBLE
        
        choiceContainer.addView(buttonRow1)
        choiceContainer.addView(buttonRow2)
        
        choiceContainer.layoutParams = (choiceContainer.layoutParams as LinearLayout.LayoutParams).apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            weight = 0f
            setMargins(dp(8), dp(8), dp(8), dp(8))
        }
        choiceContainer.gravity = Gravity.BOTTOM

        val btn1 = ModernUI.createNeonButton(this, t1, ModernUI.ColorPalette.neonPurple) { a1() }
        val btn2 = ModernUI.createNeonButton(this, t2, ModernUI.ColorPalette.neonBlue) { a2() }
        val btn3 = ModernUI.createNeonButton(this, t3, ModernUI.ColorPalette.neonPink) { a3() }
        val btn4 = ModernUI.createNeonButton(this, t4, ModernUI.ColorPalette.neonGreen) { a4() }
        
        val h = dp(55)
        
        if (t1.isNotEmpty()) buttonRow1.addView(btn1, LinearLayout.LayoutParams(0, h, 1f))
        if (t2.isNotEmpty()) buttonRow1.addView(btn2, LinearLayout.LayoutParams(0, h, 1f))
        
        if (t3.isNotEmpty()) buttonRow2.addView(btn3, LinearLayout.LayoutParams(0, h, 1f))
        if (t4.isNotEmpty()) buttonRow2.addView(btn4, LinearLayout.LayoutParams(0, h, 1f))

        // Update references
        btnChoice1 = (btn1.getChildAt(0) as? Button) ?: btnChoice1
        btnChoice2 = (btn2.getChildAt(0) as? Button) ?: btnChoice2
        btnChoice3 = (btn3.getChildAt(0) as? Button) ?: btnChoice3
        btnChoice4 = (btn4.getChildAt(0) as? Button) ?: btnChoice4
    }

    private fun createImageButton(resId: Int, onClick: () -> Unit): View {
        return ImageView(this).apply {
            setImageResource(resId)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(dp(4), dp(4), dp(4), dp(4))
            isClickable = true
            isFocusable = true
            
            setOnClickListener {
                if (vibrationEnabled) vibrate()
                
                val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                    this,
                    PropertyValuesHolder.ofFloat("scaleX", 0.92f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.92f)
                ).apply { duration = 80 }
                
                val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
                    this,
                    PropertyValuesHolder.ofFloat("scaleX", 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f)
                ).apply { duration = 80 }

                AnimatorSet().apply {
                    playSequentially(scaleDown, scaleUp)
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            onClick()
                        }
                    })
                    start()
                }
            }
        }
    }
    
    private fun formatPlayTime(millis: Long): String {
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        return "${hours}h ${minutes}m"
    }
    
    fun heal(amount: Int) {
        hp = min(maxHp, hp + amount)
        updateStats()
    }

    private fun updatePlayTime() {
        val sessionTime = System.currentTimeMillis() - sessionStartTime
        totalPlayTime += sessionTime
        sessionStartTime = System.currentTimeMillis()
    }
    
    private fun loadAchievements() {
        achievements.clear()
        val defs = listOf(
            // COMBAT
            Achievement("first_blood",   "First Blood",       "สังหารศัตรูตัวแรก",              "⚔️", "combat", "+50 Gold",    rewardGold = 50),
            Achievement("kill_10",       "นักล่า",            "สังหาร 10 ตัว",                  "🗡️", "combat", "+100 Gold",   rewardGold = 100),
            Achievement("kill_50",       "นักรบผู้โหดเหี้ยม", "สังหาร 50 ตัว",                  "💀", "combat", "+1 SP",       rewardSP = 1),
            Achievement("kill_100",      "เทพเจ้าสงคราม",     "สังหาร 100 ตัว",                 "☠️", "combat", "+2 SP",       rewardSP = 2),
            Achievement("boss_first",    "นักล่าบอส",         "เอาชนะบอสตัวแรก",               "👑", "combat", "+200 Gold",   rewardGold = 200),
            Achievement("boss_3",        "ผู้พิชิต",          "เอาชนะบอส 3 ตัว",               "🏆", "combat", "+1 SP",       rewardSP = 1),
            Achievement("boss_5",        "ผู้รอดชีวิต",       "เอาชนะบอส 5 ตัว",               "🌟", "combat", "+2 SP",       rewardSP = 2),
            Achievement("no_death_5",    "ไร้บาดแผล",         "ผ่าน 5 ชั้นโดยไม่ตาย",           "🛡️", "combat", "+150 Gold",   rewardGold = 150),
            // EXPLORE
            Achievement("floor_5",       "นักสำรวจ",          "ถึงชั้น 5",                      "🗺️", "explore", "+30 Gold",   rewardGold = 30),
            Achievement("floor_10",      "Deep Diver",        "ถึงชั้น 10",                     "⬇️", "explore", "+100 Gold",  rewardGold = 100),
            Achievement("floor_25",      "ผู้กล้า",           "ถึงชั้น 25",                     "🔥", "explore", "+1 SP",      rewardSP = 1),
            Achievement("floor_50",      "Dungeon Master",    "ถึงชั้น 50",                     "🏰", "explore", "+2 SP",      rewardSP = 2),
            Achievement("floor_100",     "ตำนานดันเจี้ยน",    "ถึงชั้น 100",                    "🌌", "explore", "+3 SP",      rewardSP = 3),
            Achievement("level_5",       "ผู้เริ่มต้น",       "ถึง Lv.5",                       "📈", "explore", "+50 Gold",   rewardGold = 50),
            Achievement("level_10",      "วีรบุรุษ",          "ถึง Lv.10",                      "⭐", "explore", "+1 SP",      rewardSP = 1),
            Achievement("level_20",      "ผู้เหนือมนุษย์",    "ถึง Lv.20",                      "💫", "explore", "+2 SP",      rewardSP = 2),
            // WEALTH
            Achievement("gold_200",      "เริ่มมีทุน",        "มีทองคำ 200 ขึ้นไป",             "💰", "wealth", "+1 SP",       rewardSP = 1),
            Achievement("rich",          "เศรษฐี",           "มีทองคำ 1000 ขึ้นไป",            "💎", "wealth", "+1 SP",       rewardSP = 1),
            Achievement("millionaire",   "มหาเศรษฐี",        "มีทองคำ 5000 ขึ้นไป",            "👑", "wealth", "+3 SP",       rewardSP = 3),
            Achievement("collector_5",   "นักสะสม",          "มีไอเทม 5 ชิ้นพร้อมกัน",         "🎒", "wealth", "+50 Gold",    rewardGold = 50),
            Achievement("collector",     "นักสะสมตัวยง",     "มีไอเทม 15 ชิ้นพร้อมกัน",        "📦", "wealth", "+100 Gold",   rewardGold = 100),
            Achievement("item_rare",     "ของหายาก",         "ได้รับไอเทม Rare ขึ้นไป",        "🔵", "wealth", "+1 SP",       rewardSP = 1),
            Achievement("item_epic",     "ของวิเศษ",         "ได้รับไอเทม Epic ขึ้นไป",        "🟣", "wealth", "+1 SP",       rewardSP = 1),
            Achievement("item_legendary","ของในตำนาน",       "ได้รับไอเทม Legendary ขึ้นไป",   "🟡", "wealth", "+2 SP",       rewardSP = 2),
            // SPECIAL
            Achievement("skill_3",       "นักเรียนรู้",       "ปลดล็อกสกิล 3 สกิล",            "📚", "special", "+100 Gold",  rewardGold = 100),
            Achievement("skill_10",      "ปรมาจารย์สกิล",    "ปลดล็อกสกิล 10 สกิล",           "🧠", "special", "+2 SP",      rewardSP = 2),
            Achievement("gambling_win",  "โชคลาภ",           "ชนะการพนันครั้งแรก",             "🎲", "special", "+150 Gold",  rewardGold = 150),
            Achievement("riddle_solver", "นักปริศนา",        "ตอบปริศนาถูกต้อง",               "❓", "special", "+100 Gold",  rewardGold = 100),
            Achievement("arena_win",     "แชมป์สังเวียน",    "ชนะห้องประลอง Elite",            "🏟️", "special", "+1 SP",      rewardSP = 1),
            Achievement("npc_helper",    "นักการกุศล",       "ช่วยเหลือ NPC ด้วยยาหรือทอง",    "🤝", "special", "+100 Gold",  rewardGold = 100)
        )
        // โหลดสถานะ unlocked จาก prefs
        defs.forEach { ach ->
            val isUnlocked = prefs.getBoolean("ach_${ach.id}", false)
            achievements.add(ach.copy(unlocked = isUnlocked))
        }
        // โหลดสถิติ
        enemiesKilled = prefs.getInt("stat_kills", 0)
        bossesKilled  = prefs.getInt("stat_bosses", 0)
    }

    private fun unlockAchievement(id: String) {
        val idx = achievements.indexOfFirst { it.id == id }
        if (idx < 0 || achievements[idx].unlocked) return
        achievements[idx] = achievements[idx].copy(unlocked = true)
        prefs.edit().putBoolean("ach_$id", true).apply()

        val ach = achievements[idx]
        // มอบรางวัล
        if (ach.rewardGold > 0) gold += ach.rewardGold
        if (ach.rewardSP > 0)   skillPoints += ach.rewardSP
        updateStats()

        val rewardLine = if (ach.rewardDesc.isNotEmpty()) "\n🎁 รางวัล: ${ach.rewardDesc}" else ""
        AlertDialog.Builder(this)
            .setTitle("🏆 ปลดล็อก Achievement!")
            .setMessage("${ach.icon} ${ach.name}\n${ach.description}$rewardLine")
            .setPositiveButton("เยี่ยม!", null)
            .show()
    }

    fun checkAchievements() {
        // Combat
        if (enemiesKilled >= 1)   unlockAchievement("first_blood")
        if (enemiesKilled >= 10)  unlockAchievement("kill_10")
        if (enemiesKilled >= 50)  unlockAchievement("kill_50")
        if (enemiesKilled >= 100) unlockAchievement("kill_100")
        if (bossesKilled >= 1)    unlockAchievement("boss_first")
        if (bossesKilled >= 3)    unlockAchievement("boss_3")
        if (bossesKilled >= 5)    unlockAchievement("boss_5")
        // Explore
        if (floor >= 5)   unlockAchievement("floor_5")
        if (floor >= 10)  unlockAchievement("floor_10")
        if (floor >= 25)  unlockAchievement("floor_25")
        if (floor >= 50)  unlockAchievement("floor_50")
        if (floor >= 100) unlockAchievement("floor_100")
        if (level >= 5)   unlockAchievement("level_5")
        if (level >= 10)  unlockAchievement("level_10")
        if (level >= 20)  unlockAchievement("level_20")
        // Wealth
        if (gold >= 200)  unlockAchievement("gold_200")
        if (gold >= 1000) unlockAchievement("rich")
        if (gold >= 5000) unlockAchievement("millionaire")
        if (inventory.size >= 5)  unlockAchievement("collector_5")
        if (inventory.size >= 15) unlockAchievement("collector")
        val maxRarity = inventory.maxOfOrNull { it.rarity.ordinal } ?: -1
        if (maxRarity >= ItemRarity.RARE.ordinal)      unlockAchievement("item_rare")
        if (maxRarity >= ItemRarity.EPIC.ordinal)      unlockAchievement("item_epic")
        if (maxRarity >= ItemRarity.LEGENDARY.ordinal) unlockAchievement("item_legendary")
        // Skills
        if (unlockedSkills.size >= 3)  unlockAchievement("skill_3")
        if (unlockedSkills.size >= 10) unlockAchievement("skill_10")
        // บันทึกสถิติ
        prefs.edit().putInt("stat_kills", enemiesKilled).putInt("stat_bosses", bossesKilled).apply()
    }

    private fun showAchievements(filter: String = currentAchievementFilter) {
        currentAchievementFilter = filter
        val list = if (filter == "all") achievements else achievements.filter { it.category == filter }
        val unlocked = list.count { it.unlocked }
        val total = list.size

        tvStory.text = buildString {
            appendLine("╔══════════════════════════════╗")
            appendLine("║       🏆 ACHIEVEMENTS        ║")
            appendLine("╚══════════════════════════════╝")
            appendLine()
            val catLabel = when (filter) {
                "combat"  -> "⚔️ การต่อสู้"
                "explore" -> "🗺️ การสำรวจ"
                "wealth"  -> "💎 ความมั่งคั่ง"
                "special" -> "⭐ พิเศษ"
                else      -> "ทั้งหมด"
            }
            appendLine("หมวด: $catLabel  |  ปลดล็อค: $unlocked/$total")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            list.forEach { ach ->
                if (ach.unlocked) {
                    appendLine("${ach.icon} ✅ ${ach.name}")
                    appendLine("   ${ach.description}")
                    if (ach.rewardDesc.isNotEmpty()) appendLine("   🎁 ${ach.rewardDesc}")
                } else {
                    appendLine("🔒 ???")
                    appendLine("   ยังไม่ปลดล็อก")
                }
                appendLine()
            }
        }

        setChoices(
            "⏮️ กลับ",
            "⚔️ ต่อสู้",
            "🗺️ สำรวจ",
            "💎 ความมั่งคั่ง/⭐",
            { showMainMenu() },
            { showAchievements("combat") },
            { showAchievements("explore") },
            {
                if (filter != "wealth") showAchievements("wealth")
                else showAchievements("special")
            }
        )
    }
    
    private fun showHowToPlay() {
        tvStory.text = "" 
        
        val howToContent = SpannableStringBuilder().apply {
            val title = "📖 คู่มือการเอาชีวิตรอดในดันเจี้ยน\n"
            append(title)
            setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ModernUI.ColorPalette.neonGold), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(1.2f), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            append("\n")
            
            fun addSection(sectionTitle: String, icon: String, body: String, color: Int) {
                val start = length
                val header = "$icon $sectionTitle\n"
                append(header)
                setSpan(StyleSpan(Typeface.BOLD), start, start + header.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(color), start, start + header.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(UnderlineSpan(), start, start + header.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                
                append(body)
                append("\n\n")
            }

            addSection("การผจญภัย", "🎮", 
                "• ดันเจี้ยนเปลี่ยนไปทุกครั้งที่ก้าวเข้าไป\n• ความตายคือจุดสิ้นสุด (Permadeath)\n• สำรวจทุกห้องเพื่อหาทรัพยากร", 
                ModernUI.ColorPalette.neonBlue)

            addSection("การต่อสู้", "⚔️", 
                "• โจมตี: สร้างความเสียหายหนัก (เสี่ยงสวนกลับ)\n• ป้องกัน: ลดความเสียหายที่ได้รับ\n• สกิล: ใช้ Soul Points เพื่อพลิกเกม", 
                ModernUI.ColorPalette.neonPurple)

            addSection("พัฒนาตัวละคร", "📈", 
                "• สะสม EXP เพื่อเลเวลอัปและเพิ่มค่าสถานะ\n• ทอง (Gold) ใช้แลกเปลี่ยนกับพ่อค้าปริศนา\n• สวมใส่อาวุธและชุดเกราะเพื่อเสริมพลัง", 
                ModernUI.ColorPalette.neonPink)

            addSection("เทคนิคพิเศษ", "💡", 
                "• ค่าความโชคดี (Luck) มีผลต่อคริติคอลและการหลบหลีก\n• เลือกเผ่าและอาชีพที่ส่งเสริมกันจะทำให้เล่นง่ายขึ้น\n• อย่าลืมเก็บ Soul Points ไว้ใช้ยามคับขัน", 
                ModernUI.ColorPalette.neonGreen)
        }

        tvStory.setText(howToContent, TextView.BufferType.SPANNABLE)
        tvStory.gravity = Gravity.START
        
        setChoices(
            "🏠 กลับสู่เมนูหลัก",
            "",
            "",
            "",
            { showMainMenu() },
            {},
            {},
            {}
        )
    }
    
    // ========== GAME FLOW (TO BE CONTINUED) ==========
    private fun startNewGame() {
        currentSeed = System.currentTimeMillis()
        floor = 1
        roomsCompleted = 0
        level = 1
        exp = 0
        expToNext = 100
        
        sessionStartTime = System.currentTimeMillis()
        updateStats()
        
        showGameIntro()
    }
    
    private fun showGameIntro() {
        tvStory.text = """
╔══════════════════════════════╗
║     🌟 เริ่มการผจญภัย       ║
╚══════════════════════════════╝

คุณคือ $playerName
${playerClass.emoji} ${playerClass.displayName}

วันหนึ่ง... คุณตื่นขึ้นในดันเจี้ยนมืดมิด
ไม่รู้ว่ามาถึงที่นี่ได้อย่างไร

เสียงกระซิบดังก้อง:
"ยินดีต้อนรับสู่ Endless Dungeon
 ต่อสู้... หรือตาย
 ไม่มีทางกลับ"

ข้างหน้ามีบันไดลงไป
ดันเจี้ยนกว้างใหญ่รอคุณอยู่...

คุณพร้อมหรือยัง ${playerClass.emoji}?
        """.trimIndent()
        
        setChoices(
            "⚔️ ลงบันได",
            "🎒 ตรวจสอบตัวเอง",
            "📖 อ่านคำเตือน",
            "💾 บันทึกเกม",
            { enterFloor() },
            { showCharacterSheet() },
            { showWarning() },
            {
                saveGame()
                Toast.makeText(this, "💾 บันทึกเกมแล้ว", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    fun showGameScreen() {
        setContentView(rootLayout)
        continueAdventure()
    }

    fun continueAdventure() {
        // Calculate equipment bonuses
        var bonusHp = 0
        var bonusAtk = 0
        var bonusDef = 0
        var bonusLuck = 0
        
        listOf(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
            if (item != null) {
                bonusHp += item.getStat("hp")
                bonusAtk += item.getStat("atk")
                bonusDef += item.getStat("def")
                bonusLuck += item.getStat("luck")
            }
        }

        // Calculate skill bonuses
        val skillBonusAtk = calculateSkillBonus("atk_bonus")
        val skillBonusDef = calculateSkillBonus("def_bonus")
        val skillBonusHp = calculateSkillBonus("hp_bonus") + calculateSkillBonus("max_hp")
        val skillBonusLuck = calculateSkillBonus("luck_bonus")

        val displayMaxHp = maxHp + bonusHp + skillBonusHp
        val displayAtk = atk + bonusAtk + skillBonusAtk
        val displayDef = def + bonusDef + skillBonusDef
        val displayLuck = luck + bonusLuck + skillBonusLuck

        val hpText = if (bonusHp + skillBonusHp != 0) "$hp/$displayMaxHp ($hp/$maxHp ${if(bonusHp+skillBonusHp>0)"+" else ""}${bonusHp+skillBonusHp})" else "$hp/$maxHp"
        val atkText = if (bonusAtk + skillBonusAtk != 0) "$displayAtk ($atk ${if(bonusAtk+skillBonusAtk>0)"+" else ""}${bonusAtk+skillBonusAtk})" else "$atk"
        val defText = if (bonusDef + skillBonusDef != 0) "$displayDef ($def ${if(bonusDef+skillBonusDef>0)"+" else ""}${bonusDef+skillBonusDef})" else "$def"
        val luckText = if (bonusLuck + skillBonusLuck != 0) "$displayLuck ($luck ${if(bonusLuck+skillBonusLuck>0)"+" else ""}${bonusLuck+skillBonusLuck})" else "$luck"

        tvStory.text = """
            ╔══════════════════════════════╗
            ║      RESUME ADVENTURE        ║
            ╚══════════════════════════════╝

            $playerName has returned!

            You are on Floor $floor
            of the dark dungeon.

            CURRENT STATS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Lv.$level ${playerClass.emoji} ${playerClass.displayName}
            ❤️ HP: $hpText
            ⚔️ ATK: $atkText | 🛡️ DEF: $defText | 🍀 LUCK: $luckText
            ✨ Soul: $soulPoints | 💰 Gold: $gold

            Total Playtime: ${formatPlayTime(totalPlayTime)}

            Are you ready to continue?
        """.trimIndent()
        
        setChoices(
            "⚔️ PROCEED",
            "🎒 STATS",
            "💾 SAVE",
            "🚪 MENU",
            { enterFloor() },
            { showCharacterSheet() },
            {
                saveGame()
                Toast.makeText(this, "💾 Game Saved", Toast.LENGTH_SHORT).show()
            },
            {
                AlertDialog.Builder(this)
                    .setTitle("⚠️ Back to Menu?")
                    .setMessage("Progress will be saved.")
                    .setPositiveButton("Yes") { _, _ ->
                        saveGame()
                        showMainMenu()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
    }
    
    internal fun showCharacterSheet() {
        // Calculate equipment bonuses
        var bonusHp = 0
        var bonusAtk = 0
        var bonusDef = 0
        var bonusLuck = 0
        
        listOf(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
            if (item != null) {
                bonusHp += item.getStat("hp")
                bonusAtk += item.getStat("atk")
                bonusDef += item.getStat("def")
                bonusLuck += item.getStat("luck")
            }
        }

        // Calculate skill bonuses
        val skillBonusAtk = calculateSkillBonus("atk_bonus")
        val skillBonusDef = calculateSkillBonus("def_bonus")
        val skillBonusHp = calculateSkillBonus("hp_bonus") + calculateSkillBonus("max_hp")
        val skillBonusLuck = calculateSkillBonus("luck_bonus")

        val displayMaxHp = maxHp + bonusHp + skillBonusHp
        val displayAtk = atk + bonusAtk + skillBonusAtk
        val displayDef = def + bonusDef + skillBonusDef
        val displayLuck = luck + bonusLuck + skillBonusLuck

        val hpText = if (bonusHp + skillBonusHp != 0) "$hp/$displayMaxHp ($hp/$maxHp ${if(bonusHp+skillBonusHp>0)"+" else ""}${bonusHp+skillBonusHp})" else "$hp/$maxHp"
        val atkText = if (bonusAtk + skillBonusAtk != 0) "$displayAtk ($atk ${if(bonusAtk+skillBonusAtk>0)"+" else ""}${bonusAtk+skillBonusAtk})" else "$atk"
        val defText = if (bonusDef + skillBonusDef != 0) "$displayDef ($def ${if(bonusDef+skillBonusDef>0)"+" else ""}${bonusDef+skillBonusDef})" else "$def"
        val luckText = if (bonusLuck + skillBonusLuck != 0) "$displayLuck ($luck ${if(bonusLuck+skillBonusLuck>0)"+" else ""}${bonusLuck+skillBonusLuck})" else "$luck"

        tvStory.text = """
╔══════════════════════════════╗
║       📊 Character Sheet     ║
╚══════════════════════════════╝

👤 $playerName
${playerClass.emoji} Lv.$level ${playerClass.displayName}

📊 STATS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❤️  HP: $hpText
⚔️  Attack: $atkText
🛡️  Defense: $defText
🍀 Luck: $luckText
✨ Soul Points: $soulPoints
💰 Gold: $gold

📈 PROGRESS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🏰 Floor: $floor
🎯 EXP: $exp/$expToNext
⏰ Playtime: ${formatPlayTime(totalPlayTime)}

🎯 SPECIAL ABILITY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
${playerClass.specialAbility}

🎒 EQUIPMENT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Weapon: ${equippedWeapon?.name ?: "None"}
Armor: ${equippedArmor?.name ?: "None"}
Items: ${inventory.size}/$maxInventorySize
        """.trimIndent()
        
        setChoices(
            "⏮️ กลับ",
            "🎒 Inventory",
            "🌳 Skills",
            "",
            { continueAdventure() },
            { showInventory() },
            { showSkillTree() },
            {}
        )
    }
    
    private var inventoryManager: InventoryManager? = null

    fun showInventory() {
        if (inventoryManager == null) {
            inventoryManager = InventoryManager(
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
        }
        inventoryManager?.showInventory()
    }

    internal fun isInventoryShowing(): Boolean {
        return inventoryManager?.let { mgr ->
            try {
                val field = mgr.javaClass.getDeclaredField("inventoryLayout")
                field.isAccessible = true
                val layout = field.get(mgr) as? View
                layout?.parent != null
            } catch (e: Exception) {
                false
            }
        } ?: false
    }

    fun showSkillTree() {
        val skillTreeManager = SkillTreeManager(
            activity = this,
            playerClass = playerClass,
            playerRace = playerRace,
            skillPoints = skillPoints,
            unlockedSkills = unlockedSkills,
            onSkillChanged = { points, skills ->
                skillPoints = points
                if (unlockedSkills !== skills) {
                    unlockedSkills.clear()
                    unlockedSkills.addAll(skills)
                }
                updateStats()
                checkAchievements()
            }
        )
        skillTreeManager.showSkillTree()
    }
    
    private fun showWarning() {
        tvStory.text = """
╔══════════════════════════════╗
║      ⚠️ คำเตือน            ║
╚══════════════════════════════╝

📜 ข้อความจากนักผจญภัยคนก่อน:

"ถ้าคุณอ่านข้อความนี้...
 แสดงว่าคุณโชคดีกว่าข้ามาก

ข้อควรระวัง:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
💀 ตายจริง - ไม่มี Continue
🎲 แต่ละชั้นสุ่ม - ไม่เหมือนเดิม
⚔️ ศัตรูแข็งแกร่งขึ้นทุกชั้น
💰 เก็บ Gold ซื้อของบ่อยๆ
✨ Soul Points มีค่ามาก
🍀 โชคเปลี่ยนเกม!

สุดท้าย...
อย่าลืมบันทึกเกมบ่อยๆ
PERMADEATH คือจริงจัง!

- นักผจญภัยที่มาถึงชั้น 42"
        """.trimIndent()
        
        setChoices(
            "⏮️ กลับ",
            "",
            "",
            "",
            { showGameIntro() },
            {},
            {},
            {}
        )
    }
    
    private fun getRandomRoomType(random: Random): String {
        val eventType = random.nextInt(100)
        return when {
            eventType < 28 -> "normal"
            eventType < 52 -> "combat"
            eventType < 62 -> "treasure"
            eventType < 70 -> "merchant"
            eventType < 77 -> "special"
            eventType < 83 -> "gamble"
            eventType < 89 -> "arena"
            eventType < 95 -> "npc"
            else -> "hidden"
        }
    }

    private fun getBiomeForFloor(f: Int): FloorBiome = when {
        f <= 10  -> FloorBiome(
            name = "สุสานคร่ำคร่า", emoji = "🪦",
            description = "กระดูกและความมืดมิดล้อมรอบคุณ",
            effects = "เสีย 3 HP/ห้อง | DEF ลด 3",
            roomEntryDamage = 3, combatDefBonus = -3
        )
        f <= 20  -> FloorBiome(
            name = "ป่าพิษ", emoji = "🌿",
            description = "ไอพิษลอยอยู่เต็มอากาศ ทุกลมหายใจเป็นอันตราย",
            effects = "เสีย 5 HP/ห้อง | ATK ลด 3",
            roomEntryDamage = 5, combatAtkBonus = -3
        )
        f <= 30  -> FloorBiome(
            name = "ถ้ำแมกมา", emoji = "🌋",
            description = "ลาวาไหลเอ่อและความร้อนแผดเผาทุกอย่าง",
            effects = "เสีย 8 HP/ห้อง | DEF ลด 5 | ATK ลด 5",
            roomEntryDamage = 8, combatAtkBonus = -5, combatDefBonus = -5
        )
        f <= 40  -> FloorBiome(
            name = "ทุ่งน้ำแข็ง", emoji = "❄️",
            description = "น้ำแข็งขาวโพลนปกคลุมทุกอย่าง ลมหนาวกัดกิน",
            effects = "เสีย 4 HP/ห้อง | DEF เพิ่ม 12",
            roomEntryDamage = 4, combatDefBonus = 12
        )
        f <= 50  -> FloorBiome(
            name = "ป่าพายุสายฟ้า", emoji = "⛈️",
            description = "ฟ้าผ่าและพายุถล่มอยู่ตลอดเวลา พลังงานสูงมาก",
            effects = "เสีย 6 HP/ห้อง | ATK เพิ่ม 15 | DEF ลด 5",
            roomEntryDamage = 6, combatAtkBonus = 15, combatDefBonus = -5
        )
        f <= 60  -> FloorBiome(
            name = "หล่มเลือด", emoji = "🩸",
            description = "พื้นเต็มไปด้วยเลือดเก่า กลิ่นคาวคลุ้งตลอดเวลา",
            effects = "เสีย 10 HP/ห้อง | ATK เพิ่ม 10 | EXP +50%",
            roomEntryDamage = 10, combatAtkBonus = 10, expMultiplier = 1.5f
        )
        f <= 70  -> FloorBiome(
            name = "โมฆะมิติ", emoji = "🌀",
            description = "มิติที่กฎของธรรมชาติพังทลาย ทุกอย่างเป็นไปได้",
            effects = "DEF ลด 15 | ATK เพิ่ม 22 | Gold +30%",
            combatAtkBonus = 22, combatDefBonus = -15, goldMultiplier = 1.3f
        )
        f <= 80  -> FloorBiome(
            name = "แดนวิญญาณ", emoji = "👻",
            description = "วิญญาณโบราณล้อมรอบและให้พลังงาน",
            effects = "ฟื้นฟู 8 HP/ห้อง | EXP +30% | Gold +20%",
            roomEntryHeal = 8, expMultiplier = 1.3f, goldMultiplier = 1.2f
        )
        f <= 90  -> FloorBiome(
            name = "แดนเก่าแก่", emoji = "⚱️",
            description = "ซากอารยธรรมโบราณผุพัง พลังงานโบราณสะสม",
            effects = "เสีย 8 HP/ห้อง | ATK เพิ่ม 18 | Gold +50%",
            roomEntryDamage = 8, combatAtkBonus = 18, goldMultiplier = 1.5f
        )
        else     -> FloorBiome(
            name = "แดนสุดท้าย", emoji = "💀",
            description = "ความมืดมิดนิรันดร์ที่ไม่มีสิ่งใดรอดชีวิต",
            effects = "เสีย 15 HP/ห้อง | ATK เพิ่ม 28 | Gold +100%",
            roomEntryDamage = 15, combatAtkBonus = 28, goldMultiplier = 2.0f
        )
    }

    internal fun enterFloor() {
        // headerContainer.visibility = View.VISIBLE // Removed, handled in setChoices
        val random = Random(currentSeed + floor + System.currentTimeMillis())
        
        // Generate 3 choices
        nextRooms.clear()
        repeat(3) { nextRooms.add(getRandomRoomType(random)) }
        
        val roomIcons = nextRooms.map { type ->
            when(type) {
                "combat" -> "⚔️ (ศัตรู)"
                "treasure" -> "💎 (หีบสมบัติ)"
                "merchant" -> "🏪 (ร้านค้า)"
                "special" -> "🌀 (เหตุการณ์)"
                "gamble" -> "🎲 (การพนัน)"
                "arena" -> "🏟️ (ประลอง)"
                "npc" -> "🧙 (พบคนแปลกหน้า)"
                "hidden" -> "❓ (???)"
                else -> "🚪 (ห้องว่าง)"
            }
        }

        val descriptions = listOf(
            "เสียงลมพัดผ่านช่องหินบอกใบ้ถึงสิ่งที่รออยู่ข้างหน้า",
            "กลิ่นอับชื้นและคราบเลือดแห้งกรังปกคลุมพื้นผิวของกำแพง",
            "คุณได้ยินเสียงโซ่ตรวนลากผ่านพื้นหินจากที่ไกลๆ",
            "บรรยากาศรอบตัวเริ่มเย็นเยียบจนคุณสัมผัสได้ถึงไอเย็น",
            "แสงไฟจากคบเพลิงสั่นไหวราวกับมีความแค้นซ่อนอยู่",
            "พื้นหินที่นี่ดูเหมือนจะเพิ่งถูกทำความสะอาด... แต่นี่มันดันเจี้ยนนะ?",
            "เสียงน้ำหยดกระทบพื้นดังสะท้อนก้องไปตามทางเดิน",
            "คุณรู้สึกเหมือนถูกจ้องมองจากเงามืดที่ลึกที่สุด",
            "ควันที่ลอยมาตามลมมีกลิ่นคล้ายสมุนไพรโบราณ",
            "รอยเท้าประหลาดปรากฏบนพื้นที่เต็มไปด้วยฝุ่น",
            "กำแพงหินสลักลวดลายที่ดูเหมือนกำลังเคลื่อนไหว",
            "ทางข้างหน้าดูเงียบสงบจนน่าขนลุก",
            "มีแสงเรืองแสงสีม่วงลอดออกมาจากช่องว่างใต้ประตู",
            "อากาศเริ่มเบาบางลงเรื่อยๆ ขณะที่คุณก้าวลึกเข้าไป",
            "เสียงหัวเราะเบาๆ ดังขึ้นแล้วจางหายไปในพริบตา",
            "คุณพบเศษอาวุธหักพังตกอยู่ตามทางเดิน",
            "ไอเวทมนตร์เข้มข้นจนทำให้ผิวหนังของคุณรู้สึกซ่า",
            "มีรอยเล็บกรีดลึกอยู่บนประตูไม้เก่าๆ",
            "แมลงสีดำตัวจิ๋วไต่ยั้วเยี้ยอยู่ตามซอกกำแพง",
            "เพดานห้องดูเหมือนจะต่ำลงจนน่าอึดอัด",
            "เสียงเครื่องกลโบราณทำงานอยู่เบื้องหลังกำแพงหิน",
            "กลิ่นหอมของดอกไม้ป่าลอยมา... ขัดกับสถานที่แห่งนี้สิ้นดี",
            "คุณรู้สึกถึงแรงสั่นสะเทือนเบาๆ ใต้ฝ่าเท้า",
            "เศษกระจกแตกกระจายอยู่บนพื้น สะท้อนภาพใบหน้าที่บิดเบี้ยวของคุณ",
            "ลมแรงพัดวูบผ่านหน้าคุณไป ราวกับวิญญาณที่เร่งรีบ",
            "มีเสียงสวดอ้อนวอนในภาษาที่คุณไม่เข้าใจดังแว่วมา",
            "มอสสีเขียวมรกตเรืองแสงช่วยนำทางคุณในความมืด",
            "คุณพบเครื่องรางเก่าๆ ตกอยู่ มันยังคงอุ่นอยู่เล็กน้อย",
            "ทางเดินข้างหน้าถูกปกคลุมไปด้วยใยแมงมุมหนาทึบ",
            "กลิ่นโลหะไหม้ลอยมาเตะจมูกคุณ",
            "คุณเห็นดวงตานับสิบคู่กะพริบอยู่ในเงามืดเบื้องหน้า",
            "ความเงียบที่นี่มันดังจนคุณเจ็บหู",
            "กำแพงที่นี่ทำจากอิฐสีแดงเข้มที่ดูเหมือนก้อนเลือด",
            "คุณก้าวข้ามกองกระดูกที่แตกละเอียดจนเป็นผง",
            "มีเสียงเพลงกล่อมเด็กที่บิดเบี้ยวพัดมาตามลม",
            "อากาศเต็มไปด้วยละอองเกสรที่ส่องประกายระยิบระยับ",
            "คุณรู้สึกถึงพลังงานบางอย่างที่พยายามจะสื่อสารกับคุณ",
            "ความร้อนระอุพุ่งขึ้นมาจากรอยแตกบนพื้น",
            "นกฮูกตัวหนึ่งเกาะอยู่บนรูปปั้นหิน มันจ้องมองคุณไม่วางตา",
            "ทางข้างหน้าดูเหมือนจะเป็นเขาวงกตที่ไม่มีวันสิ้นสุด"
        )
        val selectedDesc = descriptions[random.nextInt(descriptions.size)]

        val biome = currentBiome
        tvStory.text = """
╔══════════════════════════════╗
║       🗺️ Floor $floor Map      ║
╚══════════════════════════════╝
ห้อง: $roomsCompleted/$roomsPerFloor (อีก ${roomsPerFloor - roomsCompleted} ห้องขึ้นชั้นใหม่)
${biome.emoji} นิเวศน์: ${biome.name}
⚠️ ผลกระทบ: ${biome.effects}

คุณยืนอยู่ที่ทางแยกของดันเจี้ยน...
$selectedDesc

ทางซ้าย:   ${roomIcons[0]}
ทางตรงไป: ${roomIcons[1]}
ทางขวา:   ${roomIcons[2]}

คุณจะมุ่งหน้าไปทางไหน?
        """.trimIndent()

        setChoices(
            "⬅️ ไปทางซ้าย",
            "⬆️ ตรงไป",
            "➡️ ไปทางขวา",
            "📊 ตัวละคร",
            { executeRoom(nextRooms[0]) },
            { executeRoom(nextRooms[1]) },
            { executeRoom(nextRooms[2]) },
            { showCharacterSheet() }
        )
    }

    private fun completeRoom() {
        roomsCompleted++

        // Apply biome room-transition effect
        val biome = currentBiome
        when {
            biome.roomEntryDamage > 0 -> {
                hp = maxOf(1, hp - biome.roomEntryDamage)
                Toast.makeText(this, "${biome.emoji} ${biome.name}: เสีย ${biome.roomEntryDamage} HP!", Toast.LENGTH_SHORT).show()
            }
            biome.roomEntryHeal > 0 -> {
                hp = minOf(maxHp, hp + biome.roomEntryHeal)
                Toast.makeText(this, "${biome.emoji} ${biome.name}: ฟื้นฟู ${biome.roomEntryHeal} HP!", Toast.LENGTH_SHORT).show()
            }
        }

        if (roomsCompleted >= roomsPerFloor) {
            roomsCompleted = 0
            floor++
            checkAchievements()
            val newBiome = currentBiome
            Toast.makeText(this, "🏰 ขึ้นชั้น $floor — ${newBiome.emoji} ${newBiome.name}", Toast.LENGTH_LONG).show()
        }
        updateStats()
        enterFloor()
    }

    private fun executeRoom(type: String) {
        val random = Random(currentSeed + floor + System.currentTimeMillis())
        val actualType = if (type == "hidden") {
            val roll = random.nextInt(100)
            when {
                roll < 30 -> "combat"
                roll < 60 -> "treasure"
                roll < 80 -> "special"
                roll < 90 -> "merchant"
                else -> "normal"
            }
        } else type

        when (actualType) {
            "normal" -> normalRoom(random)
            "combat" -> combatRoom()
            "treasure" -> treasureRoom(random)
            "merchant" -> merchantRoom()
            "special" -> specialRoom(random)
            "gamble" -> gambleRoom(random)
            "arena" -> arenaRoom(random)
            "npc" -> wanderingNpcRoom(random)
        }
    }
    
    private fun normalRoom(random: Random, searched: Boolean = false, rested: Boolean = false) {
        if (!searched && !rested) {
            tvStory.text = """
╔══════════════════════════════╗
║       🏰 Floor $floor         ║
╚══════════════════════════════╝

ห้องธรรมดา

คุณเข้าสู่ห้องที่ค่อนข้างสงบ
ไม่มีศัตรูหรืออันตราย

มีบันไดลงไปชั้นถัดไป
และโคมไฟเล็กๆ ส่องแสงอยู่

คุณต้องการทำอะไร?
            """.trimIndent()
        }
        
        setChoices(
            "⚔️ ลงบันได",
            "🎒 ตรวจสอบตัวเอง",
            "💾 บันทึกเกม",
            "",
            { completeRoom() },
            { showCharacterSheet() },
            {
                saveGame()
                Toast.makeText(this, "💾 บันทึกแล้ว", Toast.LENGTH_SHORT).show()
            },
            {}
        )
    }
    
    private fun combatRoom() {
        val random = Random(currentSeed + floor + System.currentTimeMillis())
        
        // Determine number of enemies — max 4
        val enemyCount = when {
            floor % 10 == 0 -> 1   // boss floor: 1 boss
            floor < 3  -> 1
            floor < 8  -> random.nextInt(1, 3)   // 1-2
            floor < 15 -> random.nextInt(1, 4)   // 1-3
            else       -> random.nextInt(2, 5)   // 2-4
        }

        val enemies = mutableListOf<Enemy>()
        if (floor % 10 == 0) {
            enemies.add(EnemyGenerator.generateBossEnemy(floor, currentSeed + floor, difficulty))
        } else {
            repeat(enemyCount) { i ->
                enemies.add(EnemyGenerator.generateEnemy(floor, currentSeed + floor + i, difficulty))
            }
        }
        
        // Create player data for combat - Calculate final stats with equipment and skill bonuses
        var bonusHp = 0
        var bonusAtk = 0
        var bonusDef = 0
        var bonusLuck = 0
        
        listOf(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
            if (item != null) {
                bonusHp += item.getStat("hp")
                bonusAtk += item.getStat("atk")
                bonusDef += item.getStat("def")
                bonusLuck += item.getStat("luck")
            }
        }

        val skillBonusAtk = calculateSkillBonus("atk_bonus")
        val skillBonusDef = calculateSkillBonus("def_bonus")
        val skillBonusHp = calculateSkillBonus("hp_bonus") + calculateSkillBonus("max_hp")
        val skillBonusLuck = calculateSkillBonus("luck_bonus")
        val combatBiome = currentBiome
        val finalMaxHp = maxHp + bonusHp + skillBonusHp
        val finalAtk = (atk + bonusAtk + skillBonusAtk + combatBiome.combatAtkBonus).coerceAtLeast(1)
        val finalDef = (def + bonusDef + skillBonusDef + combatBiome.combatDefBonus).coerceAtLeast(0)
        val finalLuck = luck + bonusLuck + skillBonusLuck

        val player = Player(
            name = playerName,
            classData = playerClass,
            playerRace = playerRace,
            level = level,
            hp = hp,
            maxHp = finalMaxHp,
            atk = finalAtk,
            def = finalDef,
            luck = finalLuck,
            speed = 5 + finalLuck,
            soulPoints = soulPoints + calculateSkillBonus("max_soul"),
            unlockedSkills = unlockedSkills
        )
        
        // Pass inventory items to combat player
        val combatInventory = inventory.map { mainItem ->
            val combatType = when (mainItem.type) {
                ItemType.WEAPON -> ItemType.WEAPON
                ItemType.ARMOR -> ItemType.ARMOR
                ItemType.CONSUMABLE -> ItemType.CONSUMABLE
                else -> ItemType.SPECIAL
            }
            
            Item(
                id = mainItem.id,
                name = mainItem.name,
                emoji = mainItem.emoji,
                type = combatType,
                description = mainItem.description,
                rarity = mainItem.rarity,
                stats = mainItem.stats,
                consumable = mainItem.consumable,
                stackable = mainItem.stackable,
                quantity = mainItem.quantity,
                weaponType = mainItem.weaponType
            )
        }

        val aiHint = { ai: EnemyAIType -> when (ai) {
            EnemyAIType.AGGRESSIVE -> "🔴 รุก"
            EnemyAIType.BERSERKER  -> "🔥 คลั่ง"
            EnemyAIType.TACTICAL   -> "🔵 วางแผน"
            EnemyAIType.SUPPORT    -> "💚 สนับสนุน"
            EnemyAIType.PACK       -> "🐺 ฝูง"
            EnemyAIType.NORMAL     -> "⚪ ปกติ"
        }}

        val enemyText = if (enemies.size > 1) {
            val listing = enemies.joinToString("\n") { "  • ${it.emoji} ${it.name} [${aiHint(it.aiType)}]" }
            "กลุ่มศัตรู ${enemies.size} ตัว!\n$listing"
        } else {
            "${enemies[0].emoji} ${enemies[0].name} [${aiHint(enemies[0].aiType)}]"
        }

        tvStory.text = """
            |╔══════════════════════════════╗
            |║       ⚔️ การต่อสู้เริ่มขึ้น!     ║
            |╚══════════════════════════════╝
            |
            |คุณเผชิญหน้ากับ $enemyText
            |
            |${if (enemies.size == 1) enemies[0].description else "พวกมันดูหิวโหยและพร้อมจะจู่โจมคุณ!"}
        """.trimMargin()

        setChoices(
            "⚔️ เข้าสู่การต่อสู้",
            "🏃 พยายามหนี (Luck Check)",
            "",
            "",
            { startCombatSystem(player, enemies, combatInventory) },
            {
                if (Random.nextInt(100) < (luck * 5)) {
                    Toast.makeText(this, "หนีพ้นอย่างหวุดหวิด!", Toast.LENGTH_SHORT).show()
                    completeRoom()
                } else {
                    Toast.makeText(this, "หนีไม่พ้น! ต้องสู้!", Toast.LENGTH_SHORT).show()
                    startCombatSystem(player, enemies, combatInventory)
                }
            },
            {}, {}
        )
    }

    private fun startCombatSystem(player: Player, enemies: List<Enemy>, combatInventory: List<Item>) {
        // Start Combat System
        val combatManager = CombatManager(
            activity = this,
            player = player,
            enemies = enemies,
            initialInventory = combatInventory.toMutableList(),
            onCombatEnd = { victory, rewards ->
                // Restore main game layout
                setContentView(rootLayout)
                
                // Sync back stats
                this.hp = player.hp
                this.soulPoints = player.soulPoints
                
                // Sync back inventory (consumables used)
                combatInventory.forEach { combatItem ->
                    val index = inventory.indexOfFirst { it.id == combatItem.id }
                    if (index != -1) {
                        inventory[index] = inventory[index].copy(quantity = combatItem.quantity)
                    }
                }
                inventory.removeAll { it.quantity <= 0 }

                if (victory && rewards != null) {
                    this.enemiesKilled += enemies.size
                    if (enemies.any { it.isBoss }) bossesKilled++
                    if (arenaFightActive) { unlockAchievement("arena_win"); arenaFightActive = false }
                    val victorBiome = currentBiome
                    this.exp += (rewards.exp * victorBiome.expMultiplier).toInt()
                    var finalGold = (rewards.gold * victorBiome.goldMultiplier).toInt()
                    val goldBonusPct = calculateSkillBonus("gold_bonus")
                    if (goldBonusPct > 0) finalGold += finalGold * goldBonusPct / 100
                    var goldMult = 1.0
                    unlockedSkills.forEach { sid ->
                        val sk = SkillTreeData.getSkillById(playerClass, sid)
                        if (sk?.isPassive == true) {
                            val m = sk.effects["gold_multiplier"]
                            if (m is Double) goldMult *= m
                        }
                    }
                    this.gold += (finalGold * goldMult).toInt()
                    this.soulPoints += rewards.soulPoints
                    
                    // Add new items from rewards
                    rewards.items.forEach { 
                        inventory.add(it)
                        checkStoryUnlock(it)
                    }
                    
                    checkLevelUp()
                    updateStats()
                    checkAchievements()
                    showCombatVictory(rewards)
                } else if (!victory) {
                    if (this.hp <= 0) gameOver() else continueAdventure()
                }
            }
        )
        
        combatManager.startCombat()
    }
    private fun checkLevelUp() {
        while (exp >= expToNext) {
            level++
            exp -= expToNext
            expToNext = (expToNext * 1.5).toInt()
            maxHp += 15
            hp = maxHp
            atk += 3
            def += 2
            skillPoints += 1
            Toast.makeText(this, "🎊 LEVEL UP! Lv.$level\nได้รับ 1 Skill Point!", Toast.LENGTH_SHORT).show()
            checkAchievements()
        }
    }

    private fun showCombatVictory(rewards: CombatRewards) {
        tvStory.text = """
            ╔══════════════════════════════╗
            ║           VICTORY            ║
            ╚══════════════════════════════╝

            ศัตรูถูกกำจัดแล้ว!
            
            รางวัลที่ได้รับ:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            EXP: +${rewards.exp}
            Gold: +${rewards.gold}
            Soul: +${rewards.soulPoints}
            
            ไอเทมที่ได้รับ: ${if(rewards.items.isEmpty()) "-" else rewards.items.joinToString { it.name }}
        """.trimIndent()
        
        setChoices(
            "ไปต่อ", "กระเป๋า", "บันทึกเกม", "",
            { completeRoom() },
            { showInventory() },
            { saveGame(); Toast.makeText(this, "บันทึกเกมเรียบร้อย", Toast.LENGTH_SHORT).show() },
            {}
        )
    }
    
    private fun treasureRoom(random: Random) {
        val goldFound = random.nextInt(20, 50) + (floor * 5)
        val soulFound = if (random.nextFloat() < 0.3f) random.nextInt(1, 5) else 0
        
        // Randomly decide if there's an item
        val foundItem = if (random.nextFloat() < 0.4f) {
            generateRandomItem(random)
        } else null

        tvStory.text = """
            |╔══════════════════════════════╗
            |║       💎 Floor $floor         ║
            |║       TREASURE ROOM          ║
            |╚══════════════════════════════╝
            |
            |คุณพบหีบสมบัติเก่าแก่ที่ถูกทิ้งไว้!
            |
            |เมื่อเปิดออก คุณพบ:
            |━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            |💰 Gold: +$goldFound
            |${if (soulFound > 0) "✨ Soul: +$soulFound\n" else ""}${if (foundItem != null) "🎁 Item: ${foundItem.emoji} ${foundItem.name}\n" else ""}
            |${if (foundItem != null) "Description: ${foundItem.description}" else "กล่องดูเหมือนจะว่างเปล่าหลังจากเก็บเงินหมดแล้ว"}
        """.trimMargin()

        gold += goldFound
        soulPoints += soulFound
        
        if (foundItem != null) {
            setChoices(
                "✨ เก็บไอเทม",
                "❌ ทิ้งไป",
                "⬇️ ลงชั้นต่อไป",
                "",
                {
                    if (inventory.size < maxInventorySize) {
                        inventory.add(foundItem)
                        checkStoryUnlock(foundItem)
                        Toast.makeText(this, "เก็บ ${foundItem.name} แล้ว", Toast.LENGTH_SHORT).show()
                        checkAchievements()
                    } else {
                        Toast.makeText(this, "กระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
                    }
                    completeRoom()
                },
                {
                    Toast.makeText(this, "ทิ้ง ${foundItem.name}", Toast.LENGTH_SHORT).show()
                    completeRoom()
                },
                {
                    completeRoom()
                },
                {}
            )
        } else {
            setChoices(
                "⬇️ ลงชั้นต่อไป",
                "",
                "",
                "",
                {
                    completeRoom()
                },
                {}, {}, {}
            )
        }
    }

    private fun generateRandomItem(random: Random): Item {
        val types = listOf("weapon", "armor", "consumable")
        val type = types[random.nextInt(types.size)]
        
        return when (type) {
            "weapon" -> {
                val weaponTypes = listOf(
                    Triple("sword", "Sword", "⚔️"),
                    Triple("axe", "Axe", "🪓"),
                    Triple("spear", "Spear", "🔱"),
                    Triple("dagger", "Dagger", "🗡️"),
                    Triple("staff", "Staff", "🧙"),
                    Triple("bow", "Bow", "🏹"),
                    Triple("mace", "Mace", "🔨"),
                    Triple("grimoire", "Grimoire", "📖"),
                    Triple("scythe", "Scythe", "☠️"),
                    Triple("shield", "Shield", "🛡️")
                )
                val prefixes = listOf("Old", "Rusty", "Sharp", "Fine", "Balanced")
                val (wType, wName, wEmoji) = weaponTypes.random(random)
                val fullName = "${prefixes.random(random)} $wName"
                
                Item(
                    id = "w_${System.currentTimeMillis()}_${random.nextInt()}",
                    name = fullName,
                    emoji = wEmoji,
                    description = "อาวุธ $wName เพิ่มพลังโจมตี",
                    type = ItemType.WEAPON,
                    rarity = ItemRarity.COMMON,
                    stats = mapOf("atk" to random.nextInt(2, 6)),
                    weaponType = wType
                )
            }
            "armor" -> Item(
                id = "a_${System.currentTimeMillis()}_${random.nextInt()}",
                name = listOf("Leather Tunic", "Old Shield", "Cloth Hood").random(random),
                emoji = "🛡️",
                description = "เครื่องป้องกันพื้นฐาน",
                type = ItemType.ARMOR,
                rarity = ItemRarity.COMMON,
                stats = mapOf("def" to random.nextInt(1, 4))
            )
            else -> Item(
                id = "c_${System.currentTimeMillis()}_${random.nextInt()}",
                name = "Small HP Potion",
                emoji = "🧪",
                description = "ฟื้นฟู HP 15 หน่วย",
                type = ItemType.CONSUMABLE,
                rarity = ItemRarity.COMMON,
                stats = mapOf("hpRestore" to 15),
                consumable = true
            )
        }
    }

    private fun merchantRoom() {
        val random = Random(currentSeed + floor)
        val item1 = generateRandomItem(random)
        val item2 = generateRandomItem(random)
        val cost1 = 30 + (floor * 5)
        val cost2 = 50 + (floor * 10)

        tvStory.text = """
            |╔══════════════════════════════╗
            |║       🏪 Merchant            ║
            |╚══════════════════════════════╝
            |
            |"ยินดีต้อนรับนักเดินทาง... สนใจอะไรไหม?"
            |
            |สินค้าวันนี้:
            |1. ${item1.emoji} ${item1.name} ($cost1 Gold)
            |2. ${item2.emoji} ${item2.name} ($cost2 Gold)
            |3. 🍞 Bread (+10 HP) (15 Gold)
            |
            |คุณมี Gold: $gold
        """.trimMargin()

        setChoices(
            "🛒 ซื้อ ${item1.name}",
            "🛒 ซื้อ ${item2.name}",
            "🍞 ซื้อ Bread",
            "🏃 เดินต่อ",
            {
                if (gold >= cost1 && inventory.size < maxInventorySize) {
                    gold -= cost1
                    inventory.add(item1)
                    updateStats()
                    merchantRoom()
                } else Toast.makeText(this, "ทองไม่พอหรือกระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
            },
            {
                if (gold >= cost2 && inventory.size < maxInventorySize) {
                    gold -= cost2
                    inventory.add(item2)
                    updateStats()
                    merchantRoom()
                } else Toast.makeText(this, "ทองไม่พอหรือกระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
            },
            {
                if (gold >= 15) {
                    gold -= 15
                    hp = min(maxHp, hp + 10)
                    updateStats()
                    merchantRoom()
                } else Toast.makeText(this, "ทองไม่พอ!", Toast.LENGTH_SHORT).show()
            },
            { completeRoom() }
        )
    }

    private fun specialRoom(random: Random) {
        val eventType = random.nextInt(15) // 0-14
        
        when (eventType) {
            0, 1 -> { // Combat Trap - Now triggers a real fight
                val randomTrap = Random(currentSeed + floor + System.currentTimeMillis())
                val enemyCount = when {
                    floor < 10 -> randomTrap.nextInt(1, 3)
                    floor < 20 -> randomTrap.nextInt(2, 4)
                    else -> randomTrap.nextInt(2, 5)
                }
                
                val enemies = mutableListOf<Enemy>()
                repeat(enemyCount) { i ->
                    enemies.add(EnemyGenerator.generateEnemy(floor, currentSeed + floor + i + 777, difficulty))
                }

                tvStory.text = """
                    |╔══════════════════════════════╗
                    |║       ⚠️ กับดักซุ่มโจมตี!      ║
                    |╚══════════════════════════════╝
                    |
                    |ขณะที่คุณกำลังสำรวจ...
                    |คุณเผลอเหยียบสวิตช์บนพื้น!
                    |
                    |กรงเหล็กตกลงมาปิดทางออก 
                    |และมีเสียงฝีเท้าจำนวนมากดังมาจากเงามืด...
                    |
                    |ศัตรู ${enemies.size} ตัว กระโจนเข้าใส่คุณ!
                """.trimMargin()
                
                setChoices(
                    "⚔️ เตรียมตัวสู้",
                    "",
                    "",
                    "",
                    { 
                        // Directly start combat system for trap ambush
                        // Re-using player and inventory prep logic from combatRoom would be better
                        // but for simplicity we can trigger a variant of combatRoom or extract the prep.
                        // Let's call a modified version or just use combatRoom with pre-generated enemies.
                        startCombatFromTrap(enemies)
                    },
                    {}, {}, {}
                )
            }
            2 -> { // Fountain of Life
                tvStory.text = """
                    |╔══════════════════════════════╗
                    |║       ⛲ น้ำพุแห่งชีวิต        ║
                    |╚══════════════════════════════╝
                    |
                    |คุณพบน้ำพุใสสะอาดที่ส่องประกายแสงสีฟ้า
                    |น้ำในน้ำพุนี้ดูมีพลังลึกลับบางอย่าง
                """.trimMargin()
                
                setChoices(
                    "💧 ดื่มน้ำ (ใช้โชค)",
                    "✨ ชำระล้าง (+15 Soul)",
                    "🚶 เดินผ่านไป",
                    "",
                    {
                        val successChance = 0.5 + (luck * 0.01)
                        if (Random.nextDouble() < successChance) {
                            val heal = (maxHp * 0.4).toInt()
                            hp = min(maxHp, hp + heal)
                            Toast.makeText(this, "โชคดี! ฟื้นฟู $heal HP", Toast.LENGTH_SHORT).show()
                        } else {
                            val heal = (maxHp * 0.15).toInt()
                            hp = min(maxHp, hp + heal)
                            Toast.makeText(this, "รสชาติแปลกๆ... ฟื้นฟูเพียง $heal HP", Toast.LENGTH_SHORT).show()
                        }
                        updateStats()
                        completeRoom()
                    },
                    {
                        soulPoints += 15
                        updateStats()
                        Toast.makeText(this, "ได้รับ 15 Soul Points", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    { completeRoom() },
                    {}
                )
            }
            3 -> { // Cursed Altar
                tvStory.text = """
                    |╔══════════════════════════════╗
                    |║       🗿 แท่นบูชาต้องสาป      ║
                    |╚══════════════════════════════╝
                    |
                    |แท่นหินเก่าแก่ที่มีกลิ่นอายแห่งความมืด
                    |มันเรียกร้องการสังเวยเพื่อแลกกับพลัง
                """.trimMargin()
                
                setChoices(
                    "🩸 สังเวยเลือด (ใช้ความรู้)",
                    "💎 สังเวยทอง (+10 Luck)",
                    "🚶 เมินเฉย",
                    "",
                    {
                        // Intelligence check (based on Class if no INT stat)
                        val isSmartClass = playerClass == CharacterClass.MAGE || playerClass == CharacterClass.NECROMANCER || playerClass == CharacterClass.DRUID
                        val successChance = if (isSmartClass) 0.8 else 0.4
                        
                        if (Random.nextDouble() < successChance) {
                            atk += 8
                            hp -= 10
                            Toast.makeText(this, "คุณประกอบพิธีอย่างถูกต้อง! (+8 ATK, -10 HP)", Toast.LENGTH_SHORT).show()
                        } else {
                            atk += 3
                            hp -= 25
                            Toast.makeText(this, "พิธีกรรมผิดพลาด... (+3 ATK, -25 HP)", Toast.LENGTH_SHORT).show()
                        }
                        hp = hp.coerceAtLeast(1)
                        updateStats()
                        completeRoom()
                    },
                    {
                        if (gold >= 50) {
                            gold -= 50
                            luck += 10
                            updateStats()
                            Toast.makeText(this, "โชคชะตาเปลี่ยนไป...", Toast.LENGTH_SHORT).show()
                            completeRoom()
                        } else Toast.makeText(this, "ทองไม่พอ!", Toast.LENGTH_SHORT).show()
                    },
                    { completeRoom() },
                    {}
                )
            }
            4 -> { // Mysterious Statue
                tvStory.text = """
                    |╔══════════════════════════════╗
                    |║       🗽 รูปปั้นปริศนา        ║
                    |╚══════════════════════════════╝
                    |
                    |รูปปั้นนักรบในชุดเกราะยืนตระหง่าน
                    |ที่ฐานมีช่องขนาดพอดีกับอาวุธของคุณ
                """.trimMargin()
                
                setChoices(
                    "⚔️ ถวายอาวุธ (อัปเกรดอาวุธ)",
                    "🙏 สวดอ้อนวอน (+5 DEF)",
                    "🏃 เดินต่อ",
                    "",
                    {
                        if (equippedWeapon != null) {
                            val weapon = equippedWeapon!!
                            val currentAtk = weapon.stats["atk"] ?: 0
                            val newStats = weapon.stats.toMutableMap().apply {
                                put("atk", currentAtk + 3)
                            }
                            equippedWeapon = weapon.copy(stats = newStats)
                            updateStats()
                            Toast.makeText(this, "อาวุธทรงพลังขึ้น!", Toast.LENGTH_SHORT).show()
                            completeRoom()
                        } else Toast.makeText(this, "คุณไม่มีอาวุธที่ติดตั้งอยู่!", Toast.LENGTH_SHORT).show()
                    },
                    {
                        def += 5
                        updateStats()
                        Toast.makeText(this, "ความรู้สึกปลอดภัยเพิ่มขึ้น", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    { completeRoom() },
                    {}
                )
            }
            5 -> { // Wishing Well
                tvStory.text = """
                    |╔══════════════════════════════╗
                    |║       ⛲ Wishing Well        ║
                    |╚══════════════════════════════╝
                    |
                    |บ่อน้ำที่ดูเหมือนจะมีความขลังอยู่
                    |คุณเห็นแสงสะท้อนจากก้นบ่อ...
                """.trimMargin()
                
                setChoices(
                    "🪙 โยน 5 Gold (ขอพร)",
                    "🪙 โยน 20 Gold (ขอพรชุดใหญ่)",
                    "🏃 เดินผ่านไป",
                    "",
                    {
                        if (gold >= 5) {
                            gold -= 5
                            if (random.nextFloat() < 0.5f) {
                                hp = min(maxHp, hp + 15)
                                Toast.makeText(this, "รู้สึกสดชื่นขึ้น! (+15 HP)", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "น้ำนิ่งสนิท... ไม่มีอะไรเกิดขึ้น", Toast.LENGTH_SHORT).show()
                            }
                            completeRoom()
                        } else Toast.makeText(this, "ทองไม่พอ!", Toast.LENGTH_SHORT).show()
                    },
                    {
                        if (gold >= 20) {
                            gold -= 20
                            val luckBonus = random.nextInt(1, 4)
                            luck += luckBonus
                            Toast.makeText(this, "คุณรู้สึกโชคดีขึ้น! (+ $luckBonus Luck)", Toast.LENGTH_SHORT).show()
                            completeRoom()
                        } else Toast.makeText(this, "ทองไม่พอ!", Toast.LENGTH_SHORT).show()
                    },
                    { completeRoom() },
                    {}
                )
            }
            6 -> { // Hidden Library
                tvStory.text = """
                    |╔══════════════════════════════╗
                    |║       📚 Hidden Library      ║
                    |╚══════════════════════════════╝
                    |
                    |ห้องที่เต็มไปด้วยหนังสือเก่าแก่
                    |คุณอาจได้เรียนรู้อะไรบางอย่างที่นี่
                """.trimMargin()
                
                setChoices(
                    "📖 อ่านหนังสือเวทย์ (+Soul)",
                    "📖 อ่านบันทึกการรบ (+DEF)",
                    "🏃 ออกจากห้อง",
                    "",
                    {
                        soulPoints += 5
                        Toast.makeText(this, "ได้รับ Soul Points +5!", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    {
                        def += 1
                        Toast.makeText(this, "พลังป้องกันเพิ่มขึ้น!", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    { completeRoom() },
                    {}
                )
            }
            8 -> { // Riddle Room
                val riddles = listOf(
                    Triple("มีหัวและหาง แต่ไม่มีลำตัว\nทุกคนต้องการฉัน\nฉันคืออะไร?",
                        listOf("งู", "เหรียญ", "กุญแจ", "กระดูก"), 1),
                    Triple("ยิ่งเพิ่มมาก ยิ่งมีน้อยลง\nคืออะไร?",
                        listOf("น้ำ", "เวลา", "รู (ช่องว่าง)", "ทอง"), 2),
                    Triple("ฉันพูดได้ แต่ไม่มีปาก\nฉันได้ยินได้ แต่ไม่มีหู\nฉันไม่มีร่างกาย แต่มีชีวิต\nฉันคืออะไร?",
                        listOf("ลม", "เสียงสะท้อน", "วิญญาณ", "เงา"), 1),
                    Triple("ฉันตายได้แต่ไม่มีชีวิต\nตีไม่ได้แต่มีหัว\nฉันคืออะไร?",
                        listOf("ตะปู", "เหรียญ", "หินก้อนกลม", "ใบมีด"), 0),
                    Triple("ฉันเดินทางรอบโลก\nแต่อยู่กับที่ตลอดเวลา\nฉันคืออะไร?",
                        listOf("ความฝัน", "เวลา", "แสงอาทิตย์", "แสตมป์บนซองจดหมาย"), 3)
                )
                val (question, options, correctIdx) = riddles[random.nextInt(riddles.size)]
                tvStory.text = """
╔══════════════════════════════╗
║       ❓ ห้องปริศนา          ║
╚════════════════════════���═════╝

วิญญาณโบราณปรากฏขึ้นขวางทาง...

"ตอบคำถามของข้าให้ถูก
 แล้วข้าจะให้รางวัลแก่เจ้า
 ผิดพลาด... เจ็บปวดจะตามมา!"

❓ $question

เลือกคำตอบของคุณ:
                """.trimIndent()
                setChoices(
                    "ก) ${options[0]}",
                    "ข) ${options[1]}",
                    "ค) ${options[2]}",
                    "ง) ${options[3]}",
                    {
                        if (correctIdx == 0) { atk += 5; def += 5; unlockAchievement("riddle_solver"); Toast.makeText(this, "✅ ถูกต้อง! ATK+5 DEF+5", Toast.LENGTH_LONG).show() }
                        else { val dmg = 15 + floor * 2; hp = maxOf(1, hp - dmg); Toast.makeText(this, "❌ ผิด! เสีย $dmg HP", Toast.LENGTH_SHORT).show() }
                        completeRoom()
                    },
                    {
                        if (correctIdx == 1) { atk += 5; def += 5; unlockAchievement("riddle_solver"); Toast.makeText(this, "✅ ถูกต้อง! ATK+5 DEF+5", Toast.LENGTH_LONG).show() }
                        else { val dmg = 15 + floor * 2; hp = maxOf(1, hp - dmg); Toast.makeText(this, "❌ ผิด! เสีย $dmg HP", Toast.LENGTH_SHORT).show() }
                        completeRoom()
                    },
                    {
                        if (correctIdx == 2) { atk += 5; def += 5; unlockAchievement("riddle_solver"); Toast.makeText(this, "✅ ถูกต้อง! ATK+5 DEF+5", Toast.LENGTH_LONG).show() }
                        else { val dmg = 15 + floor * 2; hp = maxOf(1, hp - dmg); Toast.makeText(this, "❌ ผิด! เสีย $dmg HP", Toast.LENGTH_SHORT).show() }
                        completeRoom()
                    },
                    {
                        if (correctIdx == 3) { atk += 5; def += 5; unlockAchievement("riddle_solver"); Toast.makeText(this, "✅ ถูกต้อง! ATK+5 DEF+5", Toast.LENGTH_LONG).show() }
                        else { val dmg = 15 + floor * 2; hp = maxOf(1, hp - dmg); Toast.makeText(this, "❌ ผิด! เสีย $dmg HP", Toast.LENGTH_SHORT).show() }
                        completeRoom()
                    }
                )
            }
            9 -> { // Training Room
                val classTraining = when (playerClass) {
                    CharacterClass.WARRIOR, CharacterClass.BERSERKER, CharacterClass.PALADIN ->
                        Triple("⚔️ ฝึกท่าทางจากปรมาจารย์นักรบ", "atk", 8)
                    CharacterClass.MAGE, CharacterClass.NECROMANCER, CharacterClass.DRUID ->
                        Triple("🔮 ทำสมาธิเสริมพลังเวทย์", "soul", 25)
                    CharacterClass.ROGUE, CharacterClass.RANGER, CharacterClass.SAMURAI ->
                        Triple("🎯 ฝึกความแม่นยำในที่มืด", "luck", 8)
                    else -> Triple("🌀 ฝึกสมดุลทุกด้าน", "def", 6)
                }
                tvStory.text = """
╔════════════════════════��═════╗
║       ⚔️ ห้องฝึกซ้อม         ║
╚══════════════════════════════╝

ห้องที่เต็มไปด้วยหุ่นฝึกและอุปกรณ์เก่าแก่
บรรยากาศรู้สึกปลอดภัยและอบอุ่นผิดปกติ

ป้ายข้อความ: "สถานที่นี้ถูกอุทิศให้กับการฝึก
 เฉพาะผู้เต็มใจพัฒนาตนเองเท่านั้น"

เลือกรูปแบบการฝึก:
                """.trimIndent()
                setChoices(
                    classTraining.first,
                    "🛡️ ฝึกรับแรงกระแทก (+DEF permanent)",
                    "❤️ ฝึกความอดทน (+Max HP)",
                    "🏃 ออกจากห้องฝึก",
                    {
                        when (classTraining.second) {
                            "atk" -> { atk += classTraining.third; Toast.makeText(this, "💪 ATK +${classTraining.third}!", Toast.LENGTH_SHORT).show() }
                            "soul" -> { soulPoints += classTraining.third; Toast.makeText(this, "✨ Soul +${classTraining.third}!", Toast.LENGTH_SHORT).show() }
                            "luck" -> { luck += classTraining.third; Toast.makeText(this, "🍀 LUCK +${classTraining.third}!", Toast.LENGTH_SHORT).show() }
                            else -> { def += classTraining.third; Toast.makeText(this, "🛡️ DEF +${classTraining.third}!", Toast.LENGTH_SHORT).show() }
                        }
                        completeRoom()
                    },
                    { def += 6; updateStats(); Toast.makeText(this, "🛡️ DEF +6 จากการฝึก!", Toast.LENGTH_SHORT).show(); completeRoom() },
                    { maxHp += 15; hp = min(hp + 15, maxHp); updateStats(); Toast.makeText(this, "❤️ Max HP +15!", Toast.LENGTH_SHORT).show(); completeRoom() },
                    { completeRoom() }
                )
            }
            10 -> { // Elemental Shrine
                val (elemName, elemEmoji, buffDesc, buffAction) = when (playerClass) {
                    CharacterClass.MAGE, CharacterClass.NECROMANCER -> listOf("ไฟ", "🔥", "ATK +10, Soul +20", "fire")
                    CharacterClass.PALADIN, CharacterClass.CLERIC -> listOf("แสง", "✨", "DEF +8, HP +30", "light")
                    CharacterClass.RANGER, CharacterClass.ROGUE -> listOf("ลม", "💨", "LUCK +10, ATK +5", "wind")
                    CharacterClass.DRUID, CharacterClass.SHAMAN -> listOf("ธรรมชาติ", "🌿", "HP regen +20, DEF +5", "nature")
                    else -> listOf("โลหะ", "⚙️", "ATK +8, DEF +8", "metal")
                }
                tvStory.text = """
╔════════════════════��═════════╗
║       $elemEmoji ศาลเจ้าธาตุ$elemName        ║
╚═══════════════════════════���══╝

แท่นหินที่เปล่งแสงธาตุ$elemName สว่างไสว
มันสั่นสะเทือนเมื่อคุณเข้าใกล้

"เจ้าผู้นั้น... สายเลือดธาตุ$elemName
 ศาลเจ้านี้รับรู้ถึงพลังของเจ้า"

$elemEmoji บัฟ: $buffDesc
                """.trimIndent()
                setChoices(
                    "$elemEmoji รับพรธาตุ$elemName",
                    "🙏 สวดอ้อนวอน (Soul +15)",
                    "💰 บริจาคทอง 30G (Luck +8)",
                    "🚶 เดินต่อ",
                    {
                        when (buffAction) {
                            "fire" -> { atk += 10; soulPoints += 20 }
                            "light" -> { def += 8; hp = min(maxHp, hp + 30) }
                            "wind" -> { luck += 10; atk += 5 }
                            "nature" -> { maxHp += 20; hp = min(hp + 20, maxHp); def += 5 }
                            else -> { atk += 8; def += 8 }
                        }
                        updateStats()
                        Toast.makeText(this, "$elemEmoji ได้รับพรจากธาตุ$elemName!", Toast.LENGTH_LONG).show()
                        completeRoom()
                    },
                    { soulPoints += 15; updateStats(); Toast.makeText(this, "✨ Soul +15", Toast.LENGTH_SHORT).show(); completeRoom() },
                    {
                        if (gold >= 30) { gold -= 30; luck += 8; updateStats(); Toast.makeText(this, "🍀 LUCK +8!", Toast.LENGTH_SHORT).show(); completeRoom() }
                        else Toast.makeText(this, "ทองไม่พอ!", Toast.LENGTH_SHORT).show()
                    },
                    { completeRoom() }
                )
            }
            11 -> { // Cursed Chest
                val cursedItem = ItemGenerator.generateEquipment(floor + 2, null, random)
                val cursedItemDisplay = "${cursedItem.rarity.emoji} ${cursedItem.emoji} ${cursedItem.name}"
                tvStory.text = """
╔══════════════════════════════╗
║       💀 หีบต้องสาป           ║
╚══════════════════════════════╝

หีบสีดำเรืองแสงสีม่วงลอยอยู่กลางห้อง
โซ่ทองแดงพันรอบ เต็มไปด้วยตัวอักษรโบราณ

คุณรู้สึกถึงพลังมืดเรียกหา...
แต่ข้างในดูเหมือนจะมีของดี!

ไอเทมที่มองเห็น: $cursedItemDisplay

⚠️ "เปิดแล้วอาจมีผลเสียตามมา..."
                """.trimIndent()
                setChoices(
                    "🔓 เปิดหีบ (รับรางวัล + คำสาป)",
                    "💪 บังคับเปิดด้วยแรง (Luck Check)",
                    "🔥 เผาหีบทิ้ง (Soul +20)",
                    "🚶 เดินออกไป",
                    {
                        if (inventory.size < maxInventorySize) inventory.add(cursedItem)
                        val cursePenalty = listOf(
                            { atk -= 3; "⚔️ ATK -3 (คำสาปอ่อนแอ)" },
                            { def -= 3; "🛡️ DEF -3 (คำสาปเปราะบาง)" },
                            { luck -= 3; "🍀 LUCK -3 (คำสาปโชคร้าย)" },
                            { hp -= maxHp / 5; hp = maxOf(1, hp); "❤️ HP -${maxHp/5} (คำสาปดูดชีพ)" }
                        ).random(random)
                        val curseMsg = cursePenalty()
                        updateStats()
                        Toast.makeText(this, "รับ ${cursedItem.name}!\n$curseMsg", Toast.LENGTH_LONG).show()
                        completeRoom()
                    },
                    {
                        val luckCheck = random.nextInt(100) < luck * 3
                        if (luckCheck) {
                            if (inventory.size < maxInventorySize) inventory.add(cursedItem)
                            updateStats()
                            Toast.makeText(this, "🍀 โชคดี! เปิดได้โดยไม่มีคำสาป!", Toast.LENGTH_LONG).show()
                        } else {
                            val dmg = maxHp / 4
                            hp = maxOf(1, hp - dmg)
                            updateStats()
                            Toast.makeText(this, "💥 หีบระเบิดใส่หน้า! -$dmg HP", Toast.LENGTH_SHORT).show()
                        }
                        completeRoom()
                    },
                    { soulPoints += 20; updateStats(); Toast.makeText(this, "🔥 เผาทิ้ง! Soul +20", Toast.LENGTH_SHORT).show(); completeRoom() },
                    { completeRoom() }
                )
            }
            12 -> { // Chaos Room
                val chaosEvents = listOf(
                    { gold += 200; "💰 ทองทะลักมาจากรอยแตก! +200 Gold" },
                    { hp = maxHp; "❤️ แสงขาวฮีลคุณเต็ม HP!" },
                    { atk += 15; "⚔️ คุณรู้สึกแกร่งขึ้นมาก! ATK +15" },
                    { def += 15; "🛡️ ผิวหนังแข็งกว่าเดิม! DEF +15" },
                    { luck += 20; "🍀 โชคชะตาเปลี่ยน! LUCK +20" },
                    { soulPoints += 50; "✨ วิญญาณหลั่งไหล! Soul +50" },
                    { val bonusItem = ItemGenerator.generateEquipment(floor, ItemRarity.RARE, random); if (inventory.size < maxInventorySize) inventory.add(bonusItem); "🎁 ไอเทม Rare ตกลงมา! ${bonusItem.name}" },
                    { val dmgPct = 30; hp = maxOf(1, hp - maxHp * dmgPct / 100); "💀 ระเบิดพลังงาน! -${maxHp * dmgPct / 100} HP" },
                    { atk -= 5; def -= 5; "😈 คำสาปมืด! ATK-5 DEF-5 (ชั่วคราว)" },
                    { gold = maxOf(0, gold - 50); "💸 ปีศาจล้วงกระเป๋า! -50 Gold" },
                    { level++; skillPoints++; checkLevelUp(); "🎊 พลังงานปริศนา LEVEL UP บังคับ! +1 SP" },
                    { maxHp += 20; hp = min(maxHp, hp + 20); "❤️ พลังชีวิตเพิ่มถาวร! Max HP +20" }
                )
                tvStory.text = """
╔════════════════════���═════════╗
║       🌀 ห้องแห่งความวุ่นวาย  ║
╚════════════════════════════���═╝

ห้องนี้ไม่มีทางออก...
ไม่มีทางเข้า...
คุณไม่รู้ว่ามาถึงที่นี่ได้อย่างไร

พลังงานบ้าคลั่งวนเวียนรอบตัวคุณ
อะไรบางอย่างกำลังจะเกิดขึ้น!

🎲 ผลลัพธ์: สุ่มสมบูรณ์แบบ
(ดีหรือร้าย ไม่มีใครรู้)
                """.trimIndent()
                setChoices(
                    "🌀 ยอมรับพลังงานปริศนา",
                    "🛡️ ต้านทาน (ลดผลร้าย 50%)",
                    "🙏 อธิษฐาน (ใช้ Luck)",
                    "",
                    {
                        val event = chaosEvents.random(random)
                        val msg = event()
                        updateStats()
                        Toast.makeText(this, "🌀 $msg", Toast.LENGTH_LONG).show()
                        completeRoom()
                    },
                    {
                        // ลดผลร้าย: ถ้าเป็นลบ ลดครึ่ง ถ้าเป็นบวกก็ยังได้
                        val roll = random.nextInt(chaosEvents.size)
                        if (roll >= 7) { // bad events
                            val safeMsg = "🛡️ ต้านทานผลร้ายสำเร็จ! ไม่เป็นไร"
                            Toast.makeText(this, safeMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            val msg = chaosEvents[roll]()
                            updateStats()
                            Toast.makeText(this, "🌀 $msg", Toast.LENGTH_LONG).show()
                        }
                        completeRoom()
                    },
                    {
                        // Luck check: โอกาสได้ผลดี
                        val luckRoll = random.nextInt(100)
                        val goodEvents = chaosEvents.take(7)
                        val badEvents = chaosEvents.drop(7)
                        val msg = if (luckRoll < luck * 4) {
                            val e = goodEvents.random(random); e()
                        } else {
                            val e = badEvents.random(random); e()
                        }
                        updateStats()
                        Toast.makeText(this, "🙏 $msg", Toast.LENGTH_LONG).show()
                        completeRoom()
                    },
                    { completeRoom() }
                )
            }
            13 -> { // Black Market
                val rareItem1 = ItemGenerator.generateEquipment(floor + 3, ItemRarity.EPIC, random)
                val rareItem2 = ItemGenerator.generateEquipment(floor + 3, ItemRarity.RARE, random)
                val rareConsume = ItemGenerator.generateConsumable(null, random)
                val price1 = 150 + floor * 15
                val price2 = 80 + floor * 10
                val price3 = 60 + floor * 5
                tvStory.text = """
╔══════════════════════════════╗
║       🖤 ตลาดมืด              ║
╚══════════════════════════════╝

ประตูลับเปิดออกสู่ห้องเล็กที่ไม่ควรมีอยู่
ชายสวมหน้ากากยืนอยู่หลังโต๊ะ

"สวัสดี... อย่าบอกใครว่าเจอฉัน
 ฉันมีของที่หายากมาให้ เฉพาะคนรู้จัก"

💰 ทองของคุณ: $gold

🛒 สินค้าพิเศษ:
${rareItem1.rarity.emoji} ${rareItem1.emoji} ${rareItem1.name} — $price1 G
${rareItem2.rarity.emoji} ${rareItem2.emoji} ${rareItem2.name} — $price2 G
${rareConsume.emoji} ${rareConsume.name} — $price3 G
                """.trimIndent()
                setChoices(
                    "💰 ซื้อ ${rareItem1.name} (${price1}G)",
                    "💰 ซื้อ ${rareItem2.name} (${price2}G)",
                    "🧪 ซื้อ ${rareConsume.name} (${price3}G)",
                    "🚶 ออกจากตลาดมืด",
                    {
                        if (gold >= price1 && inventory.size < maxInventorySize) {
                            gold -= price1; inventory.add(rareItem1); updateStats()
                            Toast.makeText(this, "ซื้อ ${rareItem1.name} แล้ว!", Toast.LENGTH_SHORT).show()
                            completeRoom()
                        } else Toast.makeText(this, if (gold < price1) "ทองไม่พอ!" else "กระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
                    },
                    {
                        if (gold >= price2 && inventory.size < maxInventorySize) {
                            gold -= price2; inventory.add(rareItem2); updateStats()
                            Toast.makeText(this, "ซื้อ ${rareItem2.name} แล้ว!", Toast.LENGTH_SHORT).show()
                            completeRoom()
                        } else Toast.makeText(this, if (gold < price2) "ทองไม่พอ!" else "กระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
                    },
                    {
                        if (gold >= price3 && inventory.size < maxInventorySize) {
                            gold -= price3; inventory.add(rareConsume); updateStats()
                            Toast.makeText(this, "ซื้อ ${rareConsume.name} แล้ว!", Toast.LENGTH_SHORT).show()
                            completeRoom()
                        } else Toast.makeText(this, if (gold < price3) "ทองไม่พอ!" else "กระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
                    },
                    { completeRoom() }
                )
            }
            14 -> { // Soul Shrine
                tvStory.text = """
╔══════════════════════════════╗
║       💠 ศาลวิญญาณ            ║
╚══════════════════════════════╝

หินลึกลับสีน้ำเงินเรืองแสงตั้งตระหง่าน
เสียงกระซิบจากอีกมิติดังขึ้น...

"วิญญาณของเจ้า... มันมีค่ามาก
 แลกเปลี่ยนกับข้าได้
 ข้าจะมอบพลังที่เจ้าต้องการ"

✨ Soul ของคุณ: $soulPoints

⚠️ Soul Point จะถูกใช้ไปถาวร
                """.trimIndent()
                setChoices(
                    "✨ แลก 30 Soul → ATK+15 DEF+10",
                    "✨ แลก 50 Soul → Level UP!",
                    "✨ แลก 20 Soul → Max HP +40",
                    "🚶 ปฏิเสธ",
                    {
                        if (soulPoints >= 30) {
                            soulPoints -= 30; atk += 15; def += 10; updateStats()
                            Toast.makeText(this, "💠 ATK+15 DEF+10! (-30 Soul)", Toast.LENGTH_LONG).show()
                            completeRoom()
                        } else Toast.makeText(this, "Soul ไม่พอ! (ต้องการ 30)", Toast.LENGTH_SHORT).show()
                    },
                    {
                        if (soulPoints >= 50) {
                            soulPoints -= 50
                            level++; exp = 0; expToNext = (expToNext * 1.5).toInt()
                            maxHp += 15; hp = maxHp; atk += 3; def += 2; skillPoints += 1
                            updateStats()
                            Toast.makeText(this, "🎊 LEVEL UP! Lv.$level! (-50 Soul) +1 SP", Toast.LENGTH_LONG).show()
                            completeRoom()
                        } else Toast.makeText(this, "Soul ไม่พอ! (ต้องการ 50)", Toast.LENGTH_SHORT).show()
                    },
                    {
                        if (soulPoints >= 20) {
                            soulPoints -= 20; maxHp += 40; hp = min(hp + 40, maxHp); updateStats()
                            Toast.makeText(this, "❤️ Max HP +40! (-20 Soul)", Toast.LENGTH_LONG).show()
                            completeRoom()
                        } else Toast.makeText(this, "Soul ไม่พอ! (ต้องการ 20)", Toast.LENGTH_SHORT).show()
                    },
                    { completeRoom() }
                )
            }
            else -> { // Standard Trap Room (was 7, now covers any remaining)
                val isRogue = playerClass == CharacterClass.ROGUE
                val isRanger = playerClass == CharacterClass.RANGER
                
                tvStory.text = """
                    |╔══════════════════════════════╗
                    |║       ⚠️ ห้องกับดัก           ║
                    |╚══════════════════════════════╝
                    |
                    |คุณเดินเข้ามาในทางเดินที่ดูเงียบผิดปกติ...
                    |ทันใดนั้น กลไกกับดักลูกธนูอาบยาพิษก็ทำงาน!
                    |
                    |${if (isRogue) "✨ ด้วยสัญชาตญาณของโจร คุณสังเกตเห็นกลไกก่อน!" else ""}
                    |${if (isRanger) "🏹 ในฐานะนักล่า คุณคุ้นเคยกับกับดักประเภทนี้ดี" else ""}
                """.trimMargin()

                if (isRogue || isRanger) {
                    setChoices(
                        "🤸 หลบหลีก (โอกาส 100%)",
                        "🛠️ ปลดชนวน (+10 Soul)",
                        "🏃 วิ่งผ่านไป",
                        "",
                        {
                            Toast.makeText(this, "คุณหลบกับดักได้อย่างง่ายดาย!", Toast.LENGTH_SHORT).show()
                            completeRoom()
                        },
                        {
                            soulPoints += 10
                            updateStats()
                            Toast.makeText(this, "ปลดชนวนสำเร็จ! ได้รับ Soul", Toast.LENGTH_SHORT).show()
                            completeRoom()
                        },
                        { completeRoom() },
                        {}
                    )
                } else {
                    val damage = 10 + (floor * 2)
                    setChoices(
                        "🛡️ พยายามป้องกัน (-$damage HP)",
                        "🎲 เสี่ยงดวงหลบหลีก (Luck Check)",
                        "🏃 วิ่งฝ่าไป (-${damage/2} HP)",
                        "",
                        {
                            hp -= damage
                            updateStats()
                            Toast.makeText(this, "คุณป้องกันได้บางส่วน แต่ยังเจ็บอยู่", Toast.LENGTH_SHORT).show()
                            if (hp <= 0) gameOver() else { completeRoom() }
                        },
                        {
                            if (random.nextInt(100) < luck * 2) {
                                Toast.makeText(this, "โชคดีมาก! คุณหลบได้หวุดหวิด", Toast.LENGTH_SHORT).show()
                                completeRoom()
                            } else {
                                val trapDamage = damage + 10
                                hp -= trapDamage
                                updateStats()
                                Toast.makeText(this, "หลบไม่พ้น! โดนเต็มๆ (-$trapDamage HP)", Toast.LENGTH_SHORT).show()
                                if (hp <= 0) gameOver() else { completeRoom() }
                            }
                        },
                        {
                            hp -= damage / 2
                            updateStats()
                            Toast.makeText(this, "คุณวิ่งเร็วมากจนได้รับแผลเพียงเล็กน้อย", Toast.LENGTH_SHORT).show()
                            if (hp <= 0) gameOver() else { completeRoom() }
                        },
                        {}
                    )
                }
            }
        }
    }
    
    private fun startCombatFromTrap(enemies: List<Enemy>) {
        // Create player data for combat - Calculate final stats with equipment and skill bonuses
        var bonusHp = 0
        var bonusAtk = 0
        var bonusDef = 0
        var bonusLuck = 0
        
        listOf(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
            if (item != null) {
                bonusHp += item.getStat("hp")
                bonusAtk += item.getStat("atk")
                bonusDef += item.getStat("def")
                bonusLuck += item.getStat("luck")
            }
        }

        val skillBonusAtk = calculateSkillBonus("atk_bonus")
        val skillBonusDef = calculateSkillBonus("def_bonus")
        val skillBonusHp = calculateSkillBonus("hp_bonus") + calculateSkillBonus("max_hp")
        val skillBonusLuck = calculateSkillBonus("luck_bonus")

        val finalMaxHp = maxHp + bonusHp + skillBonusHp
        val finalAtk = atk + bonusAtk + skillBonusAtk
        val finalDef = def + bonusDef + skillBonusDef
        val finalLuck = luck + bonusLuck + skillBonusLuck

        val player = Player(
            name = playerName,
            classData = playerClass,
            playerRace = playerRace,
            level = level,
            hp = hp,
            maxHp = finalMaxHp,
            atk = finalAtk,
            def = finalDef,
            luck = finalLuck,
            speed = 5 + finalLuck,
            soulPoints = soulPoints + calculateSkillBonus("max_soul"),
            unlockedSkills = unlockedSkills
        )
        
        // Pass inventory items to combat player
        val combatInventory = inventory.map { mainItem ->
            val combatType = when (mainItem.type) {
                ItemType.WEAPON -> ItemType.WEAPON
                ItemType.ARMOR -> ItemType.ARMOR
                ItemType.CONSUMABLE -> ItemType.CONSUMABLE
                else -> ItemType.SPECIAL
            }
            
            Item(
                id = mainItem.id,
                name = mainItem.name,
                emoji = mainItem.emoji,
                type = combatType,
                description = mainItem.description,
                rarity = mainItem.rarity,
                stats = mainItem.stats,
                consumable = mainItem.consumable,
                stackable = mainItem.stackable,
                quantity = mainItem.quantity,
                weaponType = mainItem.weaponType
            )
        }

        startCombatSystem(player, enemies, combatInventory)
    }

    // ========== HELPER: BUILD COMBAT PLAYER & INVENTORY ==========
    private fun buildCombatPlayer(): Player {
        var bonusHp = 0; var bonusAtk = 0; var bonusDef = 0; var bonusLuck = 0
        listOf(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
            if (item != null) {
                bonusHp += item.getStat("hp"); bonusAtk += item.getStat("atk")
                bonusDef += item.getStat("def"); bonusLuck += item.getStat("luck")
            }
        }
        val skillBonusAtk = calculateSkillBonus("atk_bonus")
        val skillBonusDef = calculateSkillBonus("def_bonus")
        val skillBonusHp = calculateSkillBonus("hp_bonus") + calculateSkillBonus("max_hp")
        val skillBonusLuck = calculateSkillBonus("luck_bonus")
        return Player(
            name = playerName, classData = playerClass, playerRace = playerRace, level = level,
            hp = hp, maxHp = maxHp + bonusHp + skillBonusHp,
            atk = atk + bonusAtk + skillBonusAtk, def = def + bonusDef + skillBonusDef,
            luck = luck + bonusLuck + skillBonusLuck,
            speed = 5 + luck + bonusLuck + skillBonusLuck,
            soulPoints = soulPoints + calculateSkillBonus("max_soul"),
            unlockedSkills = unlockedSkills
        )
    }

    private fun buildCombatInventory(): MutableList<Item> {
        return inventory.map { mainItem ->
            Item(
                id = mainItem.id, name = mainItem.name, emoji = mainItem.emoji,
                type = when (mainItem.type) {
                    ItemType.WEAPON -> ItemType.WEAPON
                    ItemType.ARMOR -> ItemType.ARMOR
                    ItemType.CONSUMABLE -> ItemType.CONSUMABLE
                    else -> ItemType.SPECIAL
                },
                description = mainItem.description, rarity = mainItem.rarity, stats = mainItem.stats,
                consumable = mainItem.consumable, stackable = mainItem.stackable,
                quantity = mainItem.quantity, weaponType = mainItem.weaponType
            )
        }.toMutableList()
    }

    // ========== NEW ROOM 1: GAMBLE ROOM ==========
    private fun gambleRoom(random: Random) {
        tvStory.text = """
╔══════════════════════════════╗
║       🎲 ห้องนักพนัน         ║
╚═════════��════════════════════╝

ชายสวมหมวกสีแดงนั่งอยู่ข้างโต๊ะไม้เก่า
เขายิ้มและพยักหน้าชวนคุณ

"โชคชะตาล้วนเป็นเรื่องของความเสี่ยง
 ลองดูไหม นักเดินทาง?"

💰 ทองของคุณ: $gold
✨ Soul: $soulPoints
        """.trimIndent()

        setChoices(
            "🎲 เดิมพันเล็ก (30G)",
            "🎰 เดิมพันใหญ่ (100G)",
            "🃏 ไพ่ทำนายดวง (50G)",
            "🏃 ไม่สนใจ",
            {
                if (gold >= 30) {
                    gold -= 30
                    val roll = random.nextInt(100)
                    val msg = when {
                        roll < 10 -> { gold += 120; unlockAchievement("gambling_win"); "🎉 แจ็คพ็อต! ×4 รับ 120 Gold!" }
                        roll < 40 -> { gold += 60;  unlockAchievement("gambling_win"); "😄 ชนะ! รับ 60 Gold" }
                        roll < 65 -> { gold += 20;  unlockAchievement("gambling_win"); "😅 ชนะเล็กน้อย... รับ 20 Gold" }
                        else -> "😤 แพ้! เสีย 30 Gold"
                    }
                    updateStats(); checkAchievements(); Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    completeRoom()
                } else Toast.makeText(this, "ทองไม่พอ! (ต้องการ 30)", Toast.LENGTH_SHORT).show()
            },
            {
                if (gold >= 100) {
                    gold -= 100
                    val roll = random.nextInt(100)
                    val msg = when {
                        roll < 5  -> { gold += 500; "🤑 MEGA JACKPOT! ×5 รับ 500 Gold!!" }
                        roll < 25 -> { gold += 300; "🎊 ชนะใหญ่! รับ 300 Gold" }
                        roll < 50 -> { gold += 150; "🎉 ชนะ! รับ 150 Gold" }
                        roll < 70 -> { gold += 100; "😐 คืนทุน 100 Gold" }
                        else -> "💸 แพ้! เสีย 100 Gold"
                    }
                    updateStats(); Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    completeRoom()
                } else Toast.makeText(this, "ทองไม่พอ! (ต้องการ 100)", Toast.LENGTH_SHORT).show()
            },
            {
                if (gold >= 50) {
                    gold -= 50
                    val roll = random.nextInt(6)
                    val msg = when (roll) {
                        0 -> { atk += 8; "⚔️ ไพ่กล่าวว่า: ATK +8!" }
                        1 -> { def += 8; "🛡️ ไพ่กล่าวว่า: DEF +8!" }
                        2 -> { luck += 10; "🍀 ไพ่กล่าวว่า: LUCK +10!" }
                        3 -> { hp = min(maxHp, hp + maxHp / 2); "❤️ ไพ่กล่าวว่า: HP +${maxHp/2}!" }
                        4 -> { soulPoints += 40; "✨ ไพ่กล่าวว่า: Soul +40!" }
                        else -> { maxHp += 15; hp = min(hp + 15, maxHp); "❤️ ไพ่กล่าวว่า: Max HP +15!" }
                    }
                    updateStats(); Toast.makeText(this, "🃏 $msg", Toast.LENGTH_LONG).show()
                    completeRoom()
                } else Toast.makeText(this, "ทองไม่พอ! (ต้องการ 50)", Toast.LENGTH_SHORT).show()
            },
            { completeRoom() }
        )
    }

    // ========== NEW ROOM 2: ARENA ROOM ==========
    private fun arenaRoom(random: Random) {
        val baseEnemy = EnemyGenerator.generateEnemy(floor + 2, currentSeed + floor + 999L, difficulty)
        val eliteEnemy = baseEnemy.copy(
            name = "⭐ ${baseEnemy.name}",
            hp = baseEnemy.hp * 2,
            maxHp = baseEnemy.maxHp * 2,
            atk = (baseEnemy.atk * 1.5).toInt(),
            def = (baseEnemy.def * 1.5).toInt(),
            expReward = baseEnemy.expReward * 3,
            goldReward = baseEnemy.goldReward * 3,
            soulReward = baseEnemy.soulReward + 15
        )

        tvStory.text = """
╔══════════════════════════════╗
║       🏟️ ห้องประลอง Elite    ║
╚══════════════��═══════════════╝

คุณเดินเข้ามาในสนามวงกลมขนาดใหญ่
เสียงโห่ร้องดังก้องจากที่ไหนสักแห่ง

เงาร่างใหญ่โตก้าวออกมากลางเวที...

${eliteEnemy.emoji} ${eliteEnemy.name}
HP: ${eliteEnemy.maxHp} | ATK: ${eliteEnemy.atk} | DEF: ${eliteEnemy.def}

"เอาชนะมันได้... รางวัลคือ EXP ×3, Gold ×3!"
"แพ้... ก็แค่ตาย"

⚠️ ศัตรูนี้แข็งแกร่งกว่าปกติมาก!
        """.trimIndent()

        setChoices(
            "⚔️ ยอมรับการดวล!",
            "🏃 ปฏิเสธ (เดินออกไป)",
            "",
            "",
            {
                val player = buildCombatPlayer()
                val combatInventory = buildCombatInventory()
                arenaFightActive = true
                startCombatSystem(player, listOf(eliteEnemy), combatInventory)
            },
            { completeRoom() },
            {}, {}
        )
    }

    // ========== NEW ROOM 3: WANDERING NPC ==========
    // ========== NEW ROOM 3: WANDERING NPC ==========
    private fun wanderingNpcRoom(random: Random) {
        val npcIndex = random.nextInt(4)
        when (npcIndex) {
            0 -> { // นักเดินทางบาดเจ็บ
                tvStory.text = """
╔══════════════════════════════╗
║     🧑 นักเดินทางบาดเจ็บ     ║
╚══════════════════════════════╝

ชายชราล้มอยู่ข้างทาง
บาดแผลลึกปกคลุมแขนขา

"ช่วยด้วย... ฉันหลงทางและโดนโจมตี"

คุณจะทำอะไร?
                """.trimIndent()
                setChoices(
                    "💊 ให้ยา (-1 ยา, ได้รางวัล)",
                    "💰 ให้ทอง 20G (LUCK+8)",
                    "🚶 เดินผ่านไป",
                    "📊 ดูสถิติ",
                    {
                        val potion = inventory.firstOrNull { it.type == ItemType.CONSUMABLE && (it.stats["hpRestore"] ?: 0) > 0 }
                        if (potion != null) {
                            inventory.remove(potion); atk += 5; def += 5; luck += 5; updateStats()
                            unlockAchievement("npc_helper")
                            Toast.makeText(this, "🙏 ชายชราขอบคุณ! ATK+5 DEF+5 LUCK+5", Toast.LENGTH_LONG).show()
                        } else Toast.makeText(this, "คุณไม่มียาฟื้นฟู!", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    {
                        if (gold >= 20) { gold -= 20; luck += 8; updateStats(); unlockAchievement("npc_helper"); Toast.makeText(this, "💰 LUCK +8 รางวัลแห่งเมตตา!", Toast.LENGTH_LONG).show() }
                        else Toast.makeText(this, "ทองไม่พอ!", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    { Toast.makeText(this, "คุณเดินผ่านไป...", Toast.LENGTH_SHORT).show(); completeRoom() },
                    { showCharacterSheet() }
                )
            }
            1 -> { // พ่อมดโบราณ
                tvStory.text = """
╔══════════════════════════════╗
║       🧙 พ่อมดโบราณ          ║
╚══════════════════════════════╝

ชายชราสวมเสื้อคลุมดาวยืนอยู่กลางห้อง

"เยาวชน! แลกเปลี่ยนความรู้กับข้าไหม?
 ข้าอยู่ที่นี่มาหลายร้อยปีแล้ว..."

ทองของคุณ: ${"$"}gold | Soul: ${"$"}soulPoints
                """.trimIndent()
                setChoices(
                    "📚 เรียนรู้เวทย์ (30G → +1 SP)",
                    "🔮 ขอคำพยากรณ์ (20G)",
                    "✨ แบ่ง Soul (30 Soul → ATK+12)",
                    "🚶 ผ่านไป",
                    {
                        if (gold >= 30) { gold -= 30; skillPoints++; updateStats(); Toast.makeText(this, "📚 ได้ Skill Point จากพ่อมด!", Toast.LENGTH_LONG).show() }
                        else Toast.makeText(this, "ทองไม่พอ! (ต้องการ 30)", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    {
                        if (gold >= 20) {
                            gold -= 20
                            val prophecy = listOf("ห้องถัดไปจะมีศัตรูรอ เตรียมตัวให้ดี",
                                "โชคชะตาเชื่อมโยงกับแสงทอง", "ความตายรออยู่ข้างหน้า แต่ชัยชนะก็เช่นกัน",
                                "ทักษะจะทำให้เอาชนะบอสได้", "มีสมบัติซ่อนอยู่ใต้ชั้นที่ 10").random(random)
                            Toast.makeText(this, "🔮 \"${"$"}prophecy\"", Toast.LENGTH_LONG).show()
                        } else Toast.makeText(this, "ทองไม่พอ! (ต้องการ 20)", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    {
                        if (soulPoints >= 30) { soulPoints -= 30; atk += 12; updateStats(); Toast.makeText(this, "✨ พ่อมดรับ Soul! ATK+12!", Toast.LENGTH_LONG).show() }
                        else Toast.makeText(this, "Soul ไม่พอ! (ต้องการ 30)", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    { completeRoom() }
                )
            }
            2 -> { // พ่อค้าของเถื่อน
                val blackItem = ItemGenerator.generateEquipment(floor + 1, ItemRarity.RARE, random)
                val blackPotion = ItemGenerator.generateConsumable(null, random)
                tvStory.text = """
╔══════════════════════════════╗
║       🥷 พ่อค้าของเถื่อน     ║
╚══════════════════════════════╝

บุรุษลึกลับยืนอยู่หลังผ้าใบขาดๆ

"psst... มีของดีมาขาย ราคาพิเศษ!"

ทองของคุณ: ${"$"}gold
                """.trimIndent()
                setChoices(
                    "💎 Skill Point (80G)",
                    "🎁 ${"$"}{blackItem.name} (60G)",
                    "🧪 ${"$"}{blackPotion.name} (35G)",
                    "🚶 ไม่สนใจ",
                    {
                        if (gold >= 80) { gold -= 80; skillPoints++; updateStats(); Toast.makeText(this, "💎 ได้ Skill Point!", Toast.LENGTH_LONG).show() }
                        else Toast.makeText(this, "ทองไม่พอ! (80G)", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    {
                        if (gold >= 60 && inventory.size < maxInventorySize) { gold -= 60; inventory.add(blackItem); updateStats(); Toast.makeText(this, "🎁 ได้ ${"$"}{blackItem.name}!", Toast.LENGTH_SHORT).show() }
                        else Toast.makeText(this, if (gold < 60) "ทองไม่พอ!" else "กระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    {
                        if (gold >= 35 && inventory.size < maxInventorySize) { gold -= 35; inventory.add(blackPotion); updateStats(); Toast.makeText(this, "🧪 ได้ ${"$"}{blackPotion.name}!", Toast.LENGTH_SHORT).show() }
                        else Toast.makeText(this, if (gold < 35) "ทองไม่พอ!" else "กระเป๋าเต็ม!", Toast.LENGTH_SHORT).show()
                        completeRoom()
                    },
                    { completeRoom() }
                )
            }
            else -> { // วิญญาณนักรบแห่งอดีต
                tvStory.text = """
╔══════════════════════════════╗
║   👻 วิญญาณนักรบแห่งอดีต     ║
╚══════════════════════════════╝

วิญญาณในชุดเกราะเก่าปรากฏขึ้น...

"ข้าตายในดันเจี้ยนนี้ไปนานแล้ว
 ข้ายังต้องการส่งต่อความรู้
 ก่อนที่จะไปสู่สุคติ..."

เลือกสิ่งที่วิญญาณจะส่งต่อให้คุณ:
                """.trimIndent()
                setChoices(
                    "⚔️ รับพลัง (ATK+12)",
                    "🛡️ รับเทคนิค (DEF+12)",
                    "🍀 รับพรโชค (LUCK+12)",
                    "🚶 ปล่อยให้จากไป",
                    { atk += 12; updateStats(); Toast.makeText(this, "⚔️ ATK+12!", Toast.LENGTH_LONG).show(); completeRoom() },
                    { def += 12; updateStats(); Toast.makeText(this, "🛡️ DEF+12!", Toast.LENGTH_LONG).show(); completeRoom() },
                    { luck += 12; updateStats(); Toast.makeText(this, "🍀 LUCK+12!", Toast.LENGTH_LONG).show(); completeRoom() },
                    { luck += 3; updateStats(); Toast.makeText(this, "👻 วิญญาณจากไปในสงบ LUCK+3", Toast.LENGTH_LONG).show(); completeRoom() }
                )
            }
        }
    }

    private fun gameOver() {
        updatePlayTime() // อัปเดตเวลาเล่นปัจจุบันก่อนแสดงผล
        deathCount++
        
        tvStory.text = """
╔══════════════════════════════╗
║        💀 GAME OVER         ║
╚══════════════════════════════╝

$playerName ล้มลง...

คุณมาถึงชั้น $floor
ก่อนจะถูกความมืดกลืนกิน

📊 สถิติครั้งนี้:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🏰 Floor reached: $floor
⚔️ Level: $level
💀 Enemies killed: $enemiesKilled
💰 Gold collected: $gold
⏰ Time played: ${formatPlayTime(totalPlayTime)}

💭 "ใน Roguelike... ความตายคือครู"

ลองใหม่อีกครั้งไหม?
        """.trimIndent()
        
        clearSaveData()
        
        setChoices(
            "🔄 เล่นใหม่",
            "📊 ดูสถิติ",
            "🏠 เมนูหลัก",
            "",
            { showClassSelection() },
            { showStats() },
            { showMainMenu() },
            {}
        )
    }
    
    private fun showStats() {
        // Stats screen implementation
        Toast.makeText(this, "📊 Stats screen - Implementation in progress", Toast.LENGTH_SHORT).show()
    }
}
