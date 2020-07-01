package com.example.habitbread.ui.viewModel

import com.example.habitbread.data.CommitResponse

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.habitbread.data.DetailResponse
import com.example.habitbread.repository.DetailRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Error

class DetailViewModel : ViewModel() {

    var detailData: MutableLiveData<DetailResponse> = MutableLiveData()
    var commitData: MutableLiveData<Response<CommitResponse>> = MutableLiveData()

    fun getDetailData(habitId: Int, year: Int, month: Int){
        GlobalScope.launch {
            try {
                val data = DetailRepository().getDetailData(habitId, year, month)
                detailData.postValue(data)
            }catch (err: Error){
                Log.e("HabitBread", err.printStackTrace().toString())
            }
        }
    }

    fun postCommit(habitId: Int) {
        GlobalScope.launch {
            try {
                val data = DetailRepository().postCommit(habitId)
                commitData.postValue(data)
            }catch (err: Error) {
                Log.e("HabitBread", err.printStackTrace().toString())
            }
        }
    }
}