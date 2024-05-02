package com.example.alphatrade

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class CustomViewChat(context: Context, encodedImage: String, message: String, name:String): LinearLayout(context) {

    private lateinit var imgView: ImageView
    private lateinit var textBar: TextView
    private lateinit var chatName: TextView
    private val maxChatSize: Long = 20

    init {
        LayoutInflater.from(context).inflate(R.layout.customview_chat, this, true)

        imgView = findViewById(R.id.chatImage)
        textBar = findViewById(R.id.textBar)
        textBar.text = message
        chatName = findViewById(R.id.chatName)
        chatName.text = name
        updateImage(imgView, encodedImage)
        updateDatabase(message, encodedImage, name)

    }

    fun updateImage(imgView: ImageView, encodedImage: String) {

        if (encodedImage != null) {

            val imageBytes = Base64.decode(encodedImage , Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes , 0 , imageBytes.size)
            imgView.setImageBitmap(decodedImage)
        }
    }

    fun updateDatabase(message: String, encodedImage: String, name: String) {

        val queueCollection = FirebaseFirestore.getInstance().collection("queue")
        val queue = mutableListOf<DocumentSnapshot>()

        queueCollection.orderBy(FieldPath.documentId())
            .limit(maxChatSize)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val data = hashMapOf(
                    "message" to message,
                    "name" to name,
                    "profile_picture" to encodedImage
                )

                if (querySnapshot.size() < maxChatSize) {

                    queueCollection.document((querySnapshot.size()+1).toString()).set(data)
                }
                else {

                    val docRef = queueCollection.document(maxChatSize.toString())
                    docRef.delete()

                    for (i in 1 until querySnapshot.size()) {

                        queue.add(querySnapshot.documents[i])
                    }

                    fun updateDocumentsInQueue(index: Int) {
                        if (index < queue.size) {

                            queueCollection.document((index + 1).toString()).set(queue[index].data!!)
                                .addOnSuccessListener {

                                    updateDocumentsInQueue(index + 1) // Proceed to the next iteration
                                }
                        }
                        else {

                            queueCollection.document(maxChatSize.toString()).set(data)
                        }
                    }
                    updateDocumentsInQueue(0)
                }
            }
            .addOnFailureListener { e ->
                // Error occurred while retrieving the documents
            }

    }
}