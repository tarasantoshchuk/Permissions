package io.github.tarasantoshchuk.permissions

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment


class PermissionsFragment: Fragment() {
    internal val permissionRequests = HashMap<Int, RequestInfo>()

    private fun requestResults(
        permissions: Array<String>,
        grantResults: IntArray
    ): RequestResult {
        val results = Array(permissions.size) {
            PermissionStatus.GRANTED
        }

        grantResults.forEachIndexed { index: Int, item: Int ->
            results[index] = when (item) {
                PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
                PackageManager.PERMISSION_DENIED -> {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            activity!!,
                            permissions[index]
                        )
                    ) {
                        PermissionStatus.DENIED_FOREVER
                    } else {
                        PermissionStatus.DENIED
                    }
                }
                else -> throw IllegalArgumentException()
            }
        }

        return RequestResult(permissions, results)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val requestResult = requestResults(permissions, grantResults)

        val requestInfo = permissionRequests[requestCode]

        requestInfo!!.listener.apply {
            when (requestResult.result) {
                PermissionStatus.GRANTED -> onGranted(requestInfo, requestResult)
                PermissionStatus.DENIED -> onDenied(requestInfo, requestResult)
                PermissionStatus.DENIED_FOREVER -> onDeniedForever(requestInfo, requestResult)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun shouldShowRequestPermissionRationale(permissions: Array<out String>): Boolean {
        for (permission in permissions) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true
            }
        }

        return false
    }
}
