package com.veyra.app

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object VeyraCloudSync {
    private const val COLLECTION = "users"
    private const val DOCUMENT = "veyra"

    private fun firestore(): FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    suspend fun signInSync(context: Context): String {
        val user = VeyraAuth.currentUser() ?: return "Not signed in"
        val db = firestore() ?: return "Cloud sync is not configured"
        val ref = db.collection(COLLECTION).document(user.uid).collection("app").document(DOCUMENT)
        val snapshot = ref.get().await()
        val cloud = snapshot.getString("backup")
        val store = VeyraStore(context)
        return if (!cloud.isNullOrBlank()) {
            store.importJson(cloud)
            HabitReminderScheduler.rescheduleAll(context)
            NudgeScheduler.rescheduleAll(context)
            "Cloud data restored"
        } else {
            ref.set(mapOf("backup" to store.exportJson(), "updatedAt" to System.currentTimeMillis())).await()
            "Local data uploaded"
        }
    }

    suspend fun upload(context: Context): Boolean {
        val user = VeyraAuth.currentUser() ?: return false
        val db = firestore() ?: return false
        val ref = db.collection(COLLECTION).document(user.uid).collection("app").document(DOCUMENT)
        ref.set(mapOf("backup" to VeyraStore(context).exportJson(), "updatedAt" to System.currentTimeMillis())).await()
        return true
    }
}
