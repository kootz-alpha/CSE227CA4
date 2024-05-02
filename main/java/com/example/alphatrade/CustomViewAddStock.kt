package com.example.alphatrade

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class CustomViewAddStock(context: Context, userID: String, stockSymbol: String, apiKeyFinnub: String): ConstraintLayout(context) {

    private var docRef: DocumentReference
    private var watchList: MutableList<String> = mutableListOf()

    init {
        LayoutInflater.from(context).inflate(R.layout.customview_addstock, this, true)

        val symbol = findViewById<TextView>(R.id.cv_addstock_symbol)
        val name = findViewById<TextView>(R.id.cv_addstock_name)
        val addSwitch = findViewById<Switch>(R.id.cv_addstock_switch)

        docRef = FirebaseFirestore.getInstance().collection("user_profiles").document(userID)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {

                    val watchListString = documentSnapshot.getString("watchlist")

                    if (watchListString != "")
                        watchList = watchListString?.split(", ")!!.toMutableList()

                    addSwitch.isChecked = stockSymbol in watchList
                }
            }
            .addOnFailureListener { e ->

                Log.e("Firestore", "Error retrieving watchlist document", e)
            }

        symbol.text = stockSymbol

        getNameAPI(stockSymbol, apiKeyFinnub) { companyName ->

            name.text = companyName
        }

        addSwitch.setOnCheckedChangeListener { _, isChecked ->

            docRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {

                        val watchListString = documentSnapshot.getString("watchlist")

                        if (watchListString != "")
                            watchList = watchListString?.split(", ")!!.toMutableList()

                        if (isChecked) {

                            if (stockSymbol !in watchList) {
                                watchList.add(stockSymbol)
                            }
                        }
                        else {
                            if (stockSymbol in watchList) {
                                watchList.remove(stockSymbol)
                            }
                        }

                        docRef.update("watchlist", watchList.joinToString(", "))
                    }
                }
                .addOnFailureListener { e ->

                    Log.e("Firestore", "Error retrieving watchlist document", e)
                }

            }

    }

    fun getNameAPI(stockSymbol: String, apiKeyFinnub: String, callback: (String) -> Unit) {

        val client = OkHttpClient()

        val url = "https://finnhub.io/api/v1/stock/profile2?symbol=$stockSymbol&token=$apiKeyFinnub"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                val data = JSONObject(json)
                val companyName = data.optString("name").trim()

                Handler(Looper.getMainLooper()).post {
                    callback(companyName)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
        })
    }
}