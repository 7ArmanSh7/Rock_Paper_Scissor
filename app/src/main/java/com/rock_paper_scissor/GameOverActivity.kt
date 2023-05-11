package com.rock_paper_scissor

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.zybooks.rock_paper_scissor.R


class GameOverActivity : AppCompatActivity() {
    private lateinit var playAgain: Button;
    private lateinit var exitButton: Button;
    private var mediaPlayer: MediaPlayer? = null
    lateinit var face: ImageView
    lateinit var gameOverTxet: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)
        playAgain = findViewById<Button>(R.id.gameOver)
        exitButton = findViewById<Button>(R.id.exitGameOver)
        gameOverTxet = findViewById<TextView>(R.id.gameOverScreenText)

        setButtonsEventHandelers()
        face = findViewById(R.id.face)
        playAnimation()

    }

    /**
     * This functions plays the sad face scaling animation using PropertyValuesHolder
     */
    private fun playAnimation() {
        //Using PropertyValuesHolder to hold X and Y axis scaling options
        val x = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.5f)
        val y = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.5f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            face, x, y)
        animator.duration = 500
        animator.repeatCount = 3
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        getPlayersName()
        playSound()
    }

    /**
     * It plays the game over sound
     */
    private fun playSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.failure)
        mediaPlayer?.start()
    }

    /**
     * This method gets player and location sound to show it on the game over screen
     */
    private fun getPlayersName() {
        val sharePref = PreferenceManager.getDefaultSharedPreferences(this)
        val playerNamePreference = sharePref.getString("name", "")
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("scores", MODE_PRIVATE)
        val playerLoc = sharedPreferences.getString("location", "")
        gameOverTxet.setText(playerNamePreference +" "+ playerLoc+
                " \nYou lost:(" +
                " \nGame Over")
    }


    override fun onPause() {
        super.onPause()
        // releasing the media value resources
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Setting button event actions, so they
     * either start another game or finish the game
     * based on user choice
     */
    private fun setButtonsEventHandelers() {
        playAgain.setOnClickListener { view: View ->
            play()
        }
        exitButton.setOnClickListener { view: View ->
            exitApp()
        }
    }

    /**
     * It goes to GameActivity screen and starts another game
     */
    private fun play() {
        val newIntent = Intent(applicationContext,   GameActivity::class.java)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(newIntent)
        finish()
    }

    private fun exitApp(){
        resetSharedPrefernces()
        //Calling the exit dialog to make sure
        //user wants to exit the game
        ExitDialog.callAlertDialog(this);
    }

    /**
     * It resets the game and player score back to 0 when
     * so the next game starts with right game and player score
     */
    private fun resetSharedPrefernces() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("scores", MODE_PRIVATE)
        val myEdit: SharedPreferences.Editor = sharedPreferences.edit()
        myEdit.putInt("playerScore", 0)
        myEdit.putInt("gameScore", 0)
        myEdit.commit()

        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, true);
    }

}