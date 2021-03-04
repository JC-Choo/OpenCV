package dev.chu.opencv.util

import android.content.Context
import android.widget.Toast

val Any.TAG: String get() = this::class.java.simpleName ?: this.toString()

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()