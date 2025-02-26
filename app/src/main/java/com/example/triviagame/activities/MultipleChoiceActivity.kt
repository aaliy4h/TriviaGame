package com.example.triviagame.activities

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.triviagame.R

/**
 * Inherits from GameActivity and provides the multiple choice functionality
 */
class MultipleChoiceActivity : GameActivity() {

    override val layoutId = R.layout.classic_multiple_choice_activity
    private lateinit var radioGroupTop: RadioGroup
    private lateinit var radioGroupBottom: RadioGroup

    /**
     * Sets listeners for both radio groups to ensure only one button can be selected at once
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        radioGroupTop = findViewById(R.id.radio_group_top)
        radioGroupBottom = findViewById(R.id.radio_group_bottom)

        radioGroupTop.setOnCheckedChangeListener { _, _ ->
            if (radioGroupBottom.checkedRadioButtonId != -1) {
                radioGroupBottom.clearCheck()
            }
        }
        radioGroupBottom.setOnCheckedChangeListener { _, _ ->
            if (radioGroupTop.checkedRadioButtonId != -1) {
                radioGroupTop.clearCheck()
            }
        }
    }

    override fun getSelectedRadioGroup(): RadioGroup {
        return if (radioGroupTop.checkedRadioButtonId != -1) {
            radioGroupTop
        } else {
            radioGroupBottom
        }
    }

    override fun getSelectedButton(): RadioButton? {
        val button = getSelectedRadioGroup().checkedRadioButtonId
        val selectedButton = findViewById<RadioButton>(button)
        return selectedButton
    }

}