package com.example.lab

import com.example.lab.utils.PokedexResponse
import com.example.lab.utils.PokemonApiService
import com.example.lab.utils.PokemonEntry
import com.example.lab.utils.PokemonNetwork
import com.example.lab.utils.PokemonSpecies
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApi = mockk<PokemonApiService>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Mock Singleton PokemonNetwork
        mockkObject(PokemonNetwork)
        // แก้จาก coEvery เป็น every เพราะ .api เป็น property ไม่ใช่ suspend function
        every { PokemonNetwork.api } returns mockApi
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun fetchPokemon_updatesList_onSuccess() = runTest {
        // Arrange
        val expectedList = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
            PokemonEntry(2, PokemonSpecies("ivysaur", "url2"))
        )
        val mockResponse = PokedexResponse(expectedList)
        coEvery { mockApi.getKantoPokedex() } returns mockResponse

        val viewModel = PokemonViewModel()

        // Act
        viewModel.fetchPokemon()
        advanceUntilIdle()

        // Assert
        assertEquals(expectedList, viewModel.pokemonList.value)
    }

    @Test
    fun fetchPokemon_doesNotUpdate_onError() = runTest {
        // Arrange
        coEvery { mockApi.getKantoPokedex() } throws Exception("Network Error")
        
        val viewModel = PokemonViewModel()

        // Act
        viewModel.fetchPokemon()
        advanceUntilIdle()

        // Assert
        assertEquals(emptyList<PokemonEntry>(), viewModel.pokemonList.value)
    }
}
