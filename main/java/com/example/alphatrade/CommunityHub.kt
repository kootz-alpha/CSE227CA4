package com.example.alphatrade

import MessageSample
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Button
import android.widget.EdgeEffect
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CommunityHub : AppCompatActivity() {

    private lateinit var customViewsContainer: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var chatSend: Button
    private lateinit var name: String
    private lateinit var userID: String
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var messageList: ArrayList<MessageSample>
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_hub)

        customViewsContainer = findViewById(R.id.chatBox)
        messageBox = findViewById(R.id.messageBox)
        chatSend = findViewById(R.id.chatSend)
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("chat")
        messageList = arrayListOf()

        name = intent.getStringExtra("name")!!
        userID = intent.getStringExtra("userID")!!

        messageAdapter = MessageAdapter(messageList)
        customViewsContainer.adapter = messageAdapter
        customViewsContainer.layoutManager = LinearLayoutManager(this)

        firebaseDatabase.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(MessageSample::class.java)
                if (message != null) {

                    messageList.add(message)
                    messageAdapter.notifyItemInserted(messageList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        chatSend.setOnClickListener {

            val msgID = firebaseDatabase.push().key!!
            val newMessage = MessageSample(msgID.toString(), name.toString(), userID, messageBox.text.toString(), "")

            firebaseDatabase.child(msgID).setValue(newMessage)
                .addOnCompleteListener {

                    messageBox.text.clear()

                }.addOnFailureListener { err ->
                    Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
                }
        }




    }

    fun refreshCustomViews() {

        val collectionRef = FirebaseFirestore.getInstance().collection("community_hub")
        customViewsContainer.removeAllViews()

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // width
            LinearLayout.LayoutParams.WRAP_CONTENT // height
        )
        layoutParams.setMargins(20, 30, 20, 30)

        collectionRef.orderBy(FieldPath.documentId())
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Enqueue the documents in the desired order
                for (documentSnapshot in querySnapshot.documents) {

                    val data = documentSnapshot.data!!
                    val name = data["name"].toString()
                    val profile_picture = data["profile_picture"].toString()
                    val message = data["message"].toString()

                    val customView = CustomViewChat(this, profile_picture, message, name)
                    customViewsContainer.addView(customView, layoutParams)
                }
            }
            .addOnFailureListener { e ->
                // Error occurred while retrieving the documents
            }
    }

    fun sendMessage() {

        val message = messageBox.text.toString()
        messageBox.setText("")

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // width
            LinearLayout.LayoutParams.WRAP_CONTENT // height
        )
        layoutParams.setMargins(20, 30, 20, 30)

        val customView = CustomViewChat(this, "encodedImage", message, name)
        customViewsContainer.addView(customView, layoutParams)

    }
}