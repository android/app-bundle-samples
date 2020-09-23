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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette.Swatch
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.samples.dynamicfeatures.state.ColorViewModel
import com.google.android.samples.dynamicfeatures.state.ReviewViewModel
import com.google.android.samples.dynamicfeatures.state.ReviewViewModelProviderFactory
import com.google.android.samples.playcore.picture.R
import com.google.android.samples.playcore.picture.databinding.PaletteBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Fragment displaying palette content background colors in a list.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Keep
class PaletteFragment : Fragment(R.layout.palette) {

    private var binding: PaletteBinding? = null
    private val paletteViewModel by viewModels<PaletteViewModel>()
    private val reviewViewModel by activityViewModels<ReviewViewModel> {
        ReviewViewModelProviderFactory(ReviewManagerFactory.create(requireContext()))
    }
    private val colorViewModel by activityViewModels<ColorViewModel>()

    private val getPicturePreview = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
    ) {
        if (it == null) {
            parentFragmentManager.popBackStack()
        } else {
            paletteViewModel.requestPalette(it)
        }
        resultHandled = true
    }
    private var resultHandled = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return PaletteBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding!!.recyclerView) {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            setHasFixedSize(true)
            adapter = instantiatePaletteAdapter()
        }

        paletteViewModel.swatches.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                Log.w(TAG, "Issue generating Palette, attempting to get new picture.")
                getPicturePreview.launch(null)
            }
        })

        super.onViewCreated(view, savedInstanceState)
    }

    private fun instantiatePaletteAdapter(): PaletteAdapter {
        return PaletteAdapter { color ->
            colorViewModel.backgroundColor.value = color
            parentFragmentManager.popBackStack()
        }.apply {
            if (!paletteViewModel.swatches.value.isNullOrEmpty()) {
                items = paletteViewModel.swatches.value!!
                notifyDataSetChanged()
            }
            paletteViewModel.swatches.observe(viewLifecycleOwner, Observer {
                items = it
                notifyDataSetChanged()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        reviewViewModel.preWarmReview()
        if (!resultHandled) getPicturePreview.launch(null)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

internal class PaletteAdapter(
        val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<PaletteHolder>() {

    var items = emptyList<Swatch>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PaletteHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.color_item, parent, false)
            )

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PaletteHolder, position: Int) {
        with(holder.itemView as Button) {
            val backgroundColor = items[position].rgb
            val bodyTextColor = items[position].bodyTextColor
            setBackgroundColor(backgroundColor)
            setTextColor(bodyTextColor)
            text = "#${Integer.toHexString(backgroundColor).substring(2)}"

            setOnClickListener {
                onColorSelected(backgroundColor)
            }
        }
    }
}

internal class PaletteHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

private const val TAG = "PictureFragment"
