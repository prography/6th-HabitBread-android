package com.habitbread.main.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.habitbread.main.R
import com.habitbread.main.data.NewChangedHabitReq
import com.habitbread.main.ui.fragment.ModificationBottomSheet
import com.habitbread.main.ui.viewModel.DetailViewModel
import com.habitbread.main.util.DateCalculation
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.android.synthetic.main.activity_detail.*
import java.time.LocalDate
import kotlin.math.abs

class DetailActivity : AppCompatActivity(), ModificationBottomSheet.SetNewDataOnHabitListener {

    private lateinit var materialCalendarView: MaterialCalendarView
    private val detailViewModel: DetailViewModel by viewModels()
    private var habitId: Int = -1
    private var habitName: String? = ""
    private var habitDescription: String? = ""
    val year = LocalDate.now().toString().substring(0, 4).toInt() // 현 시각 년도
    val month = LocalDate.now().toString().substring(5, 7).toInt() // 현 시각 월
    var dayOfWeek: String = ""
    var alarmTime: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        habitId = intent.getIntExtra("habitId", -1)
        habitName = intent.getStringExtra("habitName")
        habitDescription = intent.getStringExtra("habitDescription")
        setDetailContents()
        setCalendarInfo(habitId, year, month)
        onClickCommit()
        onClickBackArrow()
        onSwipeMonthEvent()
        onClickSetting()
    }

    override fun setNewDataOnHabit(
        newTitle: String,
        newCategory: String,
        newDescription: String?,
        newAlarmTime: String?
    ) {
        detailViewModel.putChangedHabitData(habitId, NewChangedHabitReq(
            newTitle, newCategory, newDescription, newAlarmTime
        ))
        setCalendarInfo(habitId, year, month)
        detailViewModel.changedHabitData.observe(this, Observer {
            textView_detail_title.text = it.title
            textView_description.text = it.description
            habitName = it.title
            habitDescription = it.description
        })
    }

    private fun setDetailContents() {
        //calendar setting
        materialCalendarView = calendarView_habit_detail
        textView_detail_title.text = habitName
        textView_description.text = habitDescription
    }

    private fun setCalendarInfo(habitId: Int, year: Int, month: Int){
        detailViewModel.getDetailData(habitId, year, month)
        detailViewModel.detailData.observe(this, Observer {
            textView_continue_value.text = it.habit.continuousCount.toString() + "회"
            textView_total_value.text = it.commitFullCount.toString() + "회"
            textView_detail_compare.text = abs(it.comparedToLastMonth).toString() + "회"
            if(it.comparedToLastMonth > 0) {
                textView_detail_compare_left.text = "저번달보다 빵 구운 횟수가 "
                textView_detail_compare_right.text = "많아요!"
                textView_detail_compare.visibility = View.VISIBLE
                textView_detail_compare_right.visibility = View.VISIBLE
            }else if(it.comparedToLastMonth == 0) {
                textView_detail_compare_left.text = "저번달과 빵 구운 횟수가 똑같네요! 잘 하셨어요~"
                textView_detail_compare.visibility = View.GONE
                textView_detail_compare_right.visibility = View.GONE
            }else {
                textView_detail_compare_left.text = "저번달보다 빵 구운 횟수가 "
                textView_detail_compare_right.text = "적어요ㅠ"
                textView_detail_compare.visibility = View.VISIBLE
                textView_detail_compare_right.visibility = View.VISIBLE
            }

            this.dayOfWeek = it.habit.dayOfWeek
            this.alarmTime = it.habit.alarmTime

            // calendar commit days setting
            val committedDayList: MutableList<CalendarDay> = mutableListOf()
            for(element in it.habit.commitHistory){
                val seoulTime: String = DateCalculation().convertUTCtoSeoulTime(element.createdAt)
                val seoulYear = seoulTime.substring(0, 4).toInt()
                val seoulMonth = seoulTime.substring(5, 7).toInt()
                val seoulDay = seoulTime.substring(8, 10).toInt()
                val aDay = CalendarDay.from(seoulYear, seoulMonth, seoulDay)
                committedDayList.add(aDay)
            }
            materialCalendarView.addDecorators(DecoratorDays(committedDayList))
        })
    }

    inner class DecoratorDays(dayList: List<CalendarDay>) : DayViewDecorator{
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.icon_calendar_check)
        val list = dayList

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            return list.contains(day)
        }

        override fun decorate(view: DayViewFacade?) {
            view?.setSelectionDrawable(drawable!!)
        }
    }

    private fun onClickBackArrow(){
        imageView_back.setOnClickListener {
            finish()
        }
    }

    private fun onClickCommit() {
        button_commit.setOnClickListener {
            detailViewModel.postCommit(habitId)
            detailViewModel.commitData.observe(this, Observer {
                if(it.raw().code == 303) {
                    Toast.makeText(this, "습관빵을 이미 구웠습니다!", Toast.LENGTH_SHORT).show()
                }else if(it.code() == 201 && it.body()?.itemId == null){
                    setCalendarInfo(habitId, year, month)
                }else {
                    // TODO: 레벨업 했을 때 아이템 정보 팝업으로 띄워주기(디자인나오면 다시 하기)
                    Toast.makeText(this, "축하합니다~ 새로운 습관빵을 얻으셨네요! 베이커리에서 확인하세요!", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun onSwipeMonthEvent() {
        materialCalendarView.setOnMonthChangedListener { widget, date ->
            val swipedYear: Int = date.year
            val swipedMonth: Int = date.month
            setCalendarInfo(habitId, swipedYear, swipedMonth)
        }
    }

    private fun onClickSetting() {
        imageView_setting.setOnClickListener {
            // send detail datas from activity to fragment.
            val bundle: Bundle = Bundle()
            bundle.putInt("habitId", habitId)
            bundle.putString("title", habitName)
            bundle.putString("description", habitDescription)
            bundle.putString("dayOfWeek", dayOfWeek)
            bundle.putString("alarmTime", alarmTime)
            val modificationBottomSheet = ModificationBottomSheet()
            modificationBottomSheet.arguments = bundle
            modificationBottomSheet.show(supportFragmentManager, "showBottomSheet")
        }
    }
}