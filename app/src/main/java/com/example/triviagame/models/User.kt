package com.example.triviagame.models

data class User(val id: Int?, val accountID: String?, val classicPlayed: Int, val timedHighScore: Int,
                val hangmanWon: Int, val correctAnswers: Int, val coins: Int)
