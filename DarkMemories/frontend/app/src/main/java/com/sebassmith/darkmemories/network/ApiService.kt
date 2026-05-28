package com.sebassmith.darkmemories.network

import com.sebassmith.darkmemories.model.PhotoResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService  {
    @GET("photos")
    suspend fun getPhotos(
        @Header("X-API-KEY") apiKey: String
    ): PhotoResponse
}