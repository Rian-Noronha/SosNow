package com.rn.sosnow.firebase

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.rn.sosnow.model.Contact

class FbRepository {
    private val firestore = FirebaseFirestore.getInstance()
    fun loadContacts(): LiveData<List<Contact>>{
        return object : LiveData<List<Contact>>(){
            override fun onActive() {
                super.onActive()
                firestore.collection(CONTACTS_KEY)
                    .addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                        val contacts = querySnapshot?.mapNotNull { document ->
                            document.toObject(Contact::class.java)?.let { contact ->
                                contact.id = document.id
                                contact
                            }
                        }
                        value = contacts
                    }
            }
        }
    }

    companion object{
        const val CONTACTS_KEY = "contacts"
    }

}



