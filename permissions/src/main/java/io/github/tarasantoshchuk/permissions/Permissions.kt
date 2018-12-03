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

    fun executeRequest(code: Int, skipRationale: Boolean = false) {
        val requestInfo = permissionsHandler.permissionRequests[code]!!

        if (permissionsHandler.shouldShowRequestPermissionRationale(requestInfo.permissions) && !skipRationale) {
            requestInfo.state = State.RATIONALE
            requestInfo.listener.onShowRationale(requestInfo, callback(code))
        } else {
            requestInfo.state = State.SYSTEM_DIALOG
            permissionsHandler.requestPermissions(requestInfo.permissions, code)
        }
    }

    private fun callback(code: Int): RequestCallback {
        return object : RequestCallback {
            override fun proceed() {
                executeRequest(code, true)
            }

            override fun cancel() {
                abortRequest(code)
            }
        }
    }

    private fun abortRequest(code: Int) {
        permissionsHandler.permissionRequests[code]!!.state = State.IDLE
    }

    fun registerRequest(code: Int, listener: RequestListener, vararg permissions: String) {
        val requestInfo = permissionsHandler.permissionRequests[code]
        if (requestInfo == null) {
            permissionsHandler.permissionRequests[code] = RequestInfo(code, permissions, listener)
        } else {
            requestInfo.listener = listener

            if (requestInfo.state == State.RATIONALE) {
                listener.onShowRationale(requestInfo, callback(code))
            }
        }
    }

    fun registerAndExecuteRequest(code: Int, listener: RequestListener, vararg permissions: String) {
        val requestInfo = permissionsHandler.permissionRequests[code]

        if (requestInfo == null) {
            permissionsHandler.permissionRequests[code] = RequestInfo(code, permissions, listener)
            executeRequest(code)
        } else {
            requestInfo.listener = listener

            if (requestInfo.state == State.RATIONALE) {
                listener.onShowRationale(requestInfo, callback(code))
            }
        }
    }
}

enum class State {
    IDLE,
    RATIONALE,
    SYSTEM_DIALOG,
}

class RequestInfo constructor(
    val requestCode: Int,
    val permissions: Array<out String>,
    internal var listener: RequestListener,
    internal var state: State = State.IDLE
)

interface RequestListener {
    fun onShowRationale(request: RequestInfo, callback: RequestCallback) {
        callback.proceed()
    }

    fun onResult(request: RequestInfo, result: RequestResult) {
    }

    fun onGranted(request: RequestInfo, result: RequestResult) {
        onResult(request, result)
    }

    fun onDenied(request: RequestInfo, result: RequestResult) {
        onResult(request, result)
    }

    fun onDeniedForever(request: RequestInfo, result: RequestResult) {
        onDenied(request, result)
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