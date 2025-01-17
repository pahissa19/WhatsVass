package com.chat.whatsvass.ui.theme.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chat.whatsvass.R
import com.chat.whatsvass.data.constants.CHAT_ID_ARGUMENT
import com.chat.whatsvass.data.constants.KEY_ID
import com.chat.whatsvass.data.constants.KEY_MODE
import com.chat.whatsvass.data.constants.KNICK_ARGUMENT
import com.chat.whatsvass.data.constants.ONLINE_ARGUMENT
import com.chat.whatsvass.data.constants.SHARED_SETTINGS
import com.chat.whatsvass.data.constants.SHARED_USER_DATA
import com.chat.whatsvass.data.constants.VIEW_FROM
import com.chat.whatsvass.data.domain.model.contacts.Contacts
import com.chat.whatsvass.data.domain.repository.remote.response.create_chat.ChatRequest
import com.chat.whatsvass.ui.theme.Contrast
import com.chat.whatsvass.ui.theme.Dark
import com.chat.whatsvass.ui.theme.DarkMode
import com.chat.whatsvass.ui.theme.Light
import com.chat.whatsvass.ui.theme.Main
import com.chat.whatsvass.ui.theme.White
import com.chat.whatsvass.ui.theme.chat.ChatView
import com.chat.whatsvass.ui.theme.home.HomeView
import com.chat.whatsvass.ui.theme.home.HomeViewModel
import com.chat.whatsvass.ui.theme.login.hideKeyboard
import com.chat.whatsvass.ui.theme.settings.SettingsView
import com.chat.whatsvass.usecases.token.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val DELAY_TO_OPEN_CHAT = 150L

class ContactsView : ComponentActivity() {
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var sharedPreferencesUserData: SharedPreferences
    private lateinit var sharedPreferencesSettings: SharedPreferences

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.apply {
            @Suppress("DEPRECATION")
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        sharedPreferencesUserData = getSharedPreferences(SHARED_USER_DATA, Context.MODE_PRIVATE)

        val token = Token.token
        val myId = sharedPreferencesUserData.getString(KEY_ID, null)

        sharedPreferencesSettings = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
        val isDarkModeActive = sharedPreferencesSettings.getBoolean(KEY_MODE, false)

        setContent {

            // Observar el resultado del ViewModel
            LaunchedEffect(key1 = viewModel) {
                if (token != null) {
                    viewModel.getContacts(token)
                }
            }
            // Observar el resultado del ViewModel y configurar el contenido de la pantalla de inicio
            val contactsResult by viewModel.contactsResult.collectAsState(emptyList())
            ContactsScreen(this, token!!, myId!!, contactsResult, viewModel, isDarkModeActive)

        }
        window.decorView.setOnTouchListener { _, _ ->
            hideKeyboard(this)
            false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        @Suppress("DEPRECATION")
        super.onBackPressed()
        val intent = Intent(this, HomeView::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val token = Token.token
        if (token != null) {
            // Actualizar el estado en línea del usuario como "en línea" cuando se reanuda la actividad
            HomeViewModel().updateUserOnlineStatus(token, true)
        }
    }

    override fun onPause() {
        super.onPause()
        val token = Token.token
        if (token != null) {
            // Actualizar el estado en línea del usuario como "fuera de línea" cuando se pausa la actividad
            HomeViewModel().updateUserOnlineStatus(token, false)
        }
    }
}

@Composable
fun ContactsScreen(
    context: Context,
    token: String,
    myId: String,
    contacts: List<Contacts>,
    viewModel: ContactsViewModel,
    isDarkModeActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkModeActive) DarkMode else White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBarAndList(context, token, myId, contacts, viewModel, isDarkModeActive)

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


@Composable
fun TopBarAndList(
    context: Context,
    token: String,
    myId: String,
    contacts: List<Contacts>,
    viewModel: ContactsViewModel,
    isDarkModeActive: Boolean
) {
    val isTextWithOutContactsVisible by viewModel.isTextWithOutVisibleFlow.collectAsState(false)
    val isProgressBarVisible by viewModel.isProgressVisibleFlow.collectAsState(true)
    var searchText by remember { mutableStateOf(TextFieldValue()) }
    var listSearch by remember { mutableStateOf<List<Contacts>>(emptyList()) }

    TopAppBar(
        backgroundColor = Main,
        elevation = 4.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val intent = Intent(context, HomeView::class.java)
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_arrow_back),
                    contentDescription = "Back",
                    tint = Dark
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(White)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Spacer(modifier = Modifier.weight(0.1f))
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_search),
                        contentDescription = stringResource(R.string.search),
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    BasicTextField(
                        value = searchText.text,
                        onValueChange = { searchText = TextFieldValue(it) },
                        textStyle = MaterialTheme.typography.body1,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                if (searchText.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.textFieldSearch),
                                        style = MaterialTheme.typography.body1,
                                        color = Color.Gray
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(80.dp))

            IconButton(
                onClick = {
                    val intent = Intent(context, SettingsView::class.java)
                    intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra(VIEW_FROM, "Contacts")
                    context.startActivity(intent)
                },
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.settings),
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        if (!isProgressBarVisible) {
            LazyColumn {
                // Ordenar contactos alfabeticamente
                val sortedContacts =
                    contacts.sortedBy { it.nick.replaceFirstChar { name -> name.uppercaseChar() } }
                if (searchText.text.isEmpty()) {
                    items(sortedContacts,
                        key = { contact ->
                            // La llave sirve para que cada valor se mueva con su celda
                            contact.id
                        }
                    ) { contact ->
                        ContactItem(
                            context,
                            contact,
                            token,
                            viewModel,
                            ChatRequest(myId, contact.id),
                            isDarkModeActive
                        )
                    }
                } else {
                    listSearch =
                        sortedContacts.filter {
                            it.nick.contains(
                                searchText.text,
                                ignoreCase = true
                            )
                        }
                    items(listSearch,
                        key = { contact ->
                            contact.id
                        }
                    ) { contact ->
                        ContactItem(
                            context,
                            contact,
                            token,
                            viewModel,
                            ChatRequest(myId, contact.id),
                            isDarkModeActive
                        )
                    }
                }
            }
        }

        val textColor = if (isDarkModeActive) White else Dark
        // Si no hay contactos se muestra: "Sin contactos"
        if (isTextWithOutContactsVisible) {
            Column(
                modifier = Modifier
                    .height(400.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.withoutContacts),
                    fontSize = 22.sp,
                    color = textColor
                )
            }
        }

