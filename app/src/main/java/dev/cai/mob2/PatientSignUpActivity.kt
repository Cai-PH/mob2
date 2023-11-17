package dev.cai.mob2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer

class PatientSignUpActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var doneButton: Button
    private lateinit var dataViewModel: DataViewModel
    private lateinit var imageView: ImageView
    private var isEmpty=true
    private var link=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataViewModel= DataViewModel()
        setContentView(R.layout.activity_signup_patient)
        val uid = intent.getStringExtra("UID")
        firstNameEditText = findViewById(R.id.inp_fname)
        middleNameEditText = findViewById(R.id.inp_mname)
        lastNameEditText = findViewById(R.id.inp_lname)
        lastNameEditText = findViewById(R.id.inp_lname)
        phoneNumberEditText = findViewById(R.id.inp_phone)
        emailEditText = findViewById(R.id.inp_email)
        imageView=findViewById(R.id.inp_Img)
        doneButton = findViewById(R.id.btn_done)
        imageView.setOnLongClickListener(){
            openImageChooser()
            true
        }
        doneButton.setOnClickListener {
            if (validateInputs()) {
                val patient = Patient(
                    patientId = intent.getStringExtra("UID")!!,
                    firstName = firstNameEditText.text.toString(),
                    email= emailEditText.text.toString(),
                    lastName= lastNameEditText.text.toString(),
                    middleName = middleNameEditText.text.toString(),
                    phoneNo = phoneNumberEditText.text.toString(),
                    profilePicLink= link)

                dataViewModel.createPatient(patient)
                Log.d("sendhelp",link)
                val intent = Intent(this@PatientSignUpActivity, PatientActivity::class.java)
                intent.putExtra("UID",uid)
                startActivity(intent);


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
    private fun validateInputs(): Boolean {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        if(isEmpty) return false
        if (firstName.isEmpty()) {
            firstNameEditText.error = "Please enter your first name"
            return false
        }

        if (lastName.isEmpty()) {
            lastNameEditText.error = "Please enter your last name"
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
        if(!phoneNumber.matches(Regex("^\\d{11}$"))){
            phoneNumberEditText.error = "Please enter a valid 11 digit phone number"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email"
            return false
        }
    Log.d("sendhelp","a")
        return true
    }
}
