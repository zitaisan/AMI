package com.example.amiv1


import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class account_page : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textViewAbout: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_page)
        val buttonchat_page = findViewById<ImageButton>(R.id.imageButtonchat)
        buttonchat_page.setOnClickListener {
            val intent: Intent = Intent(this, chat_page::class.java)
            startActivity(intent)
        }
        val buttonaccount_page = findViewById<ImageButton>(R.id.imageButtonprofile)
        buttonaccount_page.setOnClickListener {
            val intent: Intent = Intent(this, account_page::class.java)
            startActivity(intent)
        }
        val buttonmain_page = findViewById<ImageButton>(R.id.imageButtonank)
        buttonmain_page.setOnClickListener {
            val intent: Intent = Intent(this, main_page::class.java)
            startActivity(intent)
        }
        imageView = findViewById(R.id.person_image)
        textViewAbout = findViewById(R.id.textabout)

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            loadUserData(userId)
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
        }
    }


        private fun loadUserData(userId: String) {
            val client = OkHttpClient()
            val url =
                "http://10.0.2.2/select_profile.php?id=$userId" // Update this URL to match your server's address

            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(
                            this@account_page,
                            "Ошибка при загрузке данных",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        runOnUiThread {
                            try {
                                val jsonResponse = JSONObject(responseBody)
                                val status = jsonResponse.getString("status")
                                if (status == "success") {
                                    val user = jsonResponse.getJSONObject("user")
                                    val userAbout = user.getString("about")
                                    val userPhoto = user.getString("photo") // Base64 encoded photo

                                    // Set the About text
                                    textViewAbout.text = userAbout

                                    // Decode the photo and set it to the ImageView
                                    val decodedBytes = Base64.decode(userPhoto, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(
                                        decodedBytes,
                                        0,
                                        decodedBytes.size
                                    )
                                    imageView.setImageBitmap(bitmap)
                                } else {
                                    Toast.makeText(
                                        this@account_page,
                                        jsonResponse.getString("message"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                Log.d("MYTAG", "Ошибка при парсинге ответа: ${e.message}")
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@account_page,
                                "Ошибка при загрузке данных",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        }
    }

