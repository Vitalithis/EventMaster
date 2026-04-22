package com.danocampo.eventmaster

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFFD700),
                    surface = Color(0xFF102A43),
                    background = Color(0xFF051423)
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    EventMasterApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMasterApp() {
    val navController = rememberNavController()
    val eventViewModel: EventViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            var showCategoryDialog by remember { mutableStateOf(false) }
            var newCategoryName by remember { mutableStateOf("") }

            if (showCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    title = { Text(stringResource(R.string.add_category), fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text(stringResource(R.string.category_name)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newCategoryName.isNotBlank()) {
                                eventViewModel.addCategory(newCategoryName)
                                newCategoryName = ""
                                showCategoryDialog = false
                            }
                        }) { Text(stringResource(R.string.create)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCategoryDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(
                                stringResource(R.string.title_home), 
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ) 
                        },
                        actions = {
                            IconButton(onClick = { showCategoryDialog = true }) {
                                Icon(Icons.Rounded.Category, contentDescription = stringResource(R.string.add_category), tint = Color(0xFFFFD700))
                            }
                        }
                    )
                },
                floatingActionButton = {
                    LargeFloatingActionButton(
                        onClick = { navController.navigate("add_event") },
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF051423),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_event), modifier = Modifier.size(32.dp))
                    }
                }
            ) { padding ->
                LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                    items(eventViewModel.categories) { category ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .background(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))),
                                    shape = RoundedCornerShape(12.dp)
                                ).padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Label, null, tint = Color(0xFF102A43))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category, color = Color(0xFF102A43), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        val filteredEvents = eventViewModel.events.filter { it.category == category }
                        if (filteredEvents.isEmpty()) {
                            Text(stringResource(R.string.no_events), modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        } else {
                            filteredEvents.forEach { event ->
                                EventItemCard(event) { navController.navigate("detail/${event.id}") }
                            }
                        }
                    }
                }
            }
        }

        composable(
            "add_event?eventId={eventId}",
            arguments = listOf(navArgument("eventId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null 
            })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val existingEvent = eventId?.let { id -> eventViewModel.events.find { it.id == id } }

            val context = LocalContext.current
            var title by remember { mutableStateOf(existingEvent?.title ?: "") }
            var location by remember { mutableStateOf(existingEvent?.location ?: "") }
            var description by remember { mutableStateOf(existingEvent?.description ?: "") }
            var selectedCategory by remember { mutableStateOf(existingEvent?.category ?: "") }
            var selectedImageUri by remember { mutableStateOf<Uri?>(existingEvent?.imageUri?.let { Uri.parse(it) }) }
            
            var scale by remember { mutableStateOf(existingEvent?.imageScale ?: 1f) }
            var offset by remember { mutableStateOf(Offset(existingEvent?.imageOffsetX ?: 0f, existingEvent?.imageOffsetY ?: 0f)) }
            
            var expanded by remember { mutableStateOf(false) }
            var showError by remember { mutableStateOf(false) }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    selectedImageUri = uri
                    scale = 1f
                    offset = Offset.Zero
                }
            }

            var dateText by remember { mutableStateOf(existingEvent?.date ?: context.getString(R.string.select_date)) }
            val calendar = Calendar.getInstance()
            val datePickerDialog = android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth -> dateText = "$dayOfMonth/${month + 1}/$year" },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (existingEvent == null) stringResource(R.string.new_event) else stringResource(R.string.edit_event), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                            }
                        }
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Gray.copy(alpha = 0.1f))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    offset += pan
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if(selectedImageUri == null) Color.Transparent else Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (selectedImageUri == null) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, null, modifier = Modifier.size(48.dp), tint = Color(0xFFFFD700))
                                }
                                Button(
                                    onClick = { launcher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFD700).copy(alpha = 0.9f),
                                        contentColor = Color(0xFF051423)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (selectedImageUri == null) stringResource(R.string.select_image) else stringResource(R.string.change_image), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    if (selectedImageUri != null) {
                        Text(
                            "Ajusta con dos dedos para encuadrar",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally),
                            color = Color(0xFFFFD700).copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    EventInputField(
                        value = title, 
                        onValueChange = { title = it }, 
                        label = stringResource(R.string.event_name), 
                        isError = showError && title.isBlank(), 
                        errorMessage = stringResource(R.string.required_field)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(dateText)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    EventInputField(
                        value = location, 
                        onValueChange = { location = it }, 
                        label = stringResource(R.string.location_label), 
                        isError = showError && location.isBlank(), 
                        errorMessage = stringResource(R.string.required_field)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.description_label)) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true }, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Style, null, tint = Color(0xFFFFD700))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(if (selectedCategory.isEmpty()) stringResource(R.string.select_category) else selectedCategory)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Rounded.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            eventViewModel.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = { selectedCategory = category; expanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            if (title.isNotBlank() && dateText != context.getString(R.string.select_date) && selectedCategory.isNotEmpty()) {
                                val eventToSave = Event(
                                    id = existingEvent?.id ?: java.util.UUID.randomUUID().toString(),
                                    title = title,
                                    description = description,
                                    category = selectedCategory,
                                    date = dateText,
                                    location = location,
                                    imageUri = selectedImageUri?.toString(),
                                    imageScale = scale,
                                    imageOffsetX = offset.x,
                                    imageOffsetY = offset.y
                                )
                                if (existingEvent == null) {
                                    eventViewModel.addEvent(eventToSave)
                                } else {
                                    eventViewModel.updateEvent(eventToSave)
                                }
                                navController.popBackStack()
                            } else {
                                showError = true
                                Toast.makeText(context, context.getString(R.string.validation_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) { 
                        Text(
                            if (existingEvent == null) stringResource(R.string.save_event) else stringResource(R.string.update_event),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ) 
                    }
                }
            }
        }

        composable(
            "detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("eventId")
            val event = eventViewModel.events.find { it.id == id }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.detail_title), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                navController.navigate("add_event?eventId=${event?.id}") 
                            }) {
                                Icon(Icons.Rounded.EditNote, contentDescription = stringResource(R.string.edit), tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                            }
                        }
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color(0xFF102A43))
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (event?.imageUri != null) {
                            AsyncImage(
                                model = event.imageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = event.imageScale,
                                        scaleY = event.imageScale,
                                        translationX = event.imageOffsetX,
                                        translationY = event.imageOffsetY
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.EventAvailable,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = Color(0xFFFFD700).copy(alpha = 0.5f)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = event?.title ?: "", 
                            style = MaterialTheme.typography.headlineLarge, 
                            color = Color(0xFFFFD700), 
                            fontWeight = FontWeight.ExtraBold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        DetailItem(Icons.Rounded.Place, stringResource(R.string.location_prefix), event?.location ?: "")
                        DetailItem(Icons.Rounded.Event, stringResource(R.string.date_prefix), event?.date ?: "")
                        DetailItem(Icons.Rounded.Loyalty, stringResource(R.string.category_prefix), event?.category ?: "")
                        
                        if (!event?.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                stringResource(R.string.details_section), 
                                style = MaterialTheme.typography.titleMedium, 
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = event?.description ?: "", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Surface(
            color = Color(0xFFFFD700).copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
