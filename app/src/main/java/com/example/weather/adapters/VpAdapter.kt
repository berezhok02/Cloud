package com.example.weather.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


/**
 * Класс переключает содержание табов HOURS и DAYS в ViewPager.
 *
 * Параметр fa - активити, в котором находится ViewPager.
 *
 * Параметр list - список с фрагментами HOURS и DAYS, которые содержат в себе всю информацию.
 */
class VpAdapter(fa: FragmentActivity, private val list: List<Fragment>) : FragmentStateAdapter(fa) {

    /**
     * Функция возвращает количество табов. В нашем случае их 2: HOURS и DAYS
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Функция возвращает фрагмент с нужной позиции
     */
    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}