        // Si no se encuentra el contacto buscado se muestra texto: "Sin coincidencias"
        if (listSearch.isEmpty() && (searchText.text.isNotEmpty() || searchText.text == R.string.textFieldSearch.toString())) {
            Column(
                modifier = Modifier
                    .height(400.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.noMatches),
                    fontSize = 22.sp,
                    color = textColor
                )
            }
        }
        // Si hay contactos y el searchText esta vacio se muestra el progressBar
        if (isProgressBarVisible && searchText.text.isEmpty()) {
            Column(
                modifier = Modifier
                    .height(400.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = Light,
                    trackColor = Dark,
                )
            }
        }
    }
}

@Composable
fun ContactItem(
    context: Context,
    contact: Contacts,
    token: String,
    viewModel: ContactsViewModel,
    chatRequest: ChatRequest,
    isDarkModeActive: Boolean
) {
    val colorWithOpacity = Contrast.copy(alpha = 0.4f)
    val isNewChatCreated by viewModel.isNewChatCreatedFlow.collectAsState(false)
    val newChat by viewModel.newChatResult.collectAsState(null)

    Row(
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .requiredWidth(width = 368.dp)
            .requiredHeight(height = 74.dp)
            .clip(shape = RoundedCornerShape(20.dp))
            .background(colorWithOpacity)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        viewModel.createNewChat(token, chatRequest)
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(DELAY_TO_OPEN_CHAT)
                            if (isNewChatCreated && newChat != null) {
                                val intent = Intent(context, ChatView::class.java)
                                intent
                                    .putExtra(CHAT_ID_ARGUMENT, newChat!!.chat.id)
                                    .putExtra(KNICK_ARGUMENT, contact.nick)
                                    .putExtra(ONLINE_ARGUMENT, contact.online.toString())
                                context.startActivity(intent)
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.couldNotCreateChat),
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Spacer(modifier = Modifier.weight(0.1f))
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.image_person),
                contentDescription = stringResource(id = R.string.profilePhoto),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = contact.nick,
                style = TextStyle(fontSize = 16.sp, color = if (isDarkModeActive) White else Dark),
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.weight(0.1f))
        Spacer(modifier = Modifier.height(50.dp))
    }
}







