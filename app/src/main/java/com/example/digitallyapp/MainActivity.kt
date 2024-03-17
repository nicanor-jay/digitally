package com.example.digitallyapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.digitallyapp.navigation.DigitallyNavHost
import com.example.digitallyapp.ui.theme.DigitallyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferencesManager = (application as DigitallyApplication).preferencesManager
            val dynamicColorBoolean by preferencesManager.dynamicColorPreferenceFlow.collectAsState(
                initial = false
            )

            Log.d("MainActivity", "dynamicColor boolean: $dynamicColorBoolean")
            DigitallyAppTheme(dynamicColor = dynamicColorBoolean) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DigitallyApp()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DigitallyApp(navController: NavHostController = rememberNavController()) {
    DigitallyNavHost(navController = navController)
}

@Composable
fun DigitallyTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    backOrExit: Boolean? = false,
    actions: @Composable RowScope.() -> Unit = {} // Add the 'actions' parameter
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                style = MaterialTheme.typography.titleLarge
            )
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    if (backOrExit == false) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_button)
                        )
                    }
                }
            }
        },
        actions = actions // Pass the 'actions' lambda to the 'actions' parameter
    )
}