package com.veyra.app

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

object VeyraAuth {
    private val auth: FirebaseAuth? by lazy { runCatching { FirebaseAuth.getInstance() }.getOrNull() }

    fun currentUser(): FirebaseUser? = auth?.currentUser
    fun isSignedIn(): Boolean = auth?.currentUser != null
    fun isConfigured(activity: Activity): Boolean = auth != null && activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName) != 0

    suspend fun signInWithGoogle(activity: Activity): FirebaseUser {
        val firebaseAuth = auth ?: error("Firebase is not configured yet.")
        val resourceId = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
        if (resourceId == 0) error("Firebase Google sign-in is not configured yet.")
        val clientId = activity.getString(resourceId)
        if (clientId.isBlank()) error("Firebase Google sign-in is not configured yet.")
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val result = CredentialManager.create(activity).getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            val authResult = firebaseAuth.signInWithCredential(
                com.google.firebase.auth.GoogleAuthProvider.getCredential(google.idToken, null)
            ).await()
            return authResult.user ?: error("Firebase did not return a user.")
        }
        error("The selected credential was not a Google account.")
    }

    fun signOut() = auth?.signOut()
}
