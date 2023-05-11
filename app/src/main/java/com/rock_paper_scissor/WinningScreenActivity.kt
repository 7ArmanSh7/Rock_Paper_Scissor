package com.rock_paper_scissor

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.zybooks.rock_paper_scissor.R

class WinningScreenActivity : AppCompatActivity() {
    private lateinit var playAgain: Button;
    private lateinit var exit: Button;
    private var mediaPlayer: MediaPlayer? = null
    lateinit var cup: ImageView
    lateinit var winningTxet: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_winning_screen)
        playAgain = findViewById<Button>(R.id.playGame)
        exit = findViewById<Button>(R.id.exit)
        winningTxet = findViewById<TextView>(R.id.winnerMessage)
        setButtonsEventHandelers()
        cup = findViewById(R.id.cup)
        playAnimation()
    }

    /**
     * This method playes animatiof of a winner cup rotating
     * -360 degree which takes 3 seconds
     */
    private fun playAnimation() {
        val animator = ObjectAnimator.ofFloat(cup, View.ROTATION,
            -360f, 0f)
        animator.duration = 3000
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        getPlayersName()
        playSound()
    }

    override fun onPause() {
        super.onPause()
        //releasing the media player resources
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun playSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.success)
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
        winningTxet.setText(playerNamePreference +" "+ playerLoc+
                " \nYou are the winner!" +
                " \nCongratulations!")
    }

    private fun setButtonsEventHandelers() {
        playAgain.setOnClickListener { view: View ->
            play()
        }

        exit.setOnClickListener { view: View ->
            exitApp()
        }
    }

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

    override fun onDestroy() {
        super.onDestroy()
        val newIntent = Intent(applicationContext,   GameActivity::class.java)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(newIntent)
        finish()
    }
}