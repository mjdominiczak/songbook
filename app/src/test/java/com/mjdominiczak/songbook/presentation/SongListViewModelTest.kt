package com.mjdominiczak.songbook.presentation

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.common.Resource
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.GetAllSongsUseCase
import com.mjdominiczak.songbook.presentation.list.SongListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class SongListViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SongListViewModel

    private val getAllSongsUseCase = mock(GetAllSongsUseCase::class.java)

    @Before
    fun setUp() {
        `when`(getAllSongsUseCase()).thenReturn(
            flow {
                emit(
                    Resource.Success(
                        listOf(
                            Song(1, 1, "asdf", "", ""),
                            Song(2, 1, "qwer", "", ""),
                            Song(3, 1, "dgfg", "", ""),
                        )
                    )
                )
            }
        )
        viewModel = SongListViewModel(getAllSongsUseCase)
    }

    @Test
    fun activateSearch_stateUpdatedCorrectly() {
        viewModel.activateSearch()
        assertThat(viewModel.state.value.isSearchActive).isTrue()
    }

    @Test
    fun deactivateSearch_stateUpdatedCorrectly() {
        viewModel.deactivateSearch()
        assertThat(viewModel.state.value.isSearchActive).isFalse()
        assertThat(viewModel.state.value.searchQuery).isEmpty()
        viewModel.activateSearch()
    }

    @Test
    fun onSearchQueryChanged_stateUpdatedCorrectly() {
        viewModel.onSearchQueryChanged("test")
        assertThat(viewModel.state.value.searchQuery).isEqualTo("test")
    }

    @Test
    fun songsFiltered_withEmptyQuery_returnsFullList() {
        viewModel.activateSearch()
        viewModel.onSearchQueryChanged("")
        assertThat(viewModel.songsFiltered).hasSize(3)
    }

    @Test
    fun songsFiltered_withQueryMatchingMany_returnsAllMatches() {
        viewModel.activateSearch()
        viewModel.onSearchQueryChanged("d")
        assertThat(viewModel.songsFiltered).hasSize(2)
    }

    @Test
    fun songsFiltered_withQueryMatchingNothing_returnsEmptyList() {
        viewModel.activateSearch()
        viewModel.onSearchQueryChanged("eerfgdzfgd")
        assertThat(viewModel.songsFiltered).isEmpty()
    }

}