package com.example.lab.architecture

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * ตัวช่วยสำหรับจัดการ Coroutines ใน Unit Test
 * เพราะใน Unit Test จะไม่มี Main Thread (UI Thread) ของ Android 
 * จึงต้องใช้ TestDispatcher จำลองว่าทำงานแบบ Synchronous แทน
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        // เมื่อเริ่มรันเทสต์ ให้เปลี่ยน Dispatchers.Main เป็นตัวจำลอง
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        // เมื่อเทสต์รันเสร็จเรียบร้อย ให้คืนค่ากลับ
        Dispatchers.resetMain()
    }
}
