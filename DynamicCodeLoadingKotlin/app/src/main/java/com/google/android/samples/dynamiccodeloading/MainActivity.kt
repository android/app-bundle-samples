/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.google.android.samples.dynamiccodeloading

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

/**
 * The single, main activity of this sample.
 *
 * Initializes the view and creates the ViewModel.
 * This is shared across the 3 flavors,
 * that's why we have to keep it clear from any Dagger bits for simplicity.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var counterText: TextView
    private lateinit var incrementButton: Button
    private lateinit var saveText: TextView
    private lateinit var saveButton: Button

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterText = findViewById(R.id.counterText)
        incrementButton = findViewById(R.id.incrementButton)
        saveText = findViewById(R.id.saveText)
        saveButton = findViewById(R.id.saveButton)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        incrementButton.setOnClickListener {
            viewModel.incrementCounter()
        }

        saveButton.setOnClickListener {
            viewModel.saveCounter()
        }

        viewModel.counter.observe(this, Observer {
            counterText.text = (it ?: 0).toString()
        })

        viewModel.loadCounter()
    }
}
