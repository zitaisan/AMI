package com.example.amiv1
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream
import java.io.IOException

import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.json.JSONArray

class main_page : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private fun displayUsers(usersArray: JSONArray) {
        val peopleContainer = findViewById<GridLayout>(R.id.people_container)
        peopleContainer.removeAllViews() // Очищаем контейнер перед добавлением новых людей

        for (i in 0 until usersArray.length()) {
            val userObject = usersArray.getJSONObject(i)
            val userName = userObject.getString("name")
            val userPhoto = userObject.getString("photo") // Предположим, что фото приходит как base64

            // Создаем ImageView для отображения фото пользователя
            val imageView = ImageView(this)
            val decodedBytes = Base64.decode(userPhoto, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
            imageView.layoutParams = GridLayout.LayoutParams().apply {
                width = 170.dpToPx()
                height = 170.dpToPx()
                setMargins(10.dpToPx(), 10.dpToPx(), 10.dpToPx(), 10.dpToPx())
            }



            // Добавляем ImageView
            peopleContainer.addView(imageView)
        }
    }

    // Метод для преобразования dp в px
    fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
    private fun loadAllUsers(userId: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2/select_all_users.php?userId=$userId" // URL для запроса всех пользователей

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@main_page, "Ошибка при загрузке пользователей", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("MYTAG", "Response: $responseBody")

                    runOnUiThread {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val status = jsonResponse.getString("status")
                            if (status == "success") {
                                val usersArray = jsonResponse.getJSONArray("users")
                                displayUsers(usersArray)
                            } else {
                                Toast.makeText(this@main_page, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            Log.d("MYTAG", "Ошибка при парсинге ответа: ${e.message}")
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@main_page, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun loadPeopleByUniversity(university: String, userId: String) {
        val client = OkHttpClient()

        // URL для запроса с параметрами университета и id пользователя
        val url = "http://10.0.2.2/select_univ.php?university=$university&userId=$userId"

        // Создаем запрос
        val request = Request.Builder()
            .url(url)
            .build()

        // Выполняем запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@main_page,
                        "Ошибка при загрузке пользователей",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("MYTAG", "Response: $responseBody") // Логируем ответ от сервера

                    runOnUiThread {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val status = jsonResponse.getString("status")
                            if (status == "success") {
                                val usersArray = jsonResponse.getJSONArray("users")
                                displayUsers(usersArray)
                            } else {
                                Toast.makeText(
                                    this@main_page,
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
                            this@main_page,
                            "Ошибка при загрузке данных",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        // Инициализация SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        if (userId == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
            finish() // Закрыть активность, если пользователь не авторизован
            return
        }
        loadAllUsers(userId)
        val misisButton = findViewById<ImageButton>(R.id.misisBut)
        val madiButton = findViewById<ImageButton>(R.id.madiBut)
        val vsheButton = findViewById<ImageButton>(R.id.vsheBut)
        val sechaButton = findViewById<ImageButton>(R.id.sechaBut)
        val loopButton = findViewById<ImageButton>(R.id.loop_image)
        loopButton.setOnClickListener{
            val intent: Intent = Intent(this, found_page::class.java)
            startActivity(intent)
        }
        misisButton.setOnClickListener {
            loadPeopleByUniversity("НИТУ МИСИС", userId)
        }

        madiButton.setOnClickListener {
            loadPeopleByUniversity("МАДИ", userId)
        }

        vsheButton.setOnClickListener {
            loadPeopleByUniversity("ВШЭ", userId)
        }

        sechaButton.setOnClickListener {
            loadPeopleByUniversity("Сеченовский", userId)
        }
        val buttonchat_page= findViewById<ImageButton>(R.id.imageButtonchat)
        buttonchat_page.setOnClickListener{
            val intent: Intent = Intent(this, chat_page::class.java)
            startActivity(intent)
        }
        val buttonaccount_page= findViewById<ImageButton>(R.id.imageButtonprofile)
        buttonaccount_page.setOnClickListener{
            val intent: Intent = Intent(this,  account_page::class.java)
            startActivity(intent)
        }
        val buttonmain_page= findViewById<ImageButton>(R.id.imageButtonank)
        buttonmain_page.setOnClickListener{
            val intent: Intent = Intent(this,  main_page::class.java)
            startActivity(intent)
        }
    }
}