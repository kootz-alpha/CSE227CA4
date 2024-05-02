package com.example.alphatrade

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat

class CustomViewWatchlist(context: Context, stockSymbol: String, apiKeyFinnub: String, apiKeyAlphaVantage: String, userID: String): ConstraintLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.customview_watchlist, this, true)

        val symbol = findViewById<TextView>(R.id.cv_watchlist_symbol)
        val name = findViewById<TextView>(R.id.cv_watchlist_name)
        val price = findViewById<TextView>(R.id.cv_watchlist_price)

        symbol.text = stockSymbol

        getNamePriceAPI(stockSymbol, apiKeyFinnub) { companyName, stockPrice ->

            name.text = companyName
            price.text = stockPrice
        }

        this.setOnClickListener{

            val displayIntent = Intent(context, StockDisplayPage::class.java)
            displayIntent.putExtra("stockSymbol", stockSymbol)
            displayIntent.putExtra("userID", userID)
            displayIntent.putExtra("apiKeyFinnub", apiKeyFinnub)
            displayIntent.putExtra("apiKeyAlphaVantage", apiKeyAlphaVantage)
            context.startActivity(displayIntent)
        }

    }

    fun getNamePriceAPI(stockSymbol: String, apiKey: String, callback: (String, String) -> Unit) {

        if (apiKey == "")
            return

        //Log.d("Debug", "apiKey: $apiKey")
        //Log.d("Debug", "stockSymbol: $stockSymbol")
        val client = OkHttpClient()

        val url = "https://finnhub.io/api/v1/stock/profile2?symbol=$stockSymbol&token=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("FinnubCallbackn", "called")
                val json = response.body()?.string()
                val data = JSONObject(json)
                val companyName = data.optString("name").trim()
                val marketCapString = data.optString("marketCapitalization").trim()
                val numSharesString = data.optString("shareOutstanding").trim()


                //Log.d("Debug", "marketCapString: $marketCapString")
                //Log.d("Debug", "numSharesString: $numSharesString")
                val marketCap = marketCapString.toDouble()
                val numShares = numSharesString.toDouble()
                val stockPriceDouble = (marketCap/numShares)

                val decimalFormat = DecimalFormat("#.##")
                val stockPrice = decimalFormat.format(stockPriceDouble).toString() + " $"

                Handler(Looper.getMainLooper()).post {
                    callback(companyName, stockPrice)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
        })
    }
}