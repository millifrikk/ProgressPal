package com.progresspal.app.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.DecimalFormat

// View Extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

// Context Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

// Fragment Extensions
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.showToast(message, duration)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

// Number Extensions
fun Float.formatWeight(): String {
    val formatter = DecimalFormat("#.#")
    return "${formatter.format(this)} kg"
}

fun Float.formatMeasurement(): String {
    val formatter = DecimalFormat("#.#")
    return "${formatter.format(this)} cm"
}

fun Float.formatBMI(): String {
    val formatter = DecimalFormat("#.#")
    return formatter.format(this)
}

fun Float.formatProgress(): String {
    val formatter = DecimalFormat("#.#")
    return "${formatter.format(this)}%"
}

// String Extensions
fun String?.isValidWeight(): Boolean {
    if (this.isNullOrBlank()) return false
    return try {
        val weight = this.toFloat()
        weight in Constants.MIN_WEIGHT_KG..Constants.MAX_WEIGHT_KG
    } catch (e: NumberFormatException) {
        false
    }
}

fun String?.isValidHeight(): Boolean {
    if (this.isNullOrBlank()) return false
    return try {
        val height = this.toFloat()
        height in Constants.MIN_HEIGHT_CM..Constants.MAX_HEIGHT_CM
    } catch (e: NumberFormatException) {
        false
    }
}

fun String?.isValidMeasurement(): Boolean {
    if (this.isNullOrBlank()) return false
    return try {
        val measurement = this.toFloat()
        measurement in Constants.MIN_MEASUREMENT_CM..Constants.MAX_MEASUREMENT_CM
    } catch (e: NumberFormatException) {
        false
    }
}