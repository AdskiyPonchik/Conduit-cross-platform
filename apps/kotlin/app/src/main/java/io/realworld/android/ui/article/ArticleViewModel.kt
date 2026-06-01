package io.realworld.android.ui.article

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realworld.android.data.ArticlesRepo
import io.realworld.api.ConduitClient
import io.realworld.api.models.entities.Article
import io.realworld.api.models.entities.Comment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {
    val api = ConduitClient.publicApi

    private val _article = MutableLiveData<Article?>()
    val article: LiveData<Article?> = _article

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchArticle(slug: String) = viewModelScope.launch {
        repeat(3) { attempt ->
            try {
                val response = api.getArticleBySlug(slug)
                val fetchedArticle = response.body()?.article
                if (fetchedArticle != null) {
                    _article.postValue(fetchedArticle)
                    _error.postValue(null)
                    return@launch
                } else {
                    Log.w("ArticleViewModel", "fetchArticle returned null article (attempt ${attempt + 1})")
                    if (attempt == 2) _error.postValue("Artikel konnte nicht geladen werden.")
                    else delay(500L * (attempt + 1))
                }
            } catch (e: Exception) {
                Log.e("ArticleViewModel", "fetchArticle failed (attempt ${attempt + 1})", e)
                if (attempt == 2) _error.postValue("Fehler beim Laden des Artikels: ${e.localizedMessage}")
                else delay(500L * (attempt + 1))
            }
        }
    }

    fun fetchComments(slug: String) = viewModelScope.launch {
        try {
            val response = api.getComments(slug)
            _comments.postValue(response.body()?.comments ?: emptyList())
        } catch (e: Exception) {
            Log.e("ArticleViewModel", "fetchComments failed", e)
            _comments.postValue(emptyList())
        }
    }


    fun createArticle(
        title:String?,
        description:String?,
        body:String?,
        tagList:List<String>?=null
    ) =viewModelScope.launch {
        val article = ArticlesRepo.createArticle(
            title=title,
            description = description,
            body=body,
            tagList = tagList
        )
    }


}