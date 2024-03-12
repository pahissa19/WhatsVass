package com.chat.whatsvass.ui.theme.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.chat.whatsvass.R
import com.chat.whatsvass.ui.theme.Principal
import com.chat.whatsvass.ui.theme.components.GeneralComponents.ButtonCustom
import com.chat.whatsvass.ui.theme.components.GeneralComponents.NavigationBarCustom
import com.chat.whatsvass.ui.theme.components.GeneralComponents.PasswordTextFieldCustom
import com.chat.whatsvass.ui.theme.components.GeneralComponents.TextFieldCustom
import com.chat.whatsvass.ui.theme.login.hideKeyboard
import com.chat.whatsvass.ui.theme.login.showMessage

class ProfileView : ComponentActivity() {

    private var imageUri: Uri? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.main)
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = remember { ProfileViewModel() }
            ProfileScreen(viewModel)
        }
        window.decorView.setOnTouchListener { _, _ ->
            hideKeyboard(this)
            false
        }

    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "!Permiso para acceder a la cámara denegado!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    val requestGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                openGallery()
            } else {
                Toast.makeText(
                    this,
                    "!Permiso para acceder a la galería denegado!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    fun showDialogToTakeOrSelectImage(context: Context) : String {

        var imageSelected = ""
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            imageSelected = openCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)

        }
        return imageSelected
        /*if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestGalleryPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                requestGalleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }*/
    }

    fun openCamera() : String {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Titulo")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descripción")
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

        return imageUri.toString()
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {

            } else {
                Toast.makeText(this, "!Operación cancelada!", Toast.LENGTH_LONG).show()
            }
        }

    private fun openGallery() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Titulo")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descripción")
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val intent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        } else {
            intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {

            } else {
                Toast.makeText(this, "!Operación cancelada!", Toast.LENGTH_LONG).show()
            }
        }
    @Composable
    fun ProfileScreen(viewModel: ProfileViewModel) {
        val context = LocalContext.current
        val registerResult by viewModel.registerResult.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff8091f2))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                NavigationBarCustom(text = "Crear Perfil")
                Spacer(modifier = Modifier.height(20.dp))
                ImageProfile()
                Spacer(modifier = Modifier.height(40.dp))
                val user = TextFieldCustom("Usuario")
                Spacer(modifier = Modifier.height(20.dp))
                val nick = TextFieldCustom("Nick")
                Spacer(modifier = Modifier.height(20.dp))
                val password = PasswordTextFieldCustom("Contraseña")
                Spacer(modifier = Modifier.height(20.dp))
                val confirmPassword = PasswordTextFieldCustom("Repetir Contraseña")
                Spacer(modifier = Modifier.weight(0.3f))
                ButtonCustom(
                    onClick = {
                        if (user.isNullOrEmpty()) {
                            Toast.makeText(context, "Ingresa tu nombre de usuario", Toast.LENGTH_SHORT)
                                .show()
                        } else if (nick.isNullOrEmpty()) {
                            Toast.makeText(context, "Ingresa tu nick", Toast.LENGTH_SHORT).show()
                        } else if (password.isNullOrEmpty()) {
                            Toast.makeText(context, "Ingresa una contraseña", Toast.LENGTH_SHORT).show()
                        } else if (confirmPassword.isNullOrEmpty()) {
                            Toast.makeText(context, "Confirma tu contraseña", Toast.LENGTH_SHORT).show()
                        } else if (password != confirmPassword) {
                            Toast.makeText(
                                context,
                                "Las contraseñas deben ser iguales",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Log.d("contraseña", password)
                        } else {
                            viewModel.registerUser(user, nick, password)
                            Log.d("contraseña", password)
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 95.dp)
                        .height(60.dp),
                    text = "Crear Usuario"
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (registerResult != null) {
            // Manejar el resultado del inicio de sesión aquí
            when (registerResult) {
                is ProfileViewModel.RegisterResult.Success -> {
                    val registerResponse =
                        (registerResult as ProfileViewModel.RegisterResult.Success).register
                    showMessage(
                        context,
                        "Usuario creado correctamente. Token: ${registerResponse.user.token}"
                    )
                    //  IR HACIA HOME

                }

                is ProfileViewModel.RegisterResult.Error -> {
                    val errorMessage = (registerResult as ProfileViewModel.RegisterResult.Error).message
                    showMessage(context, "Error al crear usuario: $errorMessage")
                }

                else -> {}
            }
        }
    }

    @Composable
    fun ImageProfile() {
        val context = LocalContext.current
        var image by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .height(152.dp)
                .width(152.dp)
        ) {
            Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .height(152.dp)
                    .width(152.dp)
                    .padding(top = 24.dp),
                painter =
                if (!image.isNullOrBlank()){
                    rememberImagePainter(image.toUri())
                } else {
                    painterResource(id = R.drawable.image_person)
                },
                contentDescription = "",
                contentScale = ContentScale.Crop
            )

            Image(
                modifier = Modifier
                    .clickable {
                        showMessage(context, "Imagen presionada")
                        image = showDialogToTakeOrSelectImage(this@ProfileView)
                    }
                    .clip(CircleShape)
                    .background(Principal)
                    .height(32.dp)
                    .width(32.dp)
                    .padding(5.dp)
                    .align(alignment = Alignment.BottomEnd),
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
        }
    }
}

