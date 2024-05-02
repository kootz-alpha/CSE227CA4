package com.example.alphatrade

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginPage : AppCompatActivity() {

    lateinit var uname: EditText
    lateinit var pass: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        uname = findViewById(R.id.etUNameLogin)
        pass = findViewById(R.id.etPassLogin)
    }

    fun signIn(v:View){

        var int = Intent(this, SignInPage::class.java)
        startActivity(int)
    }

    fun login(v:View){
        var int = Intent(this,HomePage::class.java)

        val user_id = uname.text.toString() // uname refers to the textview for username in login page
        val password = pass.text.toString() // pass refers to the textview for password in login page

        val auth = FirebaseAuth.getInstance() // Creating an instance of Firebase Authentication
        auth.signInWithEmailAndPassword("$user_id@dummy.com", password) // Since this function allows login with email and password only, and as we have username instead of email. We pass in our username in the form of a dummy email
            .addOnCompleteListener { task -> // As signInWithEmailAndPassword is an asynchronous function, we will have to do all our next steps inside a call-back function
                if (task.isSuccessful) {
                    // User authentication successful
                    val user = auth.currentUser!!
                    val uid = user.uid

                    val loggedInSharedPreferences = getSharedPreferences("LoggedIn", Context.MODE_PRIVATE)
                    val loggedInData = loggedInSharedPreferences.getString("login", "")!!.split(", ").toMutableList()
                    loggedInData[0] = "true"
                    loggedInData[1] = uid

                    loggedInSharedPreferences.edit().putString("login", loggedInData.joinToString(", ")).apply()

                    int.putExtra("UserID", uid)
                    startActivity(int)
                    finish()

                } else {
                    // User authentication failed
                    Toast.makeText(this, "Incorrect Id or Password", Toast.LENGTH_SHORT).show()
                }
            }
    }
}