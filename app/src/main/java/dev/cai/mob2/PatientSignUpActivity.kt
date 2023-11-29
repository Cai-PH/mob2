package dev.cai.mob2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import dev.cai.mob2.databinding.ActivitySignupPatient2Binding
import dev.cai.mob2.databinding.ActivitySignupPatientBinding

class PatientSignUpActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var doneButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button
    private lateinit var dataViewModel: DataViewModel
    private lateinit var imageView: ImageView
    private var isEmpty=true
    private lateinit var curUri:Uri
    private lateinit var patientBinding: ActivitySignupPatientBinding
    private lateinit var patient2Binding: ActivitySignupPatient2Binding
    var curState=0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patientBinding= ActivitySignupPatientBinding.inflate(layoutInflater)
        patient2Binding= ActivitySignupPatient2Binding.inflate(layoutInflater)
        dataViewModel= DataViewModel()
        setContentView(R.layout.activity_signup_patient)
        val uid = intent.getStringExtra("UID")
        firstNameEditText = patientBinding.inpFname
        middleNameEditText = patientBinding.inpMname
        lastNameEditText = patientBinding.inpLname
        phoneNumberEditText = patientBinding.inpPhone
        emailEditText = patientBinding.inpEmail
        imageView=patientBinding.inpImg

        doneButton = patient2Binding.btnNext
        backButton = patient2Binding.btnBack
        nextButton = patientBinding.btnNext
        imageView.setOnLongClickListener(){
            openImageChooser()
            true
        }
        doneButton.setOnClickListener {
            if (validateInputs()) {
                dataViewModel.uploadProfilePic(curUri)
            }
        }
        nextButton.setOnClickListener {
            curState=1
            setContentView(patient2Binding.root)
        }
        backButton.setOnClickListener {
            curState=0
            setContentView(patientBinding.root)
        }
        dataViewModel.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataStates.uploadProfileImageSuccess ->{
                    val patient = Patient(
                        patientId = intent.getStringExtra("UID")!!,
                        firstName = firstNameEditText.text.toString(),
                        email= emailEditText.text.toString(),
                        lastName= lastNameEditText.text.toString(),
                        middleName = middleNameEditText.text.toString(),
                        phoneNo = phoneNumberEditText.text.toString(),
                        profilePicLink= dataState.link)

                    dataViewModel.createPatient(patient)
                    Log.d("sendhelp",dataState.link)
                    dataViewModel.createUser(intent.getStringExtra("UID")!!,"Patient")
                }
                DataStates.Error -> Toast.makeText(this, "ERROR OCCURED", Toast.LENGTH_SHORT).show()
                DataStates.createUserSuccess -> {
                    val intent = Intent(this@PatientSignUpActivity, PatientActivity::class.java)
                    intent.putExtra("UID",uid)
                    startActivity(intent);
            }
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
            curUri=imageUri!!
            isEmpty=false;
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

    override fun onBackPressed() {
        if(curState==0) {
            super.onBackPressed()
        } else {
            setContentView(patientBinding.root)
        }
    }
}
