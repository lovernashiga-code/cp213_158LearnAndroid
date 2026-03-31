package com.example.lab.examples

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * ตัวอย่างการเขียน Unit Test พื้นฐานที่สุด
 * เป็นการเทสต์ตรรกะแบบธรรมดาไม่ได้ขึ้นอยู่กับ Android Framework แบบใดๆ
 */
class BasicSyntaxUnitTest {

    @Test
    fun addition_isCorrect() {
        val a = 2
        val b = 2
        val sum = a + b
        
        // Assert ว่าผลบวกได้ 4 ถูกต้องหรือไม่
        assertEquals(4, sum)
    }

    @Test
    fun string_concat_isCorrect() {
        val word1 = "Hello"
        val word2 = "World"
        
        assertEquals("HelloWorld", "$word1$word2")
    }
}
