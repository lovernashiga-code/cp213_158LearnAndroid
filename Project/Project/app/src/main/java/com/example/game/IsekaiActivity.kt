package com.example.game

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class IsekaiActivity : AppCompatActivity() {

    // ========== API Configuration ==========
    private val apiKey = "AIzaSyDucvjc0TcA-tgOOG1O1RbOxt687EIm0jg"

    // ========== UI Elements ==========
    private lateinit var chatContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var inputField: EditText
    private lateinit var btnSend: Button
    private lateinit var btnClear: Button
    private lateinit var btnSummary: Button
    private lateinit var tvWorldInfo: TextView
    private lateinit var tvMessageCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingOverlay: FrameLayout

    // ========== Game Data from MainActivity ==========
    private var soulPoints = 0
    private var gold = 0
    private var strength = 0

    // ========== Current World Data ==========
    private var currentWorld = ""
    private var worldDescription = ""
    private var playerRole = ""

    // ========== Memory Management ==========
    private val conversationHistory = mutableListOf<ChatMessage>()
    private val maxMessages = 20
    private val summaryInterval = 5
    private var messagesSinceLastSummary = 0
    private var worldSummary = ""

    // ========== File Management ==========
    private lateinit var summaryFile: File
    private lateinit var conversationFile: File
    private var selectionDialog: AlertDialog? = null

    data class ChatMessage(
        val role: String, // "user" or "ai"
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class FantasyWorld(
        val name: String,
        val emoji: String,
        val description: String,
        val startingStory: String,
        val playerRole: String,
        val specialFeatures: String
    )

    // ========== Fantasy Worlds Database ==========
    private val fantasyWorlds = listOf(
        FantasyWorld(
            name = "โลกมหาเวทย์แห่งอาร์คาเดีย",
            emoji = "🔮",
            description = "โลกที่เวทมนตร์ครองทุกสิ่ง มีสถาบันเวทย์มนตร์ขนาดใหญ่ มังกรโบราณ และดันเจี้ยนลึกลับ",
            startingStory = """คุณลืมตาขึ้นในห้องสมุดใหญ่โตแห่งสถาบันเวทมนตร์อาร์คาเดีย แสงแดดส่องผ่านหน้าต่างกระจกสี หนังสือเวทย์มนตร์นับพันเล่มลอยอยู่รอบตัว

เสียงหญิงสาวดังขึ้น: "ตื่นแล้วหรือ? คุณหมดสติไปตั้ง 3 วัน หลังจากพิธีเรียกวิญญาณ"

สาวผมเงินสวมเสื้อคลุมนักเวทย์ยืนอยู่ตรงหน้า เธอมองคุณด้วยสายตาประหลาดใจ

"แปลกจริง... พลังเวทย์ในตัวคุณไม่เหมือนใคร เหมือนมาจากโลกอื่น... คุณเป็นใครกันแน่?"

รอบๆ ตัวคุณมีแสงวิญญาณสีฟ้าลอยวนอยู่ นี่คือพลังที่คุณนำมาจากโลกเก่า...""",
            playerRole = "นักเวทย์มือใหม่จากต่างโลก (Otherworlder Mage)",
            specialFeatures = "ระบบเวทมนตร์ 7 ธาตุ, สัตว์วิเศษ, ดันเจี้ยน"
        ),
        FantasyWorld(
            name = "อาณาจักรดาบและเกียรติยศ",
            emoji = "⚔️",
            description = "โลกยุคกลางแฟนตาซี มีอัศวิน ปราสาท สงคราม และความขัดแย้งระหว่างอาณาจักร",
            startingStory = """เสียงระฆังดังก้อง คุณตื่นขึ้นมาบนเตียงนุ่มในปราสาทหิน ผ่านหน้าต่างเห็นทุ่งหญ้ากว้างใหญ่และเทือกเขาไกลลิบ

ประตูเปิด อัศวินชุดเกราะเงินเดินเข้ามา: "ท่านลอร์ดตื่นแล้วหรือ? เราพบท่านสลบอยู่ใกล้ชายแดน"

"พระราชาทรงเรียกท่าน... เมื่อคืนมีปรากฏการณ์แสงสว่างจากฟ้า ลงมาตรงจุดที่เราพบท่าน"

เขาส่งดาบบรอนซ์มาให้: "ท่านจะต้องพิสูจน์ตัวเองในสนามต่อสู้ นี่คือธรรมเนียมของอาณาจักรเอเทอร์เนีย"

นอกห้องได้ยินเสียงทหารฝึกซ้อม และกลิ่นอาหารเช้าจากห้องครัวลอยมา...""",
            playerRole = "อัศวินต่างโลกผู้ปริศนา (Mysterious Knight)",
            specialFeatures = "ระบบอัศวิน, สงครามอาณาจักร, ทัวร์นาเมนต์"
        ),
        FantasyWorld(
            name = "ดินแดนแห่งสัตว์ประหลาด",
            emoji = "🐉",
            description = "โลกที่มนุษย์อยู่ร่วมกับสัตว์วิเศษ มีระบบ Tamer และการผจญภัยในป่าลึก",
            startingStory = """คุณตื่นขึ้นในป่าใหญ่ เสียงนกร้องและลำธารไหลดังรอบข้าง แต่บางอย่างแปลก... มีสัตว์ประหลาดขนาดใหญ่เดินผ่าน

"คิวๆ!" เสียงเล็กๆ ดัง สิ่งมีชีวิตเล็กคล้ายจิ้งจกมีปีกกระโดดขึ้นบนไหล่คุณ ตาโตสีทองมองด้วยความอยากรู้

เด็กหญิงวิ่งเข้ามา: "นั่นไง! สไปรท์ของฉัน! เธอพาฉันมาที่นี่..."

เธอสะดุดหยุด: "เดี๋ยว... คุณไม่ใช่คนในหมู่บ้าน คุณคือ... คนที่สัตว์วิเศษทุกตัวรอคอย!"

รอบตัวคุณ สัตว์ประหลาดหลายตัวออกมาจากพุ่มไม้ พวกมันไม่ได้ดุร้าย แต่กลับมองด้วยความเคารพ

"คุณคือ Beast Master ในตำนานใช่มั้ย!?" เด็กหญิงถามด้วยดวงตาระยิบ...""",
            playerRole = "Beast Master แห่งตำนาน (Legendary Tamer)",
            specialFeatures = "ระบบฝึกสัตว์, วิวัฒนาการ, ดันเจี้ยนป่า"
        ),
        FantasyWorld(
            name = "เมืองลอยฟ้าแห่งสตีมพังค์",
            emoji = "⚙️",
            description = "โลกผสมผสานเวทย์และเทคโนโลยีไอน้ำ มีเมืองลอยฟ้า เครื่องจักรวิเศษ และกิลด์นักประดิษฐ์",
            startingStory = """เสียงหวีดของเครื่องจักรไอน้ำปลุกคุณ ลืมตาขึ้นเห็นท้องฟ้าเต็มไปด้วยเมืองลอยฟ้า เรือบินไอน้ำบินสวนกัน

เสียงชายแก่: "สติดีแล้วหรือ? พบเจ้าลอยอยู่กลางอากาศโดยไม่มีเครื่องบิน!"

ชายสวมแว่นตานักประดิษฐ์ถือประแจและเครื่องมือประหลาดๆ: "นี่... เจ้ามีพลังแปลกๆ ในตัว พอเจ้าสัมผัสเครื่องจักร มันทำงานดีขึ้น!"

เขาส่งนาฬิกาไอน้ำมาให้: "มาร่วมกิลด์นักประดิษฐ์สิ เมืองนีโอ-แอธีเนียนต้องการคนอย่างเจ้า"

นอกหน้าต่าง เห็นเมืองลอยฟ้าขนาดใหญ่ มีเกียร์ยักษ์หมุนอยู่ทุกหนทุกแห่ง และโกเล็มเหล็กเดินไปมา...""",
            playerRole = "นักประดิษฐ์ผู้ปริศนา (Mysterious Engineer)",
            specialFeatures = "ระบบคราฟท์, เครื่องจักรวิเศษ, เมืองลอยฟ้า"
        ),
        FantasyWorld(
            name = "ดินแดนของเทพเจ้าและมนุษย์",
            emoji = "⚡",
            description = "โลกที่เทพเจ้าและมนุษย์อยู่ร่วมกัน มีวิหารศักดิ์สิทธิ์ นักบวช และพลังศักดิ์สิทธิ์",
            startingStory = """แสงสีทองสว่างจ้า คุณตื่นขึ้นในวิหารขาวอันงดงาม รูปปั้นเทพเจ้าโอ่โถงทุกด้าน

เสียงนุ่มนวล: "ตื่นแล้วหรือ บุตรของโลกอื่น"

หญิงสาวสวมชุดนักบวชสีขาวยิ้มให้: "เทพธิดาแห่งแสงสว่างทรงเลือกท่าน ท่านถูกเรียกมาเพื่อช่วยโลกนี้"

เธอจับมือคุณ แสงศักดิ์สิทธิ์พุ่งออกมา: "น่าทึ่ง! ท่านมีพลังศักดิ์สิทธิ์บริสุทธิ์ ไม่เคยเห็นมาก่อน"

"ความมืดกำลังคืบคลานเข้ามา เทพเจ้าหลายองค์หายไป ท่านคือความหวังของเรา... คุณเต็มใจช่วยหรือไม่?"

ข้างนอก ได้ยินเสียงระฆังวิหารและบทสวดมนตร์ของพระ...""",
            playerRole = "ผู้ถูกเลือกโดยเทพเจ้า (Chosen One)",
            specialFeatures = "ระบบเวทมนตร์ศักดิ์สิทธิ์, พันธมิตรเทพเจ้า, ศาสนา"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // รับข้อมูลจาก MainActivity
        soulPoints = intent.getIntExtra("soul_points", 0)
        gold = intent.getIntExtra("gold", 0)
        strength = intent.getIntExtra("strength", 0)

        // สร้าง UI แบบ Dynamic
        createDynamicUI()

        // Setup ไฟล์จัดเก็บข้อมูล
        setupFiles()

        // แสดงหน้าเลือกโลก
        showWorldSelection()
    }

    // ========== CREATE DYNAMIC UI ==========
    private fun createDynamicUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1a1a2e"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // ========== HEADER ==========
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#16213e"))
            setPadding(24, 32, 24, 24)
        }

        // ชื่อเกม
        val titleText = TextView(this).apply {
            text = "🌟 ISEKAI ADVENTURE 🌟"
            textSize = 24f
            setTextColor(Color.parseColor("#ffd700"))
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        headerLayout.addView(titleText)

        // ข้อมูลโลก
        tvWorldInfo = TextView(this).apply {
            text = "กำลังโหลด..."
            textSize = 14f
            setTextColor(Color.parseColor("#00d4ff"))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 0)
        }
        headerLayout.addView(tvWorldInfo)

        // นับจำนวนข้อความ
        tvMessageCount = TextView(this).apply {
            text = "💬 ข้อความ: 0/20 | 📝 สรุปถัดไป: 5"
            textSize = 12f
            setTextColor(Color.parseColor("#95a5a6"))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        headerLayout.addView(tvMessageCount)

        mainLayout.addView(headerLayout)

        // ========== DIVIDER ==========
        val divider1 = View(this).apply {
            setBackgroundColor(Color.parseColor("#00d4ff"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                4
            )
        }
        mainLayout.addView(divider1)

        // ========== CHAT AREA ==========
        scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(16, 16, 16, 16)
        }

        chatContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        scrollView.addView(chatContainer)
        mainLayout.addView(scrollView)

        // ========== DIVIDER ==========
        val divider2 = View(this).apply {
            setBackgroundColor(Color.parseColor("#00d4ff"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2
            )
        }
        mainLayout.addView(divider2)

        // ========== BUTTON CONTROLS ==========
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#16213e"))
            setPadding(16, 12, 16, 12)
            gravity = Gravity.CENTER
        }

        btnClear = Button(this).apply {
            text = "🗑️ ล้าง"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#e74c3c"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 8
            }
        }
        buttonLayout.addView(btnClear)

        btnSummary = Button(this).apply {
            text = "📋 สรุป"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#3498db"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 8
            }
        }
        buttonLayout.addView(btnSummary)

        val btnWorld = Button(this).apply {
            text = "🌍 เปลี่ยนโลก"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#9b59b6"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnWorld.setOnClickListener { showWorldSelection() }
        buttonLayout.addView(btnWorld)

        mainLayout.addView(buttonLayout)

        // ========== INPUT AREA ==========
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#0f3460"))
            setPadding(16, 16, 16, 16)
        }

        // แก้ไขปัญหาพิมพ์ไทย: ใช้ช่องพิมพ์แบบพื้นฐานที่สุดเพื่อให้ระบบคีย์บอร์ดทำงานได้ปกติ
        inputField = EditText(this).apply {
            hint = "พิมพ์ข้อความของคุณ..."
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#95a5a6"))
            // ลบการตั้งค่า Background ที่เป็นสีทึบออกเพื่อให้ Android แสดง Cursor และ Keyboard ได้ถูกต้อง
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 12
            }
        }
        inputLayout.addView(inputField)

        btnSend = Button(this).apply {
            text = "📤"
            textSize = 20f
            setBackgroundColor(Color.parseColor("#00d4ff"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 16, 32, 16)
        }
        inputLayout.addView(btnSend)

        mainLayout.addView(inputLayout)

        // ========== LOADING OVERLAY ==========
        val rootLayout = FrameLayout(this)
        rootLayout.addView(mainLayout)

        loadingOverlay = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#CC000000"))
            visibility = View.GONE
        }

        val loadingContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }
        loadingContent.addView(progressBar)

        val loadingText = TextView(this).apply {
            text = "AI กำลังคิด..."
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 24, 0, 0)
        }
        loadingContent.addView(loadingText)

        loadingOverlay.addView(loadingContent, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))

        rootLayout.addView(loadingOverlay)

        setContentView(rootLayout)

        // ========== BUTTON LISTENERS ==========
        btnSend.setOnClickListener { sendMessage() }
        btnClear.setOnClickListener { clearChat() }
        btnSummary.setOnClickListener { showSummary() }
    }

    // ========== SETUP FILES ==========
    private fun setupFiles() {
        val directory = File(filesDir, "isekai_data")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        summaryFile = File(directory, "summary_$timestamp.txt")
        conversationFile = File(directory, "conversation_$timestamp.json")

        if (!summaryFile.exists()) {
            summaryFile.createNewFile()
            summaryFile.writeText("=== ISEKAI ADVENTURE SUMMARY ===\n\n")
        }
    }

    // ========== WORLD SELECTION ==========
    private fun showWorldSelection() {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.parseColor("#1a1a2e"))
        }

        val title = TextView(this).apply {
            text = "🌟 เลือกโลกแฟนตาซีของคุณ 🌟"
            textSize = 20f
            setTextColor(Color.parseColor("#ffd700"))
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 24)
        }
        dialogView.addView(title)

        val subtitle = TextView(this).apply {
            text = "💪 พลัง: $strength | ✨ Soul: $soulPoints | 💰 Gold: $gold"
            textSize = 14f
            setTextColor(Color.parseColor("#00d4ff"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        dialogView.addView(subtitle)

        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val worldContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        fantasyWorlds.forEach { world ->
            val worldCard = createWorldCard(world)
            worldContainer.addView(worldCard)
        }

        scrollView.addView(worldContainer)
        dialogView.addView(scrollView)

        selectionDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        selectionDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        selectionDialog?.show()
    }

    private fun createWorldCard(world: FantasyWorld): View {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#16213e"))
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        val worldName = TextView(this).apply {
            text = "${world.emoji} ${world.name}"
            textSize = 18f
            setTextColor(Color.parseColor("#ffd700"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        cardLayout.addView(worldName)

        val worldDesc = TextView(this).apply {
            text = world.description
            textSize = 14f
            setTextColor(Color.parseColor("#ecf0f1"))
            setPadding(0, 12, 0, 0)
        }
        cardLayout.addView(worldDesc)

        val roleText = TextView(this).apply {
            text = "🎭 บทบาท: ${world.playerRole}"
            textSize = 13f
            setTextColor(Color.parseColor("#00d4ff"))
            setPadding(0, 12, 0, 0)
        }
        cardLayout.addView(roleText)

        val featuresText = TextView(this).apply {
            text = "⚡ ฟีเจอร์: ${world.specialFeatures}"
            textSize = 13f
            setTextColor(Color.parseColor("#95a5a6"))
            setPadding(0, 8, 0, 0)
        }
        cardLayout.addView(featuresText)

        val selectButton = Button(this).apply {
            text = "✨ เลือกโลกนี้"
            textSize = 16f
            setBackgroundColor(Color.parseColor("#00d4ff"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }

        selectButton.setOnClickListener {
            selectWorld(world)
            selectionDialog?.dismiss()
        }

        cardLayout.addView(selectButton)
        return cardLayout
    }

    private fun selectWorld(world: FantasyWorld) {
        currentWorld = world.name
        worldDescription = world.description
        playerRole = world.playerRole

        // อัปเดต UI
        tvWorldInfo.text = "${world.emoji} ${world.name} | ${world.playerRole}"

        // เคลียร์ข้อมูลเก่า
        conversationHistory.clear()
        chatContainer.removeAllViews()
        messagesSinceLastSummary = 0
        worldSummary = ""

        // บันทึกข้อมูลโลกใหม่
        summaryFile.appendText("\n\n=== NEW WORLD SELECTED ===\n")
        summaryFile.appendText("World: ${world.name}\n")
        summaryFile.appendText("Role: ${world.playerRole}\n")
        summaryFile.appendText("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        summaryFile.appendText("Player Stats: STR:$strength | SOUL:$soulPoints | GOLD:$gold\n\n")

        // แสดงเรื่องเริ่มต้น
        addAIMessage(world.startingStory)

        Toast.makeText(this, "เข้าสู่ ${world.name} แล้ว!", Toast.LENGTH_LONG).show()
    }

    // ========== SEND MESSAGE ==========
    private fun sendMessage() {
        val userText = inputField.text.toString().trim()

        if (userText.isEmpty()) {
            Toast.makeText(this, "กรุณาพิมพ์ข้อความ", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentWorld.isEmpty()) {
            Toast.makeText(this, "กรุณาเลือกโลกก่อน", Toast.LENGTH_SHORT).show()
            return
        }

        // ตรวจสอบจำนวนข้อความ
        if (conversationHistory.size >= maxMessages) {
            Toast.makeText(this, "ถึงขีดจำกัดข้อความ กรุณารอสรุปอัตโนมัติ", Toast.LENGTH_SHORT).show()
            return
        }

        // แสดงข้อความผู้เล่น
        addUserMessage(userText)
        inputField.text.clear()

        // เพิ่มเข้า history
        conversationHistory.add(ChatMessage("user", userText))
        messagesSinceLastSummary++
        updateMessageCount()

        // ส่งไปยัง AI
        getAIResponse(userText)
    }

    // ========== GET AI RESPONSE ==========
    private fun getAIResponse(userMessage: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                // บังคับใช้โมเดล gemini-3-flash-preview ตามคำขอ
                val model = GenerativeModel(
                    modelName = "gemini-3-flash-preview",
                    apiKey = apiKey
                )

                // สร้าง Context สำหรับ AI
                val contextPrompt = buildContextPrompt(userMessage)

                val response = model.generateContent(content {
                    text(contextPrompt)
                })

                val aiText = response.text ?: "ขออภัย ไม่สามารถตอบกลับได้"

                // แสดงคำตอบ
                runOnUiThread {
                    addAIMessage(aiText)
                    conversationHistory.add(ChatMessage("ai", aiText))
                    messagesSinceLastSummary++
                    updateMessageCount()

                    // ตรวจสอบว่าต้องสรุปหรือไม่
                    if (messagesSinceLastSummary >= summaryInterval) {
                        performAutoSummary()
                    }

                    // บันทึกการสนทนา
                    saveConversation()
                    showLoading(false)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    addAIMessage("❌ เกิดข้อผิดพลาด: ${e.message}")
                    showLoading(false)
                }
            }
        }
    }

    // ========== BUILD CONTEXT PROMPT ==========
    private fun buildContextPrompt(userMessage: String): String {
        val sb = StringBuilder()

        // ข้อมูลโลก
        sb.append("=== ข้อมูลโลกแฟนตาซี ===\n")
        sb.append("โลก: $currentWorld\n")
        sb.append("คำอธิบาย: $worldDescription\n")
        sb.append("บทบาทผู้เล่น: $playerRole\n\n")

        // สถิติผู้เล่น
        sb.append("=== สถิติผู้เล่น ===\n")
        sb.append("พลังโจมตี: $strength\n")
        sb.append("Soul Points: $soulPoints\n")
        sb.append("ทองคำ: $gold\n\n")

        // สรุปเรื่องที่ผ่านมา
        if (worldSummary.isNotEmpty()) {
            sb.append("=== สรุปเรื่องที่ผ่านมา ===\n")
            sb.append(worldSummary)
            sb.append("\n\n")
        }

        // ประวัติการสนทนาล่าสุด (5-10 ข้อความล่าสุด)
        sb.append("=== การสนทนาล่าสุด ===\n")
        val recentMessages = conversationHistory.takeLast(10)
        recentMessages.forEach { msg ->
            val role = if (msg.role == "user") "ผู้เล่น" else "AI"
            sb.append("$role: ${msg.content}\n")
        }
        sb.append("\n")

        // คำสั่งสำหรับ AI
        sb.append("=== คำสั่งสำหรับ AI ===\n")
        sb.append("คุณคือ Game Master ของโลกแฟนตาซี '$currentWorld'\n")
        sb.append("ตอบกลับเป็นภาษาไทยเท่านั้น\n")
        sb.append("สร้างเนื้อเรื่องที่น่าสนใจและมีรายละเอียด\n")
        sb.append("ให้ผู้เล่นมีทางเลือกและการตัดสินใจ\n")
        sb.append("อ้างอิงถึงสถิติของผู้เล่นเมื่อจำเป็น\n")
        sb.append("สร้างบรรยากาศของโลกแฟนตาซีให้ชัดเจน\n")
        sb.append("ความยาวคำตอบ: 150-300 คำ\n\n")

        // ข้อความล่าสุดของผู้เล่น
        sb.append("=== ข้อความจากผู้เล่น ===\n")
        sb.append(userMessage)

        return sb.toString()
    }

    // ========== AUTO SUMMARY ==========
    private fun performAutoSummary() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                // บังคับใช้โมเดล gemini-3-flash-preview ตามคำขอ
                val model = GenerativeModel(
                    modelName = "gemini-3-flash-preview",
                    apiKey = apiKey
                )

                // สร้างข้อความสำหรับสรุป
                val summaryPrompt = buildSummaryPrompt()

                val response = model.generateContent(content {
                    text(summaryPrompt)
                })

                val summary = response.text ?: "ไม่สามารถสรุปได้"

                runOnUiThread {
                    // บันทึกสรุป
                    worldSummary += "\n[สรุป ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}]\n$summary\n"

                    // บันทึกลงไฟล์
                    summaryFile.appendText("=== AUTO SUMMARY (${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}) ===\n")
                    summaryFile.appendText(summary)
                    summaryFile.appendText("\n\n")

                    // ลบข้อความเก่า 5 ข้อความ
                    if (conversationHistory.size > 10) {
                        val toRemove = minOf(5, conversationHistory.size - 10)
                        repeat(toRemove) {
                            conversationHistory.removeAt(0)
                        }
                    }

                    messagesSinceLastSummary = 0
                    updateMessageCount()

                    Toast.makeText(this@IsekaiActivity, "✅ สรุปอัตโนมัติเสร็จสิ้น", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@IsekaiActivity, "❌ สรุปล้มเหลว: ${e.message}", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            }
        }
    }

    // ========== BUILD SUMMARY PROMPT ==========
    private fun buildSummaryPrompt(): String {
        val sb = StringBuilder()

        sb.append("สรุปเหตุการณ์ที่เกิดขึ้นจาก 5 ข้อความล่าสุดนี้อย่างกระชับ:\n\n")

        val last5 = conversationHistory.takeLast(5)
        last5.forEach { msg ->
            val role = if (msg.role == "user") "ผู้เล่น" else "AI"
            sb.append("$role: ${msg.content}\n\n")
        }

        sb.append("\nกรุณาสรุปเป็นภาษาไทย ความยาว 100-150 คำ\n")
        sb.append("เน้นเหตุการณ์สำคัญ ตัวละคร และการตัดสินใจของผู้เล่น")

        return sb.toString()
    }

    // ========== ADD MESSAGE TO UI ==========
    private fun addUserMessage(text: String) {
        val messageLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, 8, 0, 8)
        }

        val messageBubble = TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00d4ff"))
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 80
            }
        }

        messageLayout.addView(messageBubble)
        chatContainer.addView(messageLayout)
        scrollToBottom()
    }

    private fun addAIMessage(text: String) {
        val messageLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            setPadding(0, 8, 0, 8)
        }

        val messageBubble = TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#e74c3c"))
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 80
            }
        }

        messageLayout.addView(messageBubble)
        chatContainer.addView(messageLayout)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    // ========== UPDATE MESSAGE COUNT ==========
    private fun updateMessageCount() {
        val current = conversationHistory.size
        val untilSummary = summaryInterval - messagesSinceLastSummary
        tvMessageCount.text = "💬 ข้อความ: $current/$maxMessages | 📝 สรุปถัดไป: $untilSummary"
    }

    // ========== CLEAR CHAT ==========
    private fun clearChat() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ ยืนยันการล้างข้อมูล")
            .setMessage("คุณต้องการล้างการสนทนาทั้งหมดหรือไม่?\n(สรุปที่บันทึกไว้จะไม่หาย)")
            .setPositiveButton("ใช่") { _, _ ->
                conversationHistory.clear()
                chatContainer.removeAllViews()
                messagesSinceLastSummary = 0
                updateMessageCount()
                Toast.makeText(this, "ล้างข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    // ========== SHOW SUMMARY ==========
    private fun showSummary() {
        val summaryText = if (worldSummary.isEmpty()) {
            "ยังไม่มีการสรุปเรื่อง\n(จะสรุปอัตโนมัติทุกๆ 5 ข้อความ)"
        } else {
            worldSummary
        }

        val scrollView = ScrollView(this).apply {
            setPadding(32, 32, 32, 32)
        }

        val textView = TextView(this).apply {
            text = summaryText
            textSize = 16f
            setTextColor(Color.WHITE)
        }

        scrollView.addView(textView)

        AlertDialog.Builder(this)
            .setTitle("📋 สรุปเรื่องราว")
            .setView(scrollView)
            .setPositiveButton("ปิด", null)
            .show()
    }

    // ========== SAVE CONVERSATION ==========
    private fun saveConversation() {
        try {
            val jsonArray = JSONArray()
            conversationHistory.forEach { msg ->
                val jsonObj = JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                    put("timestamp", msg.timestamp)
                }
                jsonArray.put(jsonObj)
            }

            val mainObj = JSONObject().apply {
                put("world", currentWorld)
                put("playerRole", playerRole)
                put("summary", worldSummary)
                put("messages", jsonArray)
            }

            conversationFile.writeText(mainObj.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ========== SHOW/HIDE LOADING ==========
    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        btnSend.isEnabled = !show
        inputField.isEnabled = !show
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ ออกจากเกม?")
            .setMessage("ข้อมูลจะถูกบันทึกอัตโนมัติ")
            .setPositiveButton("ออก") { _, _ ->
                saveConversation()
                super.onBackPressed()
            }
            .setNegativeButton("อยู่ต่อ", null)
            .show()
    }
}