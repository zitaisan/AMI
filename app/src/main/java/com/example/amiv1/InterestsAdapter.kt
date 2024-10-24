package com.example.amiv1
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class InterestsAdapter(private val interests: MutableList<String>,
                       private val onInterestSelected: (String) -> Unit,
                       private val onCountChanged: (Int) -> Unit // Добавляем лямбду для обновления счетчика
) : RecyclerView.Adapter<InterestsAdapter.InterestViewHolder>() {

    private val selectedInterests = mutableSetOf<String>()

    inner class InterestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.buttonInterest)

        init {
            button.setOnClickListener {
                val interest = button.text.toString()
                toggleInterest(interest)  // Используем toggleInterest для переключения состояния кнопки
                onInterestSelected(interest)
                onCountChanged(selectedInterests.size) // Обновляем счетчик
            }
        }

        private fun toggleInterest(interest: String) {
            if (selectedInterests.contains(interest)) {
                selectedInterests.remove(interest)
                removeHighlight(button)  // Сбрасываем стиль кнопки
            } else {
                selectedInterests.add(interest)
                addHighlight(button)  // Применяем стиль выделенной кнопки
            }
        }

        fun addHighlight(button: Button) {
            val drawable = GradientDrawable()
            drawable.setStroke(5, Color.BLACK) // Черная обводка
            drawable.setColor(Color.WHITE) // Белый фон
            button.background = drawable
            button.setTextColor(Color.GRAY) // Серый цвет текста при нажатии
        }

        fun removeHighlight(button: Button) {
            val drawable = GradientDrawable()
            drawable.setStroke(5, Color.BLACK) // Черная обводка
            drawable.setColor(Color.WHITE) // Белый фон
            button.background = drawable
            button.setTextColor(Color.BLACK) // Черный текст по умолчанию
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
        return InterestViewHolder(view)
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        holder.button.text = interests[position]
        // Применяем правильный стиль, в зависимости от того, выбрана ли кнопка
        if (selectedInterests.contains(interests[position])) {
            holder.addHighlight(holder.button)
        } else {
            holder.removeHighlight(holder.button)
        }
    }

    fun getSelectedInterests(): List<String> {
        return selectedInterests.toList()
    }

    override fun getItemCount(): Int {
        return interests.size
    }
}
