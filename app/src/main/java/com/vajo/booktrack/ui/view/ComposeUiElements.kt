package com.vajo.booktrack.ui.view

import androidx.compose.foundation.Image
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vajo.nav.ui.uistates.MainViewModel
import android.util.Log
import androidx.compose.foundation.layout.width
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.font.Font
import androidx.navigation.NavController
import com.vajo.booktrack.data.model.BookItem
import com.vajo.booktrack.R
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberImagePainter
import com.vajo.booktrack.ui.view_model.CameraViewModel
import com.vajo.cameraapp.ui.Gallery
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService

// https://kotlinlang.org/docs/sealed-classes.html
sealed class Screen(val route: String){
    object FirstScreen: Screen("firstScreen")
    object ListScreen: Screen("listScreen")
    object AddBookScreen: Screen("addBookScreen")
    object CameraView: Screen("cameraScreen")
    object EditScreen: Screen("editScreen")
}

// https://kotlinlang.org/docs/opt-in-requirements.html
// https://developer.android.com/jetpack/compose/navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    mainViewModel: MainViewModel,
    cameraViewModel: CameraViewModel,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    directory: File,
    context: Context
){
    val state = mainViewModel.mainViewState.collectAsState()
    val navController = rememberNavController()

    Scaffold(
        floatingActionButton = {
            if (navController.currentBackStackEntryAsState().value?.destination?.route == Screen.ListScreen.route) {
                FloatingActionButton(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(16.dp),
                    onClick = { navController.navigate(Screen.AddBookScreen.route) },
                    backgroundColor = colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add,
                        contentDescription = "Add",
                        tint = colorScheme.background,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            modifier = Modifier.padding(it),
            startDestination = Screen.FirstScreen.route
        ){
            composable(Screen.FirstScreen.route){
                mainViewModel.selectScreen(Screen.FirstScreen)
                mainViewModel.getBooks()
                startScreen(mainViewModel, navController)
            }
            composable(Screen.ListScreen.route){
                mainViewModel.selectScreen(Screen.ListScreen)
                bookListScreen(mainViewModel, navController, cameraViewModel)
            }
            composable(Screen.AddBookScreen.route) {
                mainViewModel.selectScreen(Screen.AddBookScreen)
                mainViewModel.getBooks()
                addBookScreen(mainViewModel, cameraViewModel, navController)
            }
            composable(Screen.CameraView.route) {
                mainViewModel.selectScreen(Screen.CameraView)
                CameraView(mainViewModel, cameraViewModel, navController, previewView, imageCapture, cameraExecutor, directory, context)
            }
            composable(Screen.EditScreen.route) {
                mainViewModel.selectScreen(Screen.EditScreen)
                EditScreen(mainViewModel, cameraViewModel, navController)
                }
        }
    }
}

// –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

@Composable
fun CameraView(
    mainViewModel: MainViewModel,
    cameraViewModel: CameraViewModel,
    navController: NavHostController,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    directory: File,
    context: Context,
) {
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {

        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(40.dp)
        ){
            BackButtonlight(navController, Screen.AddBookScreen.route)
        }
        Button(
            modifier = Modifier.padding(25.dp),
            onClick = {
                val photoFile = File(
                    directory,
                    SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
                )

                // Call takePicture from CameraViewModel
                cameraViewModel.takePicture(
                    imageCapture = imageCapture,
                    context = context,
                    onSuccess = { uri ->
                        cameraViewModel.updateCapturedImageUri(uri) // Update the URI of the captured image

                        val backScreen = mainViewModel.mainViewState.value.originatingScreenForCamera
                        if (backScreen != null) {
                            navController.navigate(backScreen.route) {
                                popUpTo(backScreen.route) { inclusive = true }
                            }
                        }
                    },
                    onError = { exception ->
                        Log.e("camApp", "Error when capturing image", exception)
                    }
                )
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.camera), // Use painterResource
                contentDescription = "Take Photo", // Provide content description
                modifier = Modifier
                    .size(30.dp)
                    .padding(5.dp),
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addBookScreen(mainViewModel: MainViewModel, cameraViewModel: CameraViewModel, navController: NavController){

    val capturedImageUri = cameraViewModel.capturedImageUri.value

    val title by mainViewModel.bookTitle.collectAsState("")
    val author by mainViewModel.bookAuthor.collectAsState("")
    val selectedDateString by mainViewModel.bookDate.collectAsState("")
    val rating by mainViewModel.bookRating.collectAsState(3)
    val isEbook by mainViewModel.bookIsEbook.collectAsState(false)

    Column (
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Bookrow()

        Box (modifier = Modifier.fillMaxWidth(0.8f)) {

            BackButton(navController, Screen.ListScreen.route)

            Text(
            text = stringResource(R.string.newBook),
            fontSize = 30.sp,
            style = TextStyle(fontFamily = FontFamily(
                Font(R.font.milo_ot_bold) // Use the font resource
            )),
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
        )

        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        // https://www.jetpackcompose.net/textfield-in-jetpack-compose


        TextField(
            value = title,
            onValueChange = {
                newText -> mainViewModel.bookTitle.value = newText
            },
            label = { Text(text = stringResource(R.string.addScreen_title) ) },
            modifier = Modifier
                .padding(bottom = 20.dp)
                .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                .fillMaxSize(0.8f)
                .height(50.dp),

            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent, // Transparent background
                textColor = colorScheme.secondary, // Text color
                focusedIndicatorColor = Color.Transparent, // Hide the indicator line when focused
                unfocusedIndicatorColor = Color.Transparent,

            )
        )

        TextField(
            value = author,
            onValueChange = {
                    newText -> mainViewModel.bookAuthor.value = newText
            },
            label = { Text(text = stringResource(R.string.addScreen_author)) },
            modifier = Modifier
                .padding(bottom = 20.dp)
                .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                .fillMaxSize(0.8f)
                .height(50.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent, // Transparent background
                textColor = colorScheme.secondary, // Text color
                focusedIndicatorColor = Color.Transparent, // Hide the indicator line when focused
                unfocusedIndicatorColor = Color.Transparent
            ))

        DatePickerButton(
            selectedDate = selectedDateString,
            onDateChange = {
                    newDate -> mainViewModel.bookDate.value = newDate
            },
            colorScheme = colorScheme,
            0.8f
        )

        StarRatingBar(rating = rating, onRatingChange = { newRating ->
            mainViewModel.bookRating.value = newRating
        }, 45, 0.8f)

        Button (
            modifier = Modifier
                .fillMaxWidth(0.8f) // 70% of the screen width
                .height(80.dp)
                .padding(bottom = 20.dp)
            ,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.surface, // Red background color
                contentColor = colorScheme.secondary

            ),
            shape = RoundedCornerShape(15.dp),
            onClick = {
                mainViewModel.updateOriginatingScreenForCamera(Screen.AddBookScreen)
                cameraViewModel.enableCameraPreview(true)
                navController.navigate(Screen.CameraView.route)
            },

            )

        {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(0.5f)
            ){
                if (capturedImageUri != null) {
                    // Display the captured image
                    Image(
                        painter = rememberImagePainter(data = capturedImageUri),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                    )
                } else {
                    // Display the default icon and text for taking a picture
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Add Photo",
                        tint = colorScheme.secondary,
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .height(25.dp)
                    )
                    Text(
                        text = stringResource(R.string.add_photo),
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.milo_ot_medium)),
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                }
            }


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 20.dp, start = 0.dp)
                .fillMaxSize(0.8f),
        ) {
            Checkbox(
                checked = isEbook,
                onCheckedChange = { mainViewModel.bookIsEbook.value = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = colorScheme.primary, // Color when checkbox is checked
                    uncheckedColor = colorScheme.outline, // Color when checkbox is unchecked
                    checkmarkColor = colorScheme.background,
                      ),

            )
            Text(text = stringResource(R.string.addScreen_isebook))
        }

        Button(
            onClick = {
                if (selectedDateString.isNotEmpty()) { // Check if date string is not empty
                    mainViewModel.saveButton(
                        BookItem(
                            title = mainViewModel.bookTitle.value,
                            author = mainViewModel.bookAuthor.value,
                            date = mainViewModel.bookDate.value,
                            rating = mainViewModel.bookRating.value,
                            imgPath = cameraViewModel.capturedImageUri.value.toString(),
                            isEbook = mainViewModel.bookIsEbook.value
                        )
                    )
                    cameraViewModel.updateCapturedImageUri(null)
                    mainViewModel.bookTitle.value = ""
                    mainViewModel.bookAuthor.value = ""
                    mainViewModel.bookDate.value = ""
                    mainViewModel.bookRating.value = 3
                    mainViewModel.bookIsEbook.value = false
                    navController.navigate(Screen.ListScreen.route)
                } else {
                    // Handle the case where the date is not selected
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f) // 70% of the screen width
                .height(50.dp)
                .shadow(
                    elevation = 2.dp, // Small elevation for a shadow effect
                    shape = RoundedCornerShape(20.dp),
                    clip = false,
                    // To show shadow outside the button bounds // Brown color for shadow
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary // Red background color
            ),
            shape = RoundedCornerShape(25.dp) // Rounded corners
        ) {
            Text(text = stringResource(R.string.mainscreen_button_save), fontSize = 20.sp)
        }
    }
}

