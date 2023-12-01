package dev.cai.mob2

sealed class DataStates {
    data class authUserSuccess(val uid: String):DataStates()

    data class getPatientDataSuccess(val patient: Patient):DataStates()
    data class getDoctorDataSuccess(val doctor: Doctor):DataStates()

    data class uploadPatientDataSuccess(val patient: Patient):DataStates()
    data class uploadDoctorDataSuccess(val doctor: Doctor):DataStates()

    data class getUserDataSuccess(val user:User):DataStates()
    data class getAllDoctorsDataSuccess(val users:List<Doctor>) :DataStates()
    data class getAllPatientsDataSuccess(val users:List<Patient>) : DataStates()
    data class getScheduleSuccess(val appointment: Appointment):DataStates()
    data class createScheduleSuccess(val appointment: Appointment):DataStates()
    data class uploadProfileImageSuccess(val link:String):DataStates()
    data class getSchedulesSuccess(val appointments: List<Appointment>):DataStates()
    data class getSchedulesWithPatientsSuccess(
        val appointments: List<Appointment>,
        val patients: List<String>
    ):DataStates()
    data object addScheduleSuccess:DataStates()
    data object deleteScheduleSuccess:DataStates()
    data object createUserSuccess:DataStates()
    data object modifypatientappointments:DataStates()
    data object modifydoctorappointments:DataStates()

    data object userNotExist:DataStates()
    data object Loading:DataStates()
    data object Error:DataStates()

}
