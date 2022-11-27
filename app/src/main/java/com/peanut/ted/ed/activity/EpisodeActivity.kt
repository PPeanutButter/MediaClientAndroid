package com.peanut.ted.ed.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.peanut.sdk.miuidialog.MIUIDialog
import com.peanut.sdk.petlin.Extend.describeAsFileSize
import com.peanut.sdk.petlin.Extend.isLightColor
import com.peanut.sdk.petlin.Extend.toast
import com.peanut.ted.ed.R
import com.peanut.ted.ed.adapter.AttachAdapter
import com.peanut.ted.ed.adapter.EpisodeAdapter
import com.peanut.ted.ed.data.Episode
import com.peanut.ted.ed.databinding.ActivityDetailBinding
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.http
import com.peanut.ted.ed.viewmodel.ViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class EpisodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var album: String
    private var launchTime: Long = 0L
    private var initialized = false
    private var attachAdapter: AttachAdapter? = null
    private var episodeAdapter:EpisodeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setOnApplyWindowInsetsListener { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val statusBar = insets.getInsets(WindowInsets.Type.statusBars())
                val params = (binding.cover.layoutParams as ViewGroup.MarginLayoutParams)
                params.topMargin = statusBar.top + 30
                binding.cover.layoutParams = params
                val height = (binding.toolbar.layoutParams as ViewGroup.LayoutParams)
                height.height = statusBar.top + 30
                binding.toolbar.layoutParams = height
            }
            insets
        }
        album = (intent.getStringExtra("ALBUM") ?: "错误").also {
            binding.toolbarLayout.title = " "
        }
        binding.cover.setImageDrawable(ViewModel.MainActivity2DetailActivityImage)
        binding.cover.setOnClickListener {
            if (attachAdapter?.itemCount.greatThen(0)){
                MIUIDialog(this).show{
                    title(text = "下载附件")
                    customView(viewRes = R.layout.attachment_layout) {
                        val attachment = it.findViewById<RecyclerView>(R.id.attachment)
                        if (attachment.adapter == null)
                            attachment.adapter = attachAdapter
                    }
                }
            }else{
                "没有附件".toast(this)
            }
        }
        setSupportActionBar(binding.toolbar)
    }

    private fun Int?.greatThen(a: Int):Boolean{
        this?.let {
            return it > a
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        if (!initialized) {
            launchTime = System.currentTimeMillis()
            refresh()
            initialized = !initialized
        }
        if (ViewModel.watchingPosition.first != -1){
            Log.d("EpisodeActivity", "onStart: 从播放器回来")
            episodeAdapter?.let { adapter ->
                val watchTimeSeconds = (System.currentTimeMillis() - ViewModel.watchingPosition.second)/1000
                val position = ViewModel.watchingPosition.first
                val totalSeconds = adapter.getDataAtPosition(position).timeSeconds
                if (totalSeconds <= 0){
                    Toast.makeText(this, "获取时长出错", Toast.LENGTH_SHORT).show()
                }
                if (watchTimeSeconds > 0.9 * totalSeconds){
                    MIUIDialog(this).show {
                        title(text = "是否观看完毕?")
                        negativeButton(text = "否"){ ViewModel.watchingPosition = -1 to -1 }
                        positiveButton(text = "是"){
                            adapter.autoBookmark(position)
                            ViewModel.watchingPosition = -1 to -1
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refresh() {
        lifecycleScope.launch(Dispatchers.IO) {
            val server = SettingManager.getIp()
            val fileList = getJson(album)
            val info = getInfo()
            // post相关
            lifecycleScope.launch(Dispatchers.IO) {
                val title = info.getString("title")
                val certification =
                    "${info.getString("certification")} ${info.getString("genres")} • ${
                        info.getString(
                            "runtime"
                        )
                    }"
                val tagline = "“${info.getString("tagline")}”"
                val score = info.getInt("user_score_chart")
                val valueAnimator = ValueAnimator.ofInt(0, score)
                valueAnimator.addUpdateListener {
                    val value = it.animatedValue as Int //手动赋值
                    runOnUiThread {
                        binding.include2.textView6.text = value.toString()
                        binding.include2.circularProgressView.progress = value
                    }
                }
                valueAnimator.duration = 1000
                withContext(Dispatchers.Main) {
                    binding.textView.text = title
                    binding.textView2.text = certification
                    binding.textView3.text = tagline
                    binding.textView9.text = "用户评分"
                    binding.include2.root.visibility = View.VISIBLE
                    binding.include2.textView6.text = "0"
                    binding.include2.circularProgressView.progress = 0
                }
                delay(700 - System.currentTimeMillis() + launchTime)
                withContext(Dispatchers.Main) {
                    valueAnimator.start()
                }
            }
            lifecycleScope.launch(Dispatchers.Main) {
                val palette = withContext(Dispatchers.IO) {
                    val postRequestCreator = Picasso.get().load(
                        "$server/getFile/get_post_img?" +
                                "path=${Uri.encode("/$album/.post")}&" +
                                "token=${ViewModel.token}"
                    ).error(R.mipmap.post)
                    withContext(Dispatchers.Main) {
                        postRequestCreator.into(binding.post)
                            .also { binding.post.visibility = View.VISIBLE }
                    }
                    Palette.from(postRequestCreator.get()).generate()
                }
                val vibrantBody = (palette.dominantSwatch?.rgb) ?: Color.parseColor("#7367EF")
                val color = if (vibrantBody.isLightColor(0.4f)) Color.BLACK else Color.WHITE
                binding.toolbarLayout.setContentScrimColor(vibrantBody)
                binding.toolbarLayout.setBackgroundColor(vibrantBody)
                binding.toolbarLayout.setStatusBarScrimColor(vibrantBody)
                binding.textView.setTextColor(color)
                binding.textView2.setTextColor(color)
                binding.textView3.setTextColor(color)
                binding.textView9.setTextColor(color)
                WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars =
                    color == Color.BLACK
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val database = ArrayList<Episode>(fileList.length())
                val attaches = ArrayList<String>(fileList.length())
                for (index in 0 until fileList.length()) {
                    val jsonObject = fileList.getJSONObject(index)
                    val episode = jsonObject.getString("name")
                    if (jsonObject.getString("type") == "Attach") {
                        attaches.add(episode)
                    } else if (jsonObject.getString("watched") != "watched" || SettingManager.getShow()) {
                        database.add(
                            Episode(
                                episodePath = episode,
                                bitrate = if (jsonObject.getString("bitrate") != "") jsonObject.getString(
                                    "bitrate"
                                )
                                else jsonObject.getLong("length").describeAsFileSize(""),
                                date = jsonObject.getString("desc"),
                                timeSeconds = jsonObject.getDouble("lasts")
                            )
                        )
                    }
                }
                episodeAdapter = EpisodeAdapter(
                    this@EpisodeActivity,
                    dataset = database,
                    album = album
                )
                attachAdapter = AttachAdapter(
                    this@EpisodeActivity,
                    titles = attaches
                )
                val column = if (database.size <= 1) 1 else 2
                val rv = findViewById<RecyclerView>(R.id.rv)
                delay(500 - System.currentTimeMillis() + launchTime)
                withContext(Dispatchers.Main) {
                    (rv.layoutManager as StaggeredGridLayoutManager).spanCount = column
                    rv.adapter = episodeAdapter
                }
            }
        }
    }

    private suspend fun getJson(album: String):JSONArray {
        return withContext(Dispatchers.IO){
            val server = SettingManager.getIp()
            val b = "$server/getFileList?path=/$album/".http()?:"[]"
            JSONArray(b)
        }
    }

    private suspend fun getInfo():JSONObject {
        return withContext(Dispatchers.IO){
            val server = SettingManager.getIp()
            val b = "$server/getFile/get_album_info?path=${Uri.encode("/$album/.info")}".http()
            JSONObject(b?:"{}")
        }
    }

    private fun Activity.runOnUIThreadDelay(delay: Long, runnable: Runnable){
        Handler(this.mainLooper).postDelayed(runnable, delay)
    }

}