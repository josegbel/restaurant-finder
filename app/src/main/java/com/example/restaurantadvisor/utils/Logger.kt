package com.example.restaurantadvisor.utils

interface Logger {
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String)
}