@Composable
fun bookListScreen(mainViewModel: MainViewModel, navController: NavController, cameraViewModel: CameraViewModel){
    val state = mainViewModel.mainViewState.collectAsState()
    val sortingOrder = mainViewModel.sortingOrder.collectAsState()

    // https://developer.android.com/jetpack/compose/lists
    LazyColumn (
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {


        item{
            Bookrow()

            Text(
                text = stringResource(R.string.displaytodos_title),
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            FilterComponent(sortingOrder.value) {
                mainViewModel.toggleSortingOrder()
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(state.value.books){

            Box(
                modifier = Modifier
                    .fillMaxSize(0.9f)
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center

            ) {

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                            .clickable {
                                Log.d("CLICK", "Row clicked with student: ${it.title}")
                                mainViewModel.editBook(it)
                                navController.navigate(Screen.EditScreen.route)
                            }
                            .background(colorScheme.surface)
                            .padding(top = 10.dp, start = 117.dp, bottom = 10.dp),

                    )

                    {

                        Text(text = "${it.author}")
                        Text(
                            modifier = Modifier.padding(top = 5.dp),
                            text = "${it.title}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 7.dp, end = 10.dp),
                        ){
                            repeat(it.rating) { index ->
                                StarComponent(index = index, rating = it.rating, colorScheme = colorScheme)
                            }
                        }

                        Row (modifier = Modifier.padding(end = 10.dp),) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "finished on the ${it.date}",
                                color = colorScheme.outline)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, end = 10.dp),
                    ){
                        Spacer(modifier = Modifier.weight(1f))
                        // Conditionally display the icon for ebook
                        if (it.isEbook) {
                            Icon(
                                painter = painterResource(id = R.drawable.tablet),
                                contentDescription = "Ebook",
                                tint = colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                }

                if (it.imgPath != "") {
                    val imageUri = Uri.parse(it.imgPath)
                    Image(
                        painter = rememberImagePainter(imageUri),
                        contentDescription = "Book Image",
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .height(150.dp)
                            .width(107.dp)
                            .align(Alignment.CenterStart)
                    )
                } else {
                    // Display default image if imagePath is not available
                    Image(
                        painter = painterResource(id = R.drawable.bookcover),
                        contentDescription = "Default Book Cover",
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .height(150.dp)
                            .width(107.dp)
                            .align(Alignment.CenterStart)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(mainViewModel: MainViewModel, cameraViewModel: CameraViewModel, navController: NavController){

    val state = mainViewModel.mainViewState.collectAsState()
    val capturedImageUri = cameraViewModel.capturedImageUri.value

    // Use the MutableStateFlow properties directly
    val title by mainViewModel.bookTitleEdit.collectAsState()
    val author by mainViewModel.bookAuthorEdit.collectAsState()
    val selectedDateString by mainViewModel.bookDateEdit.collectAsState()
    val rating by mainViewModel.bookRatingEdit.collectAsState()
    val isEbook by mainViewModel.bookIsEbookEdit.collectAsState()
    val imgPath by mainViewModel.bookImgPathEdit.collectAsState()


    Column (
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Bookrow()

        Box (modifier = Modifier.fillMaxWidth(0.8f)) {

            BackButton(navController, Screen.ListScreen.route)

            Text(
                text = stringResource(R.string.edit_book_title),
                fontSize = 30.sp,
                style = TextStyle(fontFamily = FontFamily(
                    Font(R.font.milo_ot_bold) // Use the font resource
                )),
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

            // Delete Icon
            IconButton(
                onClick = {
                    mainViewModel.clickDelete(state.value.editBook)
                    navController.navigate(Screen.ListScreen.route)
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.trash),                                    contentDescription = "Delete",
                    tint = colorScheme.secondary // Set the color of your delete icon
                )
            }

        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        TextField(
            value = title,
            onValueChange = {
                    newText -> mainViewModel.bookTitleEdit.value = newText
            },
            label = { Text(text = stringResource(R.string.addScreen_title) ) },
            modifier = Modifier
                .padding(bottom = 20.dp)
                .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                .fillMaxSize(0.8f)
                .height(50.dp),

            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent, // Transparent background
                textColor = colorScheme.secondary, // Text color
                focusedIndicatorColor = Color.Transparent, // Hide the indicator line when focused
                unfocusedIndicatorColor = Color.Transparent,

                )
        )

        TextField(
            value = author,
            onValueChange = {
                    newText -> mainViewModel.bookAuthorEdit.value = newText
            },
            label = { Text(text = stringResource(R.string.addScreen_author)) },
            modifier = Modifier
                .padding(bottom = 20.dp)
                .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                .fillMaxSize(0.8f)
                .height(50.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent, // Transparent background
                textColor = colorScheme.secondary, // Text color
                focusedIndicatorColor = Color.Transparent, // Hide the indicator line when focused
                unfocusedIndicatorColor = Color.Transparent
            ))

        // DatePicker Button
        DatePickerButton(
            selectedDate = selectedDateString,
            onDateChange = { newDate ->
                mainViewModel.bookDateEdit.value = newDate
            },
            colorScheme = MaterialTheme.colorScheme,
            1f
        )

        StarRatingBar(rating = rating, onRatingChange = { newRating ->
            mainViewModel.bookRatingEdit.value = newRating
        }, 45, 0.8f)


            // Image Button

        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(80.dp)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.surface,
                contentColor = colorScheme.secondary
            ),
            shape = RoundedCornerShape(15.dp),
            onClick = {
                cameraViewModel.enableCameraPreview(true)
                mainViewModel.updateOriginatingScreenForCamera(Screen.EditScreen)
                navController.navigate(Screen.CameraView.route)                        }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                if (capturedImageUri != null) {
                    // Display the captured image
                    Image(
                        painter = rememberImagePainter(data = capturedImageUri),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                    )
                } else if (imgPath != "") {
                    // Display the captured image
                    Image(
                        painter = rememberImagePainter(data = imgPath),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                    )
                }
                else {
                    // Display the default icon and text for taking a picture
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Add Photo",
                        tint = colorScheme.secondary,
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .height(25.dp)
                    )
                    Text(
                        text = stringResource(R.string.add_photo),
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.milo_ot_medium)),
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 20.dp, start = 0.dp)
                .fillMaxSize(0.8f),
        ) {
            Checkbox(
                checked = isEbook,
                onCheckedChange = { mainViewModel.bookIsEbookEdit.value = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = colorScheme.primary, // Color when checkbox is checked
                    uncheckedColor = colorScheme.outline, // Color when checkbox is unchecked
                    checkmarkColor = colorScheme.background,
                ),

                )
            Text(text = stringResource(R.string.addScreen_isebook))
        }

        Button(
            onClick = {
                mainViewModel.saveEditedBook(
                            BookItem(
                                title,
                                author,
                                selectedDateString,
                                rating,
                                capturedImageUri.toString(),
                                isEbook,
                                state.value.editBook.id
                            )
                        )
                navController.navigate(Screen.ListScreen.route)
            },
            modifier = Modifier
                .fillMaxWidth(0.8f) // 70% of the screen width
                .height(50.dp)
                .shadow(
                    elevation = 2.dp, // Small elevation for a shadow effect
                    shape = RoundedCornerShape(20.dp),
                    clip = false,
                    // To show shadow outside the button bounds // Brown color for shadow
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary // Red background color
            ),
            shape = RoundedCornerShape(25.dp) // Rounded corners
        ) {
            Text(text = stringResource(R.string.mainscreen_button_save), fontSize = 20.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun startScreen(mainViewModel: MainViewModel, navController: NavController){

    Column (
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.booktrack_logo),
            contentDescription = stringResource(R.string.decorative_android_icon),
            modifier = Modifier.fillMaxWidth(0.6f),
            contentScale = ContentScale.FillWidth// Ensure image fills the width
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the screen
        ) {
            Image(
                painter = painterResource(id = R.drawable.bookshelf01),
                contentDescription = stringResource(R.string.decorative_android_icon),
                modifier = Modifier
                    .height(400.dp), // Set the fixed height
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(id = R.drawable.bookshelf03),
                contentDescription = stringResource(R.string.decorative_android_icon),
                modifier = Modifier
                    .height(400.dp), // Set the fixed height
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(id = R.drawable.bookshelf02),
                contentDescription = stringResource(R.string.decorative_android_icon),
                modifier = Modifier
                    .height(400.dp), // Set the fixed height
                contentScale = ContentScale.Crop
            )
        }

        Box(modifier = Modifier.fillMaxSize()) { // Fill the entire screen
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
            ){

                Button(
                    onClick = { navController.navigate(Screen.AddBookScreen.route) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // 70% of the screen width
                        .padding(top = 20.dp)
                        .height(50.dp)
                        .shadow(
                            elevation = 2.dp, // Small elevation for a shadow effect
                            shape = RoundedCornerShape(20.dp),
                            clip = false,
                            // To show shadow outside the button bounds // Brown color for shadow
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary // Red background color
                    ),
                    shape = RoundedCornerShape(25.dp) // Rounded corners
                ) {
                    Text(text = stringResource(R.string.startscreen_add_book), fontSize = 20.sp)
                }

                Button(
                onClick = { navController.navigate(Screen.ListScreen.route) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // 80% of the screen width
                        .padding(top = 20.dp)
                        .height(50.dp)
                        .border(3.dp, colorScheme.secondary, RoundedCornerShape(25.dp)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent, // Transparent background
                        contentColor = colorScheme.secondary // Text color as secondary theme color
                    ),
                    shape = RoundedCornerShape(25.dp) // Rounded corners
            ) {
                Text(
                    text = stringResource(R.string.startscreen_view_collection),
                    fontSize = 20.sp,
                    color = colorScheme.secondary,)
            }


            }
        }



    }
}

// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

fun showDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            onDateSelected(selectedDate.format(DateTimeFormatter.ISO_DATE)) // Update format as needed
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(selectedDate: String, onDateChange: (String) -> Unit, colorScheme: ColorScheme, maxWidth: Float) {
    val context = LocalContext.current

    Button(
        onClick = { showDatePicker(context, onDateChange) },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = colorScheme.secondary,
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .padding(bottom = 20.dp)
            .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
            .fillMaxWidth(0.8f)
            .height(50.dp)
    ) {
        Text(
            text = if (selectedDate.isNotEmpty()) selectedDate else stringResource(R.string.select_a_date),
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.milo_ot_regular)),
                fontSize = 18.sp)
        )

        Icon(
            painter = painterResource(id = R.drawable.event_icon),
            contentDescription = "Select Date",
            tint = colorScheme.secondary
        )
    }
}

@Composable
fun StarRatingBar(rating: Int, onRatingChange: (Int) -> Unit, starWidth: Int, maxWidth: Float) {

    Column (modifier = Modifier.fillMaxWidth(maxWidth)) {

        Text(
            text = stringResource(R.string.rate_your_book),
            modifier = Modifier
                .padding(bottom = 10.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth())
        {

            (1..5).forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(end = 10.dp, bottom = 30.dp)
                        .clickable { onRatingChange(index) },
                    contentAlignment = Alignment.Center,

                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.star_full),
                        contentDescription = "Rating",
                        tint = if (index <= rating) colorScheme.tertiary else Color.Transparent,
                        modifier = Modifier.size(starWidth.dp)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.star_stroke),
                        contentDescription = "Rating",
                        tint = colorScheme.secondary,
                        modifier = Modifier.size(starWidth.dp)
                    )

                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Bookrow() {
    Spacer(
        modifier = Modifier.height(20.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth() // Fill the width of the screen
    ) {
        Image(
            painter = painterResource(id = R.drawable.bookrow01),
            contentDescription = stringResource(R.string.decorative_android_icon),
            modifier = Modifier
                .height(113.dp), // Set the fixed height
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.bookrow03),
            contentDescription = stringResource(R.string.decorative_android_icon),
            modifier = Modifier
                .height(113.dp), // Set the fixed height
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.bookrow02),
            contentDescription = stringResource(R.string.decorative_android_icon),
            modifier = Modifier
                .height(113.dp), // Set the fixed height
            contentScale = ContentScale.Crop
        )
    }

    Spacer(
        modifier = Modifier.height(15.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarComponent(index: Int, rating: Int, colorScheme: ColorScheme) {
    Box(
        modifier = Modifier
            .padding(end = 2.dp, bottom = 5.dp)
            .clickable { /* handle click if needed */ }
            .size(14.dp),  // Adjust size as needed
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.star_full),
            contentDescription = "Rating",
            tint = if (index < rating) colorScheme.tertiary else Color.Transparent,
            modifier = Modifier.matchParentSize()
        )
        Icon(
            painter = painterResource(id = R.drawable.star_stroke),
            contentDescription = "Rating",
            tint = colorScheme.secondary,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
fun BackButton(navController: NavController, screenRoute: String) {
    Icon(
        painter = painterResource(id = R.drawable.back_arrow),
        contentDescription = "Back",
        modifier = Modifier
            .clickable {
                navController.navigate(screenRoute)
            }
            .size(30.dp)
            ,  // Adjust size as needed
        tint = colorScheme.secondary,  // Adjust color as needed,
    )
}

@Composable
fun BackButtonlight(navController: NavController, screenRoute: String) {
    Icon(
        painter = painterResource(id = R.drawable.back_arrow),
        contentDescription = "Back",
        modifier = Modifier
            .clickable {
                navController.navigate(screenRoute)
            }
            .size(30.dp)
        ,  // Adjust size as needed
        tint = Color.White,  // Adjust color as needed,
    )
}

@Composable
fun FilterComponent(sortingOrder: MainViewModel.SortingOrder, onFilterClick: () -> Unit) {
    val filterText = if (sortingOrder == MainViewModel.SortingOrder.NewestFirst) "newest -> oldest" else "oldest -> newest"

    Button(
        onClick = onFilterClick,
        modifier = Modifier
            .border(2.dp, colorScheme.secondary, RoundedCornerShape(35.dp)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent, // Transparent background
            contentColor = colorScheme.secondary // Text color as secondary theme color
        ),
        shape = RoundedCornerShape(35.dp),
    ) {
        Text(
            modifier = Modifier.padding(2.dp),
            text = filterText,
            fontSize = 14.sp,
            color = colorScheme.secondary,)
    }
}






