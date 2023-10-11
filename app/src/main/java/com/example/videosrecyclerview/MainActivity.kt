package com.example.videosrecyclerview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.videosrecyclerview.databinding.ActivityMainBinding
import com.example.videosrecyclerview.databinding.PlaybackControlViewBinding

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val videoList = mutableListOf<Model>()
    val videoAdapter = VideoAdapter()
    private lateinit var playBackBinding: PlaybackControlViewBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        playBackBinding = PlaybackControlViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playBackBinding.play.setOnClickListener {
            playBackBinding.play.setImageResource(R.drawable.pause)
        }
        binding.playerViewRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.playerViewRv.adapter = videoAdapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.playerViewRv)
        videoList.add(Model(R.raw.video, true))
        videoList.add(Model(R.raw.video))
        videoList.add(Model(R.raw.video))
        videoList.add(Model(R.raw.video))
        videoList.add(Model(R.raw.video))

        videoAdapter.submitList(videoList)


        binding.playerViewRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                    if (lastVisiblePosition != RecyclerView.NO_POSITION) {
                        if (videoList[lastVisiblePosition].isCheck == false) {
                            videoList.map {
                                it.isCheck = false
                                it
                            }
                            val item = videoList[lastVisiblePosition]

                            item.isCheck = true
                            videoList[lastVisiblePosition] = item
                            videoAdapter.notifyDataSetChanged()
                        }

                    }

                }
            }

        })
    }


}