package com.example.yarcafe_game

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.yarcafe_game.ui.theme.Yarcafe_gameTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.textview.MaterialTextView
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var IMG_avatar: AppCompatImageView
    private lateinit var IMG_left_arrow: Button
    private lateinit var IMG_right_arrow: Button
    private lateinit var LBL_score: MaterialTextView
    private lateinit var sensorManager: SensorManager
    private lateinit var soundPool: SoundPool
    private lateinit var cafe0: ImageView
    private lateinit var cafe1: ImageView
    private lateinit var cafe2: ImageView
    private var crashSoundId: Int = 0
    private var accelerometer: Sensor? = null
    private var currentLane: Int = 2
    private var screenHeight: Int = 0
    private var screenWidth: Int = 0
    private var distanceTraveled = 0
    private val handler = Handler()
    private var lives = 3
    private var score: Int =0
    private var moveSpeed = 10
    private val scoreHandler = Handler()
    private val hamburgers = mutableListOf<ImageView>()
    private val pizzas = mutableListOf<ImageView>()
    private val sushis = mutableListOf<ImageView>()
    private val plants = mutableListOf<ImageView>()
    private val moveEnemiesRunnable = object : Runnable {
        override fun run() {
            hamburgers.forEach { enemy ->
                moveEnemyDown(enemy, Random.nextInt(0, 5))
                checkCollision(enemy)
            }
            pizzas.forEach { enemy ->
                moveEnemyDown(enemy, Random.nextInt(0, 5))
                checkCollision(enemy)
            }
            sushis.forEach { enemy ->
                moveEnemyDown(enemy, Random.nextInt(0, 5))
                checkCollision(enemy)
            }
            plants.forEach { enemy ->
                moveEnemyDown(enemy, Random.nextInt(0, 5))
                checkCollision(enemy)  }

            handler.postDelayed(this, 50)
        }
    }

    private fun setupSensorMovement() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        IMG_left_arrow.isEnabled = false
        IMG_right_arrow.isEnabled = false
        IMG_left_arrow.visibility = View.GONE
        IMG_right_arrow.visibility=View.GONE
        if (accelerometer != null) {  // בדיקה אם החיישן קיים במכשיר
            val sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        val x = event.values[0] // קבלת נתוני ציר ה־X מהחיישן
                        val y = event.values[1]
                        handleMovementBySensor(x)
                        handleSpeedByTilt(y)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                }
            }

            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Toast.makeText(this, "No accelerometer sensor found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMovementBySensor(x: Float) {
        val sensitivityThreshold = 2.0f // סף רגישות לתזוזה (ככל שהמספר גבוה יותר, כך המכשיר יצטרך לזוז יותר כדי שהדמות תזוז)

        // הפיכת הכיוון של התנועה על ידי שינוי הסימן
        if (x < -sensitivityThreshold) { // אם זז שמאלה (ערך שלילי גדול)
            if (currentLane < 4) {
                currentLane++
                moveCarToLane(currentLane)
            }
        } else if (x > sensitivityThreshold) { // אם זז ימינה (ערך חיובי גדול)
            if (currentLane > 0) {
                currentLane--
                moveCarToLane(currentLane)
            }
        }
    }


    private fun checkCollision(enemy: ImageView) {

        val avatarRect = IMG_avatar.getHitRect()
        val enemyRect = enemy.getHitRect()

        if (Rect.intersects(avatarRect, enemyRect)) {
            // A collision has occurred, reduce lives

            if (lives > 0) {

                if (enemy.tag == "plant") {
                    addScore(10)
                    Toast.makeText(this, "+10 points!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    lives--
                    vibrateOnCollision()
                    updateHeartsDisplay()
                    playCrushSound()
                    soundPool.play(crashSoundId, 1f, 1f, 0, 0, 1f)
                    val collisionMessage = when (enemy) {
                        resources.getDrawable(
                            R.drawable.pizza_svgrepo_com,
                            null
                        ).constantState -> "No pizza in the café!"

                        resources.getDrawable(
                            R.drawable.sushi_svgrepo_com,
                            null
                        ).constantState -> "Sushi? Wrong place!"

                        resources.getDrawable(
                            R.drawable.hamburger_7_svgrepo_com,
                            null
                        ).constantState -> "Burgers? We're a café, not a diner!"


                        else -> "You hit something!"
                    }
                    Toast.makeText(
                        this,
                        collisionMessage + " Lives left: $lives",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            if (lives == 0) {
                gameOver() // עצירת תנועת האויבים
                lives--
            }


            // Optionally reset enemy position after collision
            resetEnemyPosition(enemy, Random.nextInt(0, 5))
        }
    }
    private fun getLocation(callback: (Location?) -> Unit) {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                callback(location)
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun gameOver() {

        // עצירת תנועת האויבים
        handler.removeCallbacks(moveEnemiesRunnable)
        val gameOverLayout: View = findViewById(R.id.game_over_layout)
        val finalScoreText: TextView = findViewById(R.id.final_score)
        val playerNameDisplay: TextView = findViewById(R.id.player_name_display)
        IMG_left_arrow.isEnabled = false
        IMG_right_arrow.isEnabled = false
        gameOverLayout.visibility = View.VISIBLE // מציג את מסך ה-Game Over
        finalScoreText.text = "Score: $score" // מציג את הניקוד הסופי
        playerNameDisplay.text = "Player: ${intent.getStringExtra("PLAYER_NAME") ?: "Player"}"

        // שמירת השיא עם המיקום
        getLocation { location ->
            val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Player"
            val latitude = location?.latitude ?: 0.0
            val longitude = location?.longitude ?: 0.0
            HighScoreManager.saveHighScore(this, playerName, score, latitude, longitude)
        }

        // טיפול בכפתור "Back to Start"
        val btnBackToStart: Button = findViewById(R.id.btn_back_to_start)
        btnBackToStart.setOnClickListener {
            val intent = Intent(this@MainActivity, StartScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun vibrateOnCollision() {
        // Get the Vibrator service from the system
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        // Check API level to handle vibration accordingly
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // VibrationEffect for Android Oreo (API 26) and above
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)

            vibrator.vibrate(vibrationEffect)
        } else {
            // Deprecated method for devices below API 26
            vibrator.vibrate(500) // Vibrate for 500 milliseconds
        }
    }
    private fun ImageView.getHitRect(): Rect {
        val rect = Rect()
        getHitRect(rect)
        return rect
    }
private fun updateHeartsDisplay() {
    cafe0.visibility = View.VISIBLE
    cafe1.visibility = View.VISIBLE
    cafe2.visibility = View.VISIBLE

    // עדכון לפי כמות החיים
    if (lives < 3) {
        cafe2.visibility = View.INVISIBLE // מסתיר את הלב השלישי
    }
    if (lives < 2) {
        cafe1.visibility = View.INVISIBLE // מסתיר את הלב השני
    }
    if (lives < 1) {
        cafe0.visibility = View.INVISIBLE // מסתיר את הלב הראשון
    }


}
    private fun setupAvatarMovement() {
        IMG_left_arrow.setOnClickListener {
            if (currentLane > 0) {
                currentLane--  // move left
                moveCarToLane(currentLane)
            }
        }
        IMG_right_arrow.setOnClickListener {
            if (currentLane < 4) {
                currentLane++
                moveCarToLane(currentLane)
            }
        }
    }
    private fun handleSpeedByTilt(y: Float) {
        val maxSpeed = 20 // מהירות מקסימלית
        val minSpeed = 5  // מהירות מינימלית

        // ככל שהמשתמש מטה את המכשיר קדימה (ערכים שליליים בציר ה-Y), האויבים זזים מהר יותר
        // וככל שהמשתמש מטה את המכשיר אחורה (ערכים חיוביים בציר ה-Y), האויבים זזים לאט יותר
        moveSpeed = (10 - y * 2).toInt() // עדכון מהירות לפי הטיה

        // בקרה על המהירות כדי שתישאר בטווח מסוים
        if (moveSpeed > maxSpeed) moveSpeed = maxSpeed
        if (moveSpeed < minSpeed) moveSpeed = minSpeed
    }
    private fun moveEnemyDown(enemy: ImageView, lane: Int) {
        val distanceMoved=moveSpeed
        enemy.y += moveSpeed
        distanceTraveled+= distanceMoved
        if (distanceTraveled >= 1500) {
            addScore(1) // מוסיפים נקודה אחת
            distanceTraveled = 0 // איפוס המרחק שנצבר
        }
        // If enemy reaches the bottom of the screen, reset its position
        if (enemy.y > screenHeight) {
            resetEnemyPosition(enemy, lane)
        }
    }
    private fun resetEnemyPosition(enemy: ImageView, lane: Int) {
        // Start the enemy at a random Y position above the screen for staggered entry
        enemy.y = Random.nextInt(-screenHeight, 0).toFloat() // Start somewhere above the screen

        moveEnemyToLane(enemy, lane) // Move enemy to the appropriate lane
    }
    private fun moveEnemyToLane(enemy: ImageView, lane: Int) {
        val margin_default = dpToPx(30)

        val laneXPosition = when (lane) {
            0 -> margin_default.toFloat()- (enemy.width / 2)
            1 -> ((screenWidth-margin_default.toFloat()*2) / 4)+margin_default.toFloat()- (enemy.width / 2)
            2 -> ((screenWidth-margin_default.toFloat()*2) / 4)*2+margin_default.toFloat()- (enemy.width / 2)
            3 -> ((screenWidth-margin_default.toFloat()*2) / 4)*3+margin_default.toFloat()- (enemy.width / 2)
            4 -> (screenWidth-margin_default.toFloat()*2) +margin_default.toFloat()- (enemy.width / 2)


            else -> 0f // Default case
        }

        enemy.x = laneXPosition.toFloat()
    }
    @SuppressLint("RestrictedApi")
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun getScreenDimensions() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels
    }
    private fun moveCarToLane(lane: Int) {
        // כאן תוכל לשנות את מיקום ה-X של המכונית בהתאם לנתיב

        val margin_default = dpToPx(30)

        val laneXPosition = when (lane) {
            0 -> margin_default.toFloat()- (IMG_avatar.width / 2)
            1 -> ((screenWidth-margin_default.toFloat()*2) / 4)+margin_default.toFloat()- (IMG_avatar.width / 2)
            2 -> ((screenWidth-margin_default.toFloat()*2) / 4)*2+margin_default.toFloat()- (IMG_avatar.width / 2)
            3 -> ((screenWidth-margin_default.toFloat()*2) / 4)*3+margin_default.toFloat()- (IMG_avatar.width / 2)
            4 -> (screenWidth-margin_default.toFloat()*2) +margin_default.toFloat()- (IMG_avatar.width / 2)


            else -> 0f // ברירת מחדל, לא אמור לקרות
        }
        IMG_avatar.x = laneXPosition.toFloat()
    }
    private fun addScore(points: Int){
        score+=points
        LBL_score.text="$score"
    }
    private fun createCoins() {
        val lanes = listOf(0, 1, 2, 3, 4)
        for (i in 0 until 3) { // יצירת 3 מופעים של כל אויב
            // יצירת מופעים חדשים של ImageView עבור כל אויב
            val coinSize = dpToPx(50)
            val newPlant = ImageView(this).apply {
                setImageResource(R.drawable.plant_svgrepo_com)
                layoutParams = ViewGroup.LayoutParams(coinSize, coinSize)
                tag = "plant"
            }


            // הצבת האויבים בנתיב שלהם לפני הוספתם ל-Layout
            resetEnemyPosition(newPlant, lanes[i % lanes.size])

            // הוספת האויבים שנוצרו ל-Layout אחרי ההצבה הנכונה
            (findViewById<View>(R.id.main) as ViewGroup).apply {
                addView(newPlant)

            }

            // הוספת האויבים לרשימות כדי שנוכל לנהל אותם בהמשך
            plants.add(newPlant)
        }
    }


    private fun createEnemies() {
        val lanes = listOf(0, 1, 2, 3, 4) // מסלולים אפשריים

        for (i in 0 until 3) { // יצירת 3 מופעים של כל אויב
            // יצירת מופעים חדשים של ImageView עבור כל אויב
            val enemySize = dpToPx(50)
            val newHamburger = ImageView(this).apply {
                setImageResource(R.drawable.hamburger_7_svgrepo_com) // תמונה של המבורגר
                layoutParams = ViewGroup.LayoutParams(enemySize, enemySize) // הגדרת הגודל ל-100dp
            }
            val newPizza = ImageView(this).apply {
                setImageResource(R.drawable.pizza_svgrepo_com) // תמונה של פיצה
                layoutParams = ViewGroup.LayoutParams(enemySize, enemySize)
            }
            val newSushi = ImageView(this).apply {
                setImageResource(R.drawable.sushi_svgrepo_com) // תמונה של סושי
                layoutParams = ViewGroup.LayoutParams(enemySize, enemySize)
            }
            // הצבת האויבים בנתיב שלהם לפני הוספתם ל-Layout
            resetEnemyPosition(newHamburger, lanes[i % lanes.size])
            resetEnemyPosition(newPizza, lanes[(i + 1) % lanes.size])
            resetEnemyPosition(newSushi, lanes[(i + 2) % lanes.size])

            // הוספת האויבים שנוצרו ל-Layout אחרי ההצבה הנכונה
            (findViewById<View>(R.id.main) as ViewGroup).apply {
                addView(newHamburger)
                addView(newPizza)
                addView(newSushi)
            }

            // הוספת האויבים לרשימות כדי שנוכל לנהל אותם בהמשך
            hamburgers.add(newHamburger)
            pizzas.add(newPizza)
            sushis.add(newSushi)
        }
    }
    private fun playCrushSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.crush_sound)
        mediaPlayer?.start()
    }
    private fun findViews() {
        IMG_avatar = findViewById(R.id.IMG_avatar)
        IMG_left_arrow = findViewById(R.id.IMG_left_arrow)
        IMG_right_arrow = findViewById(R.id.IMG_right_arrow)
        LBL_score = findViewById(R.id.LBL_score)
        cafe0 = findViewById(R.id.cafe0)
        cafe1 = findViewById(R.id.cafe1)
        cafe2 = findViewById(R.id.cafe2)



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_layout)
        val useSensors = intent.getBooleanExtra("USE_SENSORS", false)
        findViews()
        getScreenDimensions()
        if (useSensors) {
            setupSensorMovement()  // הפונקציה שתשתמש בחיישנים
        } else {
            setupAvatarMovement()  // הפונקציה שמזיזה עם חצים
        }
        createEnemies()
        createCoins()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        handler.post(moveEnemiesRunnable)

        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                hamburgers.forEachIndexed { index, hamburger ->
                    resetEnemyPosition(hamburger, Random.nextInt(0, 5)) // מיקום המבורגר
                }
                pizzas.forEachIndexed { index, pizza ->
                    resetEnemyPosition(pizza, Random.nextInt(0, 5)) // מיקום פיצה
                }
                sushis.forEachIndexed { index, sushi ->
                    resetEnemyPosition(sushi, Random.nextInt(0, 5)) // מיקום סושי
                }
                plants.forEachIndexed { index, plant ->
                    resetEnemyPosition(plant, Random.nextInt(0, 5))
                }

                // התחלת התנועה של האויבים לאחר שהוצבו כראוי
                handler.post(moveEnemiesRunnable)

                // הסרת המאזין לאחר שהכל הוגדר
                sushis.firstOrNull()?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        }
        sushis.firstOrNull()?.viewTreeObserver?.addOnGlobalLayoutListener(layoutListener)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Maximum simultaneous sound streams
            .setAudioAttributes(audioAttributes)
            .build()

        // Load the crash sound from res/raw folder
        //crashSoundId = soundPool.load(this, R.raw.crash_sound, 1)

        val btnBackToStart: Button = findViewById(R.id.btn_back_to_start)
        btnBackToStart.setOnClickListener {
            val intent = Intent(this@MainActivity, StartScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Yarcafe_gameTheme {
        Greeting("Android")
    }
}