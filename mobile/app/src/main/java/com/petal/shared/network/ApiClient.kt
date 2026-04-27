package com.petal.shared.network

import com.petal.shared.util.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Note: For Android Emulator to access localhost on host machine, use 10.0.2.2.
    // Replace with correct IP address if testing on a physical device.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Volatile
    private var apiService: ApiService? = null

    fun getClient(tokenManager: TokenManager): ApiService {
        return apiService ?: synchronized(this) {
            
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(AuthInterceptor(tokenManager))
                .build()

            val instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                
            val service = instance.create(ApiService::class.java)
            apiService = service
            service
        }
    }
}
