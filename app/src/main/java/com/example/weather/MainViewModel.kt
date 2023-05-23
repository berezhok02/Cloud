package com.example.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.adapters.WeatherModel

/**
 * Класс для автоматического обновления информации в карточках
 *
 * Класс MainViewModel содержит два поля класса MutableLiveData. Эти поля имеют свои обсерверы и автоматически обновляют данные в карточках, если передать в поля новые значения.
 */
class MainViewModel : ViewModel() {
    val liveDataCurrent = MutableLiveData<WeatherModel>()
    val liveDataList = MutableLiveData<List<WeatherModel>>()
}