package com.rock_paper_scissor.model

import android.media.SoundPool

class Game() {
    private var player: Player = Player()
    private var gameEngine: GameEngine = GameEngine()
    private var maxScore:Int = 5

    fun getPlayer(): Player {
        return player;
    }
    fun getGameEngine(): GameEngine {
        return gameEngine;
    }

    fun getMaxScore():Int {
        return maxScore;
    }
    fun setMaxScore(score:Int) {
        maxScore = score
    }
}