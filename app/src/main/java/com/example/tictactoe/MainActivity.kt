@file:Suppress("DEPRECATION")

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tictactoe.ui.theme.TicTacToeTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Player(
    val id: String,
    val name: String
)

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
        composable("create account") { CreateAccountScreen(navController) }
        composable("game") { GameScreen(navController, ticTacToeGame) }
        composable("winner") { WinnerScreen(navController, ticTacToeGame) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(navController: NavController) {
    val db = Firebase.firestore
    val playerName = remember { mutableStateOf("") }

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
    ){ padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = playerName.value,
                onValueChange = { playerName.value = it },
                label = { Text("Player Name") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if(playerName.value.isNotEmpty()){
                        //add new player to database
                        val player = Player(id = db.collection("Players").document().id, name = playerName.value)
                        db.collection("Players").document(player.id).set(player)
                        navController.navigate("main menu")
                    }
                },
                modifier = Modifier.width(200.dp)
            ) {
                Text("Create Account")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(navController: NavController) {
    val db = Firebase.firestore
    val gameList = MutableStateFlow<List<TicTacToeGame>>(emptyList())
    val playerList = MutableStateFlow<List<Player>>(emptyList())

    db.collection("Lobbys")
        .addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                gameList.value = value.toObjects(TicTacToeGame::class.java)
            }
        }

    db.collection("Players")
        .addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                playerList.value = value.toObjects(Player::class.java)
            }
        }

    val games = gameList.asStateFlow().collectAsStateWithLifecycle()
    val players = playerList.asStateFlow().collectAsStateWithLifecycle()

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
//            Text(
//                text = "Welcome to Tic Tac Toe!",
//                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            Button(
//                onClick = { navController.navigate("game") },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Start Game")
//            }
            item{
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Create Account",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("create account") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Account")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Games",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(games.value){ game ->
                ListItem(
                    headlineContent = { Text(game.player1 + " vs " + game.player2) }
                )
                Spacer(modifier = Modifier.height(16.dp))

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
    ) { padding ->
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
    navigateAfterDelay(2000) {
        navController.navigate("main menu")
    }
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
                text = if (winner == 1) "Player 1 wins!" else if (winner == 2) "Player 2 wins!" else "It's a draw!",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

fun navigateAfterDelay(delay: Int, function: () -> Unit) {
    android.os.Handler().postDelayed({
        function()
    }, delay.toLong())
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
    for (i in 0..2) {
        for (j in 0..2) {
            if (board[i][j] == 0) {
                return 0
            }
        }
    }
    return 3
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TicTacToeTheme {
        TicTacToeApp()
    }
}