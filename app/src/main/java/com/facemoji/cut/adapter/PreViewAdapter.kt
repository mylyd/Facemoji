package com.facemoji.cut.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.facemoji.cut.R
import com.facemoji.cut.network.entity.GIF
import com.facemoji.cut.network.entity.ItemBean

/**
 * @author : ydli
 * @time : 2020/11/20 9:30
 * @description :
 */
class PreViewAdapter(private val context: Context) :
    RecyclerView.Adapter<PreViewAdapter.PreviewHolder>() {
    var items: MutableList<GIF> = mutableListOf()
    var mItem: OnClickItemGIF? = null

    fun upRes(res: MutableList<GIF>) {
        if (res.isEmpty()) return
        items.clear()
        items.addAll(res)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder =
        PreviewHolder(LayoutInflater.from(context).inflate(R.layout.item_preview, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) = holder.bind(position)

    inner class PreviewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image: ImageView = view.findViewById(R.id.item_preview)
        private var glide :RequestManager? = null
        init {
            glide = Glide.with(context)
        }

        fun bind(position: Int) {
            if (items.isEmpty()) return
            val gif = items[position]
            glide?.load(gif.thumbnail)?.placeholder(R.drawable.default_item)?.into(image)
            itemView.setOnClickListener{mItem?.item(position, items[position]) }
        }
    }

    fun setClickItem(item: OnClickItemGIF) {
        this.mItem = item
    }
}


