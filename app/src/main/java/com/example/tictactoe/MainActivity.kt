package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    val board: MutableList<MutableList<Int>>,
    var winner: Int
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
    ticTacToeGame.value = ticTacToeGame.value ?: TicTacToeGame(
        id = 1,
        playerTurn = 1,
        player1 = "Player 1",
        player2 = "Player 2",
        board = List(3) { MutableList(3) { 0 } }.toMutableList(),
        winner = 0
    )

    NavHost(navController = navController, startDestination = "main menu") {
        composable("main menu") { MainMenu(navController) }
        composable("game") { GameScreen(navController, ticTacToeGame) }
        composable("winner") { WinnerScreen(navController, ticTacToeGame) }
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
    val playerTurn = remember { mutableIntStateOf(1) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Game Screen") },
                actions = {
                    IconButton(onClick = { navController.navigate("main menu") }){
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
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "Player ${if (playerTurn.intValue == 1) "X" else "O"}'s turn"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                for (i in 0..2) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        for (j in 0..2) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(onClick = {
                                if (board.value[i][j] == 0) {
                                    board.value[i][j] = playerTurn.intValue
                                }
                                if (playerTurn.intValue == 1) {
                                    playerTurn.intValue = 2
                                } else {
                                    playerTurn.intValue = 1
                                }
                                ticTacToeGame.value?.winner = checkWin(board.value)
                                if (ticTacToeGame.value?.winner != 0) {
                                    navController.navigate("winner")
                                }
                            })
                            {
                                Text(
                                    text = if (board.value[i][j] == 1) "X" else if (board.value[i][j] == 2) "O" else ""
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (playerTurn.intValue == 1) {
                    playerTurn.intValue = 2
                } else {
                    playerTurn.intValue = 1
                }
            }) { Text("Make Move") }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinnerScreen(navController: NavHostController, ticTacToeGame: MutableState<TicTacToeGame?>){
    val winner = ticTacToeGame.value?.winner
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WINNER") },
                actions = {
                    IconButton(onClick = { navController.navigate("main menu") }){
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
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
                .padding(padding),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "Player $winner won!",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

fun checkWin(board: List<List<Int>>): Int {
    for (i in 0..2) {
        if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != 0) {
            return board[i][0]
        }
        if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != 0) {
            return board[0][i]
        }
    }
    if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != 0) {
        return board[0][0]
    }
    if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != 0) {
        return board[0][2]
    }
    return 0
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TicTacToeTheme {
        TicTacToeApp()
    }
}