package dev.cai.mob2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.softeng2.Adapter.DoctorListAdapter
import dev.cai.mob2.databinding.ActivityDoctorsBinding

class DoctorListActivity : AppCompatActivity(){

    private lateinit var activityDoctorsBinding:ActivityDoctorsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DoctorListAdapter
    private lateinit var dataViewModel: DataViewModel
    private var count:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataViewModel= DataViewModel()
        activityDoctorsBinding= ActivityDoctorsBinding.inflate(layoutInflater)
        setContentView(activityDoctorsBinding.root)
        val puid= intent.getStringExtra("UID")?:""
        dataViewModel.getAllDoctors()
        dataViewModel.dataState.observe(this, Observer { dataState->
            when (dataState) {
                is DataStates.getAllDoctorsDataSuccess -> {
                    count = dataState.users.size
                    adapter = DoctorListAdapter(dataState.users ,count,this,puid)
                    activityDoctorsBinding.rvDoctors.adapter = adapter;
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
        activityDoctorsBinding.rvDoctors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        setContentView(activityDoctorsBinding.root)
    }
}