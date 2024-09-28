package com.example.yarcafe_game

import android.content.Context
import android.content.SharedPreferences

object HighScoreManager {

    private const val PREFS_NAME = "high_scores_prefs"
    private const val MAX_SCORES = 10 // מספר השיאים לשמירה

    // שורה חדשה: הוספת פרמטרים של מיקום לפונקציה
    fun saveHighScore(context: Context, playerName: String, score: Int, latitude: Double, longitude: Double) {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        // קבלת רשימת השיאים הנוכחית
        val scoresList = getHighScores(context).toMutableList()

        // הוספת השיא החדש (שורה חדשה)
        scoresList.add(Triple(playerName, score, Pair(latitude, longitude)))

        // מיון הרשימה בסדר יורד לפי הניקוד ושמירת רק עשרת הראשונים
        val sortedScores = scoresList.sortedByDescending { it.second }.take(MAX_SCORES)

        // ניקוי השיאים הישנים מהSharedPreferences ושמירה מחדש
        editor.clear()
        sortedScores.forEachIndexed { index, (name, highScore, location) -> // שיניתי כדי לכלול גם את המיקום
            editor.putString("player_$index", name)
            editor.putInt("score_$index", highScore)
            editor.putString("location_$index", "${location.first},${location.second}") // שורה חדשה: שמירת מיקום
        }

        editor.apply()
    }

    // שיניתי את הפונקציה כדי להחזיר גם את המיקום
    fun getHighScores(context: Context): List<Triple<String, Int, Pair<Double, Double>>> {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val scoresList = mutableListOf<Triple<String, Int, Pair<Double, Double>>>()

        for (i in 0 until MAX_SCORES) {
            val playerName = sharedPrefs.getString("player_$i", null) ?: continue
            val score = sharedPrefs.getInt("score_$i", -1)
            val locationString = sharedPrefs.getString("location_$i", null) ?: continue
            val (latitude, longitude) = locationString.split(",").map { it.toDouble() } // שורה חדשה
            if (score != -1) {
                scoresList.add(Triple(playerName, score, Pair(latitude, longitude))) // שורה חדשה
            }
        }

        return scoresList
    }
    fun clearHighScores(context: Context) {
        val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.clear() // מחיקת כל הנתונים
        editor.apply()
    }
}
