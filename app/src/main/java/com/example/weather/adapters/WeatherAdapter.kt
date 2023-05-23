package com.example.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.ListItemBinding
import com.squareup.picasso.Picasso

/**
 * Класс, который автоматически заполняет шаблоны карточек и рисует их внутри RecyclerView.
 */
class WeatherAdapter(val listener: Listener?) : ListAdapter<WeatherModel, WeatherAdapter.Holder>(Comparator()) {

    /**
     * Класс, который, хранит в себе ссылки на вью внутри карточки и заполняет ее.
     */
    class Holder(view: View, val listener: Listener?) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)
        var itemTemp: WeatherModel? = null

        init {
            itemView.setOnClickListener{
                itemTemp?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(item: WeatherModel) = with(binding) {
            itemTemp = item
            tvDate.text = item.time
            tvCondition.text = item.condition
            tvTemp.text = item.currentTemp.ifEmpty { "${item.maxTemp}°C/${item.minTemp}°C" }
            Picasso.get().load("https:${item.imageURL}").into(im)
        }
    }

    /**
     * Класс, который определяет изменилось ли содержимое внутри карточки.
     */
    class Comparator : DiffUtil.ItemCallback<WeatherModel>() {
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

    }

    /**
     * Функция, создающая карточку.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Holder(view, listener)
    }

    /**
     * Функция, заполняющая карточку.
     */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Интерфейс для обрабтки нажатий на один день из списка, с последующим появлением ифнормации о погоде в этот день на главной карточке.
     */
    interface Listener {
        fun onClick(item: WeatherModel)
    }
}