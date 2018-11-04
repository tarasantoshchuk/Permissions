package io.github.tarasantoshchuk.permissions

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity


class Permissions private constructor(fManagerProvider: () -> FragmentManager) {
    private val permissionsHandler: PermissionsFragment
        get() {
            var fragment = fragmentManager.findFragmentByTag(TAG)

            if (fragment == null) {
                fragment = PermissionsFragment()

                fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commitNow()
            }

            return fragment as PermissionsFragment
        }

    companion object {
        private val TAG = Permissions::class.java.simpleName

        fun with(activity: AppCompatActivity): Permissions {
            return Permissions { activity.supportFragmentManager }
        }

        fun with(activity: Fragment): Permissions {
            return Permissions { activity.childFragmentManager }
        }
    }

    private val fragmentManager: FragmentManager by lazy {
        fManagerProvider()
    }

    fun request(code: Int, skipRationale: Boolean = false) {
        val requestInfo = permissionsHandler.permissionRequests[code]

        if (permissionsHandler.shouldShowRequestPermissionRationale(requestInfo!!.permissions) && !skipRationale) {
            requestInfo.listener.onShowRationale(callback(code))
        }

        permissionsHandler.requestPermissions(requestInfo.permissions, code)
    }

    private fun callback(code: Int): RequestCallback {
        return object : RequestCallback {
            override fun proceed() {
                request(code, true)
            }

            override fun cancel() {
            }
        }
    }

    fun registerRequest(code: Int, listener: RequestListener, vararg permissions: String) {
        permissionsHandler.permissionRequests[code] = RequestInfo(permissions, listener)
    }

    fun requestNow(code: Int, listener: RequestListener, vararg permissions: String) {
        registerRequest(code, listener, *permissions)
        request(code)
    }
}

class RequestInfo(val permissions: Array<out String>, val listener: RequestListener)

interface RequestListener {
    fun onShowRationale(callback: RequestCallback) {
        callback.proceed()
    }

    fun onResult(result: RequestResult) {
    }

    fun onGranted(result: RequestResult) {
        onResult(result)
    }

    fun onDenied(result: RequestResult) {
        onResult(result)
    }

    fun onDeniedForever(result: RequestResult) {
        onDenied(result)
    }
}

/**
 *
 */
interface RequestCallback {
    /**
     *
     */
    fun proceed()
    fun cancel()
}

enum class PermissionStatus {
    GRANTED,
    DENIED,
    DENIED_FOREVER;

    internal companion object {
        fun merge(results: Array<PermissionStatus>): PermissionStatus {
            var mergeResult = GRANTED

            for (result in results) {
                if (result == DENIED_FOREVER) {
                    return DENIED_FOREVER
                }

                if (result == DENIED) {
                    mergeResult = DENIED
                }
            }

            return mergeResult
        }
    }
}

class RequestResult(permissions: Array<String>, results: Array<PermissionStatus>) {
    val result: PermissionStatus = PermissionStatus.merge(results)
    val detailedResult = HashMap<String, PermissionStatus>()

    fun permissionStatus(permission: String): PermissionStatus {
        return detailedResult[permission]!!
    }
}