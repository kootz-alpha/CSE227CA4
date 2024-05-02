package com.example.alphatrade

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class StockDisplayPage : AppCompatActivity() {

    private lateinit var apiKeyAlphaVantage: String
    private lateinit var apiKeyFinnub: String
    private lateinit var stockSymbol: String
    private lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_display_page)

        apiKeyFinnub = intent.getStringExtra("apiKeyFinnub")!!
        apiKeyAlphaVantage = intent.getStringExtra("apiKeyAlphaVantage")!!

        stockSymbol = intent.getStringExtra("stockSymbol")!!
        userID = intent.getStringExtra("userID")!!

        val graphContainer = findViewById<FrameLayout>(R.id.graphContainer)
        var data = listOf<Float>()
        getPriceListAPI(stockSymbol) {priceList ->
            data = priceList

            val graphView = CustomViewGraph(this, null, data)
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, // width
                FrameLayout.LayoutParams.MATCH_PARENT // height
            )
            graphContainer.addView(graphView, layoutParams)
        }

        val companyName = findViewById<TextView>(R.id.display_company_name)
        companyName.text = stockSymbol

        //Log.d("TAG", stockSymbol!!)

        val description = findViewById<TextView>(R.id.display_company_description)
        getDescriptionAPI(stockSymbol) {descr ->
            description.text = descr
        }

    }

    fun getDescriptionAPI(stockSymbol: String?, callback: (String) -> Unit) {

        val client = OkHttpClient()

        val url = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=$stockSymbol&apikey=$apiKeyAlphaVantage"


        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                val data = JSONObject(json)
                Log.d("Description", data.toString())
                val descr = data.optString("Description").trim()

                Handler(Looper.getMainLooper()).post {
                    callback(descr)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
        })
    }

    fun getPriceListAPI(stockSymbol: String?, callback: (List<Float>) -> Unit) {

        val client = OkHttpClient()

        //val url = "https://finnhub.io/api/v1/stock/candle?symbol=$stockSymbol&resolution=D&count=50&token=$apiKeyFinnub"
        val url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=$stockSymbol&outputsize=compact&apikey=$apiKeyAlphaVantage"
        val request = Request.Builder()
            .url(url)
            .build()



        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                val data = JSONObject(json)
                val timeSeries = data.getJSONObject("Time Series (Daily)")
                val priceList = mutableListOf<Float>()

                for (dateString in timeSeries.keys().asSequence().toList().reversed()) {
                    //Log.d("Debug", dateString)
                    val closePrice = timeSeries.getJSONObject(dateString).getString("4. close").toFloatOrNull()
                    closePrice?.let {
                        priceList.add(it)
                    }
                }

                Handler(Looper.getMainLooper()).post {
                    callback(priceList)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
        })
    }

    fun remind(view: View) {

        val calendar = Calendar.getInstance()

// Set the initial date and time to the current time
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val datePickerDialog = DatePickerDialog(
            this, R.style.MyDatePickerStyle,
            { _, pickedYear, pickedMonth, pickedDay ->
                // Update the calendar with the picked date
                calendar.set(Calendar.YEAR, pickedYear)
                calendar.set(Calendar.MONTH, pickedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, pickedDay)

                val timePickerDialog = TimePickerDialog(
                    this, R.style.MyTimePickerStyle,
                    { _, pickedHour, pickedMinute ->
                        calendar.set(Calendar.HOUR_OF_DAY, pickedHour)
                        calendar.set(Calendar.MINUTE, pickedMinute)

                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this, NotificationReceiver::class.java)
                        intent.putExtra("stockSymbol", stockSymbol)
                        intent.putExtra("userID", userID)
                        val pendingIntent = PendingIntent.getBroadcast(
                            this,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )

                        val vg:ViewGroup? = findViewById(R.id.custom_toast)
                        val inflater = layoutInflater

                        val layout: View = inflater.inflate(R.layout.custom_toast_layout,vg)

                        val toast = Toast(applicationContext)

                        toast.duration = Toast.LENGTH_LONG
                        toast.setView(layout)
                        toast.show()

                    },
                    hour,
                    minute,
                    false
                )
                timePickerDialog.show()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()

    }

    fun notify(view: View) {

        val vg:ViewGroup? = findViewById(R.id.custom_toast)
        val inflater = layoutInflater

        val layout: View = inflater.inflate(R.layout.custom_toast_layout,vg)
        layout.findViewById<TextView>(R.id.txt_toast).text = "Notification set successfully!"

        val toast = Toast(applicationContext)

        toast.duration = Toast.LENGTH_LONG
        toast.setView(layout)
        toast.show()
    }
}