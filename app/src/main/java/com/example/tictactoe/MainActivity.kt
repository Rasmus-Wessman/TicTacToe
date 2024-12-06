@file:Suppress("DEPRECATION")

package com.example.tictactoe

import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Player(
    val id: String = "",
    val name: String = ""
)

data class TicTacToeGame(
    var board: List<Int> = emptyList(),
    var id: String = "",
    var player1: String = "",
    var player2: String = "",
    var playerTurn: Int = 1,
    var winner: Int = 0
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
    val player = remember { mutableStateOf<Player?>(null) }
    val game = remember { mutableStateOf<TicTacToeGame?>(null) }


    NavHost(navController = navController, startDestination = "main menu") {
        composable("main menu") { MainMenu(navController, player, game) }
        composable("create account") { CreateAccountScreen(navController, player) }
        composable("game") { GameScreen(navController, game, player) }
        composable("winner") { WinnerScreen(navController, game, player) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(navController: NavController, player: MutableState<Player?>) {
    val db = Firebase.firestore

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Main Menu") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("main menu") }) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                }
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
                value = player.value?.name ?: "",
                onValueChange = { player.value = player.value?.copy(name = it) ?: Player("", it) },
                label = { Text("Name") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    player.value?.name?.let {
                        if(it.isNotEmpty()){
                            val name = it.trim()
                            db.collection("players").whereEqualTo("name", it).get().addOnSuccessListener {
                                if(it.isEmpty){
                                    db.collection("players").add(Player(id = db.collection("players").document().id, name = name))
                                    navController.navigate("main menu")
                                }
                            }

                        }
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
fun MainMenu(navController: NavController, player: MutableState<Player?>, game: MutableState<TicTacToeGame?>) {
    val db = Firebase.firestore
    val gameList = MutableStateFlow<List<TicTacToeGame>>(emptyList())
    val playerList = MutableStateFlow<List<Player>>(emptyList())

    db.collection("games")
        .addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                gameList.value = value.toObjects()
            }
        }

    db.collection("players")
        .addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                playerList.value = value.toObjects()

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
            item {
                Spacer(modifier = Modifier.height(120.dp))
                if (player.value == null) {
                    Button(
                        onClick = { navController.navigate("create account") },
                        modifier = Modifier.width(250.dp)
                    ) {
                        Text("Choose Name")
                    }
                } else {
                    Text(
                        text = "Welcome, ${player.value!!.name}!",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            game.value = TicTacToeGame(
                                board = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
                                id = db.collection("games").document().id,
                                player1 = player.value!!.name,
                                player2 = "",
                                playerTurn = 1,
                                winner = 0,
                            )
                            db.collection("games").document(game.value!!.id).set(game.value!!)
                            navController.navigate("game")
                        }
                    ) {
                        Text("Create Game")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Players",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(players.value) { availablePlayer ->
                ListItem(
                    headlineContent = { Text(availablePlayer.name) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Text(
                    text = "Games",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(games.value) { games ->
                if(games.player1.isNotEmpty() && games.player2.isNotEmpty()){
                    ListItem(
                        headlineContent = { Text(games.player1 + " vs " + games.player2) }
                    )
                }else{
                    ListItem(
                        headlineContent = { Text(games.player1 + " vs " + games.player2) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            game.value = games
                            db.collection("games").document(game.value!!.id).update("player2", player.value!!.name)
                            navController.navigate("game")
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Join Game")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavHostController, game: MutableState<TicTacToeGame?>, player: MutableState<Player?>) {
    val db = Firebase.firestore
    val gameState = remember { mutableStateOf(game.value) }

    LaunchedEffect(key1 = gameState.value?.id){
        db.collection("games").document(gameState.value!!.id).addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null && value.exists()) {
                gameState.value = value.toObject(TicTacToeGame::class.java)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Game Screen") },
                actions = {
                    IconButton(onClick = { navController.navigate("main menu") }) {
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
            if(gameState.value!!.player1.isNotEmpty() && gameState.value!!.player2.isNotEmpty()){
                Text(
                    text = gameState.value!!.player1 + " vs " + gameState.value!!.player2,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = if (player.value!!.name == gameState.value!!.player1 && gameState.value!!.playerTurn == 1 || player.value!!.name == gameState.value!!.player2 && gameState.value!!.playerTurn == 2) "Your turn" else "Opponent's turn",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    for (i in 0..2) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            for (j in 0..2) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(
                                    onClick = {
                                        if(player.value!!.name == gameState.value!!.player1 && gameState.value!!.playerTurn == 1 || player.value!!.name == gameState.value!!.player2 && gameState.value!!.playerTurn == 2){
                                            if(gameState.value!!.board[i * 3 + j] == 0 && gameState.value!!.winner == 0){
                                                gameState.value = gameState.value!!.copy(board = gameState.value!!.board.toMutableList().also { it[i * 3 + j] = if (gameState.value!!.playerTurn == 1) 1 else 2 })
                                                gameState.value = gameState.value!!.copy(playerTurn = if (gameState.value!!.playerTurn == 1) 2 else 1)
                                                gameState.value = gameState.value!!.copy(winner = checkWin(gameState.value!!.board))
                                                db.collection("games").document(gameState.value!!.id).set(gameState.value!!)
                                                if(gameState.value!!.winner != 0){
                                                    navController.navigate("winner")
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (gameState.value!!.board[i * 3 + j] == 1) "X" else if (gameState.value!!.board[i * 3 + j] == 2) "O" else "",
                                    )
                                }
                            }
                        }
                    }
                }
            }else{
                Text(
                    text = "Waiting for opponent...",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinnerScreen(navController: NavHostController, game: MutableState<TicTacToeGame?>, player: MutableState<Player?>){
    val db = Firebase.firestore

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
                text = if (game.value!!.winner == 1) game.value!!.player1 + " wins!" else if (game.value!!.winner == 2) game.value!!.player2 + " wins!" else if (game.value!!.winner == 3) "It's a draw!" else "Erm???",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }

    delay(2000) {
        db.collection("games").document(game.value!!.id).delete()
        db.collection("players").document(player.value!!.id).delete()
        navController.navigate("main menu")
        game.value = TicTacToeGame(board = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0), id = "", player1 = "", player2 = "", playerTurn = 1, winner = 0)
    }
}

fun delay(delay: Int, function: () -> Unit) {
    android.os.Handler().postDelayed({
        function()
    }, delay.toLong())
}

fun checkWin(board: List<Int>): Int {
    val rowColumnsAndDiagonals = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )
    for (i in rowColumnsAndDiagonals) {
        val (a, b, c) = i
        if (board[a] != 0 && board[a] == board[b] && board[a] == board[c]) {
            return board[a]
        }
    }
    if (board.all { it != 0 }) {
        return 3
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