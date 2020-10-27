package com.example.brazedemo

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_borderd_edit_text.*

class BorderdEditTextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borderd_edit_text)

        edit.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                edit.setBackgroundResource(R.drawable.edit_border)
            } else {
                edit.setBackgroundResource(0)
                textInputLayout.isErrorEnabled = false
            }
        }

        editText1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when {
                    s.toString().isEmpty() -> {
                        textInputLayout.error = ""
                        textInputLayout.isErrorEnabled = false
                    }
                    s.toString() != "mayank" -> {
                        textInputLayout.error = "Error will come here!"
                    }
                    else -> {
                        textInputLayout.error = ""
                        textInputLayout.isErrorEnabled = false
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        editText3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when {
                    s.toString().isEmpty() -> {
                        textInputLayout3.boxStrokeColor =
                            Color.parseColor(getString(R.string.purple))
                        error.visibility = View.GONE
                    }
                    s.toString() != "mayank" -> {
                        error.visibility = View.VISIBLE
                        textInputLayout3.boxStrokeColor = Color.parseColor(getString(R.string.red))
                    }
                    else -> {
                        textInputLayout3.boxStrokeColor =
                            Color.parseColor(getString(R.string.purple))
                        error.visibility = View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


    }
}