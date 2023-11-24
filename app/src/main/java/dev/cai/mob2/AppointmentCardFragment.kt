package dev.cai.mob2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.cai.mob2.databinding.ActivityViewschedulepatientcardBinding
import dev.cai.mob2.databinding.AppointmentCardBinding

class AppointmentCardFragment : AppCompatActivity() {

    private lateinit var appointment: Appointment
    private lateinit var type: String
    private lateinit var binding: ActivityViewschedulepatientcardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewschedulepatientcard)

        // Retrieve Appointment and Type from Intent
        intent.extras?.let {
            appointment = it.getSerializable(EXTRA_APPOINTMENT) as Appointment
            type = it.getString(EXTRA_TYPE) ?: ""
        }
        val dname: TextView = binding.tvDocname
        val demail: TextView = binding.tvDocemail
        val dphone: TextView = binding.tvDocphone
        val dtype: TextView = binding.tvDoctype
        val drate: TextView = binding.tvDocrate
        val ddate: TextView = binding.tvDate
        val bdemail: ImageButton =binding.btnDemail
        val bdphone: ImageButton =binding.btnPhone
        val back: Button =binding.btnBack
        val sched: Button =binding.btnSched
        dname.setText(appointment.plastName+", "+appointment.pfirstName + " " + appointment.pmiddleName + ".")
        ddate.setText(appointment.date +" "+ appointment.time)
        drate.setText("Fees: PHP " + appointment.price)

        demail.setText(appointment.demail)
        dphone.setText(appointment.dphoneNo)

        sched.setOnClickListener() {
            
            onBackPressed()
        }
        bdphone.setOnClickListener(){

            val phoneNumber= appointment.dphoneNo
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
        bdemail.setOnClickListener() {
                        val selection= appointment.demail
                        val recipients = arrayOf(selection)

                        val intentSelector = Intent(Intent.ACTION_SENDTO)
                        intentSelector.data = Uri.parse("mailto:")
                        val emailIntent = Intent(Intent.ACTION_SEND)
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients)
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "test")
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "test")
                        emailIntent.selector = intentSelector

                        startActivity(Intent.createChooser(emailIntent, "Send email"))
                        true
        }
    }

    companion object {
        const val EXTRA_APPOINTMENT = "extra_appointment"
        const val EXTRA_TYPE = "extra_type"
    }
}