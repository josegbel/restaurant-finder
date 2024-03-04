package com.example.restaurantadvisor

import com.example.restaurantadvisor.utils.Logger

class FakeLogger : Logger {
    override fun e(tag: String, message: String, throwable: Throwable?) {
        // Do nothing or capture logs for assertion
    }

    override fun d(tag: String, message: String) {
        // Do nothing or capture logs for assertion
    }
}