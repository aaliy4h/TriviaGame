package com.example.triviagame.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.triviagame.models.Answer
import com.example.triviagame.models.Pet
import com.example.triviagame.models.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "user.db"
        private const val DATABASE_VERSION = 19
        private const val USERS_TABLE = "users"
        private const val ANSWERS_TABLE = "answers"
        private const val PET_TABLE = "pets"
        private const val COLUMN_ID = "_id"

        // user columns
        private const val ACCOUNT_ID = "account_id"
        private const val CLASSIC_PLAYED = "classic_played"
        private const val TIMED_HIGH_SCORE = "timed_high_score"
        private const val HANGMAN_WON = "hangman_won"
        private const val CORRECT_ANSWERS = "correct_answers"
        private const val COINS = "coins"

        // answer columns
        private const val USER_ID = "user_id"
        private const val QUESTION = "question"
        private const val CORRECT_ANSWER = "correct_answer"
        private const val USER_ANSWER = "user_answer"

        // pet columns
        private const val OWNER_ID = "user_id"
        private const val PET_NAME = "pet_name"
        private const val HUNGER = "hunger"
        private const val ENTERTAINMENT = "entertainment"
        private const val HAPPINESS = "happiness"

        private const val CREATE_USER_TABLE =
            "CREATE TABLE $USERS_TABLE (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$ACCOUNT_ID TEXT," +
                    "$CLASSIC_PLAYED INTEGER," +
                    "$TIMED_HIGH_SCORE INTEGER," +
                    "$HANGMAN_WON INTEGER," +
                    "$CORRECT_ANSWERS INTEGER," +
                    "$COINS INTEGER)"

        private const val CREATE_ANSWER_TABLE =
            "CREATE TABLE $ANSWERS_TABLE (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$USER_ID INTEGER," +
                    "$QUESTION TEXT," +
                    "$CORRECT_ANSWER TEXT," +
                    "$USER_ANSWER TEXT," +
                    "FOREIGN KEY($USER_ID) REFERENCES $USERS_TABLE($COLUMN_ID))"

        private const val CREATE_PET_TABLE =
            "CREATE TABLE $PET_TABLE (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$OWNER_ID INTEGER," +
                    "$PET_NAME TEXT," +
                    "$HUNGER INTEGER," +
                    "$ENTERTAINMENT INTEGER," +
                    "$HAPPINESS INTEGER," +
                    "FOREIGN KEY($OWNER_ID) REFERENCES $USERS_TABLE($COLUMN_ID))"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USER_TABLE)
        db.execSQL(CREATE_ANSWER_TABLE)
        db.execSQL(CREATE_PET_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $USERS_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $ANSWERS_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $PET_TABLE")
        onCreate(db)
    }

    // user CRUD
    fun addUser(user: User) {
        val values = ContentValues()
        values.put(ACCOUNT_ID, user.accountID)
        values.put(CLASSIC_PLAYED, user.classicPlayed)
        values.put(TIMED_HIGH_SCORE, user.timedHighScore)
        values.put(HANGMAN_WON, user.hangmanWon)
        values.put(CORRECT_ANSWERS, user.correctAnswers)
        values.put(COINS, user.coins)
        val db = this.writableDatabase
        db.insert(USERS_TABLE, null, values)
        db.close()
    }

    fun getUserID(accountID: String): Int? {
        val sql = "select $COLUMN_ID from $USERS_TABLE where $ACCOUNT_ID = ?"
        val db = this.readableDatabase
        var userID: Int? = null
        val cursor = db.rawQuery(sql, arrayOf(accountID))
        if (cursor.moveToFirst()) {
            userID = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        }
        cursor.close()
        db.close()
        return userID
    }

    fun updateUser(user: User) {
        val values = ContentValues()
        values.put(ACCOUNT_ID, user.accountID)
        values.put(CLASSIC_PLAYED, user.classicPlayed)
        values.put(TIMED_HIGH_SCORE, user.timedHighScore)
        values.put(HANGMAN_WON, user.hangmanWon)
        values.put(CORRECT_ANSWERS, user.correctAnswers)
        values.put(COINS, user.coins)
        val db = this.writableDatabase
        db.update(
            USERS_TABLE,
            values,
            "$COLUMN_ID = ?",
            arrayOf(user.id.toString())
        )
        db.close()
    }

    fun updateUserCoins(userID: Int, coins: Int) {
        val values = ContentValues()
        values.put(COINS, coins)
        val db = this.writableDatabase
        db.update(
            USERS_TABLE,
            values,
            "$COLUMN_ID = ?",
            arrayOf(userID.toString())
        )
        db.close()
    }

    fun deleteUser(userID: Int) {
        val db = this.writableDatabase
        db.delete(USERS_TABLE, "$COLUMN_ID = ?", arrayOf(userID.toString()))
        db.close()
    }

    // answer CRUD
    fun addAnswer(answer: Answer) {
        val values = ContentValues()
        values.put(USER_ID, answer.userID)
        values.put(QUESTION, answer.question)
        values.put(CORRECT_ANSWER, answer.correctAnswer)
        values.put(USER_ANSWER, answer.userAnswer)
        val db = this.writableDatabase
        db.insert(ANSWERS_TABLE, null, values)
        db.close()
    }

    fun updateAnswer(answer: Answer) {
        val values = ContentValues()
        values.put(USER_ID, answer.userID)
        values.put(QUESTION, answer.question)
        values.put(CORRECT_ANSWER, answer.correctAnswer)
        values.put(USER_ANSWER, answer.userAnswer)
        val db = this.writableDatabase
        db.update(
            ANSWERS_TABLE,
            values,
            "$COLUMN_ID = ?",
            arrayOf(answer.id.toString())
        )
        db.close()
    }

    fun deleteAnswer(answerID: Int) {
        val db = this.writableDatabase
        db.delete(ANSWERS_TABLE, "$COLUMN_ID = ?", arrayOf(answerID.toString()))
        db.close()
    }

    // pet CRUD
    fun addPet(pet: Pet) {
        val values = ContentValues()
        values.put(OWNER_ID, pet.userID)
        values.put(PET_NAME, pet.name)
        values.put(HUNGER, pet.hunger)
        values.put(ENTERTAINMENT, pet.entertainment)
        values.put(HAPPINESS, pet.happiness)
        val db = this.writableDatabase
        db.insert(PET_TABLE, null, values)
        db.close()
    }

    fun updatePet(pet: Pet) {
        val values = ContentValues()
        values.put(OWNER_ID, pet.userID)
        values.put(PET_NAME, pet.name)
        values.put(HUNGER, pet.hunger)
        values.put(ENTERTAINMENT, pet.entertainment)
        values.put(HAPPINESS, pet.happiness)
        val db = this.writableDatabase
        db.update(
            PET_TABLE,
            values,
            "$COLUMN_ID = ?",
            arrayOf(pet.id.toString())
        )
        db.close()
    }

    fun deletePet(petID: Int) {
        val db = this.writableDatabase
        db.delete(PET_TABLE, "$COLUMN_ID = ?", arrayOf(petID.toString()))
        db.close()
    }

    // functions to retrieve data
    fun getUserAnswers(userID: Int): MutableList<Answer> {
        val sql = "select * from $ANSWERS_TABLE where $USER_ID = ?"
        val db = this.readableDatabase
        val storeAnswers = arrayListOf<Answer>()
        val cursor = db.rawQuery(sql, arrayOf(userID.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = Integer.parseInt(cursor.getString(0))
                val question = cursor.getString(2)
                val correctAnswer = cursor.getString(3)
                val userAnswer = cursor.getString(4)
                storeAnswers.add(Answer(id, userID, question, correctAnswer, userAnswer))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return storeAnswers
    }

    fun getUserCoins(userID: Int): String {
        val sql = "select $COINS from $USERS_TABLE where $COLUMN_ID = ?"
        val db = this.readableDatabase
        var coins = ""
        val cursor = db.rawQuery(sql, arrayOf(userID.toString()))

        if (cursor.moveToFirst()) {
            coins = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return coins
    }

    fun getUserStatistics(userID: Int): MutableList<Int> {
        val sql = "select * from $USERS_TABLE where $COLUMN_ID = ?"
        val db = this.readableDatabase
        val storeStats = arrayListOf<Int>()
        val cursor = db.rawQuery(sql, arrayOf(userID.toString()))
        if (cursor.moveToFirst()) {
            do {
                val classicPlayed = Integer.parseInt(cursor.getString(2))
                val timedHighScore = Integer.parseInt(cursor.getString(3))
                val hangmanWon = Integer.parseInt(cursor.getString(4))
                val correctAnswers = Integer.parseInt(cursor.getString(5))
                val coins = Integer.parseInt(cursor.getString(6))
                storeStats.add(classicPlayed)
                storeStats.add(timedHighScore)
                storeStats.add(hangmanWon)
                storeStats.add(correctAnswers)
                storeStats.add(coins)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return storeStats

    }

    fun getPetLevels(userID: Int): MutableList<Int> {
        val sql = "select * from $PET_TABLE where $OWNER_ID = ?"
        val db = this.readableDatabase
        val storeLevels = arrayListOf<Int>()
        val cursor = db.rawQuery(sql, arrayOf(userID.toString()))
        if (cursor.moveToFirst()) {
            do {
                val hunger = Integer.parseInt(cursor.getString(3))
                val entertainment = Integer.parseInt(cursor.getString(4))
                val happiness = Integer.parseInt(cursor.getString(5))
                storeLevels.add(hunger)
                storeLevels.add(entertainment)
                storeLevels.add(happiness)
            } while (cursor.moveToNext())

        }
        cursor.close()
        db.close()
        return storeLevels
    }

    fun getPetID(userID: Int): Int {
        val sql = "select $COLUMN_ID from $PET_TABLE where $OWNER_ID = ?"
        val db = this.readableDatabase
        var petID = 0
        val cursor = db.rawQuery(sql, arrayOf(userID.toString()))
        if (cursor.moveToFirst()) {
            petID = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        }
        cursor.close()
        db.close()
        return petID
    }


}