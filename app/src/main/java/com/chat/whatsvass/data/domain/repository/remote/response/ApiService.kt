package com.chat.whatsvass.data.domain.repository.remote.response

import com.chat.whatsvass.data.domain.repository.remote.response.login.LoginResponse
import com.chat.whatsvass.data.domain.model.login.Login
import com.chat.whatsvass.data.domain.repository.remote.response.chat.ChatResponse
import com.chat.whatsvass.data.domain.model.register.Register
import com.chat.whatsvass.data.domain.repository.remote.response.logout.LogoutResponse
import com.chat.whatsvass.data.domain.repository.remote.response.message.MessagesResponse
import com.chat.whatsvass.data.domain.repository.remote.response.register.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("users/login")
    suspend fun loginUser(@Body request: Login): LoginResponse

    @POST("users/register")
    suspend fun registerUser(@Body post: RegisterResponse): Register

    @POST("users/logout")
    suspend fun logoutUser(@Header("Authorization") token: String): LogoutResponse

    @GET("chats/view")
    suspend fun getChats(@Header("Authorization") token: String): List<ChatResponse>

    @GET("messages/list/{chatId}")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): MessagesResponse
    @DELETE("chats/{chatId}")
    suspend fun deleteChat(
        @Path("chatId") chatId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
}

