package io.simplelogin.android.module.alias.contact

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.Contact

class ContactListViewModel(context: Context, private val alias: Alias) : BaseViewModel(context) {
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    private var _contacts = mutableListOf<Contact>()
    val contacts: List<Contact>
        get() = _contacts

    private var _currentPage = -1
    var moreToLoad: Boolean = true
        private set
    private var _isFetching: Boolean = false

    private val _eventHaveNewContacts = MutableLiveData<Boolean>()
    val eventHaveNewContacts: LiveData<Boolean>
        get() = _eventHaveNewContacts

    fun fetchContacts() {
        if (!moreToLoad || _isFetching) return
        _isFetching = true
        SLApiService.fetchContacts(apiKey, alias.id, _currentPage + 1) { newContacts, error ->
            _isFetching = false

            if (error != null) {
                _error.postValue(error)
            } else if (newContacts != null) {
                if (newContacts.isEmpty()) {
                    moreToLoad = false
                    _eventHaveNewContacts.postValue(false)
                } else {
                    _currentPage += 1
                    _contacts.addAll(newContacts)
                    _eventHaveNewContacts.postValue(true)
                }
            }
        }
    }

    fun refreshContacts() {
        _currentPage = -1
        moreToLoad = true
        _contacts = mutableListOf()
        fetchContacts()
    }

    // Create
    private var _eventFinishCallingCreateContact = MutableLiveData<Boolean>()
    val eventFinishCallingCreateContact: LiveData<Boolean>
        get() = _eventFinishCallingCreateContact

    fun onHandleFinishCallingCreateContactComplete() {
        _eventFinishCallingCreateContact.value = false
    }

    private var _eventCreatedContact = MutableLiveData<String>()
    val eventCreatedContact: LiveData<String>
        get() = _eventCreatedContact

    fun onHandleCreatedContactComplete() {
        _eventCreatedContact.value = null
    }

    fun create(email: String) {
        SLApiService.createContact(apiKey, alias.id, email) { error ->
            _eventFinishCallingCreateContact.postValue(true)

            if (error != null) {
                _error.postValue(error)
            } else {
                _eventCreatedContact.postValue(email)
            }
        }
    }
}