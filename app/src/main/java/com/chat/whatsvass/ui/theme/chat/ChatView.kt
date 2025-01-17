package com.chat.whatsvass.ui.theme.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.chat.whatsvass.R
import com.chat.whatsvass.data.constants.CHAT_ID_ARGUMENT
import com.chat.whatsvass.data.constants.DELAY_GET_MESSAGESFORCHAT
import com.chat.whatsvass.data.constants.KEY_MODE
import com.chat.whatsvass.data.constants.KNICK_ARGUMENT
import com.chat.whatsvass.data.constants.LIMIT_GET_MESSAGESFORCHAT
import com.chat.whatsvass.data.constants.OFFSET_GET_MESSAGESFORCHAT
import com.chat.whatsvass.data.constants.ONLINE_ARGUMENT
import com.chat.whatsvass.data.constants.SHARED_SETTINGS
import com.chat.whatsvass.data.constants.SHARED_USER_DATA
import com.chat.whatsvass.data.constants.SOURCE_ID
import com.chat.whatsvass.data.domain.model.message.Message
import com.chat.whatsvass.data.domain.repository.remote.response.create_message.MessageRequest
import com.chat.whatsvass.ui.theme.Contrast
import com.chat.whatsvass.ui.theme.Dark
import com.chat.whatsvass.ui.theme.DarkMode
import com.chat.whatsvass.ui.theme.Main
import com.chat.whatsvass.ui.theme.White
import com.chat.whatsvass.ui.theme.home.HomeView
import com.chat.whatsvass.ui.theme.home.HomeViewModel
import com.chat.whatsvass.ui.theme.login.hideKeyboard
import com.chat.whatsvass.usecases.token.Token
import com.chat.whatsvass.utils.DateTimeUtils
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private lateinit var sharedPreferencesToken: SharedPreferences
private lateinit var sharedPreferencesSettings: SharedPreferences

@Suppress("DEPRECATION")
class ChatView : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    var online = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.apply {
            @Suppress("DEPRECATION")
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        sharedPreferencesToken = getSharedPreferences(SHARED_USER_DATA, Context.MODE_PRIVATE)

        val token = Token.token

        sharedPreferencesSettings = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
        val isDarkModeActive = sharedPreferencesSettings.getBoolean(KEY_MODE, false)

        val chatId = intent.getStringExtra(CHAT_ID_ARGUMENT)
        val nick = intent.getStringExtra(KNICK_ARGUMENT)

