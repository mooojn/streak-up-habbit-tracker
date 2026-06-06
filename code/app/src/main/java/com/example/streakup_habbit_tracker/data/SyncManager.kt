package com.example.streakup_habbit_tracker.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncManager(private val database: AppDatabase) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val habitDao = database.habitDao()

    /**
     * Uploads all local habits to Firestore for the currently logged-in user.
     */
    suspend fun pushLocalDataToCloud() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val habits = habitDao.getAllHabits()

        val batch = firestore.batch()
        val userHabitsCollection = firestore.collection("users").document(userId).collection("habits")

        habits.forEach { habitEntity ->
            val docRef = userHabitsCollection.document(habitEntity.id)
            batch.set(docRef, habitEntity)
        }

        try {
            batch.commit().await()
            Log.d("SyncManager", "Successfully pushed ${habits.size} habits to cloud.")
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push data to cloud.", e)
        }
    }

    /**
     * Downloads all habits from Firestore for the currently logged-in user and saves them locally.
     */
    suspend fun pullCloudDataToLocal() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        
        try {
            val snapshot = firestore.collection("users").document(userId).collection("habits").get().await()
            val cloudHabits = snapshot.documents.mapNotNull { it.toObject(HabitEntity::class.java) }
            
            if (cloudHabits.isNotEmpty()) {
                habitDao.insertHabits(cloudHabits)
                Log.d("SyncManager", "Successfully pulled ${cloudHabits.size} habits from cloud.")
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull data from cloud.", e)
        }
    }

    /**
     * Syncs a single habit change to the cloud immediately.
     */
    fun syncHabitChange(habitEntity: HabitEntity) {
        val user = auth.currentUser ?: return
        firestore.collection("users")
            .document(user.uid)
            .collection("habits")
            .document(habitEntity.id)
            .set(habitEntity)
            .addOnFailureListener { e ->
                Log.e("SyncManager", "Failed to sync individual habit.", e)
            }
    }
    
    /**
     * Deletes a habit from the cloud.
     */
    fun deleteHabitFromCloud(habitId: String) {
        val user = auth.currentUser ?: return
        firestore.collection("users")
            .document(user.uid)
            .collection("habits")
            .document(habitId)
            .delete()
            .addOnFailureListener { e ->
                Log.e("SyncManager", "Failed to delete habit from cloud.", e)
            }
    }
}
