package com.rock_paper_scissor.model

import com.zybooks.rock_paper_scissor.R

class GameEngine {
    private var maxVal = 2;
    private var minVal = 0;
    private var gameEngineScore = 0
    private var  gameEngineChoice = -5
    private val compChoiceImagePath:List<Int> = listOf(
        R.drawable.rock,
        R.drawable.paper,
        R.drawable.scissors
    )

    fun increaseGameEngineScore() {
        gameEngineScore++;
    }

    fun resetScore() {
        gameEngineScore=0;
    }

    fun setScore(score:Int) {
        gameEngineScore=score;
    }

    fun getGameEngineScore(): Int {
        return gameEngineScore;
    }

    fun getGameEngineChoice():Int {
        gameEngineChoice = (minVal..maxVal).shuffled().last();
        return gameEngineChoice;
    }

    fun getGameChoiceImagePath(choiceNumber:Int):Int {
        println("The wrong number is" +choiceNumber)
        return compChoiceImagePath[choiceNumber];
    }
}