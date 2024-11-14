package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tictactoe.ui.theme.TicTacToeTheme

data class TicTacToeGame(
    var id: Int,
    var playerTurn: Int,
    var player1: String,
    var player2: String,
    val board: MutableList<MutableList<Int>>
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeTheme {
                TicTacToeApp()
            }
        }
    }
}

@Composable
fun TicTacToeApp(){
    val navController = rememberNavController()
    val ticTacToeGame = remember { mutableStateOf<TicTacToeGame?>(null) }

    NavHost(navController = navController, startDestination = "main menu") {
        composable("main menu") { MainMenu(navController) }
        composable("game") { GameScreen(navController, ticTacToeGame) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Main Menu") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "Welcome to Tic Tac Toe!",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("game") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Game")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavHostController, ticTacToeGame: MutableState<TicTacToeGame?>) {
    val board = remember { mutableStateOf(List(3) { MutableList(3) { 0 } }) }
    val playerTurn = remember { mutableStateOf(1) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Game Screen") },
                actions = {
                    IconButton(onClick = { navController.navigateUp() }){
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                )
            )
        }
    ){ padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Player ${if (playerTurn.value == 1) "X" else "O"}'s turn",
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (playerTurn.value == 1) {
                    playerTurn.value = 2
                } else {
                    playerTurn.value = 1
                }
            }) { Text("Make Move") }
            Spacer(modifier = Modifier.height(16.dp))
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TicTacToeTheme {
        TicTacToeApp()
    }
}