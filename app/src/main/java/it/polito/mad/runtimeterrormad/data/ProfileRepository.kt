package it.polito.mad.runtimeterrormad.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


object ProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")
    private val storageRef = Firebase.storage.reference


    fun getProfile(userID: String): DocumentReference = usersRef.document(userID)

    fun updateRating(rating: Double, review: String, userID: String): Task<Nothing> {
        return db.runTransaction {
            val snapshot = it.get(usersRef.document(userID))

            val newRating: Double = snapshot.getDouble("rating")!! + rating
            it.update(usersRef.document(userID), "rating", newRating)
            it.update(usersRef.document(userID), "totalRating", FieldValue.increment(1))

            if (review.isNotBlank()) {
                usersRef.document(userID).collection("reviews").document()
                    .set(mapOf("review" to review))
            }

            null
        }
    }

    fun getReviews(userID: String): CollectionReference =
        usersRef.document(userID).collection("reviews")


    fun saveProfile(userID: String, profile: Profile): Task<Void> {
        return usersRef.document(userID).set(profile)
    }

    fun saveProfilePhoto(userID: String): StorageReference {
        return storageRef.child("users/${userID}/profilePhoto.jpg")
    }

    fun updateProfilePhoto(userID: String): DocumentReference {
        return usersRef.document(userID)
    }

    /*fun saveProfileLiveData(userID: String, profile: Profile, imageModified: Boolean) {
        if (imageModified) {
            val file =
                Uri.fromFile(File(profile.image))

            val riversRef = storageRef.child("profileImages/${userID}/profilePhoto.jpg")
            val uploadTask = riversRef.putFile(file)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                riversRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    profile.image = task.result.toString()
                    usersRef.document(userID).set(profile)
                        .addOnSuccessListener {
                            Log.d(
                                "Profile Repository",
                                "DocumentSnapshot successfully written!"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "Profile Repository",
                                "Error writing document",
                                e
                            )
                        }
                } else {
                    Log.e("Profile Repository", "ImageFile Error")
                }
            }
        } else {
            usersRef.document(userID).set(profile)
                .addOnSuccessListener {
                    Log.d(
                        "Profile Repository",
                        "DocumentSnapshot successfully written!"
                    )
                }
                .addOnFailureListener { e ->
                    Log.w(
                        "Profile Repository",
                        "Error writing document",
                        e
                    )
                }
        }
    }*/

    fun getItemsOfInterest(userID: String): Query =
        usersRef.document(userID).collection("itemsOfInterest").whereEqualTo("hidden", false)

    /*fun storeItemOfInterest(userID: String, itemID: String, item: Item) {
        usersRef.document(userID).collection("itemsOfInterest").document(itemID).set(item)
    }*/

    /*fun deleteItemOfInterest(userID: String, itemID: String) {
        usersRef.document(userID).collection("itemsOfInterest").document(itemID).delete()
    }*/
}
