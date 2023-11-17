package dev.cai.mob2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dev.cai.mob2.databinding.ActivitySignup2Binding

class MainActivity : AppCompatActivity() {
    private lateinit var dataViewModel: DataViewModel
    private lateinit var activitySignup2Binding: ActivitySignup2Binding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private val RC_SIGN_IN = 9001
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        dataViewModel=DataViewModel()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mGoogleSignInClient.revokeAccess()

        val signInButton: Button = findViewById(R.id.signInBtn)
        signInButton.setOnClickListener {
            signIn()
        }
        dataViewModel.dataState.observe(this, Observer {
            dataState ->
            when (dataState) {
                is DataStates.Error -> {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()

                    Log.d("aasdc", "test6"+ uid?:"test bcd" )
                }
                DataStates.addScheduleSuccess -> TODO()
                is DataStates.authUserSuccess ->{
                    dataViewModel.getUserData(dataState.uid)
                    uid=dataState.uid
                    Log.d("aasdc", "test3"+ uid?:"test bcd" )
                }
                is DataStates.uploadProfileImageSuccess-> TODO()
                DataStates.deleteScheduleSuccess -> TODO()
                is DataStates.getAllDoctorsDataSuccess -> TODO()
                is DataStates.getAllPatientsDataSuccess -> TODO()
                is DataStates.getDoctorDataSuccess -> TODO()
                is DataStates.getPatientDataSuccess -> TODO()
                is DataStates.uploadDoctorDataSuccess -> TODO()
                is DataStates.uploadPatientDataSuccess -> TODO()
                is DataStates.userNotExist ->{
                    Log.d("aasdc", "test4"+ uid?:"test bcd" )
                    createUser()
                }
                is DataStates.getUserDataSuccess -> {
                    Log.d("aasdc", "test5"+ uid?:"test bcd" )
                    loginUser(dataState.user)
                }
                else -> {}
            }
        })
    }
    private fun createUser() {
        Log.d("aasdc", "test2"+ uid?:"test bcd" )
        activitySignup2Binding =ActivitySignup2Binding.inflate(layoutInflater)
        setContentView(activitySignup2Binding.root)

        var autoCompleteTxt = findViewById<AutoCompleteTextView>(R.id.tv_users)
        var adapterItems = ArrayAdapter<String>(this, R.layout.dropdown_users, resources.getStringArray(R.array.users))
        autoCompleteTxt.setAdapter(adapterItems)
        autoCompleteTxt.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position).toString()
        })
        activitySignup2Binding.btnNext.setOnClickListener() {
            val selection = activitySignup2Binding.tvUsers.text.toString().lowercase()
            if(selection=="patient") {
                intent = Intent(this@MainActivity, PatientSignUpActivity::class.java)
                intent.putExtra("UID", uid);
                startActivity(intent);
            } else if (selection=="doctor") {
                val intent = Intent(this@MainActivity, DoctorSignUpActivity::class.java)
                intent.putExtra("UID", uid);
                startActivity(intent);
            }
        }
    }
    private fun loginUser(user:User) {
        if (user.userType=="Patient") {
            intent = Intent(this@MainActivity, PatientActivity::class.java)
            Log.d("aasdc", uid?:"")
            intent.putExtra("UID", uid);
            startActivity(intent);
        } else if (user.userType=="Doctor") {
            intent = Intent(this@MainActivity, DoctorActivity::class.java)
            Log.d("aasdc", uid?:"")
            intent.putExtra("UID", uid);
            startActivity(intent);

        } else {
            Toast.makeText(this, "Invalid user data. Please Contact Administrator", Toast.LENGTH_SHORT).show()
        }

    }
    private fun signIn() {
        Log.d("aasdc", "test"+ uid?:"test bcd" )
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                dataViewModel.firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign-in failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}