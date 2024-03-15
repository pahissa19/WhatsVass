package com.chat.whatsvass.ui.theme.contacts
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chat.whatsvass.commons.KEY_TOKEN
import com.chat.whatsvass.commons.SHARED_TOKEN
import com.chat.whatsvass.data.domain.model.contacts.Contacts
import com.chat.whatsvass.data.domain.repository.remote.ContactsRepository
import com.chat.whatsvass.ui.theme.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application): AndroidViewModel(application) {
    sealed class ContactsResult {
        data class Success(val contacts: List<Contacts>) : ContactsResult()
        data class Error(val message: String) : ContactsResult()
    }

    private val contactsRepository = ContactsRepository()

    private val _contactsResult = MutableStateFlow<ContactsResult?>(null)
    val contactsResult: StateFlow<ContactsResult?> = _contactsResult
    fun getContacts(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contacts = contactsRepository.getContacts(token)
                Log.d("Contactos", contacts.toString())
                if (!contacts.isNullOrEmpty()) {
                    _contactsResult.value = ContactsResult.Success(contacts)
                } else {
                    _contactsResult.value = ContactsResult.Error("No se pudo obtener los contactos")
                }
            } catch (e: Exception) {
                _contactsResult.value = ContactsResult.Error("Error al mostrar contactos: ${e.message}")
            }
        }
    }
}






