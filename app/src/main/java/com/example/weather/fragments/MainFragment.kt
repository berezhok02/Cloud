package com.example.weather.fragments

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.DialogManager
import com.example.weather.MainViewModel
import com.example.weather.adapters.VpAdapter
import com.example.weather.adapters.WeatherModel
import com.example.weather.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject

const val API_KEY = "API KEY" //  Требуется добавить API ключ

class MainFragment : Fragment() {

    private lateinit var fLocationClient: FusedLocationProviderClient

    /**
     * Список, который содержит фрагменты HOURS и DAYS с информацией о погоде.
     */
    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )

    /**
     * Список с названиями табов HOURS и DAYS
     */
    private val tList = listOf(
        "Hours",
        "Days"
    )

    private lateinit var binding: FragmentMainBinding

    /** Лаунчер для запуска диалога с запросом на разрешение доступа к геолокации. При запуске
     * получает тип разрешения в виде строки формата String. */
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    /**
     * Функция инициализации.
     */
    private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        /**
         * Адаптер, позволяющий переключаться между фрагментами HOURS и DAYS.
         */
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter

        /**
         * Медиатор, соединяющий ViewPager и TabLayout для создания эффекта перелистывания
         * содержимого ViewPager при нажатии на TabLayout.
         */
        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = tList[pos]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }
        ibSearch.setOnClickListener {
            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }
            })
        }
    }

    /**
     * Функция проверяет включена ли геолокация и вызывает функцию getLocation для получаения данных о погоде.
     */
    private fun checkLocation() {
        if (isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    /**
     * Функция проверяет включена ли геолокация.
     */
    private fun isLocationEnabled(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Функция проверяет наличие доступа к геолокации, получает широту и долготу устройства, вызывает функицю requestWeatherData, которая получает данные о погоде.
     */
    private fun getLocation() {
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude},${it.result.longitude}")
            }
    }

    /**
     * Функция заполняет главную карточку
     */
    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemp = "${it.maxTemp}°C/${it.minTemp}°C"
            tvData.text = it.time
            tvCity.text = it.city
            if (it.currentTemp.isEmpty()) {
                tvCurrentTemp.text = maxMinTemp
            } else {
                tvCurrentTemp.text = "${it.currentTemp}°C"
            }
            tvCondition.text = it.condition
            tvMaxMin.text = if (it.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load("https:${it.imageURL}").into(imWeather)
        }
    }

    /**
     * Функция инициализирует лаунчер для проверки на разрешение доступа к геолокации и выводит тост
     * с информацией о текущем состоянии разрешения.
     */
    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Если нет разрешения на доступ к геолокации, функция вызывает permissionListener()
     * для инициализации лаунчера и запускает лаунчер, чтобы у пользователя появился диалог с
     * вопросом о разрешении доступа.
     */
    private fun checkPermission() {
        if (!isPermisionGranted(ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(ACCESS_FINE_LOCATION)
        }

    }

    /**
     * Функция получает прогноз погоды с WeatherAPI и вызывает parseWeatherData для ее парсинга.
     *
     * Принимает в качестве параметра название города для которого нужен прогноз погоды.
     * Создает и добавляет в очередь GET запрос в WeatherAPI. После получает JSON данные о погоде
     * в формате String и записывает их в переменную result. Вызывает функцию parseWeatherData для парсинга JSON.
     */
    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city +
                "&days=3&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result -> parseWeatherData(result) },
            { error -> Log.d("MyLog", "Error: $error") })
        queue.add(request)
    }

    /**
     * Функци получает JSON данные в формате строки. Вызывает функции парсинга JSON данных с API для настоящего момента и последющих дней.
     */
    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    /**
     * Функция получает JSONObject с иформацией о погоде за все дни. Парсит JSON, формирует по нему список WeatherModel объектов с погодой по дням и передает его в объект класса MainViewModel, который автоматически обновляет карточки.
     */
    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }

    /**
     * Функция получает JSONObject с ифнормацией о погоде в текущий момент времени. Прасит JSON, создает объект класса WeatherModel и передает его в объект класса MainViewModel, который автоматически обновляет карточки.
     */
    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            weatherItem.hours
        )
        model.liveDataCurrent.value = item
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}