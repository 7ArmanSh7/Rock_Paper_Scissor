package com.rock_paper_scissor
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.rock_paper_scissor.model.Game
import com.rock_paper_scissor.model.GameEngine
import com.rock_paper_scissor.model.Player
import com.zybooks.rock_paper_scissor.R


class GameActivity : AppCompatActivity(){
    private lateinit var playerScoreText:TextView
    private lateinit var gameEngineScoreText:TextView
    private lateinit var game: Game
    private lateinit var rockImage:ImageView
    private lateinit var paperImage:ImageView
    private lateinit var scissorsImage:ImageView
    private lateinit var gameChoiceImage:ImageView
    private lateinit var gobutton: Button
    private lateinit var exitButton: Button
    private lateinit var buttons:List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }

    override fun onPause() {
        super.onPause()
        // Storing current player and gameEngine score into SharedPreferences
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("scores", MODE_PRIVATE)
        val gameEdit: SharedPreferences.Editor = sharedPreferences.edit()
        // Storing the player score and game engine score
        // to update the values when player comes back to game activity
        // from settings page
        gameEdit.putInt("playerScore", game.getPlayer().getPlayerScore())
        gameEdit.putInt("gameScore", game.getGameEngine().getGameEngineScore())
        gameEdit.commit()
    }

    override fun onResume() {
        super.onResume()
        rockImage = findViewById(R.id.rock)
        paperImage = findViewById(R.id.paper)
        scissorsImage = findViewById(R.id.scissors)
        gameChoiceImage = findViewById(R.id.imageView4)
        gobutton = findViewById(R.id.button)
        exitButton = findViewById(R.id.exitGame)
        buttons = listOf(gobutton,exitButton)
        //Registering goButton for context menue
        registerForContextMenu(gobutton)

        // Retrieving the value using its keys the file name
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("scores", MODE_PRIVATE)
        val gameScore = sharedPreferences.getInt("gameScore", 0)
        val playerScore = sharedPreferences.getInt("playerScore", 0)

        game = Game()
        game.getPlayer().setPlayerChoice(playerScore)
        game.getGameEngine().setScore(gameScore)
        //updating score
        updateScoreBoards()
        //Setting image listeners
        setImagesEventHandelers()
        //Setting button event listeners
        setButtonsEventHandelers()
        //Set the right preferences when
        //user come back to the game activity
        settingsPreferencesChanged()
    }

    /**
     * It updates player and game scores
     */
    private fun updateScoreBoards() {
        playerScoreText = findViewById(R.id.playerScore)
        gameEngineScoreText = findViewById(R.id.gameEngineScore)
        playerScoreText.setText(game.getPlayer().getName()+" score: "+ game.getPlayer().getPlayerScore())
        gameEngineScoreText.setText(getResources().getString(R.string.game_engine_score) + game.getGameEngine().getGameEngineScore())
    }

    /**
     * Setting the listeners for go and exit Buttons
     */
    private fun setButtonsEventHandelers() {
        gobutton.setOnClickListener { view: View ->
            calculateGameScore(gameChoiceImage)
            playerScoreText.setText(game.getPlayer().getName()+
                    " score: " + game.getPlayer().getPlayerScore().toString())
            gameEngineScoreText.setText("Game Engine score: " + game.getGameEngine().getGameEngineScore().toString())
        }

        exitButton.setOnClickListener{ view: View ->
            //Calling the exit dialog to make sure
            //user wants to exit the game
            ExitDialog.callAlertDialog(this)
            //Resetting player score and game score sharedPreferences
            resetSharedPrefernces()
        }
    }

    /**
     * Setting the preferences in the game settings page
     * to the values decided by the player
     */
    private fun settingsPreferencesChanged() {
        val sharePref = PreferenceManager.getDefaultSharedPreferences(this)
        val playerNamePreference = sharePref.getString("name", "")
        val gameMaxScore = sharePref.getInt("score",game.getMaxScore() )
        if(gameMaxScore != game.getMaxScore() ||
            !playerNamePreference.equals(game.getPlayer().getName())){
            game.setMaxScore(gameMaxScore)
            game.getPlayer().setName(playerNamePreference.toString())
            setPlayerScoreCard(game.getPlayer().getName())
        }

    }

    /**
     * onCreateContextMenu excutes and inflate the context menu
     * when player long press the go botton
     */
    override fun onCreateContextMenu(menu: ContextMenu?,
                                     v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    /**
     * This method Either reset the game or show us the game engine score
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reset -> {
                game.getPlayer().resetPlayerScore()
                game.getGameEngine().setScore(0)
                gameChoiceImage.setImageResource(0)
                playerScoreText.setText(game.getPlayer().getName() + " score: "+ game.getPlayer().getPlayerScore())
                gameEngineScoreText.setText(getResources().getString(R.string.game_engine_score) + game.getGameEngine().getGameEngineScore())
                true
            }
            R.id.go -> {
                gobutton.performClick()
                true
            }
            else -> super.onContextItemSelected(item)
        }
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

        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, true)
    }

    /**
     * It only changes player Score text view
     */
    fun setPlayerScoreCard(name:String){
        playerScoreText.setText(name+ " score: "+ game.getPlayer().getPlayerScore())
    }

    /**
     * Setting the event listeners needed for imageViews
     */
    private fun setImagesEventHandelers() {
        var player: Player = game.getPlayer()

        rockImage.setOnClickListener { view: View ->
            //Reseting game Engine Image
            resetGameChoiceImage()
            gameChoiceImage.setImageResource(0)
            //Clear the background selection of other images
            setBackBackground(paperImage)
            setBackBackground(scissorsImage)
            setPlayerChoice(game, 0)
            setSelectedChoiceBackground(view)
        }
        paperImage.setOnClickListener { view: View ->
            //Setting the player choice
            setPlayerChoice(game, 1)
            //Reseting game Engine Image
            resetGameChoiceImage()
            //Clear the background selection of other images
            setBackBackground(rockImage)
            setBackBackground(scissorsImage)
            setSelectedChoiceBackground(view)
        }

        scissorsImage.setOnClickListener { view: View ->
            //Setting the player choice
            setPlayerChoice(game, 2)
            //Resetting game Engine Image
            resetGameChoiceImage()
            //Clear the background selection of other images
            setBackBackground(rockImage)
            setBackBackground(paperImage)
            //Changing the background of current choice to green
            setSelectedChoiceBackground(view)

        }
    }

    /**
     * It resers game engine choice image
     */
    private fun resetGameChoiceImage() {
        gameChoiceImage.setImageResource(0)
    }


    private fun setPlayerChoice(game: Game, playerChoiceNum: Int) {
        game.getPlayer().setPlayerChoice(playerChoiceNum)
    }

    /**
     * It sets the background color of the player selected choice to green
     */
    fun setSelectedChoiceBackground(view: View) {
        view.setBackgroundColor(Color.parseColor(getResources().getString(R.string.green_background)))
    }

    /**
     * It resets the background image on other choices(Rock, paper or scissors icon)
     * to white when player selects one of them
     */
    fun setBackBackground(view: View) {
        val viewBackground = view.getBackground()
        if(viewBackground == null)
            return
        val imageColor = viewBackground as ColorDrawable
        val colorValue = imageColor.color
        if(colorValue == Color.parseColor(getResources().getString(R.string.green_background)) ){
            view.setBackgroundColor(Color.parseColor(getResources().getString(R.string.white_background)))
        }
    }

    fun calculateGameScore(computerChoice: ImageView){
        val player: Player = game.getPlayer()
        val gameEngine: GameEngine = game.getGameEngine()
        val gameChoice:Int = gameEngine.getGameEngineChoice()
        val gameChoiceImage = gameEngine.getGameChoiceImagePath(gameChoice)
        computerChoice.setImageResource(gameChoiceImage)
        var playerChoice:Int = player.getPlayerChoice()

        when (playerChoice) {
            0 -> {
                if(gameChoice == 1){
                    gameEngine.increaseGameEngineScore()
                }
                if(gameChoice == 2){
                    player.increasePlayerScore()
                }
            }
            1-> {
                if(gameChoice == 0){
                    player.increasePlayerScore()
                }
                if(gameChoice == 2){
                    gameEngine.increaseGameEngineScore()
                }
            }
            2 ->{
                if(gameChoice == 0){
                    gameEngine.increaseGameEngineScore()
                }
                if(gameChoice == 1){
                    player.increasePlayerScore()
                }
            }
        }
        checkGameOver()
    }

    /**
     * Check if either Player or Game Engine won the game
     */
    private fun checkGameOver() {
        val player: Player = game.getPlayer()
        val gameEngine: GameEngine = game.getGameEngine()
        if(gameEngine.getGameEngineScore()>=game.getMaxScore()){
            game.getGameEngine().setScore(0)
            game.getPlayer().setPlayerChoice(0)
            val newIntent = Intent(applicationContext,   GameOverActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(newIntent)
            finish()
        }
        else if(player.getPlayerScore()>=game.getMaxScore()){
            game.getGameEngine().setScore(0)
            game.getPlayer().setPlayerChoice(0)
            val newIntent = Intent(applicationContext,   WinningScreenActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(newIntent)
            finish()
        }
    }

    /**
     * The onCreateOptionsMenu() menu inflates the game menu
     * resource and settings icon apears in the app bar.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game_menu, menu)
        return true
    }

    /**
     * It inflates the setting screen when player chose to see
     * the settings page
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}

