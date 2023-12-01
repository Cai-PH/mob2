package dev.cai.mob2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.cai.mob2.databinding.ActivityDoctorHomeBinding
import dev.cai.mob2.databinding.ActivityPatientsBinding
import dev.cai.mob2.databinding.AppointmentCardBinding
import dev.cai.mob2.databinding.DoctorprofileBinding

class DoctorActivity : AppCompatActivity(){
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: MyAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var dataViewModel: DataViewModel
    private lateinit var dataViewModel3: DataViewModel
    private lateinit var scheduleButton: Button
    private lateinit var binding: ActivityDoctorHomeBinding
    private lateinit var binding2:ActivityPatientsBinding
    private var patientlist= mutableListOf<String>()
    private var doctor= Doctor()
    private lateinit var doctorprofileBinding:DoctorprofileBinding
    private var count:Int=0
    private lateinit var uid:String

    fun doctorProfileInit() {

        doctorprofileBinding.tvDoctorFirstName.text=doctor.lastName + ", " + doctor.firstName + " " + doctor.middleName + "."
        doctorprofileBinding.tvDoctorEmail.text=doctor.email
        doctorprofileBinding.tvDoctorPhoneNo.text=doctor.phoneNo
        doctorprofileBinding.tvDoctorRate.text= doctor.rate.toString()
        doctorprofileBinding.tvDoctorType.text=doctor.type
        doctorprofileBinding.tvDoctorAbout.text=doctor.about

        doctorprofileBinding.btnLogout.setOnClickListener {
            val intent = Intent(this@DoctorActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        Picasso.get()
            .load(doctor.profilePicLink)
            .into(doctorprofileBinding.imgPfp)
        doctorprofileBinding.btnEdit.setOnClickListener {
            val intent = Intent(this@DoctorActivity, DoctorEditDataActivity::class.java)
            intent.putExtra("doctordata",doctor)
            startActivity(intent)
        }
        doctorprofileBinding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_appts -> {
                    binding.bottomNavigation.selectedItemId=R.id.nav_appts
                    setContentView(binding.root)
                    true
                }
                R.id.nav_patients -> {
                    binding2.bottomNavigation.selectedItemId=R.id.nav_patients
                    setContentView(binding2.root)
                    true
                }
                R.id.nav_profile -> {
                    setContentView(doctorprofileBinding.root)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = intent.getStringExtra("UID").toString()
        binding= ActivityDoctorHomeBinding.inflate(layoutInflater)
        binding2=ActivityPatientsBinding.inflate(layoutInflater)
        doctorprofileBinding=DoctorprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataViewModel= DataViewModel()
        dataViewModel3= DataViewModel()
        recyclerView = binding.rvDoctors
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        dataViewModel.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataStates.getSchedulesWithPatientsSuccess -> {
                    patientlist.clear()
                    patientlist.addAll(dataState.patients)
                    count=dataState.appointments.size
                    val appointments=dataState.appointments.sortedWith(compareBy({it.patientId},{it.unix}))
                    val appointmentsByTime = dataState.appointments.sortedWith(compareBy { it.unix })
                    mAdapter = MyAdapter(appointmentsByTime,count,this)
                    binding2.rvPatients.adapter=MyAdapter(appointments,count,this)
                    binding2.rvPatients.layoutManager=LinearLayoutManager(this)
                    recyclerView.adapter = mAdapter
                }
                DataStates.Error -> Toast.makeText(this, "ERROR OCCURED", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        })

        dataViewModel.getAppointmentsForDoctorWithPatientList(uid)
        dataViewModel3.getDoctor(uid)
        dataViewModel3.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataStates.getDoctorDataSuccess -> {
                    doctor=dataState.doctor
                    doctorProfileInit()
                }
                DataStates.Error -> Toast.makeText(this, "ERROR OCCURED", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        })
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_appts -> {
                    setContentView(binding.root)
                    true
                }
                R.id.nav_patients -> {
                    binding2.bottomNavigation.selectedItemId=R.id.nav_patients
                    setContentView(binding2.root)
                    true
                }
                R.id.nav_profile -> {
                    doctorprofileBinding.bottomNavigation.selectedItemId=R.id.nav_profile
                    setContentView(doctorprofileBinding.root)
                    true
                }
                else -> false
            }
        }

        binding2.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_appts -> {
                    binding.bottomNavigation.selectedItemId=R.id.nav_appts
                    setContentView(binding.root)
                    true
                }
                R.id.nav_patients -> {
                    setContentView(binding2.root)
                    true
                }
                R.id.nav_profile -> {
                    doctorprofileBinding.bottomNavigation.selectedItemId=R.id.nav_profile
                    setContentView(doctorprofileBinding.root)
                    true
                }
                else -> false
            }
        }
    }

    private inner class MyAdapter(
        private val documentList: List<Appointment>,
        private val itemcount: Int,
        private val context: Context) : RecyclerView.Adapter<DoctorActivity.MyAdapter.ViewHolder>() {

        // ViewHolder class
        inner class ViewHolder(private val appointmentCardBinding: AppointmentCardBinding, private val context: Context) : RecyclerView.ViewHolder(appointmentCardBinding.root) {
            fun bind(apt:Appointment, pos:Int) {
                apt.let {

                }
                Picasso.get()
                    .load(apt.pprofilePicLink)
                    .into(appointmentCardBinding.imageButton1)
                appointmentCardBinding.tvFee.text=apt.price+" PHP"
                appointmentCardBinding.tvName.text= apt.plastName+ ", " + apt.pfirstName+ " " + apt.pmiddleName + "."
                appointmentCardBinding.layout12.removeView(appointmentCardBinding.tvLocation)
                appointmentCardBinding.tvTime.text="When: " + apt.date+" "+apt.time
                appointmentCardBinding.btnDetails.setOnClickListener() {
                    val intent = Intent(context, AppointmentCardDoctorActivity::class.java).apply {
                        putExtra(AppointmentCardDoctorActivity.EXTRA_APPOINTMENT, apt)
                        putExtra(AppointmentCardDoctorActivity.EXTRA_TYPE, type)
                    }
                    context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = AppointmentCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return ViewHolder(binding,context)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(documentList[position],position)
        }

        override fun getItemCount(): Int {
            Log.d("aasda",itemcount.toString())
            return itemcount
        }
    }


}
