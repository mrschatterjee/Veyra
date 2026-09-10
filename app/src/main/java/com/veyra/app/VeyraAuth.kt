package com.veyra.app

import android.app.Activity
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.security.SecureRandom
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
        val nonceBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val nonce = Base64.encodeToString(nonceBytes, Base64.NO_WRAP or Base64.URL_SAFE)
        val manager = CredentialManager.create(activity)
        val credential = try {
            val option = GetGoogleIdOption.Builder().setServerClientId(clientId).setFilterByAuthorizedAccounts(false).setNonce(nonce).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            manager.getCredential(activity, request).credential
        } catch (_: NoCredentialException) {
            val option = GetSignInWithGoogleOption.Builder().setServerClientId(clientId).setNonce(nonce).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            manager.getCredential(activity, request).credential
        }
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            val authResult = firebaseAuth.signInWithCredential(com.google.firebase.auth.GoogleAuthProvider.getCredential(google.idToken, null)).await()
            return authResult.user ?: error("Firebase did not return a user.")
        }
        error("The selected credential was not a Google account.")
    }

    suspend fun signOut(activity: Activity) {
        auth?.signOut()
        runCatching { CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest()) }
    }
}
