
package com.example.weatherapp

import android.os.Bundle
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var cityInput: EditText
    private lateinit var cityView: TextView
    private lateinit var iconView: TextView
    private lateinit var tempView: TextView
    private lateinit var conditionView: TextView
    private lateinit var feelsView: TextView
    private lateinit var humidityView: TextView
    private lateinit var windView: TextView
    private lateinit var statusView: TextView
    private lateinit var searchButton: Button

    private val blue = Color.rgb(28, 105, 190)
    private val darkBlue = Color.rgb(20, 65, 125)
    private val dark = Color.rgb(30, 45, 65)
    private val muted = Color.rgb(100, 116, 139)
    private val lightGray = Color.rgb(225, 232, 240)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = blue
        window.navigationBarColor = blue
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // Main scrollable screen
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = false
            setBackgroundColor(blue)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(24), dp(22), dp(28))
            isFocusableInTouchMode = true
        }

        scroll.addView(
            root,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Heading
        root.addView(
            makeText("Weather App", 28f, Color.WHITE, true),
            lp(MATCH, 44)
        )

        root.addView(
            makeText(
                "Discover weather around you",
                14f,
                Color.WHITE
            ),
            lp(MATCH, 30)
        )

        // Search input
        cityInput = EditText(this).apply {
            hint = "Enter city name"
            textSize = 16f
            setTextColor(dark)
            setHintTextColor(muted)
            setSingleLine(true)

            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS

            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setPadding(dp(14), dp(8), dp(14), dp(8))
            background = rounded(Color.WHITE, 14f)
        }

        searchButton = Button(this).apply {
            text = "Search"
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(darkBlue, 14f)
            setPadding(dp(8), 0, dp(8), 0)
            minHeight = dp(48)
            minimumHeight = dp(48)
            stateListAnimator = null
        }

        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        searchRow.addView(
            cityInput,
            LinearLayout.LayoutParams(
                0,
                dp(54),
                1f
            ).apply {
                marginEnd = dp(8)
            }
        )

        searchRow.addView(
            searchButton,
            LinearLayout.LayoutParams(
                dp(96),
                dp(54)
            )
        )

        root.addView(
            searchRow,
            lp(MATCH, 54).apply {
                topMargin = dp(18)
                bottomMargin = dp(8)
            }
        )

        // Search status
        statusView = makeText(
            "Getting current weather...",
            13f,
            Color.WHITE
        )

        root.addView(
            statusView,
            lp(MATCH, 34)
        )

        // Weather card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(16), dp(16), dp(16), dp(18))
            background = rounded(Color.WHITE, 24f)
            elevation = dp(4).toFloat()
        }

        cityView = makeText("New Delhi", 22f, dark, true)
        iconView = makeText("🌤️", 58f, dark)
        tempView = makeText("--°C", 46f, dark, true)
        conditionView = makeText(
            "Current weather",
            18f,
            dark,
            true
        )
        feelsView = makeText(
            "Feels like --°C",
            14f,
            muted
        )

        // Wrap content prevents text clipping
        card.addView(cityView, lp(MATCH, WRAP))
        cityView.setPadding(0, dp(8), 0, dp(8))

        card.addView(iconView, lp(MATCH, WRAP))
        iconView.setPadding(0, dp(4), 0, dp(4))

        card.addView(tempView, lp(MATCH, WRAP))
        tempView.setPadding(0, dp(4), 0, dp(8))

        card.addView(conditionView, lp(MATCH, WRAP))
        conditionView.setPadding(0, dp(4), 0, dp(4))

        card.addView(feelsView, lp(MATCH, WRAP))
        feelsView.setPadding(0, dp(4), 0, dp(8))

        // Divider
        val divider = View(this).apply {
            setBackgroundColor(lightGray)
        }

        card.addView(
            divider,
            LinearLayout.LayoutParams(
                MATCH,
                dp(1)
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(12)
            }
        )

        // Humidity and wind
        val detailsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val humidityColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val windColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        humidityColumn.addView(
            makeText("HUMIDITY", 12f, muted, true),
            lp(MATCH, WRAP)
        )

        humidityView = makeText("💧 --%", 16f, dark, true)
        humidityView.setPadding(0, dp(8), 0, dp(8))

        humidityColumn.addView(
            humidityView,
            lp(MATCH, WRAP)
        )

        windColumn.addView(
            makeText("WIND SPEED", 12f, muted, true),
            lp(MATCH, WRAP)
        )

        windView = makeText("-- km/h", 16f, dark, true)
        windView.setPadding(0, dp(8), 0, dp(8))

        windColumn.addView(
            windView,
            lp(MATCH, WRAP)
        )

        detailsRow.addView(
            humidityColumn,
            LinearLayout.LayoutParams(0, WRAP, 1f)
        )

        detailsRow.addView(
            windColumn,
            LinearLayout.LayoutParams(0, WRAP, 1f)
        )

        card.addView(detailsRow, lp(MATCH, WRAP))

        root.addView(
            card,
            lp(MATCH, WRAP).apply {
                topMargin = dp(4)
            }
        )

        // Footer
        root.addView(
            makeText(
                "Weather data powered by Open-Meteo",
                12f,
                Color.WHITE
            ),
            lp(MATCH, WRAP).apply {
                topMargin = dp(16)
            }
        )

        setContentView(scroll)

        // Prevent keyboard from opening automatically
        root.requestFocus()

        searchButton.setOnClickListener {
            searchWeather()
        }

        cityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchWeather()
                true
            } else {
                false
            }
        }

        cityInput.setText("New Delhi")
        searchWeather()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun lp(
        width: Int,
        height: Int
    ): LinearLayout.LayoutParams {
        val actualWidth =
            if (width > 0) dp(width) else width

        val actualHeight =
            if (height > 0) dp(height) else height

        return LinearLayout.LayoutParams(
            actualWidth,
            actualHeight
        )
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            includeFontPadding = true

            if (bold) {
                setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun rounded(
        color: Int,
        radius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()
        }
    }

    private fun searchWeather() {
        val city = cityInput.text.toString().trim()

        if (city.isEmpty()) {
            cityInput.error = "Please enter a city name"
            return
        }

        // Hide the keyboard when Search is pressed
        val keyboard = getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        keyboard.hideSoftInputFromWindow(
            cityInput.windowToken,
            0
        )

        cityInput.clearFocus()
        searchButton.isEnabled = false
        statusView.text = "Searching for $city..."

        executor.execute {
            try {
                val encodedCity =
                    URLEncoder.encode(city, "UTF-8")

                val geoUrl =
                    "https://geocoding-api.open-meteo.com/v1/search" +
                            "?name=$encodedCity&count=1&language=en&format=json"

                val geo = getJson(geoUrl)
                val results = geo.optJSONArray("results")

                if (results == null || results.length() == 0) {
                    throw Exception(
                        "City not found. Try another city."
                    )
                }

                val place = results.getJSONObject(0)
                val latitude = place.getDouble("latitude")
                val longitude = place.getDouble("longitude")
                val name = place.getString("name")
                val country = place.optString("country", "")

                val weatherUrl =
                    "https://api.open-meteo.com/v1/forecast" +
                            "?latitude=$latitude&longitude=$longitude" +
                            "&current=temperature_2m,relative_humidity_2m," +
                            "apparent_temperature,is_day,weather_code," +
                            "wind_speed_10m" +
                            "&temperature_unit=celsius" +
                            "&wind_speed_unit=kmh"

                val weather = getJson(weatherUrl)
                val current = weather.getJSONObject("current")

                val temperature =
                    current.getDouble("temperature_2m").toInt()

                val feels =
                    current.getDouble("apparent_temperature").toInt()

                val humidity =
                    current.getInt("relative_humidity_2m")

                val wind =
                    current.getDouble("wind_speed_10m").toInt()

                val code = current.getInt("weather_code")
                val isDay = current.getInt("is_day") == 1

                val (description, icon) =
                    weatherDescription(code, isDay)

                runOnUiThread {
                    cityView.text =
                        if (country.isNotBlank()) {
                            "$name, $country"
                        } else {
                            name
                        }

                    tempView.text = "$temperature°C"
                    iconView.text = icon
                    conditionView.text = description
                    feelsView.text = "Feels like $feels°C"
                    humidityView.text = "💧 $humidity%"
                    windView.text = "$wind km/h"

                    statusView.text = "Current weather"
                    searchButton.isEnabled = true
                }

            } catch (e: Exception) {
                runOnUiThread {
                    statusView.text =
                        e.message ?: "Unable to load weather"

                    searchButton.isEnabled = true
                }
            }
        }
    }

    private fun getJson(address: String): JSONObject {
        val connection =
            URL(address).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode !in 200..299) {
                throw Exception("Weather service unavailable")
            }

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            return JSONObject(response)

        } finally {
            connection.disconnect()
        }
    }

    private fun weatherDescription(
        code: Int,
        isDay: Boolean
    ): Pair<String, String> {
        return when (code) {
            0 -> if (isDay) "Clear sky" to "☀️"
            else "Clear sky" to "🌙"
            1 -> "Mainly clear" to "🌤️"
            2 -> "Partly cloudy" to "⛅"
            3 -> "Overcast" to "☁️"
            45, 48 -> "Foggy" to "🌫️"
            51, 53, 55 -> "Drizzle" to "🌦️"
            56, 57 -> "Freezing drizzle" to "🌧️"
            61, 63, 65 -> "Rain" to "🌧️"
            66, 67 -> "Freezing rain" to "🌧️"
            71, 73, 75, 77 -> "Snow" to "❄️"
            80, 81, 82 -> "Rain showers" to "🌦️"
            85, 86 -> "Snow showers" to "🌨️"
            95 -> "Thunderstorm" to "⛈️"
            96, 99 -> "Thunderstorm with hail" to "⛈️"
            else -> "Weather unavailable" to "🌡️"
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    companion object {
        private const val MATCH =
            ViewGroup.LayoutParams.MATCH_PARENT

        private const val WRAP =
            ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
