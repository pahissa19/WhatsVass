package com.chat.whatsvass.ui.theme.login


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chat.whatsvass.R
import com.chat.whatsvass.data.constants.DELAY_TO_ENCRYPT
import com.chat.whatsvass.data.constants.KEY_BIOMETRIC
import com.chat.whatsvass.data.constants.KEY_MODE
import com.chat.whatsvass.data.constants.KEY_PASSWORD
import com.chat.whatsvass.data.constants.KEY_USERNAME
import com.chat.whatsvass.data.constants.SHARED_SETTINGS
import com.chat.whatsvass.data.constants.SHARED_USER_DATA
import com.chat.whatsvass.ui.theme.Dark
import com.chat.whatsvass.ui.theme.DarkMode
import com.chat.whatsvass.ui.theme.Light
import com.chat.whatsvass.ui.theme.Main
import com.chat.whatsvass.ui.theme.White
import com.chat.whatsvass.ui.theme.home.HomeView
import com.chat.whatsvass.ui.theme.profile.ProfileView
import com.chat.whatsvass.ui.theme.profile.ProfileViewModel
import com.chat.whatsvass.usecases.checkinternet.isInternetActive
import com.chat.whatsvass.usecases.encrypt.Encrypt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val Shape = 20

// Hay que utilizar AppCompatActivity (En lugar de ComponentActivity) para que funcione el biometrico
class LoginView : AppCompatActivity() {

    private lateinit var sharedPreferencesSettings: SharedPreferences
    private lateinit var sharedPreferencesUserData: SharedPreferences

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesUserData = getSharedPreferences(SHARED_USER_DATA, Context.MODE_PRIVATE)
        sharedPreferencesSettings = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)

        val isBiometricActive = sharedPreferencesSettings.getBoolean(KEY_BIOMETRIC, false)
        val isDarkModeActive = sharedPreferencesSettings.getBoolean(KEY_MODE, false)
        if (isDarkModeActive) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.dark)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.dark)
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.light)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.light)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // No hagas nada cuando se presiona el botón de retroceso
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setContent {

            val viewModel = remember { LoginViewModel(application) }
            val viewModelCreateUser = remember { ProfileViewModel(application) }

            val username = remember { mutableStateOf("") }
            val password = remember { mutableStateOf("") }

            val navController = rememberNavController()


            NavHost(navController = navController, startDestination = stringResource(R.string.login),
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }) {

                composable("login") {
                    if (isDarkModeActive) {
                        window.statusBarColor = ContextCompat.getColor(this@LoginView, R.color.dark)
                    } else {
                        window.statusBarColor = ContextCompat.getColor(this@LoginView, R.color.light)
                    }
                    LoginScreen(
                        viewModel,
                        username.value,
                        password.value,
                        navController = navController,
                        isBiometricActive,
                        this@LoginView,
                        sharedPreferencesUserData,
                        isDarkModeActive
                    ) { newUser, newPassword ->
                        username.value = newUser
                        password.value = newPassword

                    }
                }
                composable("profile") {
                    window.statusBarColor = ContextCompat.getColor(this@LoginView, R.color.main)
                    ProfileView().ProfileScreen(
                        viewModelCreateUser,
                        navController = navController,
                        isDarkModeActive,
                        sharedPreferencesUserData
                    )
                }
            }
        }

        window.decorView.setOnTouchListener { _, _ ->
            hideKeyboard(this)
            false
        }

        setupAuthBiometric(this)
    }

}

@Composable
fun Logo() {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = stringResource(R.string.logo),
        modifier = Modifier.size(218.dp),
        contentScale = ContentScale.Fit
    )
}

private var canAuthenticate = false
private lateinit var prompt: BiometricPrompt.PromptInfo
fun setupAuthBiometric(context: Context): Boolean {
    if (BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    ) {
        canAuthenticate = true

        prompt = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.loginWithYourCredentials))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        return true
    } else {
        return false
    }
}

