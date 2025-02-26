package com.example.triviagame.activities

import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.triviagame.R

/**
 * Inherits from GameActivity and provides the true/false functionality
 */
class TrueFalseActivity : GameActivity() {

    override val layoutId = R.layout.classic_multiple_choice_activity
    private lateinit var radioGroup: RadioGroup

    override fun getSelectedRadioGroup(): RadioGroup {
        radioGroup = findViewById(R.id.radio_group)
        return radioGroup
    }

    override fun getSelectedButton(): RadioButton? {
        val button = radioGroup.checkedRadioButtonId
        val selectedButton = findViewById<RadioButton>(button)
        return selectedButton
    }

}