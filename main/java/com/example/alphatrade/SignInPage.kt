package com.example.alphatrade

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.InputStream

class SignInPage : AppCompatActivity() {

    lateinit var name: EditText
    lateinit var pass: EditText
    lateinit var userId: EditText
    lateinit var emailId: EditText
    lateinit var cameraSwitch: Switch
    lateinit var click_image: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private val pic_id = 742

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_page)

        name = findViewById(R.id.etName)
        pass = findViewById(R.id.etPass)
        userId = findViewById(R.id.etUName)
        emailId = findViewById(R.id.etEmail)
        cameraSwitch = findViewById(R.id.camera_switch)
        click_image = findViewById(R.id.click_image)

        sharedPreferences = getSharedPreferences("UserData" , Context.MODE_PRIVATE)

        cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

                if (galleryIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(galleryIntent, pic_id)
                } else {

                    Toast.makeText(this, "No gallery app available", Toast.LENGTH_SHORT).show()
                }

            } else {
                // The switch is off
                val layoutParams = click_image.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.width = 0
                layoutParams.height = 0
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = 0
                click_image.layoutParams = layoutParams

                click_image.setImageDrawable(null)
            }
        }

    }

    fun signUp(v: View){

        val full_name = name.text.toString().trim()
        val password = pass.text.toString()
        val user_id = userId.text.toString().trim()
        val user_email = emailId.text.toString().trim()

        if (full_name != "" && password != "" && user_id != "") {

            if (Patterns.EMAIL_ADDRESS.matcher(user_email).matches()) {

                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                val userProfilesCollection = firestore.collection("user_profiles")


                auth.createUserWithEmailAndPassword("$user_id@dummy.com", password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser!!
                        val uid = user.uid

                        val userProfileData = hashMapOf(
                            "email" to user_email,
                            "name" to full_name,
                            "watchlist" to ""
                        )

                        userProfilesCollection.document(uid).set(userProfileData)
                            .addOnSuccessListener {
                                Toast.makeText(this , "SignUp successful" , Toast.LENGTH_SHORT).show()
                                saveImage(click_image, uid.toString(), this)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error adding document", e)
                            }
                    } else {

                        Toast.makeText(this , "SignUp failed!" , Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else {
                Toast.makeText(this , "Incorrect email-id!" , Toast.LENGTH_SHORT).show()
            }

        }
        else {
            Toast.makeText(this , "Fill the details carefully" , Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pic_id && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {

                val inputStream: InputStream? = contentResolver.openInputStream(it)
                var imageBitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap!!, 1000, 1000, true)

                if (imageBitmap!!.byteCount > 200*1024) {

                    val quality = (200*1024*100)/imageBitmap!!.byteCount
                    val outputStream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    imageBitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
                }

                click_image.setImageBitmap(imageBitmap)
            }
        }
    }

    fun saveImage(imageView: ImageView, uid: String, context: Context){

        var bitmap = imageView.drawable.toBitmap()

        if (bitmap == null) {

            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.default_user)
        }

        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("profile_pictures/$uid.png")

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val data = outputStream.toByteArray()

        // Upload image to Firebase Storage
        imageRef.putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully
                Log.d(TAG, "Image uploaded successfully: ${taskSnapshot.metadata?.path}")
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful upload
                Log.e(TAG, "Error uploading image", exception)
            }
    }
}