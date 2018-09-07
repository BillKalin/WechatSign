package com.wechat.wechatsign

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import com.wechat.wechatsign.service.SignService
import com.wechat.wechatsign.util.formatTime


const val PACKAGE_WECHAT_WORK = "com.tencent.wework"

class MainActivity : AppCompatActivity() {

    private lateinit var startTimeTv: TextView
    private lateinit var stopTimeTv: TextView
    private lateinit var offworkStartTimeTv: TextView
    private lateinit var offworkStopTimeTv: TextView
    private lateinit var mStartWorkSwitch:CheckBox
    private lateinit var mStopWorkSwitch:CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        startTimeTv = findViewById(R.id.start_time_tv)
        stopTimeTv = findViewById(R.id.stop_time_tv)
        offworkStartTimeTv = findViewById(R.id.start_time_tv1)
        offworkStopTimeTv = findViewById(R.id.stop_time_tv1)
        mStartWorkSwitch = findViewById(R.id.start_work_cb)
        mStopWorkSwitch = findViewById(R.id.off_work_cb)


        val startTimeStr = getStartWorkStartTimeStr()
        val stopTimeStr = getStartWorkStopTimeStr()
        val offStartTimeStr = getOffWorkStartTimeStr()
        val offStopTimeStr = getOffWorkStopTimeStr()
        val isStartOpen = SharePrefHelper.getBoolean(IS_OPEN_START_WORK_SIGN_TASK, false)
        val isOffOpen = SharePrefHelper.getBoolean(IS_OPEN_STOP_WORK_SIGN_TASK, false)

        startTimeTv.text = formatTime(startTimeStr)
        stopTimeTv.text = formatTime(stopTimeStr)
        offworkStartTimeTv.text = formatTime(offStartTimeStr)
        offworkStopTimeTv.text = formatTime(offStopTimeStr)
        mStartWorkSwitch.isChecked = isStartOpen
        mStopWorkSwitch.isChecked = isOffOpen

        val cbCheckCHange = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when(buttonView.id) {
                R.id.start_work_cb -> {
                    SharePrefHelper.putBoolean(IS_OPEN_START_WORK_SIGN_TASK, isChecked)
                }
                R.id.off_work_cb -> {
                    SharePrefHelper.putBoolean(IS_OPEN_STOP_WORK_SIGN_TASK, isChecked)
                }
            }
        }
        mStartWorkSwitch.setOnCheckedChangeListener(cbCheckCHange)
        mStopWorkSwitch.setOnCheckedChangeListener(cbCheckCHange)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.open_acc -> {
                gotoAccessibilityAct()
            }
            R.id.start_sign -> {
                startSign()
                gotoWeWork()
            }
            R.id.start_time_tv -> {
                val startTimeStr = getStartWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showDateTimePicker(true, true, startHour, startMinute)
            }
            R.id.stop_time_tv -> {
                val stopTimeStr = getStartWorkStopTimeStr()
                var stopHour = stopTimeStr.split(":")[0].toInt()
                val stopMinute = stopTimeStr.split(":")[1].toInt()
                showDateTimePicker(false, true, stopHour, stopMinute)
            }
            R.id.start_time_tv1 -> {
                val startTimeStr = getOffWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showDateTimePicker(true, false, startHour, startMinute)
            }
            R.id.stop_time_tv1 -> {
                val stopTimeStr = getOffWorkStopTimeStr()
                var stopHour = stopTimeStr.split(":")[0].toInt()
                val stopMinute = stopTimeStr.split(":")[1].toInt()
                showDateTimePicker(false, false, stopHour, stopMinute)
            }
        }
    }

    private fun gotoAccessibilityAct() {
        SignService.mStartOpen = true
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        closeDateTimePicker()
    }

    private fun gotoWeWork() {
        val intent = packageManager.getLaunchIntentForPackage(PACKAGE_WECHAT_WORK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun startSign() {
        val intent = Intent(this, SignService::class.java)
        intent.action = SignService.ACTION_DO_ALARM_SIGN
        startService(intent)
    }

    private var mTimePickerDialog: TimePickerDialog? = null

    private fun showDateTimePicker(isStart: Boolean, isStartWork: Boolean, hour: Int, min: Int) {
        closeDateTimePicker()
        mTimePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            closeDateTimePicker()
            val timeStr = formatTime("$hourOfDay:$minute")
            if (isStartWork) {
                if (isStart) {
                    SharePrefHelper.putString(SIGN_TASK_START_WORK_START_TIME, timeStr)
                    Toast.makeText(this, "上班打卡开始时间:$timeStr", Toast.LENGTH_SHORT).show()
                    startTimeTv.text = "$timeStr"
                } else {
                    SharePrefHelper.putString(SIGN_TASK_START_WORK_STOP_TIME, "$timeStr")
                    stopTimeTv.text = "$timeStr"
                    Toast.makeText(this, "上班打卡结束时间:$timeStr", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (isStart) {
                    SharePrefHelper.putString(SIGN_TASK_STOP_WORK_START_TIME, timeStr)
                    Toast.makeText(this, "下班打卡开始时间:$timeStr", Toast.LENGTH_SHORT).show()
                    startTimeTv.text = "$timeStr"
                } else {
                    SharePrefHelper.putString(SIGN_TASK_STOP_WORK_STOP_TIME, "$timeStr")
                    stopTimeTv.text = "$timeStr"
                    Toast.makeText(this, "下班打卡结束时间:$timeStr", Toast.LENGTH_SHORT).show()
                }
            }
        }, hour, min, true)
        mTimePickerDialog?.let {
            it.show()
        }
    }

    private fun closeDateTimePicker() {
        mTimePickerDialog?.cancel()
        mTimePickerDialog = null
    }
}
