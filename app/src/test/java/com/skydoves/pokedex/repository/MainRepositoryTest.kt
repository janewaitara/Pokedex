/*
 * Designed and developed by 2020 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("SpellCheckingInspection")

package com.skydoves.pokedex.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.skydoves.pokedex.MainCoroutinesRule
import com.skydoves.pokedex.model.PokemonResponse
import com.skydoves.pokedex.network.PokedexClient
import com.skydoves.pokedex.network.PokedexService
import com.skydoves.pokedex.persistence.PokemonDao
import com.skydoves.pokedex.utils.MockUtil.mockPokemonList
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class MainRepositoryTest {

  private lateinit var repository: MainRepository
  private lateinit var client: PokedexClient
  private val service: PokedexService = mock()
  private val pokemonDao: PokemonDao = mock()

  @ExperimentalCoroutinesApi
  @get:Rule
  var coroutinesRule = MainCoroutinesRule()

  @get:Rule
  var instantExecutorRule = InstantTaskExecutorRule()

  @ExperimentalCoroutinesApi
  @Before
  fun setup() {
    client = PokedexClient(service)
    repository = MainRepository(client, pokemonDao)
  }

  @Test
  fun fetchPokemonListFromNetwork() = runBlocking {
    val mockData = PokemonResponse(count = 984, next = null, previous = null, results = mockPokemonList())
    whenever(pokemonDao.getPokemonList(page_ = 0)).thenReturn(emptyList())
    whenever(service.fetchPokemonList()).thenReturn(ApiResponse.of { Response.success(mockData) })

    repository.fetchPokemonList(
      page = 0,
      onSuccess = {},
      onError = {}
    ).collect {
      assertThat(it[0].page, `is`(0))
      assertThat(it[0].name, `is`("bulbasaur"))
      assertThat(it, `is`(mockPokemonList()))
    }

    verify(pokemonDao, atLeastOnce()).getPokemonList(page_ = 0)
    verify(service, atLeastOnce()).fetchPokemonList()
    verify(pokemonDao, atLeastOnce()).insertPokemonList(mockData.results)
  }

  @Test
  fun fetchPokemonListFromDatabase() = runBlocking {
    val mockData = PokemonResponse(count = 984, next = null, previous = null, results = mockPokemonList())
    whenever(pokemonDao.getPokemonList(page_ = 0)).thenReturn(mockData.results)
    whenever(service.fetchPokemonList()).thenReturn(ApiResponse.of { Response.success(mockData) })

    repository.fetchPokemonList(
      page = 0,
      onSuccess = {},
      onError = {}
    ).collect {
      assertThat(it[0].page, `is`(0))
      assertThat(it[0].name, `is`("bulbasaur"))
      assertThat(it, `is`(mockPokemonList()))
    }

    verify(pokemonDao, atLeastOnce()).getPokemonList(page_ = 0)
    verifyNoMoreInteractions(service)
  }
}
