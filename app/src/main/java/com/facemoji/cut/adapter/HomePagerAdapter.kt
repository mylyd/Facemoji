package com.facemoji.cut.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.facemoji.cut.R
import com.facemoji.cut.network.entity.AddPreview
import com.facemoji.cut.network.entity.ItemBean
import com.facemoji.cut.utils.AddPreviewUtils
import com.facemoji.cut.utils.Util

/**
 * @author : ydli
 * @time : 2020/11/18 14:31
 * @description :
 */
class HomePagerAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val itemVip = 0
    private val itemAds = 1
    private val itemLayout = 2
    private var items: MutableList<ItemBean> = mutableListOf()
    private var mItem: OnClickItem? = null
    private var addWhatApps: MutableList<AddPreview>? = null

    fun upRes(res: MutableList<ItemBean>, add: MutableList<AddPreview>? = null) {
        if (res.isEmpty()) return
        addWhatApps = add
        items.clear()
        items.addAll(res)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> { itemVip }
            3,6,9 ->{ itemAds }
            else -> { itemLayout }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType){
            itemVip ->{
                return VipView(
                    LayoutInflater.from(context).inflate(R.layout.item_vip, parent, false)
                )
            }
            itemAds ->{
                return AdsView(
                    LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false)
                )
            }
            else ->{
                return FacemojiView(
                    LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false)
                )
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FacemojiView -> {
                holder.bind(position)
            }
            is VipView -> {
                holder.bind(position)
            }
            is AdsView -> {
                holder.bind(position)
            }
        }
    }

    inner class FacemojiView(view: View) : RecyclerView.ViewHolder(view) {
        private val textTitle: TextView = view.findViewById(R.id.item_title)
        private val imageDownload: ImageView = view.findViewById(R.id.item_download)
        private val itemImage1: ImageView = view.findViewById(R.id.item_image_1)
        private val itemImage2: ImageView = view.findViewById(R.id.item_image_2)
        private val itemImage3: ImageView = view.findViewById(R.id.item_image_3)
        private val itemImage4: ImageView = view.findViewById(R.id.item_image_4)
        private val itemImage5: ImageView = view.findViewById(R.id.item_image_5)
        private val layout: LinearLayout = view.findViewById(R.id.layout_item)
        private var glide: RequestManager? = null

        init {
            glide = Glide.with(context)
        }

        @SuppressLint("CheckResult")
        fun bind(position: Int) {
            if (items.isEmpty()) return
            layout.setOnClickListener { mItem?.item(position, items[position]) }
            val itemBean: ItemBean = items[position]
            textTitle.text = itemBean.name
            glide!!.load(itemBean.items?.get(0)?.thumbnail).placeholder(R.drawable.default_item)
                .into(itemImage1)
            glide!!.load(itemBean.items?.get(1)?.thumbnail).placeholder(R.drawable.default_item)
                .into(itemImage2)
            glide!!.load(itemBean.items?.get(2)?.thumbnail).placeholder(R.drawable.default_item)
                .into(itemImage3)
            glide!!.load(itemBean.items?.get(3)?.thumbnail).placeholder(R.drawable.default_item)
                .into(itemImage4)
            glide!!.load(itemBean.items?.get(4)?.thumbnail).placeholder(R.drawable.default_item)
                .into(itemImage5)
            if (addWhatApps == null) return
            if (addWhatApps?.isNotEmpty()!!) {
                if (AddPreviewUtils.contains(addWhatApps!!, itemBean.name!!)) {
                    imageDownload.setImageResource(R.mipmap.item_ok)
                } else {
                    imageDownload.setImageResource(R.mipmap.item_download)
                }
            } else {
                imageDownload.setImageResource(R.mipmap.item_download)
            }
        }
    }

    inner class VipView(view: View) : RecyclerView.ViewHolder(view) {
        private val textTitle: ImageView = view.findViewById(R.id.image_vip)

        @SuppressLint("CheckResult")
        fun bind(position: Int) {
            if (items.isEmpty()) return
            textTitle.setOnClickListener { mItem?.item(position, items[position]) }
        }
    }

    inner class AdsView(view: View) : RecyclerView.ViewHolder(view) {
        private val textTitle: TextView = view.findViewById(R.id.item_title)
        private val imageDownload: ImageView = view.findViewById(R.id.item_download)
        private val itemImage1: ImageView = view.findViewById(R.id.item_image_1)
        private val itemImage2: ImageView = view.findViewById(R.id.item_image_2)
        private val itemImage3: ImageView = view.findViewById(R.id.item_image_3)
        private val itemImage4: ImageView = view.findViewById(R.id.item_image_4)
        private val itemImage5: ImageView = view.findViewById(R.id.item_image_5)
        private val ads: ImageView = view.findViewById(R.id.image_ads)
        private val layout: LinearLayout = view.findViewById(R.id.layout_item)
        private var glide: RequestManager? = null

        init {
            glide = Glide.with(context)
        }

        @SuppressLint("CheckResult", "SetTextI18n")
        fun bind(position: Int) {
            when(position){
                3 -> {
                    textTitle.text = "Smilemoji"
                    glide!!.load(R.drawable.smilemoji_mip_1).into(itemImage1)
                    glide!!.load(R.drawable.smilemoji_mip_2).into(itemImage2)
                    glide!!.load(R.drawable.smilemoji_mip_3).into(itemImage3)
                    glide!!.load(R.drawable.smilemoji_mip_4).into(itemImage4)
                    glide!!.load(R.drawable.smilemoji_mip_5).into(itemImage5)
                }
                6 -> {
                    textTitle.text = "Lovemoji"
                    glide!!.load(R.drawable.lovemoji_mip_1).into(itemImage1)
                    glide!!.load(R.drawable.lovemoji_mip_2).into(itemImage2)
                    glide!!.load(R.drawable.lovemoji_mip_3).into(itemImage3)
                    glide!!.load(R.drawable.lovemoji_mip_4).into(itemImage4)
                    glide!!.load(R.drawable.lovemoji_mip_5).into(itemImage5)
                }
                9 -> {
                    textTitle.text = "Vemoji"
                    glide!!.load(R.drawable.vemoji_item_1).into(itemImage1)
                    glide!!.load(R.drawable.vemoji_item_2).into(itemImage2)
                    glide!!.load(R.drawable.vemoji_item_3).into(itemImage3)
                    glide!!.load(R.drawable.vemoji_item_4).into(itemImage4)
                    glide!!.load(R.drawable.vemoji_item_5).into(itemImage5)
                }
            }

            imageDownload.setImageResource(R.mipmap.item_start_up)
            ads.visibility = View.VISIBLE
            layout.setOnClickListener { mItem?.item(position, items[position]) }
        }
    }

    fun setClickItem(item: OnClickItem) {
        this.mItem = item
    }
}