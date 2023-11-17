package dev.cai.mob2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer

class DoctorSignUpActivity : AppCompatActivity() {
    val timeSlots = Array(24) { i -> String.format("%02d:00", i+1) }
    val checkedItems = BooleanArray(24) // Tracks which time slots are checked
    private lateinit var selectedSlots: MutableList<String>;
    private val items = arrayOf("Dentist", "Dermatologist", "Pediatrician", "Therapist", "Ophthalmologist")
    private lateinit var autoCompleteTxt: AutoCompleteTextView
    private lateinit var adapterItems: ArrayAdapter<String>

    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var doneButton: Button
    private lateinit var bio:EditText
    private lateinit var rate:EditText
    private lateinit var type:AutoCompleteTextView

    private lateinit var dataViewModel: DataViewModel
    private lateinit var imageView: ImageView
    private var isEmpty=true

    private var link=""
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
    private fun openImageChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            imageView.setImageURI(imageUri)
            isEmpty=false;
            imageUri?.let { dataViewModel.uploadProfilePic(it) }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_doctor)

        val uid = intent.getStringExtra("UID")
        dataViewModel= DataViewModel()
        imageView=findViewById(R.id.inp_Img)
        firstNameEditText = findViewById(R.id.inp_fname)
        middleNameEditText = findViewById(R.id.inp_mname)
        lastNameEditText = findViewById(R.id.inp_lname)
        phoneNumberEditText = findViewById(R.id.inp_phone)
        emailEditText = findViewById(R.id.inp_email)
        doneButton = findViewById(R.id.btn_done)
        bio=findViewById(R.id.inp_bio)
        rate = findViewById(R.id.inp_rate)
        type = findViewById(R.id.tv_doctorType)
        imageView.setOnLongClickListener(){
            openImageChooser()
            true
        }
        val selectTimeSlotsButton: Button = findViewById(R.id.selectTimeSlotsButton)
        selectTimeSlotsButton.setOnClickListener {
            showTimeSlotsDialog()
        }
        autoCompleteTxt = findViewById<AutoCompleteTextView>(R.id.tv_doctorType)
        adapterItems = ArrayAdapter<String>(this, R.layout.dropdown_users, items)

        autoCompleteTxt.setAdapter(adapterItems)

        autoCompleteTxt.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position).toString()
        })

        doneButton.setOnClickListener {
            if (validateInputs()) {
                Log.d("sendhelp","b")
                val doctor = Doctor(

                    doctorId = intent.getStringExtra("UID")!!,
                    firstName = firstNameEditText.text.toString(),
                    email= emailEditText.text.toString(),
                    lastName= lastNameEditText.text.toString(),
                    middleName = middleNameEditText.text.toString(),
                    phoneNo = phoneNumberEditText.text.toString(),
                    profilePicLink= link,
                    rate = rate.text.toString().toInt(),
                    type=type.text.toString(),
                    about=  bio.text.toString(),
                    activeTakenSlots = emptyMap(),
                    timeSlotsSettings = selectedSlots
                )
                val uData = HashMap<String,Any>()
                uData["fname"] = firstNameEditText.text.toString()
                uData["mname"] = middleNameEditText.text.toString()
                uData["lname"] = lastNameEditText.text.toString()
                uData["phone"] = phoneNumberEditText.text.toString()
                uData["email"] = emailEditText.text.toString()
                uData["bio"] = bio.text.toString()
                uData["rate"] = rate.text.toString().toInt()
                uData["type"] = type.text.toString()

            }
        }
        dataViewModel.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataStates.uploadProfileImageSuccess -> link=(dataState.link)
                DataStates.Error -> Toast.makeText(this, "ERROR OCCURED", Toast.LENGTH_SHORT).show()
                // Handle other states as needed
                else -> {}
            }
        })
    }
    private fun showTimeSlotsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Time Slots")

        val view = layoutInflater.inflate(R.layout.dialog_time_slots, null)
        val listView: ListView = view.findViewById(R.id.list_view_time_slots)

        val adapter = TimeSlotAdapter(this, timeSlots.toList(), checkedItems)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        listView.setOnItemClickListener { _, _, position, _ ->
            checkedItems[position] = !checkedItems[position]
            adapter.notifyDataSetChanged()
        }

        builder.setView(view)
        builder.setPositiveButton("OK") { _, _ ->
            handleSelectedTimeSlots()
        }
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }
    private fun handleSelectedTimeSlots() {
        val selectedSlots = mutableListOf<String>()

        for (index in checkedItems.indices) {
            if (checkedItems[index]) {
                selectedSlots.add(timeSlots[index])
            }
        }

        this.selectedSlots =selectedSlots
        Log.d("dumplist", selectedSlots.toString())
    }

    class TimeSlotAdapter(context: Context, private val timeSlots: List<String>, private val checkedItems: BooleanArray) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, timeSlots) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            view.setBackgroundColor(if (checkedItems[position]) Color.GREEN else Color.RED)
            return view
        }
    }
    private fun validateInputs(): Boolean {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val fee= rate.text.toString().trim();

        if(isEmpty) return false
        if (firstName.isEmpty()) {
            firstNameEditText.error = "Please enter your first name"
            return false
        }

        if (lastName.isEmpty()) {
            lastNameEditText.error = "Please enter your last name"
            return false
        }

        if(!phoneNumber.matches(Regex("^\\d{11}$"))){
            phoneNumberEditText.error = "Please enter a valid 11 digit phone number"
            return false
        }
        if (phoneNumber.isEmpty()) {
            phoneNumberEditText.error = "Please enter your phone number"
            return false
        }

        if (email.isEmpty()) {
            emailEditText.error = "Please enter your email"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email"
            return false
        }
        try {
            fee.toInt()
        } catch (e: NumberFormatException) {
            return false
        }

        return true
    }
}
