# Yarcafe Game

Welcome to the Yarcafe Game repository! This is an interactive and engaging mobile game where players dodge items and collect points in a dynamic café-themed setting. The project incorporates accelerometer-based controls, location-based high scores, and a leaderboard system to create a fun and challenging experience.

---

## Features

### Gameplay Mechanics
- **Avatar Movement**:
  - Control your character with either:
    - On-screen buttons.
    - Tilt-based accelerometer controls.
- **Dodging Obstacles**:
  - Avoid items like hamburgers, pizzas, and sushi, which don't belong in the café.
- **Scoring**:
  - Gain points by collecting plants or traveling a certain distance.
  - Each plant adds 10 points to your score.
- **Collision Effects**:
  - Vibrations and sound effects for immersive gameplay.
  - Humorous messages appear when colliding with wrong items.

### Game Over Screen
- Displays:
  - Final score.
  - Player name.
  - Option to return to the start screen.
- Saves high scores along with player location.

### Leaderboard and High Scores
- **Leaderboard**:
  - Displays top 10 high scores.
- **Location Mapping**:
  - View high scores on a map.
  - Each high score includes the player's location for added context.

### Admin Features
- Clear high scores with a single action.

---

## Project Structure

### Activities
1. **MainActivity**:
   - Handles gameplay mechanics, collision detection, and scoring.
   - Manages avatar and enemy movements.
2. **StartScreen**:
   - Provides options to start the game with buttons or sensors.
   - Displays leaderboard and high scores map.

### Components
- **HighScoreManager**:
  - Manages saving, retrieving, and clearing high scores.
  - Stores player name, score, and location.

### Assets
- Sound effects for collisions.
- Graphics for avatars, enemies (hamburgers, pizzas, sushi), and plants.

---

## How to Play
1. Launch the app and enter your name.
2. Choose your preferred control method (buttons or sensors).
3. Move your avatar to:
   - Avoid incorrect items (hamburgers, pizzas, sushi).
   - Collect plants for points.
4. View your final score and compare it on the leaderboard.
5. Check the map for high scores and their locations.

---

## Setup Instructions

### Prerequisites
- **Android Studio**: Latest version.
- **Android Device**: With an accelerometer for sensor-based gameplay.

### Running the Project
1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the app on your Android device.

---

## Future Enhancements
- Add new enemy types and obstacles.
- Introduce power-ups and bonus rounds.
- Implement achievements and social media sharing.

---

## Contributors
- **Ariel Halevy**: Design and development.

---

## Acknowledgments
Special thanks to open-source resources and the gaming community for inspiring this project.

