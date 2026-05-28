package com.sebassmith.darkmemories.network
import androidx.annotation.Keep
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient  {
    const val API_KEY = "nasigoreng-ituenak-123"

    private  const val BASE_URL = "http://10.0.2.2:8080/"

    @Keep
    private const val SECRET_ENDPOINT = "/s3cr3t_n4sg0r_g0r3ng"


    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}