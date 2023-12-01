package dev.cai.mob2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.BitmapCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.cai.mob2.R.color.dgreen
import dev.cai.mob2.databinding.ActivitySignupDoctor2Binding
import dev.cai.mob2.databinding.ActivitySignupDoctor3Binding
import dev.cai.mob2.databinding.ActivitySignupDoctorBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DoctorEditDataActivity : AppCompatActivity() {
    var prevState=0;
    val amTimeSlots = listOf(
        "12:00AM - 1:00AM",
        "1:00AM - 2:00AM",
        "2:00AM - 3:00AM",
        "3:00AM - 4:00AM",
        "4:00AM - 5:00AM",
        "5:00AM - 6:00AM",
        "6:00AM - 7:00AM",
        "7:00AM - 8:00AM",
        "8:00AM - 9:00AM",
        "9:00AM - 10:00AM",
        "10:00AM - 11:00AM",
        "11:00AM - 12:00PM"
    )
    val pmTimeSlots = listOf(
        "12:00PM - 1:00PM",
        "1:00PM - 2:00PM",
        "2:00PM - 3:00PM",
        "3:00PM - 4:00PM",
        "4:00PM - 5:00PM",
        "5:00PM - 6:00PM",
        "6:00PM - 7:00PM",
        "7:00PM - 8:00PM",
        "8:00PM - 9:00PM",
        "9:00PM - 10:00PM",
        "10:00PM - 11:00PM",
        "11:00PM - 12:00AM"
    )
    val checkedItems = BooleanArray(24) // Tracks which time slots are checked
    private lateinit var selectedSlots: MutableList<String>;
    private val items = arrayOf("Dentist", "Dermatologist", "Pediatrician", "Therapist", "Ophthalmologist")
    private lateinit var autoCompleteTxt: AutoCompleteTextView
    private lateinit var adapterItems: ArrayAdapter<String>
    private lateinit var adapterAm:DoctorEditDataActivity.TimeSlotAdapter
    private lateinit var adapterPm:DoctorEditDataActivity.TimeSlotAdapter

    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var doneButton: Button
    private lateinit var nextButton: Button
    private lateinit var next2Button: Button
    private lateinit var backButton: Button
    private lateinit var back2Button: Button
    private lateinit var bio:EditText
    private lateinit var rate:EditText
    private lateinit var type:AutoCompleteTextView

    private lateinit var dataViewModel: DataViewModel
    private lateinit var imageView: ImageView
    private lateinit var binding1: ActivitySignupDoctorBinding
    private lateinit var binding2: ActivitySignupDoctor2Binding
    private lateinit var binding3: ActivitySignupDoctor3Binding

    private lateinit var doctor:Doctor
    private lateinit var curUri: Uri
    private lateinit var tempdoctor:Doctor
    private fun loadData() {

        binding1.textView3.setText("Edit Your Information")
        binding1.tvSignup.setText("Edit Profile")
        binding2.textView3.setText("Edit Your Information")
        binding2.tvSignup.setText("Edit Profile")
        binding3.textView3.setText("Edit Your Information")
        binding3.tvSignup.setText("Edit Profile")
        binding1.textView3
        binding1.inpFname.setText(doctor.firstName)
        binding1.inpMname.setText(doctor.middleName)
        binding1.inpLname.setText(doctor.lastName)
        binding1.inpPhone.setText(doctor.phoneNo)
        binding1.inpEmail.setText(doctor.email)

        binding2.inpBio.setText(doctor.about)
        binding2.inpRate.setText(doctor.rate.toString())
        binding2.tvDoctorType.setText(doctor.type)
        Picasso.get()
            .load(doctor.profilePicLink)
            .into(binding1.inpImg)
        adapterAm.setData(doctor.timeSlotsSettings.intersect(amTimeSlots).toList())
        adapterPm.setData(doctor.timeSlotsSettings.intersect(pmTimeSlots).toList())
    }
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
            val imageUri = convertToJpegAndGetUri(this, data.data!!)
            imageView.setImageURI(imageUri)
            curUri=imageUri!!
        }
    }
    fun convertToJpegAndGetUri(context: Context, imageUri: Uri): Uri? {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

            val jpegFileName = "converted_image_${System.currentTimeMillis()}.jpeg"
            val jpegFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), jpegFileName)

            FileOutputStream(jpegFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            return Uri.fromFile(jpegFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doctor = (intent.getSerializableExtra("doctordata") as? Doctor)!!
        binding1=ActivitySignupDoctorBinding.inflate(layoutInflater)
        binding2=ActivitySignupDoctor2Binding.inflate(layoutInflater)
        binding3=ActivitySignupDoctor3Binding.inflate(layoutInflater)
        dataViewModel= DataViewModel()
        imageView= binding1.inpImg
        firstNameEditText = binding1.inpFname
        middleNameEditText = binding1.inpMname
        lastNameEditText =binding1.inpLname
        phoneNumberEditText = binding1.inpPhone
        emailEditText = binding1.inpEmail
        doneButton = binding3.btnDone
        backButton=binding2.btnBack
        back2Button=binding3.btnBack
        next2Button=binding2.btnNext
        nextButton=binding1.btnNext
        bio=binding2.inpBio
        rate = binding2.inpRate
        type = binding2.tvDoctorType
        imageView.setOnLongClickListener(){
            openImageChooser()
            true
        }
        nextButton.setOnClickListener {
            if (validateInputs()) {
                setContentView(binding2.root)
            }
        }
        next2Button.setOnClickListener {
            if (validateInputs2()) {
                prevState=1
                setContentView(binding3.root)
            }
        }
        backButton.setOnClickListener {
            onBackPressed()
        }
        back2Button.setOnClickListener {
            onBackPressed()
        }
        autoCompleteTxt = binding2.tvDoctorType
        adapterItems = ArrayAdapter<String>(this, R.layout.dropdown_users, items)

        autoCompleteTxt.setAdapter(adapterItems)

        autoCompleteTxt.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position).toString()
        })

        doneButton.setOnClickListener {
            if (validateInputs()) {
                if (::curUri.isInitialized) dataViewModel.uploadProfilePic(curUri) else {
                    selectedSlots.clear()
                    selectedSlots.addAll(adapterAm.getData().sortedBy { item -> amTimeSlots.indexOf(item) })
                    selectedSlots.addAll(adapterPm.getData().sortedBy { item -> pmTimeSlots.indexOf(item) })
                    Log.d("sendhelp","b")
                    val doctor = Doctor(
                        doctorId = doctor.doctorId,
                        firstName = firstNameEditText.text.toString(),
                        email= emailEditText.text.toString(),
                        lastName= lastNameEditText.text.toString(),
                        middleName = middleNameEditText.text.toString(),
                        phoneNo = phoneNumberEditText.text.toString(),
                        profilePicLink= this.doctor.profilePicLink,
                        rate = rate.text.toString().toInt(),
                        type=type.text.toString(),
                        about=  bio.text.toString(),
                        activeTakenSlots = emptyMap(),
                        timeSlotsSettings = selectedSlots
                    )
                    tempdoctor=doctor
                    dataViewModel.createDoctor(doctor)
                    Log.d("sendhelp",this.doctor.profilePicLink)
                    dataViewModel.createUser(doctor.doctorId,"Doctor")
                }
            }
        }
        selectedSlots= mutableListOf<String>()
        adapterAm = TimeSlotAdapter(this, amTimeSlots)
        adapterPm = TimeSlotAdapter(this, pmTimeSlots)
        binding3.recyclerViewAmTimeSlots.adapter=adapterAm
        binding3.recyclerViewPmTimeSlots.adapter=adapterPm
        binding3.recyclerViewPmTimeSlots.layoutManager=LinearLayoutManager(this)
        binding3.recyclerViewAmTimeSlots.layoutManager=LinearLayoutManager(this)
        dataViewModel.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataStates.uploadProfileImageSuccess -> {
                    selectedSlots.clear()
                    selectedSlots.addAll(adapterAm.getData().sortedBy { item -> amTimeSlots.indexOf(item) })
                    selectedSlots.addAll(adapterPm.getData().sortedBy { item -> pmTimeSlots.indexOf(item) })
                    Log.d("sendhelp","b")
                    val doctor = Doctor(
                        doctorId = doctor.doctorId,
                        firstName = firstNameEditText.text.toString(),
                        email= emailEditText.text.toString(),
                        lastName= lastNameEditText.text.toString(),
                        middleName = middleNameEditText.text.toString(),
                        phoneNo = phoneNumberEditText.text.toString(),
                        profilePicLink= dataState.link,
                        rate = rate.text.toString().toInt(),
                        type=type.text.toString(),
                        about=  bio.text.toString(),
                        activeTakenSlots = emptyMap(),
                        timeSlotsSettings = selectedSlots

                    )
                    tempdoctor=doctor
                    dataViewModel.createDoctor(doctor)
                    Log.d("sendhelp",dataState.link)
                    dataViewModel.createUser(doctor.doctorId,"Doctor")
                }
                DataStates.Error -> Toast.makeText(this, "ERROR OCCURED", Toast.LENGTH_SHORT).show()
                DataStates.createUserSuccess -> {
                    dataViewModel.getAppointmentsForDoctor(tempdoctor.doctorId)

                }
                is DataStates.getSchedulesSuccess -> {
                    dataViewModel.updateAppointmentsDoctor(dataState.appointments,tempdoctor)
                }

                DataStates.modifydoctorappointments -> {
                    prevState=-1
                    onBackPressed()
                }
                else -> {}
            }
        })

        setContentView(binding1.root)

        loadData()
    }

    class TimeSlotAdapter(context: Context, private val timeSlots: List<String>) : RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {
        private val selectedSlots = mutableListOf<String>()

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.item123)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timeslot, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val timeSlot = timeSlots[position]
            holder.textView.text = timeSlot
            holder.itemView.setBackgroundColor(
                if (selectedSlots.contains(timeSlot)) Color.GREEN else Color.TRANSPARENT
            )
            holder.itemView.setOnClickListener {
                if (selectedSlots.contains(timeSlot)) {
                    selectedSlots.remove(timeSlot)
                } else {
                    selectedSlots.add(timeSlot)
                }
                notifyItemChanged(position)
            }
        }

        override fun getItemCount() = timeSlots.size
        fun getData(): List<String> {
            return selectedSlots
        }
        fun setData(list: List<String>) {
            selectedSlots.clear()
            selectedSlots.addAll(list)
            notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        if (prevState==1){
            prevState=0
            setContentView(binding2.root)
        } else if (prevState==0){
            setContentView(binding1.root)
        } else if (prevState==-1){
            super.onBackPressed()
        }
    }
    private fun validateInputs2():Boolean {

        val fee= rate.text.toString().trim();
        if (bio.text.isEmpty()) {

            bio.error = "Please enter a valid bio"
            return false
        };
        try {
            fee.toInt()
        } catch (e: NumberFormatException) {
            rate.error = "Please enter a valid fee"

            return false
        }
        return true;
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


        return true
    }
}
