package com.example.restaurantadvisor.utils

fun Float.roundOffDecimal(): Float {
    return "%.2f".format(this).toFloat()
}