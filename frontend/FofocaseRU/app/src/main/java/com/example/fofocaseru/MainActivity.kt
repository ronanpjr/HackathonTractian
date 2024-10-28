package com.example.fofocaseru

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fofocaseru.ui.theme.FofocasERUTheme
import kotlinx.coroutines.launch
import uploadAudioFile

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FofocasERUTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    AudioInputScreen()
                }
            }
        }
    }
}

@Composable
fun AudioInputScreen(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.background(Color(0xFF363636))) {
        AudioFileSelector()
    }
}

@Composable
private fun AudioFileSelector() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selectedAudioUri = remember { mutableStateOf<Uri?>(null) }
    val tasksList = remember { mutableStateOf<List<String>?>(null) }
    val isChecked = remember { mutableStateOf<List<Boolean>>(listOf()) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedAudioUri.value = uri
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            painter = painterResource(R.drawable.wisedog_removebg_preview),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = {
                launcher.launch("audio/*")
            },
            modifier = Modifier.padding(top = 50.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_audio_file),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.audio_button_title))
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Adiciona espaço entre o botão e a lista

        tasksList.value?.let { tasks ->
            LazyColumn {
                itemsIndexed(tasks) { index, task ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp)) // Aplica o arredondamento
                            .background(color = Color.White)
                            .border(
                                border = BorderStroke(width = 3.dp, color = Color.White),
                                shape = RoundedCornerShape(8.dp) // Adiciona cantos arredondados
                            )
                            .padding(8.dp) // Adiciona padding para evitar que o conteúdo toque as bordas
                    ) {
                        Checkbox(
                            checked = isChecked.value[index],
                            onCheckedChange = { checked ->
                                val newCheckedList = isChecked.value.toMutableList()
                                newCheckedList[index] = checked
                                isChecked.value = newCheckedList
                            }
                        )
                        Text(
                            text = task
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }
    }

    selectedAudioUri.value?.let { uri ->
        coroutineScope.launch {
            tasksList.value = uploadAudioFile(context, uri)
            // Initialize isChecked with the same size as tasksList
            isChecked.value = List(tasksList.value?.size ?: 0) { false }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InputAudioScreenPreview() {
    FofocasERUTheme {
        AudioInputScreen()
    }
}
