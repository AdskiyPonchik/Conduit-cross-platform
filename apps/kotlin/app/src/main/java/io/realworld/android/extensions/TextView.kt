package io.realworld.android.extensions

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.widget.TextView
import java.util.*

@SuppressLint("ConstantLocale")
val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

@SuppressLint("ConstantLocale")
val appDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

// Additional date formats to handle different server responses
@SuppressLint("ConstantLocale")
val isoDateFormatWithOffset = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX", Locale.getDefault())

@SuppressLint("ConstantLocale")
val isoDateFormatSimple = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

var TextView.timeStamp: String
    set(value) {
        val date =
            try {
                isoDateFormat.parse(value)
            } catch (e: Exception) {
                try {
                    isoDateFormatWithOffset.parse(value)
                } catch (e2: Exception) {
                    try {
                        isoDateFormatSimple.parse(value)
                    } catch (e3: Exception) {
                        android.util.Log.e("TextView", "Failed to parse date: $value", e3)
                        null
                    }
                }
            }
        text = if (date != null) appDateFormat.format(date) else "Unknown date"
    }
    get() {
        val date = appDateFormat.parse(text.toString())
        return isoDateFormat.format(date)
    }