        setContent {
            val messages by viewModel.message.collectAsState(emptyMap())

            if (token != null && chatId != null) {
                viewModel.getMessagesForChat(
                    token, chatId, OFFSET_GET_MESSAGESFORCHAT, LIMIT_GET_MESSAGESFORCHAT
                )
            }

            if (nick != null) {
                ChatScreen(chatId = chatId, messages = messages, nick = nick, isDarkModeActive)
            }

        }
        window.decorView.setOnTouchListener { _, _ ->
            hideKeyboard(this)
            false
        }
    }


    @Composable
    fun ChatScreen(
        chatId: String?, messages: Map<String, List<Message>>, nick: String, isDarkModeActive: Boolean
    ) {
        val token = Token.token

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkModeActive) DarkMode else Color.White)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                TopBarChat(nick)
                chatId?.let {
                    MessageList(chatId = it, messages = messages, token!!, isDarkModeActive)
                }
            }
            BottomBar(chatId, isDarkModeActive, onSendMessage = { })
        }
    }

    @Composable
    fun TopBarChat(nick: String) {
        online = intent.getStringExtra(ONLINE_ARGUMENT).toBoolean()
        TopAppBar(
            backgroundColor = Main,
            elevation = 4.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .fillMaxWidth()
                    .requiredWidth(width = 368.dp)
                    .requiredHeight(height = 74.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(Main),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(this@ChatView, HomeView::class.java)
                    startActivity(intent)
                    this@ChatView.finish()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_arrow_back),
                        contentDescription = "Back",
                        tint = Dark
                    )
                }
                Spacer(modifier = Modifier.weight(0.1f))
                Box(
                    modifier = Modifier.size(53.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.image_person),
                            contentDescription = stringResource(R.string.profilePhoto),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }
                    val color: Color = if (online) Color.Green
                    else Color.Red
                    Icon(
                        painter = painterResource(id = R.drawable.ic_circle),
                        contentDescription = stringResource(R.string.customIcon),
                        tint = color,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(17.dp)
                            .padding(
                                end = 5.dp,
                                bottom = 4.dp
                            )
                    )
                }

                Text(
                    text = nick,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 40.dp)
                        .weight(1f)
                )
            }
        }
    }

    @Composable
    fun MessageList(
        chatId: String, messages: Map<String, List<Message>>, token: String, isDarkModeActive: Boolean
    ) {
        val sourceId = sharedPreferencesToken.getString(SOURCE_ID, null)
        var refreshing by remember { mutableStateOf(false) }
        val isTextStartTheChatVisible by viewModel.isTextStartTheChatVisibleFlow.collectAsState(
            false
        )
        val chatMessages = messages[chatId] ?: emptyList()

        SwipeRefresh(state = rememberSwipeRefreshState(refreshing), onRefresh = {
            refreshing = true
            MainScope().launch {
                viewModel.getMessagesForChat(
                    token, chatId, OFFSET_GET_MESSAGESFORCHAT, LIMIT_GET_MESSAGESFORCHAT
                )
                delay(DELAY_GET_MESSAGESFORCHAT)
                refreshing = false
            }
        }) {
            val messagesDates = mutableListOf<String>()
            for (i in chatMessages) {
                messagesDates.add(DateTimeUtils().formatTimeToSeparateMessages(i.date, this))
            }
            val dates = messagesDates.distinct().sortedDescending()

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in dates) {
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Text(
                        text = if (i.contains(getString(R.string.today))) {
                            getString(R.string.today)
                        } else if (i.contains(getString(R.string.yesterday))) {
                            getString(R.string.yesterday)
                        } else {
                            i
                        }, style = TextStyle(
                            fontSize = 14.sp, color = if (isDarkModeActive) White else Color.Gray
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    chatMessages.forEach { message ->
                        if (DateTimeUtils().formatTimeToSeparateMessages(message.date, this@ChatView) == i) {
                            if (message.source == sourceId) {
                                MessageItem(message, true, isDarkModeActive)
                            } else {
                                MessageItem(message, false, isDarkModeActive)
                            }
                        }
                    }
                }

                if (isTextStartTheChatVisible && dates.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.startTheChat), style = TextStyle(
                                fontSize = 22.sp, color = if (isDarkModeActive) White else Dark
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MessageItem(messages: Message, isSentByUser: Boolean, isDarkModeActive: Boolean) {
        val horizontalPadding = 30.dp
        val verticalPadding = 8.dp

        val backgroundColor = if (isDarkModeActive) Contrast.copy(alpha = 0.4f) else White
        val alignment = if (isSentByUser) TextAlign.Start else TextAlign.End
        val startPadding = if (isSentByUser) horizontalPadding else 0.dp
        val endPadding = if (isSentByUser) 0.dp else horizontalPadding

        val formattedTime = DateTimeUtils().formatTimeFromApiHourChatView(messages.date)


        Row(
            modifier = Modifier
                .padding(vertical = verticalPadding, horizontal = horizontalPadding)
                .fillMaxWidth(),
            horizontalArrangement = if (isSentByUser) Arrangement.End else Arrangement.Start,
        ) {

            Box(
                modifier = Modifier
                    .padding(start = startPadding, end = endPadding)
                    .background(
                        color = backgroundColor, shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = verticalPadding, horizontal = horizontalPadding)

            ) {

                Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                    Text(
                        text = messages.message,
                        color = if (isDarkModeActive) White else Color.Black,
                        textAlign = alignment,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = formattedTime,
                        style = TextStyle(
                            fontSize = 14.sp, color = if (isDarkModeActive) White else Color.Gray
                        ),
                    )
                }
            }
        }
    }

    @Composable
    fun BottomBar(
        chatId: String?,
        isDarkModeActive: Boolean,
        onSendMessage: (String) -> Unit,
    ) {
        var messageText by remember { mutableStateOf("") }
        val token = Token.token
        val sourceID = sharedPreferencesToken.getString(SOURCE_ID, null)


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 30.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .background(Color.Transparent)
                    .clip(RoundedCornerShape(40.dp)),
                placeholder = { Text(text = stringResource(R.string.writeAMessage)) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            IconButton(onClick = {

                if ((chatId != null) && (token != null) && messageText.isNotEmpty()) {

                    lifecycleScope.launch {
                        viewModel.createNewMessageAndReload(
                            token, MessageRequest(chatId, sourceID!!, messageText)
                        )
                    }
                }
                onSendMessage(messageText)
                messageText = ""

            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.Send),
                    Modifier.size(40.dp),
                    tint = if (isDarkModeActive) White else Dark
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@ChatView, HomeView::class.java)
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