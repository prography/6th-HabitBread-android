package com.example.habitbread.api

import com.example.habitbread.base.BaseApplication
import com.example.habitbread.utils.SharedPreference
import okhttp3.Interceptor
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServerImpl {

    val interceptor: AccessTokenInterceptor = AccessTokenInterceptor()
    val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

    private const val BASE_URL = "https://habitbread.tk"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val APIService : HabitBreadAPI = retrofit.create(HabitBreadAPI::class.java)
}

class AccessTokenInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = BaseApplication.preferences.myIdToken
        val builder = chain.request().newBuilder().addHeader("Authorization", "Bearer " + token).build()
        return chain.proceed(builder)
    }
}

