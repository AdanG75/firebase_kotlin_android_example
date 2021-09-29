package com.platzi.android.firestore.network

import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User
import com.platzi.android.firestore.network.RealtimeDataListener

const val CRYPTO_COLLECTION_NAME = "cryptos"
const val USER_COLLECTION_NAME = "users"

class FirestoreService(val firebaseFirestore: FirebaseFirestore) {

    fun setDocument(data: Any, collectionName: String, id: String, callback: Callback<Void>) {
        firebaseFirestore.collection(collectionName).document(id).set(data)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener { exception -> callback.onFailed(exception) }
    }

    fun updateUser(user: User, callback: Callback<User>?) {
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(user.username)
            .update("cryptosList", user.cryptosList)
            .addOnSuccessListener {
                callback?.onSuccess(user)
            }
            .addOnFailureListener { exception ->
                callback?.onFailed(exception)
            }
    }

    fun updateCrypto(crypto: Crypto) {
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available", crypto.available)
    }

    fun getCryptos(callback: Callback<List<Crypto>>?) {
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
            .get()
            .addOnSuccessListener{ result ->
                for(document in result) {
                    val crytoList = result.toObjects(Crypto::class.java)
                    callback?.onSuccess(crytoList)
                    break
                }
            }
            .addOnFailureListener { exception ->
                callback?.onFailed(exception)
            }

    }


    fun findUserById(id: String, callback: Callback<User>?){
        firebaseFirestore.collection(USER_COLLECTION_NAME)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    callback?.onSuccess(document.toObject(User::class.java))
                } else {
                    callback?.onSuccess(null)
                }
            }
            .addOnFailureListener { exception -> callback?.onFailed(exception) }
    }

    //listener para cambios en cryptos
    fun listenForUpdates(cryptos: List<Crypto>, listener:  RealtimeDataListener<Crypto>) {
        val cryptoReference = firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
        for(crypto in cryptos) {
            cryptoReference.document(crypto.getDocumentId()).addSnapshotListener{ snapshot, e ->
                if(e != null) {
                    listener.onError(e)
                }
                if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(Crypto::class.java)?.let { listener.onDataChange(it) }
                }

            }
        }
    }

    //listenar para cambios en usuario
    fun listenForUpdates(user: User, listener: RealtimeDataListener<User>) {
        val userReference = firebaseFirestore.collection(USER_COLLECTION_NAME)

        userReference.document(user.username).addSnapshotListener{ snapshot, e ->
            if ( e !=  null) {
                listener.onError(e)
            }
            if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(User::class.java)?.let { listener.onDataChange(it) }
            }
        }
    }
}