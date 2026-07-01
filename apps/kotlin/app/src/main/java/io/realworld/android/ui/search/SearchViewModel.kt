package io.realworld.android.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realworld.android.data.ArticlesRepo
import io.realworld.api.models.entities.Article
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _results = MutableLiveData<List<Article>>()
    val results: LiveData<List<Article>> = _results

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentOffset = 0
    private val allArticles = mutableListOf<Article>()
    private var currentQuery = ""
    private var loadJob: Job? = null

    fun search(query: String) {
        loadJob?.cancel()
        currentQuery = query
        currentOffset = 0
        allArticles.clear()
        _hasMore.postValue(true)
        loadPage(0)
    }

    fun loadMore() {
        if (_isLoading.value == true || _hasMore.value == false || currentQuery.isBlank()) return
        loadPage(currentOffset)
    }

    private fun loadPage(offset: Int) {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                _isLoading.postValue(true)
                try {
                    val result = ArticlesRepo.searchArticles(currentQuery, offset)
                    if (result != null) {
                        allArticles.addAll(result)
                        currentOffset = allArticles.size
                        _hasMore.postValue(result.size >= ArticlesRepo.pageSize)
                        _results.postValue(allArticles.toList())
                        _errorMessage.postValue(
                            if (allArticles.isEmpty()) "Keine Artikel gefunden." else null,
                        )
                    } else {
                        Log.i("SearchViewModel", "searchArticles returned null at offset $offset")
                        _hasMore.postValue(false)
                        if (offset == 0) {
                            _errorMessage.postValue("Server nicht verfügbar. Bitte später erneut versuchen.")
                            _results.postValue(emptyList())
                        }
                    }
                } catch (e: SecurityException) {
                    Log.w("SearchViewModel", "search unauthorized for query '$currentQuery'")
                    _hasMore.postValue(false)
                    if (offset == 0) {
                        _errorMessage.postValue("Du musst angemeldet sein, um suchen zu können.")
                        _results.postValue(emptyList())
                    }
                } catch (e: Exception) {
                    Log.e("SearchViewModel", "search failed for query '$currentQuery' at offset $offset", e)
                    _hasMore.postValue(false)
                    if (offset == 0) {
                        _errorMessage.postValue("Server nicht verfügbar. Bitte später erneut versuchen.")
                        _results.postValue(emptyList())
                    }
                } finally {
                    _isLoading.postValue(false)
                }
            }
    }
}
