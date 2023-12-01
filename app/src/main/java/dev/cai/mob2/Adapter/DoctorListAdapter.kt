package com.example.softeng2.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.cai.mob2.Doctor
import dev.cai.mob2.DoctorListActivity
import dev.cai.mob2.PatientActivity
import dev.cai.mob2.ScheduleActivity
import dev.cai.mob2.databinding.DoctorCardBinding

class DoctorListAdapter(
    private var documentList: MutableList<Doctor>,
    private val context: Context,
    private val puid: String
) : RecyclerView.Adapter<DoctorListAdapter.ViewHolder>(){
    var num=0
    fun updateDoctors(newDoctor: List<Doctor>) {
        documentList.clear()
        documentList.addAll(newDoctor)
        notifyDataSetChanged()
    }
    class ViewHolder(private val doctorCardBinding: DoctorCardBinding, private val context: Context) : RecyclerView.ViewHolder(doctorCardBinding.root){
        fun bind(doc:Doctor, pos:Int) {
            Log.d("errorasd",doc.doctorId.toString())
                    doctorCardBinding.tvName.text = (
                            doc.lastName.toString()
                                    + ", " + doc.firstName.toString()
                                    + " " + doc.middleName
                                .toString() + ".")

                        doctorCardBinding.tvType.text = (
                                doc.type.toString())

                        doctorCardBinding.tvFee.text = ("PHP " +
                                doc.rate.toString())
            Picasso.get()
                .load(doc.profilePicLink)
                .into(doctorCardBinding.ivPerson)
            doctorCardBinding.btnBook.setOnClickListener() {
                val uid= (context as PatientActivity).intent.getStringExtra("UID")
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("PUID",uid)
                intent.putExtra("DUID",doc.doctorId)
                Log.d("asdc",uid+" " + doc.doctorId)
                context.startActivity(intent)
            }
            doctorCardBinding.tvBio.text=doc.about
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DoctorCardBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding,context)
    }

    override fun getItemCount() =documentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(documentList[position], position)
    }

}