fun loginBiometric(
    username: String,
    password: String,
    viewModel: LoginViewModel,
    activity: LoginView,
    context: Context,
    checkBox: Boolean,
    auth: (auth: Boolean) -> Unit
) {

    var loginResult = viewModel.loginResult.value
    CoroutineScope(Dispatchers.Main).launch {
        viewModel.loginResult.collect {
            loginResult = it
        }
    }

    if (canAuthenticate) {
        BiometricPrompt(activity, ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    val decryptPassword = Encrypt().decryptPassword(password)
                    if (decryptPassword.isNotEmpty()) {
                        viewModel.loginUser(username, decryptPassword, checkBox)
                        // Agregar un retraso antes de iniciar la siguiente actividad
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (loginResult != null) {
                                val intent = Intent(context, HomeView::class.java)
                                context.startActivity(intent)
                                showMessage(context, context.getString(R.string.welcome, username))
                            } else {
                                showMessage(context, context.getString(R.string.failedToLoginTryAgain))
                            }
                        }, DELAY_TO_ENCRYPT)

                        auth(true)
                    }
                }
            }).authenticate(prompt)
    } else {
        auth(true)
    }

}

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    username: String,
    password: String,
    navController: NavController,
    isBiometricActive: Boolean,
    activity: LoginView,
    sharedPreferences: SharedPreferences,
    isDarkModeActive: Boolean,
    onCredentialsChange: (String, String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isLoginPressed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var rememberCredentials by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkModeActive) Dark else Light)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Logo()
            Spacer(modifier = Modifier.height(60.dp))
            val textFieldModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 70.dp)
                .height(60.dp)
            UserTextField(
                value = username,
                isDarkModeActive,
                onValueChange = { onCredentialsChange(it, password) },
                onImeActionPerformed = { action ->
                    if (action == ImeAction.Done || action == ImeAction.Next) {
                        keyboardController?.hide()
                    }
                },
                modifier = textFieldModifier
            )
            Spacer(modifier = Modifier.height(40.dp))
            PasswordTextField(
                value = password,
                isDarkModeActive,
                onValueChange = { onCredentialsChange(username, it) },
                modifier = textFieldModifier,
                onImeActionPerformed = { action ->
                    if (action == ImeAction.Done || action == ImeAction.Next) {
                        // iniciar sesión
                        viewModel.loginUser(username, password)
                        keyboardController?.hide()
                    }
                }
            )

            val showDialog = remember { mutableStateOf(false) }
            val isDialogOpen = remember { mutableStateOf(false) }

            if (!context.isInternetActive() && !isDialogOpen.value) {
                showDialog.value = true
            }

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text(stringResource(R.string.internetRequired)) },
                    text = { Text(stringResource(id = R.string.youNeedInternet)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog.value = false
                            }
                        ) {
                            Text(stringResource(R.string.accept))
                        }
                    }
                )
                isDialogOpen.value = true
            }


            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberCredentials,
                    onCheckedChange = { rememberCredentials = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colors.primary,
                        uncheckedColor = Color.White
                    ),
                )

                Text(
                    text = stringResource(R.string.checkBox),
                    color = White
                )
                Spacer(modifier = Modifier.width(20.dp))
            }


            var auth by remember { mutableStateOf(false) }
            val isBiometricActiveInDispositive = setupAuthBiometric(context)
            val myUsername = sharedPreferences.getString(KEY_USERNAME, null)
            val myPassword = sharedPreferences.getString(KEY_PASSWORD, null)

            if (isBiometricActive && myUsername != null) {
                FloatingActionButton(
                    onClick = {

                        if (isBiometricActiveInDispositive) {
                            if (auth) {
                                auth = false
                            } else {

                                if (myPassword != null) {
                                    loginBiometric(
                                        myUsername,
                                        myPassword,
                                        viewModel,
                                        activity,
                                        context,
                                        rememberCredentials,
                                    ) { auth = it }
                                } else {
                                    showMessage(
                                        context,
                                        context.getString(R.string.failedToLoginTryAgain)
                                    )
                                }
                            }
                        } else {
                            showMessage(
                                context,
                                context.getString(R.string.enableTheBiometricSensor)
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                        .size(70.dp),
                    backgroundColor = Light,
                    contentColor = White,
                    shape = CircleShape
                ) {
                    androidx.compose.material.Icon(
                        painter = painterResource(id = R.drawable.icon_fingerprint),
                        contentDescription = stringResource(R.string.biometric),
                        modifier = Modifier.size(40.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(90.dp))
            }

            LoginButton(
                onClick = {
                    isLoginPressed = true
                    viewModel.loginUser(username, password, rememberCredentials)
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 95.dp)
                    .height(60.dp)
            )
            Spacer(modifier = Modifier.weight(0.3f))
            CreateAccountText(navController = navController)
            Spacer(modifier = Modifier.height(40.dp))

        }
    }

    if (isLoginPressed) {
        viewModel.loginResult.collectAsState().value?.let { result ->
            when (result) {
                is LoginViewModel.LoginResult.Error -> {
                    showMessage(context, stringResource(R.string.credentialsAreNotCorrect))
                }

                is LoginViewModel.LoginResult.Success -> {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        // Guardar datos en sharedPreferences para utilizarlos en el biometrico
                        val encryptPassword = Encrypt().encryptPassword(password)
                        val edit = sharedPreferences.edit()
                        edit.putString(KEY_USERNAME, username).apply()
                        edit.putString(KEY_PASSWORD, encryptPassword).apply()

                        val intent = Intent(context, HomeView::class.java)
                        context.startActivity(intent)

                    } else {
                        showMessage(context, stringResource(R.string.pleaseEnterUserAndPassword))
                    }
                }
            }
            isLoginPressed = false
        }
    }

}

@Composable
fun UserTextField(
    value: String,
    isDarkModeActive: Boolean,
    onValueChange: (String) -> Unit,
    onImeActionPerformed: (ImeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorText = if (isDarkModeActive) White else Color.Black

    val containerColor = if (isDarkModeActive) DarkMode else White
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = stringResource(R.string.enterYourUser),
                color = if (isDarkModeActive) White else Color.Black,
                fontSize = 14.sp
            )
        },
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onDone = { onImeActionPerformed(ImeAction.Next) }),
        colors = TextFieldDefaults.colors(
            focusedTextColor = colorText,
            unfocusedTextColor = colorText,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            cursorColor = colorText,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
fun PasswordTextField(
    value: String,
    isDarkModeActive: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onImeActionPerformed: (ImeAction) -> Unit

) {
    var passwordVisibility by remember { mutableStateOf(false) }
    val colorText = if (isDarkModeActive) White else Color.Black

    val containerColor = if (isDarkModeActive) DarkMode else White
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            androidx.compose.material.Text(
                text = stringResource(R.string.enterYourPassword),
                color = if (isDarkModeActive) White else Color.Black,
                fontSize = 14.sp
            )
        },
        shape = RoundedCornerShape(Shape.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onImeActionPerformed(ImeAction.Done) }),
        visualTransformation = if (passwordVisibility) {
            VisualTransformation.None
        } else PasswordVisualTransformation(),
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedTextColor = colorText,
            unfocusedTextColor = colorText,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            cursorColor = colorText,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            IconButton(
                onClick = { passwordVisibility = !passwordVisibility },
                modifier = Modifier.padding(8.dp)
            ) {
                val icon = if (passwordVisibility) {
                    ImageVector.vectorResource(id = R.drawable.visible_off)
                } else {
                    ImageVector.vectorResource(id = R.drawable.visible_on)
                }
                Icon(
                    icon,
                    contentDescription = stringResource(id = R.string.togglePasswordVisibility),
                    tint = if (isDarkModeActive) White else Color.Black
                )
            }
        }
    )
}

@Composable
fun LoginButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(Shape.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Main),
    ) {
        Text(
            text = stringResource(id = R.string.login),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}


@Composable
fun CreateAccountText(navController: NavController) {
    Text(
        text = stringResource(R.string.createUser),
        color = Color.White,
        fontSize = 18.sp,
        modifier = Modifier.clickable {
            navController.navigate("profile")
        }
    )
}


fun showMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


