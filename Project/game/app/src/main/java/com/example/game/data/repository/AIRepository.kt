package com.example.game.data.repository

import com.example.game.data.model.ChatMessage
import com.example.game.data.model.FantasyWorld
import com.example.game.data.model.PlayerStats
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = "AIzaSyDucvjc0TcA-tgOOG1O1RbOxt687EIm0jg"
    )

    suspend fun sendMessage(
        userText: String,
        world: FantasyWorld,
        history: List<ChatMessage>,
        player: PlayerStats,
        worldSummary: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val context = buildContext(userText, world, history, player, worldSummary)
            val response = model.generateContent(content { text(context) })
            response.text ?: "..."
        }
    }

    suspend fun summarizeHistory(
        history: List<ChatMessage>,
        world: FantasyWorld,
        existingSummary: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val chatText = history.takeLast(8).joinToString("\n") {
                val role = if (it.role == "user") "ผู้เล่น" else "เกมมาสเตอร์"
                "$role: ${it.content}"
            }
            val prompt = """
สรุปเหตุการณ์สำคัญในการผจญภัยใน ${world.name} อย่างกระชับ (ไม่เกิน 100 คำ):

สรุปเดิม: $existingSummary

บทสนทนาล่าสุด:
$chatText

กรุณาสรุปเป็นภาษาไทย เน้นเหตุการณ์สำคัญและพัฒนาการของตัวละคร
            """.trimIndent()
            val response = model.generateContent(content { text(prompt) })
            response.text ?: existingSummary
        }
    }

    private fun buildContext(
        userText: String,
        world: FantasyWorld,
        history: List<ChatMessage>,
        player: PlayerStats,
        worldSummary: String
    ): String {
        val recentHistory = history.takeLast(12).joinToString("\n") {
            val role = if (it.role == "user") "ผู้เล่น" else "เกมมาสเตอร์"
            "$role: ${it.content}"
        }
        return """
คุณคือเกมมาสเตอร์ของเกม ISEKAI RPG ในโลก: ${world.emoji} ${world.name}

🌍 ข้อมูลโลก:
${world.description}

⚔️ บทบาทผู้เล่น: ${world.playerRole}
✨ ระบบพิเศษ: ${world.specialFeatures}

📊 สถานะตัวละคร:
- STR: ${player.strength} | SOUL: ${player.soulPoints} | Gold: ${player.gold}
- Level: ${player.level} | HP: ${player.hp}/${player.maxHp}

📖 สรุปเรื่องราวที่ผ่านมา:
${worldSummary.ifBlank { "เริ่มต้นการผจญภัยใหม่" }}

💬 บทสนทนาล่าสุด:
$recentHistory

ผู้เล่น: $userText

---
คำแนะนำ: ตอบในฐานะเกมมาสเตอร์ ใช้ภาษาไทย ตอบ 100-250 คำ
สร้างเรื่องราวที่น่าตื่นเต้น มีตัวเลือกให้ผู้เล่น 2-3 ตัวเลือก
อ้างอิงสถานะตัวละครในการตอบ ถ้าเหมาะสม
        """.trimIndent()
    }
}
