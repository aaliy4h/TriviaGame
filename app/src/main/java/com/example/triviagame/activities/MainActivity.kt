package com.example.triviagame.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.triviagame.R
import com.example.triviagame.fragments.AccountDialogFragment
import com.example.triviagame.helpers.DatabaseHelper
import com.example.triviagame.models.Pet
import com.example.triviagame.models.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * References:
 *
 * [1] - used in PetWorker
 *  * https://medium.com/@arjunnarikkuni00/workmanager-using-kotlin-android-c72660afef31
 *  * Author: Arjun V
 *  * Date Published: 26.07.2023
 *  * Date Accessed: 08.12.2023
 *
 * [2] - used in AlarmReceiver, HomeScreenActivity, and PetWorker
 * https://medium.com/@tolgapirim25/send-notifications-at-a-specific-time-with-alarm-manager-on-android-13c7cc9d8e7a
 * Author: Tolga Pirim
 * Date Published: 15.01.2024
 * Date Accessed: 05.12.2024
 *
 * [3] - used in StatisticsActivity
 * https://medium.com/@charles-raj/creating-material-carousels-in-android-with-recyclerview-and-maskableframelayout-68c54998a6fd
 * Author: Charles Raj Iruthayaraj
 * Date Published: 23.09.2024
 * Date Accessed: 01.12.2024
 *
 * [4] - used in GameActivity
 * https://www.dhiwise.com/post/kotlin-timer-how-to-create-a-simple-countdown-timer
 * Author: Dhruv Gandhi
 * Date Published: 01.10.2024
 * Date Accessed: 29.11.2024
 */

/**
 * Entry point of the application containing the login and register buttons
 */
class MainActivity : AppCompatActivity(), AccountDialogFragment.DialogListener {

    private val mDatabase = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Firebase.auth.currentUser != null) {
            val intent = Intent(this, HomeScreenActivity::class.java)
            startActivity(intent)
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.app_bar)
        setSupportActionBar(toolbar)



        val registerButton = findViewById<MaterialButton>(R.id.register_button)
        registerButton.setOnClickListener {
            AccountDialogFragment(getString(R.string.register), 0)
                .show(supportFragmentManager, "AccountDialog")
        }

        val loginButton = findViewById<MaterialButton>(R.id.login_button)
        loginButton.setOnClickListener {
            val dialog = AccountDialogFragment(getString(R.string.login), 1)
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, "AccountDialog")
        }
    }

    /**
     * Receives the registration or log in result and handles them accordingly
     */
    override fun onDialogResult(username: String, password: String, position: Int) {
        if (password.length < 6) {
            val contextView = findViewById<View>(R.id.main)
            Snackbar.make(contextView, getString(R.string.password_length), Snackbar.LENGTH_SHORT).show()

        } else {
            val mAuth = FirebaseAuth.getInstance()
            if (position == 0) {
                mAuth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            val accountID = task.result.user?.uid
                            val user = User(null, accountID, 0, 0, 0, 0, 0)
                            mDatabase.addUser(user)
                            Handler(Looper.getMainLooper()).postDelayed({
                                createSharedUserID(true)
                            }, 2000)

                            homeScreenIntent()
                        } else {
                            val contextView = findViewById<View>(R.id.main)
                            Snackbar.make(contextView, getString(R.string.invalid_user), Snackbar.LENGTH_SHORT).show()
                        }
                    }
            } else {
                mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful) {
                            createSharedUserID(false)
                            homeScreenIntent()
                        } else {
                            val contextView = findViewById<View>(R.id.main)
                            Snackbar.make(contextView, getString(R.string.invalid_user), Snackbar.LENGTH_SHORT).show()
                        }
                    }
            }

        }
    }

    /**
     * Intent to go to the home screen
     */
    private fun homeScreenIntent() {
        val intent = Intent(this, HomeScreenActivity::class.java)
        startActivity(intent)
    }

    /**
     * Creates a shared preference for the user's ID
     */
    private fun createSharedUserID(newUser: Boolean) {
        val mAuth = FirebaseAuth.getInstance()
        val accountID = mAuth.currentUser?.uid
        val sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (accountID != null) {
            val userID = mDatabase.getUserID(accountID)
            Log.d("Debug", "User ID: $userID")
            editor.putString("account_id", accountID)
            if (userID != null) {
                editor.putInt("user_id", userID)
                editor.apply()
                if (newUser) {
                    createPet(userID)
                }
            }
        }
    }

    /**
     * Creates a pet for every new user
     */
    private fun createPet(userID: Int) {
        val pet = (Pet(null, userID, "Default Pet", 50, 50, 50))
        mDatabase.addPet(pet)
    }

}