package dev.chu.opencv.util

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.widget.Toast
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission


val Any.TAG: String get() = this::class.java.simpleName ?: this.toString()

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

fun View.click(block: (View) -> Unit) {
    setOnClickListener(block)
}

fun Context.hasPermissions(vararg permissions: String): Boolean {
    var ret = 0
    //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
    for (perms in permissions) {
        ret = checkCallingOrSelfPermission(this, perms)
        if (ret != PackageManager.PERMISSION_GRANTED) {
            //퍼미션 허가 안된 경우
            return false
        }
    }
    //모든 퍼미션이 허가된 경우
    return true
}