package com.rock_paper_scissor
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import com.rock_paper_scissor.model.Game
import com.rock_paper_scissor.model.GameEngine
import com.rock_paper_scissor.model.Player
import com.zybooks.rock_paper_scissor.R
import java.util.*


class GameActivity : AppCompatActivity(){
    private lateinit var playerScoreText:TextView
    private lateinit var gameEngineScoreText:TextView
    private lateinit var locationText:TextView
    private lateinit var game: Game
    private lateinit var rockImage:ImageView
    private lateinit var paperImage:ImageView
    private lateinit var scissorsImage:ImageView
    private lateinit var gameChoiceImage:ImageView
    private lateinit var gobutton: Button
    private lateinit var exitButton: Button
    private lateinit var setLocationBtn: Button
    private lateinit var buttons:List<Button>
    private lateinit var locationManager:LocationManager
    private var soundMedia: MediaPlayer? = null
    private lateinit var playbutton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private var shouldPlay:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }

    override fun onPause() {
        super.onPause()
        soundMedia?.stop()
        soundMedia?.release()
        soundMedia = null
        // Storing current player and gameEngine score into SharedPreferences
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("scores", MODE_PRIVATE)
        val gameEdit: SharedPreferences.Editor = sharedPreferences.edit()
        // Storing the player score and game engine score
        // to update the values when player comes back to game activity
        // from settings page
        gameEdit.putInt("playerScore", game.getPlayer().getPlayerScore())
        gameEdit.putInt("gameScore", game.getGameEngine().getGameEngineScore())
        gameEdit.putString("location", game.getPlayer().getPlayerLocation())
        gameEdit.commit()
    }

    override fun onResume() {
        super.onResume()
        soundMedia = MediaPlayer.create(this, R.raw.media)

        rockImage = findViewById(R.id.rock)
        paperImage = findViewById(R.id.paper)
        scissorsImage = findViewById(R.id.scissors)
        gameChoiceImage = findViewById(R.id.imageView4)
        gobutton = findViewById(R.id.button)
        exitButton = findViewById(R.id.exitGame)
        playbutton = findViewById(R.id.play)
        stopButton = findViewById(R.id.stop)
        pauseButton = findViewById(R.id.pause)
        exitButton = findViewById(R.id.exitGame)
        setLocationBtn = findViewById(R.id.setLocation)
        buttons = listOf(gobutton,exitButton,setLocationBtn)
        //Registering goButton for context menue
        registerForContextMenu(gobutton)

        // Retrieving the value using its keys the file name
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("scores", MODE_PRIVATE)
        val gameScore = sharedPreferences.getInt("gameScore", 0)
        val playerScore = sharedPreferences.getInt("playerScore", 0)
        val playerLoc = sharedPreferences.getString("location", "")



        game = Game();
        game.getPlayer().setPlayerScore(playerScore)
        game.getGameEngine().setScore(gameScore)
        //updating score
        updateScoreBoards(playerLoc)
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
    private fun updateScoreBoards(playerLoc: String?) {
        playerScoreText = findViewById(R.id.playerScore)
        gameEngineScoreText = findViewById(R.id.gameEngineScore)
        locationText = findViewById(R.id.location)
        playerScoreText.setText(game.getPlayer().getName()+" score: "+ game.getPlayer().getPlayerScore())
        gameEngineScoreText.setText(getResources().getString(R.string.game_engine_score) + game.getGameEngine().getGameEngineScore())
        if(playerLoc!=null && playerLoc.length >0){
            locationText.setText(playerLoc)
        }
        else{
            locationText.setText("From: "+playerLoc)
        }
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

        setLocationBtn.setOnClickListener{ view: View ->
            //Calling the exit dialog to make sure
            //user wants to exit the game
            getLocation()
        }

        playbutton.setOnClickListener{ view: View ->
            //Plays the sound
            playSound()
        }

        pauseButton.setOnClickListener{ view: View ->
            //Pause the sound
            pauseSound()
        }

        stopButton.setOnClickListener{ view: View ->
            //Stop the sound
            stopSound()
        }
    }

    /**
     * This method pause the sound
     */
    private fun pauseSound() {
        soundMedia?.pause()
    }

    /**
     * This method stops the sound
     */
    private fun stopSound() {
        soundMedia?.stop()
        soundMedia = MediaPlayer.create(this, R.raw.media)
    }

    /***
     * Tt sets the sound when it's apporopriate
     * Sets looping tp true
     * and plays the song
     */
    private fun playSound() {
        if(soundMedia==null){
            soundMedia = MediaPlayer.create(this, R.raw.media)
            soundMedia?.setLooping(true)
            soundMedia?.start()
        }
        else{
            soundMedia?.setLooping(true)
            soundMedia?.start()
        }
    }


    /**
     * This method checks for permision
     * If it's mot given, it regques give
     * and when it's granted it finds the location coordinates
     * Using the coordinates and Geocoder library it finds the user address
     * and sets the location textView to true
     */
    private fun getLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        client.lastLocation
            .addOnSuccessListener(this) { location ->
                if(location!=null){
                    var latitude = location.latitude
                    var longitude = location.longitude
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val geocodeListener = @RequiresApi(33) object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            //The address is a list, we want the admin area
                            // and country code to show up on the screen
                            val provience = addresses!![0].adminArea
                            val countryName = addresses!![0].countryCode
                            locationText.setText("From: " + provience +","+ countryName)
                            // Setting the location textview to the user location
                            game.getPlayer().setPlayerLocation("From: " + provience +","+ countryName)
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 33) {
                        geocoder.getFromLocation(latitude, longitude, 10, geocodeListener)
                    } else {
                        var addresses = geocoder.getFromLocation(latitude, longitude, 10)
                        val cityName = addresses!![0].adminArea
                        val countryName = addresses!![0].countryCode
                        locationText.setText("From: " + cityName +","+ countryName)
                        game.getPlayer().setPlayerLocation("From: " + cityName +","+ countryName)
                    }
                }
            }


    }

    val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // App can request location
            getLocation()
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

    /**
     * It calculates the player and game enginer score based on their current choice
     */
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
            game.getGameEngine().resetScore()
            game.getPlayer().resetPlayerScore()
            val newIntent = Intent(applicationContext,   GameOverActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(newIntent)
            finish()
        }
        else if(player.getPlayerScore()>=game.getMaxScore()){
            game.getGameEngine().resetScore()
            game.getPlayer().resetPlayerScore()
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

