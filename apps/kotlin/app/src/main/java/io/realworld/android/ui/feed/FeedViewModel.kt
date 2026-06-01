package io.realworld.android.ui.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realworld.android.data.ArticlesRepo
import io.realworld.api.models.entities.Article
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val _feed = MutableLiveData<List<Article>>()
    val feed: LiveData<List<Article>> = _feed

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentOffset = 0
    private val allArticles = mutableListOf<Article>()
    private var feedType: FeedType = FeedType.GLOBAL
    private var loadJob: Job? = null

    enum class FeedType { GLOBAL, MY_FEED }

    fun fetchGlobalFeed() {
        feedType = FeedType.GLOBAL
        resetAndLoad()
    }

    fun fetchMyFeed() {
        feedType = FeedType.MY_FEED
        resetAndLoad()
    }

    fun loadMore() {
        if (_isLoading.value == true || _hasMore.value == false) return
        loadPage(currentOffset)
    }

    private fun resetAndLoad() {
        loadJob?.cancel()
        currentOffset = 0
        allArticles.clear()
        _hasMore.postValue(true)
        loadPage(0)
    }

    private fun loadPage(offset: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
        _isLoading.postValue(true)
        try {
            val result = when (feedType) {
                FeedType.GLOBAL -> ArticlesRepo.getGlobalFeed(offset)
                FeedType.MY_FEED -> ArticlesRepo.getMyFeed(offset)
            }
            if (result != null) {
                _errorMessage.postValue(null)
                allArticles.addAll(result)
                currentOffset = allArticles.size
                _hasMore.postValue(result.size >= ArticlesRepo.pageSize)
                _feed.postValue(allArticles.toList())
            } else {
                Log.i("FeedViewModel", "loadPage returned null at offset $offset")
                _hasMore.postValue(false)
                if (offset == 0) {
                    _errorMessage.postValue("Server nicht verfügbar. Bitte später erneut versuchen.")
                    _feed.postValue(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "loadPage failed at offset $offset", e)
            _hasMore.postValue(false)
            if (offset == 0) {
                _errorMessage.postValue("Server nicht verfügbar. Bitte später erneut versuchen.")
                _feed.postValue(emptyList())
            }
        }finally {
            _isLoading.postValue(false)
        }
        }
    }
}
