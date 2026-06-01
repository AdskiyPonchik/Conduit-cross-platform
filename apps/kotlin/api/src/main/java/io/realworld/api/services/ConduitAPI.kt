package io.realworld.api.services

import io.realworld.api.models.requests.LoginRequest
import io.realworld.api.models.requests.SignupRequest
import io.realworld.api.models.responses.ArticleResponse
import io.realworld.api.models.responses.ArticlesResponse
import io.realworld.api.models.responses.CommentsResponse
import io.realworld.api.models.responses.TagsResponse
import io.realworld.api.models.responses.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ConduitAPI {

    @POST("users")
    suspend fun signupUser(
        @Body userCreds: SignupRequest
    ): Response<UserResponse>

    @POST("users/login")
    suspend fun loginUser(
        @Body userCreds: LoginRequest
    ): Response<UserResponse>

    @GET("articles")
    suspend fun getArticles(
        @Query("author") author: String? = null,
        @Query("favourited") favourited: String? = null,
        @Query("tag") tag: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ArticlesResponse>

    @GET("articles/{slug}")
    suspend fun getArticleBySlug(
        @Path("slug") slug: String
    ): Response<ArticleResponse>

    @GET("articles/{slug}/comments")
    suspend fun getComments(
        @Path("slug") slug: String
    ): Response<CommentsResponse>

    @GET("tags")
    suspend fun getTags(): Response<TagsResponse>

}