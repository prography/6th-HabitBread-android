package com.example.habitbread.api

import com.example.habitbread.data.*
import retrofit2.Call
import retrofit2.http.*

interface HabitBreadAPI {
    @GET("/habits")
    fun getAllHabitLists(): Call<List<HabitResponse>>
    @GET("/habits/{habitId}/calendar/{year}/{month}")
    fun getHabitDetail(@Path("habitId") habitId: Int, @Path("year") year: Int, @Path("month") month: Int): Call<DetailResponse>
    @GET("/ranking")
    fun getAllRankings() : Call<RankResponse>
    @POST("/habits")
    fun postNewHabit(@Body body: NewHabitReq): Call<NewHabitRes>
}