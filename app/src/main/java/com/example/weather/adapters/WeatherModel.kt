package com.example.weather.adapters

/**
 * Класс данных с информацией о погоде за день, в том числе по часам.
 */
data class WeatherModel(
    val city: String,
    val time: String,
    val condition: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val imageURL: String,
    val hours: String
)
