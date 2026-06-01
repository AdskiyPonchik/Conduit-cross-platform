package io.realworld.android.data

import io.realworld.android.utils.withRetry
import io.realworld.api.ConduitClient
import io.realworld.api.models.entities.Article
import io.realworld.api.models.entities.ArticleData
import io.realworld.api.models.requests.UpsertArticleRequest

object ArticlesRepo {
    val api = ConduitClient.publicApi
    val authApi = ConduitClient.authApi

    val pageSize = 20

    suspend fun getGlobalFeed(offset: Int = 0) =
        withRetry { api.getArticles(limit = pageSize, offset = offset) }.body()?.articles

    suspend fun getMyFeed(offset: Int = 0) =
        withRetry { authApi.getFeedArticles(limit = pageSize, offset = offset) }.body()?.articles

    suspend fun searchArticles(query: String, offset: Int = 0): List<Article>? {
        val response = withRetry {
            authApi.searchArticles(
                query = query.trim().replace(" ", "+"),
                limit = pageSize,
                offset = offset
            )
        }
        if (response.code() == 401) throw SecurityException("Nicht autorisiert")
        return response.body()?.articles
    }

    suspend fun createArticle(
        title:String?,
        description:String?,
        body:String?,
        tagList:List<String>?=null
    ) : Article? {
        val response =authApi.createArticle(
            UpsertArticleRequest(
            ArticleData(
                title=title,
                description = description,
                body = body,
                tagList = tagList
            )
        )
        )

        return response.body()?.article
    }
}