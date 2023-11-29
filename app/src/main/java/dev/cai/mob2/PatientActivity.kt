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
import com.squareup.picasso.Picasso
import dev.cai.mob2.databinding.AppointmentCardBinding

class PatientActivity : AppCompatActivity(){
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: MyAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var dataViewModel: DataViewModel
    private lateinit var scheduleButton:Button

    private var count:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_patient_home)
        scheduleButton= findViewById(R.id.btn_findDoc)
        dataViewModel= DataViewModel()
        drawerLayout = findViewById(R.id.drawerLayout)
        recyclerView = findViewById(R.id.rv_patients)
        // Set the listener for navigation item clicks

        // Create and set the ActionBarDrawerToggle
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // Set the action bar's Home button to act as the navigation drawer toggle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scheduleButton.setOnClickListener {
            val uid= intent.getStringExtra("UID")
            Log.d("aasdb", uid?:"")
            val intent = Intent(this@PatientActivity, DoctorListActivity::class.java)
            intent.putExtra("UID",uid)
            startActivity(intent)
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle ActionBarDrawerToggle clicks
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
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
}
