package com.veyra.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class GoogleSignInActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val requestCode = 7401

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            startActivityForResult(VeyraAuth.googleSignInIntent(this), requestCode)
        } catch (e: Exception) {
            fail(e.message ?: "Google sign-in could not be started.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != this.requestCode) return
        if (resultCode != RESULT_OK) { fail("Google sign-in was cancelled."); return }
        scope.launch {
            try {
                VeyraAuth.completeGoogleSignIn(this@GoogleSignInActivity, data)
                startActivity(Intent(this@GoogleSignInActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
                finish()
            } catch (e: Exception) {
                fail(e.message ?: "Google sign-in failed.")
            }
        }
    }

    private fun fail(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
