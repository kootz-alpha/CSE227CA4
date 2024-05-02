package com.example.alphatrade

import MessageSample
import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MessageAdapter (private val messageList: ArrayList<MessageSample>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.customview_chat, parent, false)
        return ViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        holder.message.text = currentMessage.message
        holder.name.text = currentMessage.name

        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("profile_pictures/${currentMessage.id}.png")

        val ONE_MEGABYTE: Long = 1024 * 1024
        imageRef.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener { bytes ->
                // Image downloaded successfully
                // Convert byte array to Bitmap
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                // Load the Bitmap into ImageView
                holder.profilePicture.setImageBitmap(bitmap)
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                Log.e(TAG, "Error downloading image: ${exception.message}")
            }

    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val message : TextView = itemView.findViewById(R.id.textBar)
        val name: TextView = itemView.findViewById(R.id.chatName)
        val profilePicture: ImageView = itemView.findViewById(R.id.chatImage)

    }

}