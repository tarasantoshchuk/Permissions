package io.github.tarasantoshchuk.permissions.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Toast
import io.github.tarasantoshchuk.permissions.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val FRAGMENT_TAG = "FRAGMENT_TAG"
    }

    private val listener = object : RequestListener {
        override fun onResult(request: RequestInfo, result: RequestResult) {
            Toast.makeText(this@MainActivity, "request #${request.requestCode} :: Result is ${result.result}!", Toast.LENGTH_SHORT).show()
        }

        override fun onShowRationale(request: RequestInfo, callback: RequestCallback) {
            AlertDialog.Builder(this@MainActivity)
                .setMessage("Please provide following permissions: ${request.permissions.contentToString()}")
                .setPositiveButton("OK") {_, _ ->
                    callback.proceed()
                }
                .setOnCancelListener {
                    callback.cancel()
                }
                .show()
        }
    }

    private val permissions by lazy { Permissions.with(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, FragmentWithRequest(), FRAGMENT_TAG)
                .commitNow()
        }

        permissions.apply {
            registerRequest(1, listener, android.Manifest.permission.GET_ACCOUNTS)
            registerAndExecuteRequest(2, listener, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        text.setOnClickListener {
            permissions.executeRequest(1)
        }
    }


}
