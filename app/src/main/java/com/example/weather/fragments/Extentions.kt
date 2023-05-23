package com.example.weather.fragments

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Функация проверяет наличие разрешения.
 *
 * Функция принимает на вход название интересующего разрешения "p", передает его вместе с конткстом фрагмента в метод checkSelfPermission, которая выдает число: 0 - есть разрешение, -1 - разрешения нет.
 */
fun Fragment.isPermisionGranted(p: String): Boolean {
    return ContextCompat.checkSelfPermission(
        activity as AppCompatActivity,
        p
    ) == PackageManager.PERMISSION_GRANTED
}