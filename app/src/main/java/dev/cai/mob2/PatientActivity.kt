package dev.cai.mob2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.softeng2.Adapter.DoctorListAdapter
import com.squareup.picasso.Picasso
import dev.cai.mob2.databinding.ActivityDoctorsBinding
import dev.cai.mob2.databinding.ActivityPatientHomeBinding
import dev.cai.mob2.databinding.AppointmentCardBinding

class PatientActivity : AppCompatActivity(){
    private lateinit var patientHomeBinding: ActivityPatientHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: MyAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var dataViewModel: DataViewModel
    private lateinit var scheduleButton:Button
    private var count:Int=0


    private lateinit var activityDoctorsBinding:ActivityDoctorsBinding
    private lateinit var rvDlist: RecyclerView
    private lateinit var adapterDlist: DoctorListAdapter
    private lateinit var dataViewModel2: DataViewModel
    private var count2:Int=0
    private lateinit var uid:String;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        patientHomeBinding=ActivityPatientHomeBinding.inflate(layoutInflater)
        scheduleButton= patientHomeBinding.btnFindDoc
        dataViewModel= DataViewModel()
        recyclerView = patientHomeBinding.rvPatients

        // Set the action bar's Home button to act as the navigation drawer toggle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        uid= intent.getStringExtra("UID").toString()
        scheduleButton.setOnClickListener {
            setContentView(activityDoctorsBinding.root)
        }
        // Set up the RecyclerView
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        dataViewModel.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataStates.getSchedulesSuccess -> {
                    count=dataState.appointments.size
                    mAdapter = MyAdapter(dataState.appointments,count,this)
                    recyclerView.adapter = mAdapter
                }
                DataStates.Error -> Toast.makeText(this, "ERROR OCCURED", Toast.LENGTH_SHORT).show()
                else -> {}
            }
        })
        dataViewModel.getAppointmentsForPatient(intent.getStringExtra("UID").toString())
        mAdapter = MyAdapter(emptyList(),count, this)
        recyclerView.adapter = mAdapter

        setContentView(patientHomeBinding.root)


        dataViewModel2= DataViewModel()
        activityDoctorsBinding= ActivityDoctorsBinding.inflate(layoutInflater)
        dataViewModel2.getAllDoctors()

        adapterDlist = DoctorListAdapter(mutableListOf<Doctor>() ,this,uid)
        activityDoctorsBinding.rvDoctors.adapter = adapterDlist;
        activityDoctorsBinding.rvDoctors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dataViewModel2.dataState.observe(this, Observer { dataState->
            when (dataState) {
                is DataStates.getAllDoctorsDataSuccess -> {
                    adapterDlist.updateDoctors(dataState.users)
                    Log.d("initializing doc " ,dataState.users.size.toString() + ""+count)
                }
                DataStates.Loading -> {
                    // Show loading indicator
                }
                DataStates.Error -> {
                    // Handle error
                }

                else -> {}
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle ActionBarDrawerToggle clicks
        return super.onOptionsItemSelected(item)
    }


    private inner class MyAdapter(
        private val documentList: List<Appointment>,
        private val itemcount: Int,
        private val context: Context) : RecyclerView.Adapter<PatientActivity.MyAdapter.ViewHolder>() {

        // ViewHolder class
        inner class ViewHolder(private val appointmentCardBinding: AppointmentCardBinding,private val context: Context) : RecyclerView.ViewHolder(appointmentCardBinding.root) {
            fun bind(apt:Appointment, pos:Int) {
                apt.let {

                }
                Picasso.get()
                    .load(apt.dprofilePicLink)
                    .into(appointmentCardBinding.imageButton1)
                appointmentCardBinding.tvFee.text=apt.price+" PHP"
                appointmentCardBinding.tvName.text= apt.dlastName+ ", " + apt.dfirstName+ " " + apt.dmiddleName + "."
                appointmentCardBinding.layout12.removeView(appointmentCardBinding.tvLocation)
                appointmentCardBinding.tvTime.text="When: " + apt.date+" "+apt.time
                appointmentCardBinding.btnDetails.setOnClickListener() {
                    val intent = Intent(context, AppointmentCardFragment::class.java).apply {
                        putExtra(AppointmentCardFragment.EXTRA_APPOINTMENT, apt)
                        putExtra(AppointmentCardFragment.EXTRA_TYPE, type)
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

    override fun onBackPressed() {
        if(false) {
            super.onBackPressed()
        }
    }
}
