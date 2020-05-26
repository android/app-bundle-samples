/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.dynamicfeatures.ondemand

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.palette.graphics.Palette.Swatch
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.samples.dynamicfeatures.state.ColorSource
import com.google.android.samples.playcore.picture.R
import com.google.android.samples.playcore.picture.databinding.PaletteBinding

/**
 * Fragment displaying palette content background colors in a list.
 */
class PaletteFragment : Fragment(R.layout.palette) {

    private var binding: PaletteBinding? = null
    // Using activityViewModels here as Palette is passed into another fragment once it's generated.
    private val viewModel by activityViewModels<PaletteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PaletteBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding!!.recyclerView) {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            setHasFixedSize(true)
            adapter = PaletteAdapter().also {
                if (!viewModel.swatches.value.isNullOrEmpty()) {
                    it.items = viewModel.swatches.value!!
                    it.notifyDataSetChanged()
                }
                viewModel.swatches.observe(viewLifecycleOwner) { swatchList ->
                    it.items = swatchList
                    it.notifyDataSetChanged()
                }
            }
            ColorSource.colorConsumed.observe(viewLifecycleOwner) { consumed ->
                consumed.getContentIfNotHandled()?.also {
                    if (!it) requireActivity().finish()
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

internal class PaletteAdapter :
    RecyclerView.Adapter<PaletteHolder>() {

    var items = emptyList<Swatch>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PaletteHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.color_item, parent, false)
        )

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PaletteHolder, position: Int) {
        with(holder.itemView as Button) {
            val bodyTextColor = items[position].bodyTextColor
            val color = items[position].rgb
            setTextColor(bodyTextColor)
            setBackgroundColor(color)
            text = "#${Integer.toHexString(color).substring(2)}"

            setOnClickListener {
                ColorSource.backgroundColor = color
                ColorSource.textColor = bodyTextColor
            }
        }
    }
}

internal class PaletteHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
