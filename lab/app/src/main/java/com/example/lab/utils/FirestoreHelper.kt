package com.example.lab.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Data Model ง่ายๆ สำหรับทดสอบ (เช่น สมุดเยี่ยมชม)
data class GuestMessage(
    val id: String = "",
    val name: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object FirestoreHelper {
    // เข้าถึงตัว Database
    private val db = FirebaseFirestore.getInstance()
    // private val db by lazy { FirebaseFirestore.getInstance() }
    // ชื่อ Collection (เปรียบเสมือน Folder เก็บเอกสาร หรือ Table ใน SQL)
    private const val COLLECTION_NAME = "guestbook"

    // 1. ฟังก์ชันเพิ่มข้อมูล (Add)
    fun addMessage(name: String, message: String, onSuccess: () -> Unit) {
        val newMessage = GuestMessage(
            name = name,
            message = message
        )

        // .add() จะสร้าง ID แบบสุ่มให้อัตโนมัติ
        db.collection(COLLECTION_NAME)
            .add(newMessage)
            .addOnSuccessListener {
                Log.d("Firestore", "เพิ่มข้อมูลสำเร็จ ID: ${it.id}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "พังยับ: $e")
            }
    }

    // 2. ฟังก์ชันดึงข้อมูลแบบ Realtime (Listen)
    // จุดขายของ Firebase คือพอดันข้อมูลขึ้น Cloud ปุ๊บ เครื่องอื่นเห็นปั๊บ
    fun listenToMessages(onUpdate: (List<GuestMessage>) -> Unit) {
        db.collection(COLLECTION_NAME)
            .orderBy("timestamp", Query.Direction.DESCENDING) // เรียงตามเวลาใหม่สุดขึ้นก่อน
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore", "ฟังข้อมูลล้มเหลว", error)
                    return@addSnapshotListener
                }

                val messages = ArrayList<GuestMessage>()
                for (doc in value!!) {
                    // แปลงข้อมูลจาก Firestore กลับเป็น Object ของเรา
                    val msg = doc.toObject(GuestMessage::class.java).copy(id = doc.id)
                    messages.add(msg)
                }
                // ส่งข้อมูลกลับไปที่ UI
                onUpdate(messages)
            }
    }

    // 3. ฟังก์ชันลบข้อมูล (Delete)
    fun deleteMessage(docId: String) {
        db.collection(COLLECTION_NAME).document(docId).delete()
    }
}