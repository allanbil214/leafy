package com.allanbil214.leafyapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.allanbil214.leafyapp.databinding.ItemForecastBinding
import kotlin.math.roundToInt

class ForecastAdapter : ListAdapter<ForecastItem, ForecastAdapter.ForecastViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ForecastViewHolder(
        private val binding: ItemForecastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ForecastItem) {
            binding.apply {
                tvForecastDay.text = item.day
                tvForecastTemp.text = "${item.temperature.roundToInt()}°C"
                tvForecastDescription.text = item.description

                Glide.with(itemView)
                    .load(item.iconUrl)
                    .into(ivForecastIcon)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ForecastItem>() {
            override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
                return oldItem.day == newItem.day
            }

            override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}