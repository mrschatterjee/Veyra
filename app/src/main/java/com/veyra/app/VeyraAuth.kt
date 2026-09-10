package com.veyra.app

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

object VeyraAuth {
    private val auth: FirebaseAuth? by lazy { runCatching { FirebaseAuth.getInstance() }.getOrNull() }
    fun currentUser(): FirebaseUser? = auth?.currentUser
    fun isSignedIn(): Boolean = auth?.currentUser != null
    fun isConfigured(activity: Activity): Boolean = auth != null && activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName) != 0
    private fun googleClientId(activity: Activity): String { val id=activity.resources.getIdentifier("default_web_client_id","string",activity.packageName); if(id==0) error("Firebase Google sign-in is not configured yet."); return activity.getString(id).takeIf{it.isNotBlank()} ?: error("Firebase Google sign-in is not configured yet.") }
    private fun googleOptions(activity: Activity)=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(googleClientId(activity)).requestEmail().requestProfile().build()
    fun googleSignInIntent(activity: Activity): Intent { if(auth==null) error("Firebase is not configured yet."); return GoogleSignIn.getClient(activity,googleOptions(activity)).signInIntent }
    suspend fun completeGoogleSignIn(activity: Activity,data: Intent?): FirebaseUser { val firebaseAuth=auth ?: error("Firebase is not configured yet."); val account=GoogleSignIn.getSignedInAccountFromIntent(data).await(); val token=account.idToken ?: error("Google did not return an ID token."); val result=firebaseAuth.signInWithCredential(com.google.firebase.auth.GoogleAuthProvider.getCredential(token,null)).await(); return result.user ?: error("Firebase did not return a user.") }
    suspend fun signInWithGoogle(activity: Activity): FirebaseUser { val account=GoogleSignIn.getClient(activity,googleOptions(activity)).silentSignIn().await(); val token=account.idToken ?: error("Google did not return an ID token."); val result=auth?.signInWithCredential(com.google.firebase.auth.GoogleAuthProvider.getCredential(token,null))?.await() ?: error("Firebase is not configured yet."); return result.user ?: error("Firebase did not return a user.") }
    suspend fun signOut(activity: Activity) { auth?.signOut(); runCatching { GoogleSignIn.getClient(activity,googleOptions(activity)).signOut().await() } }
}
