@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)

package com.example.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.time.ExperimentalTime


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val locale = Locale.forLanguageTag(resources.getString(R.string.locale))
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                TaskManagerLayout()
            }
        }
    }
}

@Composable
fun TaskManagerLayout(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }


    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.input_header),
                        modifier = Modifier
                            .align(alignment = Alignment.Start)
                            .padding(top = 8.dp),
                    )
                    EditTextField(
                        label = R.string.task,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                        ),
                        value = input,
                        onValueChanged = { input = it },
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 16.dp)
                            .fillMaxWidth(),
                    )
                    Button(onClick = { showDatePicker = true }) {
                        Text(
                            text = stringResource(R.string.select_date),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(bottom = 16.dp))

                    if (showDatePicker) {
                        TaskDatePickerField(onDateSelected = { millis ->
                            selectedDateMillis = millis
                        }, onDismiss = {
                            showDatePicker = false
                        })
                    }
                }
            })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { },
            shape = CircleShape,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.adicionar),
                contentDescription = stringResource(R.string.add_task)
            )
        }
    }) { it ->
        LazyColumn(
            modifier = modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = { }
        )
    }
}

@Composable
fun EditTextField(
    @StringRes label: Int,
    //@DrawableRes leadingIcon: Int,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        singleLine = true,
        //leadingIcon = { Icon(painter = painterResource(id = leadingIcon), null) },
        modifier = modifier,
        onValueChange = onValueChanged,
        label = { Text(stringResource(label)) },
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun TaskDatePickerField(
    onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            onDateSelected(datePickerState.selectedDateMillis)
            onDismiss()
        }) {
            Text(stringResource(R.string.confirm))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.cancel))
        }
    }) {
        DatePicker(state = datePickerState, title = {
            Text(
                text = stringResource(R.string.select_date),
                modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }, headline = {
            Text(
                text = stringResource(R.string.select_date),
                modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                style = MaterialTheme.typography.headlineLarge
            )
        })
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDatePickerLayoutPreview() {
    MaterialTheme {
        Surface {
            TaskManagerLayout()
        }
    }
}