package com.example.alphatrade

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class WatchListPage : Fragment() {

    private lateinit var apiKeyFinnub: String
    private lateinit var apiKeyAlphaVantage: String
    private lateinit var docRef: DocumentReference
    private lateinit var watchList: MutableList<String>
    private lateinit var userID: String
    private lateinit var layoutParams: LinearLayout.LayoutParams
    private lateinit var scrollView: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_watchlist, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homePage = activity as HomePage
        apiKeyFinnub = homePage.apiKeyFinnub
        apiKeyAlphaVantage = homePage.apiKeyAlphaVantage
        userID = homePage.userID

        scrollView = view.findViewById(R.id.scroll_watchlist_page)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // width
            LinearLayout.LayoutParams.WRAP_CONTENT // height
        )
        layoutParams.setMargins(20, 30, 20, 30)

        docRef = FirebaseFirestore.getInstance().collection("user_profiles").document(userID)
    }

    override fun onResume() {
        super.onResume()

        requireActivity().title = "My Watchlist"

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    // Document exists, retrieve the value of a specific key
                    watchList = documentSnapshot.getString("watchlist")?.split(", ")!!.toMutableList()
                    scrollView.removeAllViews()

                    for (stock in watchList) {

                        if (stock != "") {

                            val customView = CustomViewWatchlist(requireContext(), stock, apiKeyFinnub, apiKeyAlphaVantage, userID)
                            scrollView.addView(customView, layoutParams)
                        }
                    }
                }
            }
    }
}