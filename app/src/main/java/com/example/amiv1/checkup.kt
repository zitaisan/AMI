package com.example.amiv1


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class checkup : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private fun loginUser(email: String, password: String, studentNumber: String): String {
        return try {
            val url = URL("http://10.0.2.2/authorization.php") // локальный адрес для эмулятора Android
            val postData = "mail=$email&password=$password&student_number=$studentNumber"

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            // Отправка POST-данных
            val outputStreamWriter = OutputStreamWriter(conn.outputStream)
            outputStreamWriter.write(postData)
            outputStreamWriter.flush()

            // Чтение ответа сервера
            val responseCode = conn.responseCode
            val responseMessage = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }

            Log.d("ServerResponse", responseMessage) // Логируем ответ от сервера

            if (responseCode == 200) {
                responseMessage // Возвращаем тело ответа
            } else {
                "{\"status\":\"error\", \"message\":\"Ошибка: $responseCode\"}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "{\"status\":\"error\", \"message\":\"Ошибка: ${e.message}\"}"
        }
    }

    private fun registerUser(email: String, password: String, studentNumber: String): String {
        return try {
            val url = URL("http://10.0.2.2/registration.php") // локальный адрес для эмулятора Android
            val postData = "mail=$email&password=$password&student_number=$studentNumber"

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            // Отправка POST-данных
            val outputStreamWriter = OutputStreamWriter(conn.outputStream)
            outputStreamWriter.write(postData)
            outputStreamWriter.flush()

            // Чтение ответа сервера
            val responseCode = conn.responseCode
            val responseMessage = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }

            Log.d("ServerResponse", responseMessage) // Логируем ответ от сервера

            if (responseCode == 200) {
                responseMessage // Возвращаем тело ответа
            } else {
                "{\"status\":\"error\", \"message\":\"Ошибка: $responseCode\"}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "{\"status\":\"error\", \"message\":\"Ошибка: ${e.message}\"}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        setContentView(R.layout.activity_checkup)
        val emailInput = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordInput = findViewById<EditText>(R.id.editTextTextPassword)
        val studentNumberInput = findViewById<EditText>(R.id.editTextNumber)
        val loginButton = findViewById<Button>(R.id.button1)
        val registerButton = findViewById<Button>(R.id.button2)

        // Восстанавливаем сохраненные данные
        emailInput.setText(sharedPreferences.getString("email", ""))
        passwordInput.setText(sharedPreferences.getString("password", ""))
        studentNumberInput.setText(sharedPreferences.getString("student_number", ""))

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val studentNumber = studentNumberInput.text.toString()

            // Запускаем HTTP-запрос в другом потоке
            CoroutineScope(Dispatchers.IO).launch {
                val response = loginUser(email, password, studentNumber)

                // Обновляем UI на главном потоке
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(response)
                        val status = jsonResponse.getString("status")
                        if (status == "success") {
                            // Сохраняем данные пользователя в SharedPreferences
                            with(sharedPreferences.edit()) {
                                putString("email", jsonResponse.getString("user"))
                                putString("student_number", jsonResponse.getString("student_number"))
                                putString("user_id", jsonResponse.getString("user_id")) // Сохраняем user_id
                                apply()
                            }

                            // Успешная авторизация, переходим на main_page
                            val intent = Intent(this@checkup, main_page::class.java)
                            startActivity(intent)
                            finish() // Закрываем текущую активность
                        } else {
                            Toast.makeText(this@checkup, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        Log.e("JSONError", "Ошибка парсинга: ${e.message}")
                        Toast.makeText(this@checkup, "Ошибка при обработке ответа от сервера", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        registerButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val studentNumber = studentNumberInput.text.toString()

            // Запускаем HTTP-запрос в другом потоке
            CoroutineScope(Dispatchers.IO).launch {
                val response = registerUser(email, password, studentNumber)

                // Обновляем UI на главном потоке
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(response)
                        val status = jsonResponse.getString("status")
                        if (status == "success") {
                            // Сохраняем данные пользователя в SharedPreferences
                            with(sharedPreferences.edit()) {
                                putString("email", email)
                                putString("student_number", studentNumber)
                                putString("user_id", jsonResponse.getString("user_id")) // Сохраняем user_id
                                apply()
                            }

                            // Успешная регистрация, переходим на activity_registration
                            val intent = Intent(this@checkup, registration::class.java)
                            startActivity(intent)
                            finish() // Закрываем текущую активность
                        } else {
                            Toast.makeText(this@checkup, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        Log.e("JSONError", "Ошибка парсинга: ${e.message}")
                        Toast.makeText(this@checkup, "Ошибка при обработке ответа от сервера", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }
}
