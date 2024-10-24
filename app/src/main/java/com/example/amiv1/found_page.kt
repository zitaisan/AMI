package com.example.amiv1

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class found_page : AppCompatActivity() {
    private val interestsList = mutableListOf<String>()
    private lateinit var interestsAdapter: InterestsAdapter // Объявляем адаптер здесь
    private lateinit var countText: TextView // Объявляем TextView для счетчика

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_found_page)

        val continueButton = findViewById<Button>(R.id.completeButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton1)
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        countText = findViewById(R.id.count_text) // Инициализируем TextView

        // Проверка на null
        if (userId == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
            finish() // Закрыть активность, если пользователь не авторизован
            return
        }

        loadAllInterests(userId)
        cancelButton.setOnClickListener {
            val intent = Intent(this@found_page, main_page::class.java)
            startActivity(intent)
            finish() // Закрываем текущую активность
        }
        continueButton.setOnClickListener {
            // Проверяем, инициализирован ли адаптер
            if (::interestsAdapter.isInitialized) {
                val selectedInterests = interestsAdapter.getSelectedInterests()
                if (selectedInterests.isNotEmpty()) {
                    loadUsersByInterest(selectedInterests, userId)
                } else {
                    Toast.makeText(this, "Выберите хотя бы один интерес", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("found_page", "InterestsAdapter is not initialized")
                Toast.makeText(this, "Ошибка: интересы не загружены", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayInterests(interestsArray: JSONArray) {
        interestsList.clear() // Очистка списка перед добавлением новых данных

        for (i in 0 until interestsArray.length()) {
            val interestObject = interestsArray.getJSONObject(i)
            val interestName = interestObject.getString("name")
            interestsList.add(interestName)
        }

        // Убедитесь, что recyclerView объявлен правильно
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInterests)
// Устанавливаем GridLayoutManager с нужным количеством колонок, например 2 колонки
        val gridLayoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = gridLayoutManager


        // Создаем адаптер и подключаем его к RecyclerView
        interestsAdapter = InterestsAdapter(interestsList,{ interest ->
            Log.d("SelectedInterest", "Выбранный интерес: $interest")
        }, { count ->
            countText.text = "$count из ${interestsList.size}" // Обновляем текст в TextView
        })

        recyclerView.adapter = interestsAdapter
        interestsAdapter.notifyDataSetChanged() // Уведомляем адаптер об изменении данных

        Log.d("Interests", interestsList.toString())
    }


    private fun filterUsers(selectedInterests: List<String>, userId: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2/filter_users.php" // URL для запроса

        val jsonBody = JSONObject().apply {
            put("userId", userId) // Add userId to the JSON body
            put("interests", JSONArray(selectedInterests))
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@found_page, "Ошибка при загрузке пользователей: ${e.message}", Toast.LENGTH_SHORT).show()
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
                                displayUsers(usersArray) // Отображаем пользователей
                            } else {
                                Toast.makeText(this@found_page, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            Log.d("MYTAG", "Ошибка при парсинге ответа: ${e.message}")
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@found_page, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun displayUsers(usersArray: JSONArray) {
        val peopleContainer = findViewById<GridLayout>(R.id.people_container)
        peopleContainer.removeAllViews() // Clear container before adding new users

        for (i in 0 until usersArray.length()) {
            val userObject = usersArray.getJSONObject(i)
            val userPhoto = userObject.getString("photo") // Get base64 photo string

            // Create ImageView to display user's photo
            val imageView = ImageView(this)
            val decodedBytes = Base64.decode(userPhoto, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
            imageView.layoutParams = GridLayout.LayoutParams().apply {
                width = 170.dpToPx()
                height = 170.dpToPx()
                setMargins(10.dpToPx(), 10.dpToPx(), 10.dpToPx(), 10.dpToPx())
            }
            // Add ImageView to the container
            peopleContainer.addView(imageView)
        }
    }


    // Метод для преобразования dp в px
    fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun loadAllInterests(userId: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2/select_interests.php?userId=$userId" // URL для запроса всех интересов

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@found_page, "Ошибка при загрузке интересов: ${e.message}", Toast.LENGTH_SHORT).show()
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
                                if (jsonResponse.has("interests")) {
                                    val interestsArray = jsonResponse.getJSONArray("interests")
                                    displayInterests(interestsArray) // Отображаем интересы
                                } else {
                                    Log.d("MYTAG", "No 'interests' key in the response")
                                    Toast.makeText(this@found_page, "Интересы не найдены", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@found_page, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            Log.d("MYTAG", "Ошибка при парсинге ответа: ${e.message}")
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@found_page, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }




    private fun loadUsersByInterest(selectedInterests: List<String>, userId: String) {
        // Вызываем метод filterUsers с выбранными интересами
        filterUsers(selectedInterests, userId)
    }
}
