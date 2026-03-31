package com.example.lab.architecture

import com.example.lab.PokemonViewModel
import com.example.lab.utils.PokedexResponse
import com.example.lab.utils.PokemonEntry
import com.example.lab.utils.PokemonNetwork
import com.example.lab.utils.PokemonSpecies
import io.mockk.coEvery
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {

    // กำหนดให้ Test Dispatchers จัดการ Coroutines แทน Main Thread ของ Android
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PokemonViewModel

    @Before
    fun setup() {
        // Mock ตัวแปรแบบ Singleton Object (PokemonNetwork)
        mockkObject(PokemonNetwork)
        
        // ทดสอบสร้าง ViewModel ตัวใหม่
        viewModel = PokemonViewModel()
    }

    @After
    fun tearDown() {
        // เมื่อจบแต่ละเทสต์ ต้องทำการยกเลิก Object ที่ Mock ไว้ไม่ให้ไปกระทบเทสต์อื่น
        unmockkAll()
    }

    @Test
    fun fetchPokemon_success_updatesStateWithMockList() = runTest {
        // 1. เตรียมข้อมูลจำลองให้มัน (Mocking)
        // จำลองข้อมูล Pokemon (Pikachu, Bulbasaur)
        val mockEntries = listOf(
            PokemonEntry(25, PokemonSpecies("pikachu", "url")),
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url"))
        )
        val mockResponse = PokedexResponse(mockEntries)

        // ถ้าเรียกเมธอด `getKantoPokedex` ใน API (ผ่าน Coroutines) ให้ส่ง `mockResponse` กลับมาทันทีโดยไม่ต้องต่อเน็ตจริงๆ
        coEvery { PokemonNetwork.api.getKantoPokedex() } returns mockResponse

        // 2. สั่งเรียกคำสั่งที่เราต้องการเทสต์
        viewModel.fetchPokemon()

        // 3. ตรวจสอบผลลัพธ์ (Assertion)
        // สมมติฐานคือ หลังเรียกคำสั่งไปแล้วตัวแปร _pokemonList stateFlow ต้องอัปเดตค่าให้ตรงกับ mockEntries!
        val currentList = viewModel.pokemonList.value
        assertEquals(2, currentList.size)
        assertEquals("pikachu", currentList[0].pokemon_species.name)
        assertEquals("bulbasaur", currentList[1].pokemon_species.name)
    }

    @Test
    fun fetchPokemon_error_doesNotCrashAndLeavesStateEmpty() = runTest {
        // จำลองสถานการณ์เกิดข้อผิดพลาดในการต่อเน็ต
        coEvery { PokemonNetwork.api.getKantoPokedex() } throws Exception("Network Error!")

        // เรียกใช้งาน API (ซึ่งข้างใน ViewModel.kt จะมี try-catch รับมือไว้แล้ว)
        viewModel.fetchPokemon()

        // ตรวจสอบว่าแอปไม่ระเบิด และ State flow ในบรรทัดต่อมายังคงเป็นค่าเริ่มต้น (Empty List)
        val currentList = viewModel.pokemonList.value
        assertEquals(0, currentList.size)
    }
}
