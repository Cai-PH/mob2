package dev.cai.mob2

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date

class ScheduleActivity:AppCompatActivity() {


    private lateinit var viewModel: DataViewModel
    private lateinit var datePicker: DatePicker
    private lateinit var timeSpinner: Spinner
    private lateinit var doneButton: Button
    private lateinit var doctor: Doctor
    private lateinit var patient: Patient
    private lateinit var DUID: String
    private lateinit var PUID: String
    lateinit var selectedDate: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = DataViewModel()
        setContentView(R.layout.activity_scheduleselect)

        datePicker = findViewById(R.id.datePicker)
        timeSpinner = findViewById(R.id.timeSpinner)
        doneButton = findViewById(R.id.scheduleButton)

        doneButton.setOnClickListener {
            if (validateFields()) {
                if(datePicker.year<Calendar.getInstance().get(Calendar.YEAR)) {
                    Toast.makeText(this, "Please pick a valid date", Toast.LENGTH_SHORT).show()

                } else if(datePicker.year==Calendar.getInstance().get(Calendar.YEAR)){
                    if(datePicker.month<Calendar.getInstance().get(Calendar.MONTH)) {
                        Toast.makeText(this, "Please pick a valid date", Toast.LENGTH_SHORT).show()
                    } else if(datePicker.month==Calendar.getInstance().get(Calendar.MONTH)&&datePicker.dayOfMonth<Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                        Toast.makeText(this, "Please pick a valid date", Toast.LENGTH_SHORT).show()
                    } else {
                        createAndSendSchedule()
                        it.isEnabled = false
                        onBackPressed()
                    }
                } else {
                    createAndSendSchedule()
                    it.isEnabled = false
                    onBackPressed()
                }
            }
        }

        selectedDate = "${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)}-${Calendar.getInstance().get(Calendar.MONTH)+1}-${Calendar.getInstance().get(Calendar.YEAR)}"
        datePicker.init(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
            object : DatePicker.OnDateChangedListener {
                override fun onDateChanged(
                    view: DatePicker,
                    year: Int,
                    monthOfYear: Int,
                    dayOfMonth: Int
                ) {
                    selectedDate = "${dayOfMonth}-${monthOfYear + 1}-$year"
                    populateTimeSpinner()

                }
            })
        observeViewModel()
        PUID = intent.getStringExtra("PUID").toString()
        DUID = intent.getStringExtra("DUID").toString()
        viewModel.getDoctor(DUID)

    }

    private fun populateTimeSpinner() {
        var timeSlots = doctor.timeSlotsSettings.toMutableList()
        Log.d("test123",doctor.timeSlotsSettings.toString())
        Log.d("test123",doctor.activeTakenSlots.get(selectedDate).toString())
        Log.d("test123",doctor.activeTakenSlots.get("24-11-2023").toString())
        Log.d("test123",selectedDate)
        Log.d("test123", doctor.activeTakenSlots.toString())
        timeSlots.removeAll(doctor.activeTakenSlots.get(selectedDate)?: listOf())
        if (timeSlots.isEmpty()) {
            timeSlots.add("No Remaining Slots.")
            timeSlots.add("Select Another Day")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSpinner.adapter = adapter
    }

    private fun validateFields(): Boolean {
        return datePicker.dayOfMonth != null && timeSpinner.selectedItem != null && datePicker.year != null && datePicker.month != null && timeSpinner.selectedItem != "No Remaining Slots"
    }

    private fun createAndSendSchedule() {
        val selectedDate = "${datePicker.dayOfMonth}-${datePicker.month + 1}-${datePicker.year}"
        val selectedTime = timeSpinner.selectedItem.toString()
        getUnixLong(selectedTime,datePicker.dayOfMonth,datePicker.month+1,datePicker.year)
        viewModel.createAndUploadAppointment(doctor, patient,selectedDate,selectedTime,getUnixLong(timeSpinner.selectedItem.toString(),datePicker.dayOfMonth,datePicker.month+1,datePicker.year))
    }
    private fun getUnixLong(time:String, day: Int, month: Int, year: Int):Long {
        Log.d("time check",convertTimeTo24Hour(time).toString()+" "+year+month+day)
        val localDateTime = LocalDateTime.of(year, month, day, convertTimeTo24Hour(time)!!,0)

        return localDateTime.toEpochSecond(ZoneOffset.UTC)
    }
    fun convertTimeTo24Hour(inputTime: String): Int? {
        val regex = """(\d{1,2}):\d{2}[APM]+""".toRegex()
        val result = regex.find(inputTime)
        Log.d("test123",result.toString() + inputTime)
        if (result != null) {
            val startHour = result.groupValues[1].toInt()
            val startIndicator = result.value.takeLast(2)
            val convertedStartHour = if (startIndicator == "PM") {
                startHour + 12 // Add 12 hours for "PM"
            } else {
                startHour
            }

            return convertedStartHour
        }

        return null
    }



    private fun observeViewModel() {
        viewModel.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataStates.Loading -> {
                    doneButton.isEnabled = false;
                }
                is DataStates.createScheduleSuccess -> {
                    doneButton.isEnabled = true // Re-enable the button
                }
                is DataStates.Error -> {
                    doneButton.isEnabled = true // Re-enable the button
                }
                is DataStates.getDoctorDataSuccess -> {
                    doctor=dataState.doctor
                    viewModel.getPatient(PUID)
                }
                is DataStates.getPatientDataSuccess -> {
                    patient= dataState.patient
                    populateTimeSpinner()
                    doneButton.isEnabled = true;
                }

                else -> {}
            }
        })
    }

}