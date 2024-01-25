package com.example.datastoreexample

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.datastoreexample.ui.theme.DataStoreExampleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val Context.dataStore by preferencesDataStore(name = "prefs_user")

class MainActivity : ComponentActivity() {

    companion object {
        const val KEY_NAME = "key_name"
        const val KEY_VIP = "key_vip"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DataStoreExampleTheme {
                var name by rememberSaveable { mutableStateOf("") }
                var isVip by rememberSaveable { mutableStateOf(false) }
                var nameResponse by rememberSaveable { mutableStateOf("") }
                DataStoreExampleScreen(
                    modifier = Modifier.fillMaxSize(),
                    name = name,
                    nameResponse = nameResponse,
                    isVip = isVip,
                    onNameChanged = { name = it },
                    onIsVipChanged = { isVip = it }
                ) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        saveUser(name, isVip)
                        delay(500)

                        getUser().collect { user ->
                            withContext(Dispatchers.Main) {
                                nameResponse = user.name
                                if (user.vip) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "is VIP",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getUser() = dataStore.data.map { prefs ->
        User(
            name = prefs[stringPreferencesKey(KEY_NAME)].orEmpty(),
            vip = prefs[booleanPreferencesKey(KEY_VIP)] ?: false
        )
    }

    private suspend fun saveUser(name: String, isVip: Boolean) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(KEY_NAME)] = name
            prefs[booleanPreferencesKey(KEY_VIP)] = isVip
        }
    }
}


@Composable
fun DataStoreExampleScreen(
    modifier: Modifier = Modifier,
    name: String = "",
    nameResponse: String = "",
    isVip: Boolean = false,
    onNameChanged: (String) -> Unit = {},
    onIsVipChanged: (Boolean) -> Unit = {},
    onClick: () -> Unit
) {

    Column(
        modifier = modifier
            .padding(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            label = { Text(text = stringResource(id = R.string.hint_name)) },
            onValueChange = onNameChanged
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.is_vip))
            Switch(
                checked = isVip,
                onCheckedChange = onIsVipChanged,
                thumbContent = {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null
                    )
                }
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Text(text = stringResource(id = R.string.save))
        }

        Text(text = nameResponse, fontSize = 24.sp)
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DataStoreExampleScreenPreview() {
    DataStoreExampleTheme {
        DataStoreExampleScreen {

        }
    }
}