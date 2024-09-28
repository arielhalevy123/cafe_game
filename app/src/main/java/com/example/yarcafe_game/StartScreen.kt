package com.example.yarcafe_game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.yarcafe_game.ui.theme.Yarcafe_gameTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StartScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_screen_layout)
        val btnStart: Button = findViewById(R.id.btn_start)
        val btnStartSens: Button = findViewById(R.id.btn_start_sens)
        val btnLeaderboard: Button = findViewById(R.id.btn_leaderboard)
        val leaderboardLayout: View = findViewById(R.id.leaderboard_layout)
        val startLayout: View = findViewById(R.id.start_layout)
        val btnBackToStart: Button = findViewById(R.id.btn_back_to_start)
        val leaderboardContent: TextView = findViewById(R.id.leaderboard_content)
        val playerNameInput: EditText = findViewById(R.id.player_name_input)
        val btnHighScoreMap: Button = findViewById(R.id.btn_high_score_map)
        val mapContainer: View = findViewById(R.id.map_fragment_container)
        val btnBackFromMap: Button=findViewById(R.id.btn_back_from_map)
        mapContainer.visibility = View.GONE
        btnStart.setOnClickListener {
            val playerName = playerNameInput.text.toString()
            val intent = Intent(this@StartScreen, MainActivity::class.java)
            intent.putExtra("PLAYER_NAME", playerName)  // Pass player name to the game
            intent.putExtra("USE_SENSORS", false)
            startActivity(intent)
            finish()
        }
        btnStartSens.setOnClickListener {
            val playerName = playerNameInput.text.toString()
            val intent = Intent(this@StartScreen, MainActivity::class.java)
            intent.putExtra("PLAYER_NAME", playerName)  // Pass player name to the game
            intent.putExtra("USE_SENSORS", true)
            startActivity(intent)
            finish()
        }
        btnLeaderboard.setOnClickListener {
            startLayout.visibility = View.GONE
            leaderboardLayout.visibility = View.VISIBLE

            // Update leaderboard content
            val highScores = HighScoreManager.getHighScores(this)
            val formattedScores = highScores.joinToString(separator = "\n") { (name, score) ->
                "$name: $score"
            }
            leaderboardContent.text = formattedScores
        }
        btnHighScoreMap.setOnClickListener {
            // Show the map container
            mapContainer.visibility = View.VISIBLE
            btnBackFromMap.visibility = View.VISIBLE

            // Logic to open the map fragment only when the button is clicked
            val mapFragment = SupportMapFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.map_fragment_container, mapFragment)
                .commit()
            mapFragment.getMapAsync { googleMap ->
                googleMap.uiSettings.isZoomControlsEnabled = true
                val highScores = HighScoreManager.getHighScores(this)
                highScores.forEach { (name, _, location) ->
                    val userLatLng = LatLng(location.first, location.second)
                    googleMap.addMarker(
                        MarkerOptions().position(userLatLng).title("$name's High Score")
                    )
                }


            }
        }

        btnBackToStart.setOnClickListener {
            startLayout.visibility = View.VISIBLE
            leaderboardLayout.visibility = View.GONE
        }

        btnBackFromMap.setOnClickListener {
            // Hide the map and back button, return to the leaderboard
            mapContainer.visibility = View.GONE
            btnBackFromMap.visibility = View.GONE
            leaderboardLayout.visibility = View.VISIBLE
        }


    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    Yarcafe_gameTheme {
        Greeting2("Android")
    }
}