package com.example.lab.utils

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SharedPreferencesUtilTest {

    // ใช้ relaxed = true เพื่อให้ Mock คืนค่า default (เช่น "", 0, false) ให้เองหากไม่ได้ระบุ every
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockPrefs = mockk<SharedPreferences>(relaxed = true)
    private val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

    @Before
    fun setup() {
        // กำหนดพฤติกรรมพื้นฐาน
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        
        // สำหรับ Editor ต้องคืนค่าตัวมันเองเสมอเมื่อเรียก put... (Fluent API)
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.clear() } returns mockEditor

        // เริ่มต้นการใช้งานใหม่ทุกครั้งก่อนเริ่มแต่ละ Test case
        SharedPreferencesUtil.init(mockContext)
    }

    @Test
    fun saveString_isCalledCorrectly() {
        SharedPreferencesUtil.saveString("KEY_NAME", "Pikachu")

        // ยืนยันว่ามีการเรียก putString และ apply จริงๆ
        verify { mockEditor.putString("KEY_NAME", "Pikachu") }
        verify { mockEditor.apply() }
    }

    @Test
    fun getString_returnsCorrectValue() {
        // ระบุว่าถ้าเรียก getString ด้วย KEY_STARTER ไม่ว่าจะส่ง Default อะไรมา ให้คืน Charmander
        every { mockPrefs.getString("KEY_STARTER", any()) } returns "Charmander"

        val result = SharedPreferencesUtil.getString("KEY_STARTER", "")

        assertEquals("Charmander", result)
    }
    
    @Test
    fun saveInt_isCalledCorrectly() {
        SharedPreferencesUtil.saveInt("KEY_LEVEL", 99)
        verify { mockEditor.putInt("KEY_LEVEL", 99) }
        verify { mockEditor.apply() }
    }
}
