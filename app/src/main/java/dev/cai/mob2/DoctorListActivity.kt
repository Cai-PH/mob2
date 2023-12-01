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
    private lateinit var rvDlist: RecyclerView
    private lateinit var adapterDlist: DoctorListAdapter
    private lateinit var dataViewModel2: DataViewModel
    private var count2:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}