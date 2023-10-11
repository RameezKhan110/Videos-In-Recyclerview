package com.example.videosrecyclerview

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.videosrecyclerview.databinding.VideoItemBinding


@UnstableApi
class VideoAdapter : ListAdapter<Model, RecyclerView.ViewHolder>(DiffUtil()) {

    private lateinit var dataSource: RawResourceDataSource


    class DiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<Model>() {
        override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean {
            return oldItem.video == newItem.video
        }

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as VideoAdapterViewHolder).exoPLayer?.stop()
    }

    inner class VideoAdapterViewHolder(private val binding: VideoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var exoPLayer: ExoPlayer? = ExoPlayer.Builder(binding.root.context).build()
        val pauseBtn: ImageView = binding.playerView.findViewById(R.id.pauseBtn)
        val playBtn: ImageView = binding.playBtn.findViewById(R.id.playBtn)
        val seekBar: SeekBar = binding.seekBar
        val playerView = binding.playerView

        init {
            binding.playerView.player = exoPLayer
        }

        fun restartPlayer() {

            exoPLayer?.seekTo(0)
            exoPLayer?.playWhenReady = true
        }

        fun releasePlayer() {
            exoPLayer?.apply {
                playWhenReady = false
                stop()
                release()
            }
            exoPLayer = null
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VideoAdapterViewHolder(
            VideoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val item = getItem(position)
        val viewHolder = (holder as VideoAdapterViewHolder)

        VideoItemBinding.bind(holder.itemView).apply {
            viewHolder.exoPLayer?.apply {

                var isClick = false
                playerView.setOnClickListener {
                    if (!isClick) {
                        seekBar.visibility = View.INVISIBLE
                        pauseBtn.visibility = View.INVISIBLE
                        isClick = true
                    } else {
                        seekBar.visibility = View.VISIBLE
                        pauseBtn.visibility = View.VISIBLE
                        isClick = false
                    }

                }
                val handler = Handler()

                val playerListener = object : Player.Listener {
                    @SuppressLint("SwitchIntDef")
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        when (playbackState) {
                            STATE_ENDED -> {
                                viewHolder.restartPlayer()
                            }

                            STATE_READY -> {
                                Log.d("TAG", "duration: " + duration / 1000)
                                seekBar.max = duration.toInt()
                                playWhenReady = true

                                val updateProgressAction = object : Runnable {
                                    override fun run() {
                                        val progress = currentPosition.toInt()
                                        seekBar.progress = progress
                                        Log.d(
                                            "TAG",
                                            "progress: " + ((currentPosition) / duration).toInt()
                                        )
                                        handler.postDelayed(this, 1)
                                    }
                                }
                                handler.post(updateProgressAction)

                            }
                        }
                    }
                }

                pauseBtn.setOnClickListener {
                    pauseBtn.visibility = View.GONE
                    playBtn.visibility = View.VISIBLE
                    stop()
                }
                playBtn.setOnClickListener {
                    playBtn.visibility = View.GONE
                    pauseBtn.visibility = View.VISIBLE
                    prepare()
                    play()
                }

                dataSource = RawResourceDataSource(holder.itemView.context)
                dataSource.open(
                    DataSpec(
                        RawResourceDataSource.buildRawResourceUri(
                            item.video ?: 0
                        )
                    )
                )
                setMediaSource(getProgressiveMediaSource(dataSource.uri))
                prepare()
                if (item.isCheck == true) {
                    addListener(playerListener)
                    play()
                    seekBar.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            p0: SeekBar?,
                            progress: Int,
                            p2: Boolean
                        ) {

                        }

                        override fun onStartTrackingTouch(p0: SeekBar?) {
                            stop()
                        }

                        override fun onStopTrackingTouch(p0: SeekBar?) {
                            playBtn.visibility = View.GONE
                            pauseBtn.visibility = View.VISIBLE
                            prepare()
                            play()
                            val newPosition = p0?.progress
                            Log.d("TAG", "drag pos: " + p0?.progress)
                            newPosition?.let { seekTo(it.toLong()) }
                        }

                    })
                } else {
                    stop()

                }
            }

        }

    }

    private fun getProgressiveMediaSource(video: Uri?): MediaSource {
        return ProgressiveMediaSource.Factory { dataSource }
            .createMediaSource(MediaItem.fromUri(video!!))
    }
}