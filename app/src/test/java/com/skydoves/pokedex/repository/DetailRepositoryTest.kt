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
import com.skydoves.pokedex.network.PokedexClient
import com.skydoves.pokedex.network.PokedexService
import com.skydoves.pokedex.persistence.PokemonInfoDao
import com.skydoves.pokedex.utils.MockUtil.mockPokemonInfo
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
class DetailRepositoryTest {

  private lateinit var repository: DetailRepository
  private lateinit var client: PokedexClient
  private val service: PokedexService = mock()
  private val pokemonInfoDao: PokemonInfoDao = mock()

  @ExperimentalCoroutinesApi
  @get:Rule
  var coroutinesRule = MainCoroutinesRule()

  @get:Rule
  var instantExecutorRule = InstantTaskExecutorRule()

  @ExperimentalCoroutinesApi
  @Before
  fun setup() {
    client = PokedexClient(service)
    repository = DetailRepository(client, pokemonInfoDao)
  }

  @Test
  fun fetchPokemonInfoFromNetwork() = runBlocking {
    val mockData = mockPokemonInfo()
    whenever(pokemonInfoDao.getPokemonInfo(name_ = "bulbasaur")).thenReturn(null)
    whenever(service.fetchPokemonInfo(name = "bulbasaur")).thenReturn(ApiResponse.of { Response.success(mockData) })

    repository.fetchPokemonInfo(name = "bulbasaur", onSuccess = {}, onError = {}).collect { pokemonInfo ->
      assertThat(pokemonInfo?.id, `is`(mockData.id))
      assertThat(pokemonInfo?.name, `is`(mockData.name))
      assertThat(pokemonInfo, `is`(mockData))
    }

    verify(pokemonInfoDao, atLeastOnce()).getPokemonInfo(name_ = "bulbasaur")
    verify(service, atLeastOnce()).fetchPokemonInfo(name = "bulbasaur")
    verify(pokemonInfoDao, atLeastOnce()).insertPokemonInfo(mockData)
  }

  @Test
  fun fetchPokemonInfoFromDatabase() = runBlocking {
    val mockData = mockPokemonInfo()
    whenever(pokemonInfoDao.getPokemonInfo(name_ = "bulbasaur")).thenReturn(mockData)
    whenever(service.fetchPokemonInfo(name = "bulbasaur")).thenReturn(ApiResponse.of { Response.success(mockData) })

    repository.fetchPokemonInfo(name = "bulbasaur", onSuccess = {}, onError = {}).collect { pokemonInfo ->
      assertThat(pokemonInfo?.id, `is`(mockData.id))
      assertThat(pokemonInfo?.name, `is`(mockData.name))
      assertThat(pokemonInfo, `is`(mockData))
    }

    verify(pokemonInfoDao, atLeastOnce()).getPokemonInfo(name_ = "bulbasaur")
    verifyNoMoreInteractions(service)
  }
}
