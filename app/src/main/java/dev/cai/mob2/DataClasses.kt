package dev.cai.mob2

data class Patient(
    val patientId: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val phoneNo: String = "",
    val profilePicLink: String = ""
)
data class Doctor(
    val doctorId: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val phoneNo: String = "",
    val rate: Int = 0,
    val type: String = "",
    val about: String = "",
    val activeTakenSlots: Map<String, List<String>> = emptyMap(),
    val timeSlotsSettings: List<String>,
    val profilePicLink: String = ""
)


data class Appointment(
    val scheduleId: String = "",
    val patientId: String = "",
    val pfirstName: String = "",
    val plastName: String = "",
    val pmiddleName: String = "",
    val pphoneNo: String = "",
    val pprofilePicLink: String = "",
    val pemail: String = "",
    val doctorId: String = "",
    val dfirstName: String = "",
    val dlastName: String = "",
    val dmiddleName: String = "",
    val dphoneNo: String = "",
    val dprofilePicLink: String = "",
    val demail: String = "",
    val price: String = "",
    val date: String = "",
    val time: String = "",
    val unix: Long = 0
)
data class User(
    val userId: String = "",
    val userType:String =""
)