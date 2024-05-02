package com.example.alphatrade

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class HomePage : AppCompatActivity() {

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private  lateinit var drawerLayout: DrawerLayout
    var apiKeyFinnub: String = ""
    var apiKeyAlphaVantage: String = ""
    lateinit var userID: String
    private var userData: DocumentSnapshot? = null
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        drawerLayout = findViewById(R.id.drawerLayout)
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) // This class is used to tie together the navigation drawer and the action bar. It allows the navigation drawer to be opened and closed by tapping the app's "hamburger" icon in the action bar.
        drawerLayout.addDrawerListener(drawerToggle) // This line adds the ActionBarDrawerToggle as a listener to the DrawerLayout. This enables the toggle to respond to drawer events, such as opening and closing.
        drawerToggle.syncState() // This method synchronizes the state of the toggle icon (hamburger icon) with the state of the drawer.


        supportActionBar?.setDisplayHomeAsUpEnabled(true) // This allows home button (hamburger) to also act as a back button when drawer is opened
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.actionbar))) // Sets background colour for the action bar

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val prox = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        val proximitySensorEventListener = object: SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {

                if (event!!.sensor.type == Sensor.TYPE_PROXIMITY) {

                    if (event!!.values[0] == 0f) {

                        Toast.makeText(applicationContext, "Device is too close to your eyes!", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                return
            }

        }

        sensorManager.registerListener(proximitySensorEventListener, prox, SensorManager.SENSOR_DELAY_NORMAL)

        val navDrawerUsername = findViewById<TextView>(R.id.nav_drawer_username)
        imageView = findViewById<ImageView>(R.id.userImage)

        val firestore = FirebaseFirestore.getInstance()

        userID = intent.getStringExtra("UserID")!!
        var documentRef = firestore.collection("user_profiles").document(userID)
        val imageDocumentRef = Firebase.storage.getReference().child("profile_pictures/$userID.png")

        documentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    // Document exists, retrieve the value of a specific key
                    userData = documentSnapshot

                    navDrawerUsername.text = userData?.getString("name")

                }
            }
            .addOnFailureListener { e ->
                // Error occurred while retrieving the document
                Log.e("Firestore", "Error retrieving document", e)
            }

        val ONE_MEGABYTE: Long = 1024 * 1024
        imageDocumentRef.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener { bytes ->
                // Convert downloaded data into Bitmap
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                // Display the Bitmap in the ImageView
                imageView.setImageBitmap(bitmap)
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during download
                Log.e("Image", "Error downloading image", exception)
            }

        val apiDocRef = firestore.collection("api_keys").document("api_keys")
        apiDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    // Document exists, retrieve the value of a specific key
                    apiKeyFinnub = documentSnapshot.getString("finnub")!!
                    apiKeyAlphaVantage = documentSnapshot.getString("alphaVantage")!!
                    Log.d("FinnubAPI", "$apiKeyFinnub\n$apiKeyAlphaVantage")

                    val watchListFragment = WatchListPage()

                    // Get the FragmentManager and start a transaction
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()

                    // Replace the container layout with the SignInFragment
                    fragmentTransaction.replace(R.id.container, watchListFragment)

                    // Add the transaction to the back stack
                    fragmentTransaction.addToBackStack(null)

                    // Commit the transaction
                    fragmentTransaction.commit()
                }
            }
            .addOnFailureListener { e ->
                // Error occurred while retrieving the document
                Log.e("FirestoreAPI", "Error retrieving API document", e)
            }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        if (item.itemId == R.id.btn_sign_out) {

            val loggedInSharedPreferences = getSharedPreferences("LoggedIn", Context.MODE_PRIVATE)
            val loggedInList = loggedInSharedPreferences.getString("login", "")!!.split(", ").toMutableList()
            loggedInList[0] = "false"

            loggedInSharedPreferences.edit().putString("login", loggedInList.joinToString(", ")).apply()
            val restartIntent = Intent(this, SplashScreen::class.java)

            restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(restartIntent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    fun addStock(view: View) {

        val addStockFragment = AddStockPage()

        // Get the FragmentManager and start a transaction
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.container, addStockFragment)

        // Add the transaction to the back stack
        fragmentTransaction.addToBackStack(null)

        // Commit the transaction
        fragmentTransaction.commit()
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    fun communityHub(view: View) {

        val intent = Intent(this, CommunityHub::class.java)

        val bitmap = imageView.drawable.toBitmap(1000, 1000)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        intent.putExtra("userID", userID)
        intent.putExtra("name", userData?.getString("name"))
        intent.putExtra("profile_picture", byteArray)
        startActivity(intent)
    }

}