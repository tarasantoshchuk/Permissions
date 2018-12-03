package io.github.tarasantoshchuk.permissions.app

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.github.tarasantoshchuk.permissions.*
import kotlinx.android.synthetic.main.fragment_layout.*

class FragmentWithRequest : Fragment() {
    private val permissions by lazy { Permissions.with(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_layout, container, false)

    }


    private val listener = object : RequestListener {
        override fun onResult(request: RequestInfo, result: RequestResult) {
            Toast.makeText(this@FragmentWithRequest.context, "Result is ${result.result}!", Toast.LENGTH_SHORT).show()
        }

        override fun onShowRationale(request: RequestInfo, callback: RequestCallback) {
            Toast.makeText(this@FragmentWithRequest.context, "Showing rationale!", Toast.LENGTH_SHORT).show()
            callback.proceed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        permissions.registerRequest(3, listener, android.Manifest.permission.READ_SMS)

        button.setOnClickListener {
            permissions.executeRequest(3)
        }
    }
}
