package com.rock_paper_scissor

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.preference.PreferenceManager
import com.zybooks.rock_paper_scissor.R

class WinningScreenActivity : AppCompatActivity() {
    private lateinit var playAgain: Button;
    private lateinit var exit: Button;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_winning_screen)
        playAgain = findViewById<Button>(R.id.playGame)
        exit = findViewById<Button>(R.id.exit)
        setButtonsEventHandelers()
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
        val newIntent = Intent(applicationContext,  GameActivity::class.java)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(newIntent)
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