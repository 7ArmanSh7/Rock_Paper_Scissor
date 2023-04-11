package com.rock_paper_scissor.model

class Player {
    private var name = ""
    private var playerScore = 0
    private var playerChoice = 0

    fun getName(): String {
        return name;
    }

    fun setName(Player:String){
        name =  Player
    }

    fun increasePlayerScore() {
        playerScore++;
    }

    fun getPlayerScore(): Int {
        return playerScore;
    }

    fun resetPlayerScore() {
        playerScore=0
    }

    fun getPlayerChoice(): Int {
        return playerChoice;
    }

    fun setPlayerChoice(choiceNumber:Int) {
        playerChoice = choiceNumber;
    }
}