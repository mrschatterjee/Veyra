package com.veyra.app

import android.app.Activity
import android.content.Intent
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

object VeyraAuth {
    private val auth: FirebaseAuth? by lazy { runCatching { FirebaseAuth.getInstance() }.getOrNull() }

    fun currentUser(): FirebaseUser? = auth?.currentUser
    fun isSignedIn(): Boolean = auth?.currentUser != null
    fun isConfigured(activity: Activity): Boolean = auth != null && activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName) != 0

    private fun googleClientId(activity: Activity): String {
        val id = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
        if (id == 0) error("Google sign-in is not configured. Check Firebase Google provider and google-services.json.")
        return activity.getString(id).takeIf { it.isNotBlank() }
            ?: error("Google sign-in is not configured. Check Firebase Google provider and google-services.json.")
    }

    /** Explicit Google button flow. This is the primary Veyra login path. */
    suspend fun signInWithGoogle(activity: Activity): FirebaseUser {
        val firebaseAuth = auth ?: error("Firebase is not configured yet.")
        val clientId = googleClientId(activity)
        val nonceBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val nonce = Base64.encodeToString(
            nonceBytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
        val option = GetSignInWithGoogleOption.Builder(clientId)
            .setNonce(nonce)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val result = CredentialManager.create(activity).getCredential(activity, request)
        val credential = result.credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            error("Google did not return a supported sign-in credential.")
        }
        val google = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = com.google.firebase.auth.GoogleAuthProvider.getCredential(google.idToken, null)
        return firebaseAuth.signInWithCredential(firebaseCredential).await().user
            ?: error("Firebase did not return a user.")
    }

    // Legacy activity-based methods are retained for compatibility with the old activity class.
    private fun googleOptions(activity: Activity) = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(googleClientId(activity))
        .requestEmail()
        .requestProfile()
        .build()

    fun googleSignInIntent(activity: Activity): Intent {
        if (auth == null) error("Firebase is not configured yet.")
        return GoogleSignIn.getClient(activity, googleOptions(activity)).signInIntent
    }

    suspend fun completeGoogleSignIn(activity: Activity, data: Intent?): FirebaseUser {
        val firebaseAuth = auth ?: error("Firebase is not configured yet.")
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
        val token = account.idToken ?: error("Google did not return an ID token.")
        return firebaseAuth.signInWithCredential(com.google.firebase.auth.GoogleAuthProvider.getCredential(token, null)).await().user
            ?: error("Firebase did not return a user.")
    }

    suspend fun signOut(activity: Activity) {
        auth?.signOut()
        runCatching { CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest()) }
        runCatching { GoogleSignIn.getClient(activity, googleOptions(activity)).signOut().await() }
    }
}
