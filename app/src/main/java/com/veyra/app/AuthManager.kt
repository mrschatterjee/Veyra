package com.veyra.app

import android.content.Context
import android.util.Base64
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.ClearCredentialStateRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.security.SecureRandom

class AuthManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    fun currentUser() = auth.currentUser

    fun buildRequest(filterAuthorized: Boolean): GetCredentialRequest {
        val nonceBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val nonce = Base64.encodeToString(nonceBytes, Base64.NO_WRAP or Base64.URL_SAFE)
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(com.veyra.app.R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(filterAuthorized)
            .setNonce(nonce)
            .build()
        return GetCredentialRequest.Builder().addCredentialOption(option).build()
    }

    suspend fun signIn(response: GetCredentialResponse): Result<Unit> = runCatching {
        val credential: Credential = response.credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            throw IllegalStateException("Google sign-in credential was not returned")
        }
        val googleCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: GoogleIdTokenParsingException) {
            throw IllegalStateException("Unable to read the Google account credential", e)
        }
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        auth.signInWithCredential(firebaseCredential).awaitResult()
        Unit
    }

    suspend fun signOut() {
        auth.signOut()
        runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
    }
}
