package com.chat.whatsvass.ui.theme.contacts

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.whatsvass.data.domain.model.contacts.Contacts
import com.chat.whatsvass.data.domain.model.create_chat.CreatedChat
import com.chat.whatsvass.data.domain.repository.remote.ContactsRepository
import com.chat.whatsvass.data.domain.repository.remote.response.create_chat.ChatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {

    private val contactsRepository = ContactsRepository()

    private val isTextWithOutContactsVisible = MutableStateFlow(false)
    var isTextWithOutVisibleFlow: Flow<Boolean> = isTextWithOutContactsVisible

    private val _contactsResult = MutableStateFlow<List<Contacts>>(emptyList())
    val contactsResult: StateFlow<List<Contacts>> = _contactsResult


    fun getContacts (token: String){
        viewModelScope.launch {
            async {
                getContactsList(token)
            }.await()
            if (contactsResult.value.isEmpty()){
                isTextWithOutContactsVisible.value = true
            }
        }
    }
    suspend fun getContactsList(token: String) {
        try {
            val contacts = contactsRepository.getContacts(token!!)
            _contactsResult.value = contacts
            Log.d("Contactos", contacts.toString())
            isTextWithOutContactsVisible.value = false
        } catch (e: Exception) {
            Log.d("Contactos", "$token")
            Log.d("Contactos", "Error al mostrar contactos: ${e.message}")
        }

    }

    private val _newChatResult = MutableStateFlow<CreatedChat?>(null)
    val newChatResult: StateFlow<CreatedChat?> = _newChatResult
    fun createNewChat(context: Context, token: String, chatRequest: ChatRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newChat = contactsRepository.createNewChat(token!!, chatRequest)
                _newChatResult.value = newChat
                Log.d("Nuevo chat", newChat.toString())

            } catch (e: Exception) {
                Toast.makeText(context, "Error al crear chat", Toast.LENGTH_SHORT).show()
                Log.d("Nuevo chat", "Error al crear chat: ${e.message}")
            }
        }
    }
}





