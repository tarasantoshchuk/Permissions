package io.github.tarasantoshchuk.permissions.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import io.github.tarasantoshchuk.permissions.Permissions
import io.github.tarasantoshchuk.permissions.RequestCallback
import io.github.tarasantoshchuk.permissions.RequestListener
import io.github.tarasantoshchuk.permissions.RequestResult
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val listener = object : RequestListener {
        override fun onResult(result: RequestResult) {
            Toast.makeText(this@MainActivity, "Result is ${result.result}!", Toast.LENGTH_SHORT).show()
        }

        override fun onShowRationale(callback: RequestCallback) {
            Toast.makeText(this@MainActivity, "Showing rationale!", Toast.LENGTH_SHORT).show()
            callback.proceed()
        }
    }

    private val permissions by lazy { Permissions.with(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissions.apply {
            registerRequest(1, listener, android.Manifest.permission.GET_ACCOUNTS)
            requestNow(2, listener, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        text.setOnClickListener {
            permissions.request(1)
        }
    }


}
