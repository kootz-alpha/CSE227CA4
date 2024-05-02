package com.example.alphatrade

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class AddStockPage : Fragment() {

    private lateinit var apiKeyFinnub: String
    private lateinit var apiKeyAlphaVantage: String
    private lateinit var docRef: DocumentReference
    private var stockList: MutableList<String> = mutableListOf()
    private lateinit var userID: String
    private lateinit var layoutParams: LinearLayout.LayoutParams
    private lateinit var scrollView: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_add_stock, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = "Add Stock to Watchlist"

        val homePage = activity as HomePage
        apiKeyFinnub = homePage.apiKeyFinnub
        apiKeyAlphaVantage = homePage.apiKeyAlphaVantage
        userID = homePage.userID

        scrollView = view.findViewById(R.id.scroll_add_stock_page)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(20, 30, 20, 30)

        docRef = FirebaseFirestore.getInstance().collection("stock_list").document("stock_list")

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {

                    stockList = documentSnapshot.getString("list")?.split(", ")!!.toMutableList()
                    for (stock in stockList) {

                        val customView = CustomViewAddStock(requireContext(), userID, stock, apiKeyFinnub)
                        scrollView.addView(customView, layoutParams)
                    }
                    Log.d("FirestoreStockList", "Successfully retrieved stocklist document")

                }
            }
            .addOnFailureListener { e ->

                stockList = mutableListOf()
                Log.e("FirestoreStockList", "Error retrieving stocklist document", e)
            }
    }


}