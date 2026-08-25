package com.luizkawai.mariposa.feature.characters.list

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.usecase.GetCharactersUseCase
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

class CharacterPagingSource(
    private val query: String,
    private val getCharacters: GetCharactersUseCase,
) : PagingSource<Int, Character>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        val page = params.key ?: INITIAL_PAGE

        return try {
            val characterPage = getCharacters(query, page)

            LoadResult.Page(
                data = characterPage.characters,
                prevKey = characterPage.previousPage,
                nextKey = characterPage.nextPage,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: SerializationException) {
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE = 1
    }
}
