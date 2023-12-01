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
import com.squareup.picasso.Picasso
import dev.cai.mob2.databinding.ActivitySignupPatient2Binding
import dev.cai.mob2.databinding.ActivitySignupPatientBinding

class PatientEditDataActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var allergiesEditText: EditText
    private lateinit var recentMedEditText: EditText
    private lateinit var medHistEditText: EditText
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
    private lateinit var patient:Patient
    private lateinit var tempPatient:Patient
    private fun loadData() {
        patientBinding.textView3.setText("Edit Your Information")
        patientBinding.tvSignup.setText("Edit Profile")
        patient2Binding.textView3.setText("Edit Your Information")
        patient2Binding.tvSignup.setText("Edit Profile")
        patientBinding.inpFname.setText(patient.firstName)
        patientBinding.inpMname.setText(patient.middleName)
        patientBinding.inpLname.setText(patient.lastName)
        patientBinding.inpPhone.setText(patient.phoneNo)
        patientBinding.inpEmail.setText(patient.email)

        patient2Binding.inpAllergies.setText(patient.allergies)
        patient2Binding.inpMedhist.setText(patient.medicalHistory)
        patient2Binding.inpRecentmed.setText(patient.recentMedications)
        Picasso.get()
            .load(patient.profilePicLink)
            .into(patientBinding.inpImg)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patient = (intent.getSerializableExtra("patientdata") as? Patient)!!
        patientBinding= ActivitySignupPatientBinding.inflate(layoutInflater)
        patient2Binding= ActivitySignupPatient2Binding.inflate(layoutInflater)
        dataViewModel= DataViewModel()
        setContentView(patientBinding.root)
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
        allergiesEditText=patient2Binding.inpAllergies
        medHistEditText=patient2Binding.inpMedhist
        recentMedEditText=patient2Binding.inpRecentmed
        imageView.setOnLongClickListener(){
            openImageChooser()
            true
        }
        doneButton.setOnClickListener {
            if (validateInputs2()) {
                if (::curUri.isInitialized) dataViewModel.uploadProfilePic(curUri) else {

                    val patient = Patient(
                        patientId = this.patient.patientId,
                        firstName = firstNameEditText.text.toString(),
                        email= emailEditText.text.toString(),
                        lastName= lastNameEditText.text.toString(),
                        middleName = middleNameEditText.text.toString(),
                        phoneNo = phoneNumberEditText.text.toString(),
                        profilePicLink= patient.profilePicLink,
                        medicalHistory = medHistEditText.text.toString(),
                        allergies = allergiesEditText.text.toString(),
                        recentMedications = recentMedEditText.text.toString())
                    tempPatient=patient
                    dataViewModel.createPatient(patient)
                    Log.d("sendhelp",patient.profilePicLink)
                    dataViewModel.createUser(this.patient.patientId,"Patient")
                }

            }
        }
        nextButton.setOnClickListener {

            if (validateInputs()) {
                curState=1
                setContentView(patient2Binding.root)
            }
        }
        backButton.setOnClickListener {
            curState=0
            setContentView(patientBinding.root)
        }
        dataViewModel.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataStates.uploadProfileImageSuccess ->{
                    val patient = Patient(
                        patientId = this.patient.patientId,
                        firstName = firstNameEditText.text.toString(),
                        email= emailEditText.text.toString(),
                        lastName= lastNameEditText.text.toString(),
                        middleName = middleNameEditText.text.toString(),
                        phoneNo = phoneNumberEditText.text.toString(),
                        profilePicLink= dataState.link,
                        medicalHistory = medHistEditText.text.toString(),
                        allergies = allergiesEditText.text.toString(),
                        recentMedications = recentMedEditText.text.toString())

                    tempPatient=patient
                    dataViewModel.createPatient(patient)
                    Log.d("sendhelp",dataState.link)
                    dataViewModel.createUser(this.patient.patientId,"Patient")
                }
                DataStates.Error -> Toast.makeText(this, "ERROR OCCURED", Toast.LENGTH_SHORT).show()
                DataStates.createUserSuccess -> {
                    dataViewModel.getAppointmentsForPatient(tempPatient.patientId)

                }
                is DataStates.getSchedulesSuccess -> {
                    dataViewModel.updateAppointmentsPatient(dataState.appointments,tempPatient)
                }
                DataStates.modifypatientappointments -> {
                    curState=0
                    onBackPressed()
                }
                // Handle other states as needed
                else -> {}
            }
        })
        loadData()
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
    private fun validateInputs2(): Boolean {
        val allergies = allergiesEditText.text.toString().trim()
        val medhist = medHistEditText.text.toString().trim()
        val recentmed = recentMedEditText.text.toString().trim()
        if (allergies.isEmpty()) {
            allergiesEditText.error = "Please enter your first name"
            return false
        }
        if (medhist.isEmpty()) {
            medHistEditText.error = "Please enter your first name"
            return false
        }
        if (recentmed.isEmpty()) {
            recentMedEditText.error = "Please enter your first name"
            return false
        }
        return true
    }
    private fun validateInputs(): Boolean {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
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
             curState=0
            setContentView(patientBinding.root)
        }
    }
}
