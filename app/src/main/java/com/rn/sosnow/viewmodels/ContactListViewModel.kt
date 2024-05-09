package com.rn.sosnow.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rn.sosnow.firebase.FbRepository
import com.rn.sosnow.model.Contact

class ContactListViewModel : ViewModel() {
    private val repo = FbRepository()
    private var contactList: LiveData<List<Contact>>? = null

    fun getContacts(): LiveData<List<Contact>> {
        if (contactList == null) {
            contactList = repo.loadContacts()
        }
        return contactList!!
    }
}