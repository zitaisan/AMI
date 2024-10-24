package com.example.amiv1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream
import java.io.IOException

class registration : AppCompatActivity() {
    private fun loadAllInterests(userId: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2/select_interests.php?userId=$userId" // URL для запроса всех интересов

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@registration, "Ошибка при загрузке интересов", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("MYTAG", "Response: $responseBody") // Логируем ответ от сервера

                    runOnUiThread {
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
                                        Toast.makeText(this@registration, "Интересы не найдены", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this@registration, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: JSONException) {
                                Log.d("MYTAG", "Ошибка при парсинге ответа: ${e.message}")
                            }
                        }

                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@registration, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    val interestsList = mutableListOf<String>()
    private fun displayInterests(interestsArray: JSONArray) {
        interestsList.clear() // Очистка списка перед добавлением новых данных

        for (i in 0 until interestsArray.length()) {
            val interestObject = interestsArray.getJSONObject(i)
            val interestName = interestObject.getString("name")
            interestsList.add(interestName) // Добавляем название интереса в список
        }

        // Убедитесь, что recyclerView объявлен правильно
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInterests)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // Например, 2 колонки


        // Создаем адаптер и подключаем его к RecyclerView
        val interestsAdapter = InterestsAdapter(interestsList, { interest ->
            Log.d("SelectedInterest", "Выбранный интерес: $interest")
        }, { count -> countText.text = "" // Обновляем текст в TextView


        })

        recyclerView.adapter = interestsAdapter
        interestsAdapter.notifyDataSetChanged() // Уведомляем адаптер об изменении данных

        Log.d("Interests", interestsList.toString())
    }



    private val pickImageRequest = 1
    private var selectedImageUri: Uri? = null
    private lateinit var universitySpinner: Spinner
    private lateinit var countText: TextView // Объявляем TextView для счетчика
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        universitySpinner = findViewById(R.id.spinnerUniversity)
        // Массив с университетами
        val universities = arrayOf("НИТУ МИСИС", "МАДИ", "ВШЭ", "Сеченовский")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, universities)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        universitySpinner.adapter = adapter
        // Инициализация полей ввода и кнопок
        val nameInput = findViewById<EditText>(R.id.editTextName)
        val ageInput = findViewById<EditText>(R.id.editTextAge)
        val aboutInput = findViewById<EditText>(R.id.editTextAbout)
        val uploadButton = findViewById<Button>(R.id.buttoncontinue)



        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInterests)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // Например, 2 колонки



        countText = findViewById(R.id.count_text) // Инициализируем TextView

        // Получение userId из SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        // Проверьте, что userId не null, прежде чем продолжать
        if (userId == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
            finish() // Закрыть активность, если пользователь не авторизован
            return
        }

        loadAllInterests(userId)


        // Обработка нажатия кнопки загрузки профиля
        uploadButton.setOnClickListener {
            if (selectedImageUri != null) {
                val university = universitySpinner.selectedItem.toString() // Получение выбранного университета
                val name = nameInput.text.toString()
                val age = ageInput.text.toString()
                val about = aboutInput.text.toString()

                // Запускаем корутину для загрузки данных профиля
                CoroutineScope(Dispatchers.IO).launch {
                    uploadProfileData(userId, university, name, age, about, selectedImageUri!!)
                }
            } else {
                Toast.makeText(this, "Выберите изображение", Toast.LENGTH_SHORT).show()
            }
        }

        // Обработка выбора изображения
        findViewById<Button>(R.id.chooseImageButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, pickImageRequest)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequest && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
        }
    }

    private fun uploadProfileData(userId: String, university: String, name: String, age: String, about: String, imageUri: Uri) {
        val client = OkHttpClient()
        val fileDescriptor = contentResolver.openFileDescriptor(imageUri, "r") ?: return
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileBytes = fileInputStream.readBytes()
        val fileName = getFileName(imageUri)

        // Преобразуем список интересов в JSON-формат
        val jsonInterests = interestsList.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }


        // Создаем тело запроса
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId) // Передаем user_id
            .addFormDataPart("university", university)
            .addFormDataPart("name", name)
            .addFormDataPart("age", age)
            .addFormDataPart("about", about)
            .addFormDataPart("interests", jsonInterests)
            .addFormDataPart("photo", fileName, RequestBody.create("image/*".toMediaTypeOrNull(), fileBytes))
            .build()

        // Создаем запрос
        val request = Request.Builder()
            .url("http://10.0.2.2/insertion_profile.php")
            .post(requestBody)
            .build()

        // Выполняем запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@registration, "Ошибка при отправке данных", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@registration, "Данные успешно добавлены", Toast.LENGTH_SHORT).show()
                                // Дополнительные действия после успешного добавления данных
                                // Успешная регистрация, переходим на activity_registration
                                val intent = Intent(this@registration, main_page::class.java)
                                startActivity(intent)
                                finish() // Закрываем текущую активность
                            } else {
                                Toast.makeText(this@registration, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            Log.d("MYTAG", "Ошибка при парсинге ответа: ${e.message}")
                        }
                    }
                } else {
                    Log.e("Response Error", "Ошибка: ${response.code}")
                    runOnUiThread {
                        Toast.makeText(this@registration, "Ошибка при регистрации. Код: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }




    private fun getFileName(uri: Uri): String {
        var fileName = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    fileName = cursor.getString(columnIndex)
                }
            }
        }
        return fileName
    }
}
