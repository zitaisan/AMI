package com.example.amiv1

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class Item(val photoUrl: String, val title: String, val description: String)
class ItemAdapter(private val itemList: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.name)
        val description: TextView = itemView.findViewById(R.id.description)
        val photo: ImageView = itemView.findViewById(R.id.photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.title.text = item.title
        holder.description.text = item.description

        // Загрузка изображения с использованием Glide
        Glide.with(holder.itemView.context)
            .load(item.photoUrl)
            .placeholder(R.drawable.misis) // Используйте изображение-заглушку
            .error(R.drawable.secha) // Используйте изображение для ошибки загрузки
            .into(holder.photo)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}

class chat_page : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_page)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        Log.d("CHECKDDD", "userId = $userId")

        if (userId != null) {
            loadDataFromServer(userId)
        } else {
            Log.e("chat_page", "userId is null, cannot load data")
            // Можно показать уведомление пользователю
        }
    }

    private fun loadDataFromServer(userId: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2/select_chat.php?userId=$userId"  // Передача userId как параметр запроса
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("chat_page", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val items = parseJsonData(responseBody)

                    // Логирование для проверки
                    Log.d("chat_page", "userId from SharedPreferences: $userId")
                    Log.d("chat_page", "Response from server: $responseBody")

                    runOnUiThread {
                        recyclerView.adapter = ItemAdapter(items)
                    }
                } else {
                    Log.e("chat_page", "Response not successful: ${response.code}")
                }
            }
        })
    }

    private fun parseJsonData(jsonData: String?): List<Item> {
        val itemList = mutableListOf<Item>()
        if (jsonData != null) {
            try {
                // Преобразование строки JSON в JSONObject
                val jsonObject = JSONObject(jsonData)
                // Получение массива данных из объекта
                val jsonArray = jsonObject.getJSONArray("items") // Убедитесь, что "data" — это ключ, который вы используете

                for (i in 0 until jsonArray.length()) {
                    val jsonItem = jsonArray.getJSONObject(i)
                    val name = jsonItem.getString("title") // Измените на правильное название поля
                    val description = jsonItem.getString("description") // Измените на правильное название поля
                    val photoUrl = jsonItem.getString("photoUrl") // Измените на правильное название поля
                    itemList.add(Item(photoUrl, name, description))
                }
            } catch (e: Exception) {
                Log.e("chat_page", "Error parsing JSON data: ${e.message}")
            }
        }
        return itemList
    }

}

