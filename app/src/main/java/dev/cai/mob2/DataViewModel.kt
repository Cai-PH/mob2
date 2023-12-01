package dev.cai.mob2

import android.net.Uri
import android.provider.ContactsContract.Data
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID

class DataViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db_users = Firebase.database.reference
    private var fb_storage = Firebase.storage.reference
    private val _dataState = MutableLiveData<DataStates>()
    val dataState: LiveData<DataStates> = _dataState

    fun getDataStates(): LiveData<DataStates> = _dataState
    fun firebaseAuthWithGoogle(account: String?) {
        _dataState.value=DataStates.Loading
        val credential = GoogleAuthProvider.getCredential(account, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) _dataState.value = DataStates.authUserSuccess(auth.currentUser!!.uid)
            else _dataState.value = DataStates.Error
        }.addOnFailureListener {
            _dataState.value = DataStates.Error
        }
    }
    fun attemptSignIn() {

    }
    fun uploadProfilePic(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    _dataState.value = DataStates.uploadProfileImageSuccess(uri.toString())
                }
            }
            .addOnFailureListener {
                _dataState.value = DataStates.Error
            }
    }
    fun createPatient(patient: Patient) {
        _dataState.value=DataStates.Loading
        val userRef = FirebaseDatabase.getInstance().getReference("patients")
        userRef.child(patient.patientId).setValue(patient)
        FirebaseDatabase.getInstance().getReference("users").child(patient.patientId).setValue(User(userType = "Patient", userId = patient.patientId))
        _dataState.value = DataStates.uploadPatientDataSuccess(patient)
    }
    fun createDoctor(doctor: Doctor) {
        _dataState.value=DataStates.Loading
        val userRef = FirebaseDatabase.getInstance().getReference("doctors")
        userRef.child(doctor.doctorId).setValue(doctor)
        _dataState.value = DataStates.uploadDoctorDataSuccess(doctor)

    }
    fun getUserData(uid: String) {
        _dataState.value=DataStates.Loading
        val userRef = FirebaseDatabase.getInstance().getReference("users")
        Log.d("","check call")
        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("","user exist2")
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java) ?: User()
                    _dataState.value = DataStates.getUserDataSuccess(user)
                    Log.d("","user exist")
                } else {
                    Log.d("","user not exist")
                    _dataState.value=DataStates.userNotExist
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("","error as whole")
                _dataState.value = DataStates.Error
            }
        })

    }
    fun createAndUploadAppointment(doctor: Doctor, patient: Patient, date:String,time:String, unix:Long ) {
        _dataState.value = DataStates.Loading
        val appointment = Appointment(
            scheduleId = UUID.randomUUID().toString(),
            patientId = patient.patientId,
            pfirstName = patient.firstName,
            plastName = patient.lastName,
            pmiddleName = patient.middleName,
            pphoneNo = patient.phoneNo,
            pprofilePicLink = patient.profilePicLink,
            pemail = patient.email,
            doctorId = doctor.doctorId,
            dfirstName = doctor.firstName,
            dlastName = doctor.lastName,
            dmiddleName = doctor.middleName,
            dphoneNo = doctor.phoneNo,
            dprofilePicLink = doctor.profilePicLink,
            demail = doctor.email,
            price = doctor.rate.toString(),
            date = date,
            time = time,
            unix = unix
        )
        val map = doctor.activeTakenSlots.toMutableMap()
        val list = mutableListOf<String>(time)
        if (map.containsKey(date)) {
            list.addAll(map.get(date)?:listOf())
        }

        val userRef = FirebaseDatabase.getInstance().getReference("doctors").child(doctor.doctorId).child("activeTakenSlots").child(date)
        userRef.setValue(list)

        val databaseReference = FirebaseDatabase.getInstance().getReference("appointments")
        databaseReference.child(appointment.scheduleId).setValue(appointment)
            .addOnSuccessListener {
                _dataState.value = DataStates.addScheduleSuccess
            }
            .addOnFailureListener {
                _dataState.value = DataStates.Error
            }
    }
    fun createUser(uid:String,type:String) {
        _dataState.value=DataStates.Loading
        val userRef = FirebaseDatabase.getInstance().getReference("users")

        userRef.child(uid).setValue(User(userId = uid, userType = type))
        _dataState.value = DataStates.createUserSuccess
    }
    fun getAppointmentsForDoctor(doctorId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("appointments")
        val query = databaseReference.orderByChild("doctorId").equalTo(doctorId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val appointments = mutableListOf<Appointment>()
                for (snapshot in dataSnapshot.children) {
                    val appointment = snapshot.getValue(Appointment::class.java)
                    appointment?.let {
                        appointments.add(it)
                    }
                }
                _dataState.value=DataStates.getSchedulesSuccess(appointments)
                // Here you have a list of appointments for the specified doctor
                // You can pass this list to the UI or handle it as needed
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _dataState.value=DataStates.Error
            }
        })
    }
    fun getLinkedPatientDocAppointment(doctorId: String,patientId:String){

        val databaseReference = FirebaseDatabase.getInstance().getReference("appointments")
        val query = databaseReference.orderByChild("doctorId").equalTo(doctorId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val appointments = mutableListOf<Appointment>()
                for (snapshot in dataSnapshot.children) {
                    val appointment = snapshot.getValue(Appointment::class.java)
                    appointment?.let {
                        if(it.patientId==patientId) {
                            appointments.add(it)
                        }
                    }
                }
                _dataState.value=DataStates.getSchedulesSuccess(appointments)
                // Here you have a list of appointments for the specified doctor
                // You can pass this list to the UI or handle it as needed
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _dataState.value=DataStates.Error
            }
        })
    }
    fun getAppointmentsForPatient(patientId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("appointments")
        val query = databaseReference.orderByChild("patientId").equalTo(patientId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val appointments = mutableListOf<Appointment>()
                for (snapshot in dataSnapshot.children) {
                    val appointment = snapshot.getValue(Appointment::class.java)
                    appointment?.let {
                        appointments.add(it)
                    }
                }
                _dataState.value=DataStates.getSchedulesSuccess(appointments)
                // Here you have a list of appointments for the specified patient
                // You can pass this list to the UI or handle it as needed
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _dataState.value=DataStates.Error
            }
        })
    }
    fun createAndUploadAppointment(appointment: Appointment) {
        _dataState.value = DataStates.Loading
        val databaseReference = FirebaseDatabase.getInstance().getReference("appointments")

        databaseReference.child(appointment.scheduleId).setValue(appointment)
            .addOnSuccessListener {
                _dataState.value = DataStates.createScheduleSuccess(appointment)
            }
            .addOnFailureListener { e ->
                Log.e("DataViewModel", "Failed to upload appointment", e)
                _dataState.value = DataStates.Error
            }
    }
    fun getAllPatients() {
        _dataState.value = DataStates.Loading
        val databaseReference = FirebaseDatabase.getInstance().getReference("patients")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val patients = mutableListOf<Patient>()
                for (patientSnapshot in snapshot.children) {
                    val patient = patientSnapshot.getValue(Patient::class.java)
                    patient?.let { patients.add(it) }
                }
                _dataState.value = DataStates.getAllPatientsDataSuccess(patients)
            }

            override fun onCancelled(error: DatabaseError) {
                _dataState.value = DataStates.Error
            }
        })
    }
    fun getAllDoctors() {
        _dataState.value = DataStates.Loading
        val databaseReference = FirebaseDatabase.getInstance().getReference("doctors")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctors = mutableListOf<Doctor>()
                for (docSnapshot in snapshot.children) {
                    val doctor = docSnapshot.getValue(Doctor::class.java)
                    doctor?.let { doctors.add(it)
                    Log.d("initializing doc " ,it.doctorId)}
                }
                _dataState.value = DataStates.getAllDoctorsDataSuccess(doctors)
            }

            override fun onCancelled(error: DatabaseError) {
                _dataState.value = DataStates.Error
            }
        })
    }
    fun getDoctor(doctorId:String) {
        _dataState.value = DataStates.Loading
        val databaseReference = FirebaseDatabase.getInstance().getReference("doctors").child(doctorId)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                    val doctor = snapshot.getValue(Doctor::class.java)
                _dataState.value = DataStates.getDoctorDataSuccess(doctor!!)
            }
            override fun onCancelled(error: DatabaseError) {
                _dataState.value = DataStates.Error
            }
        })
    }
    fun getPatient(patientId:String) {
        _dataState.value = DataStates.Loading
        val databaseReference = FirebaseDatabase.getInstance().getReference("patients").child(patientId)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val patient = snapshot.getValue(Patient::class.java)
                _dataState.value = DataStates.getPatientDataSuccess(patient!!)
            }
            override fun onCancelled(error: DatabaseError) {
                _dataState.value = DataStates.Error
            }
        })
    }

    fun deleteAppointment(appointment: Appointment) {
        _dataState.value = DataStates.Loading

        var databaseReference =
            FirebaseDatabase.getInstance().getReference("doctors").child(appointment.doctorId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctor = snapshot.getValue(Doctor::class.java)!!
                val map = doctor.activeTakenSlots.toMutableMap()
                if (map.containsKey(appointment.date)) {
                    val list = map.get(appointment.date)!!.toMutableList()
                    list.removeAll(listOf(appointment.time))
                    val userRef = FirebaseDatabase.getInstance().getReference("doctors").child(appointment.doctorId).child("activeTakenSlots").child(appointment.date)
                    userRef.setValue(list)

                    databaseReference = FirebaseDatabase.getInstance().getReference("appointments")
                    databaseReference.child(appointment.scheduleId).removeValue()
                        .addOnSuccessListener {
                            _dataState.value = DataStates.deleteScheduleSuccess
                        }
                        .addOnFailureListener {
                            _dataState.value = DataStates.Error
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                _dataState.value = DataStates.Error
            }
        })
    }
}