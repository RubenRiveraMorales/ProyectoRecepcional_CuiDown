package uv.tc.tesisapp.adapter

import uv.tc.tesisapp.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uv.tc.tesisapp.pojo.Video


class VideoAdapter(private val context: Context, private val arrayList: ArrayList<Video>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_videos, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = arrayList[position]
        holder.title.text = video.title
        Glide.with(context).load(video.url).into(holder.imageView)
        holder.itemView.setOnClickListener { onItemClickListener?.onClick(video) }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.list_item_image)
        val title: TextView = itemView.findViewById(R.id.list_item_title)
    }

    interface OnItemClickListener {
        fun onClick(video: Video)
    }
}
