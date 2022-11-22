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
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.peanut.sdk.miuidialog.MIUIDialog
import com.peanut.ted.ed.R
import com.peanut.ted.ed.adapter.AttachAdapter
import com.peanut.ted.ed.adapter.EpisodeAdapter
import com.peanut.ted.ed.data.Episode
import com.peanut.ted.ed.databinding.ActivityDetailBinding
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities
import com.peanut.ted.ed.utils.Unities.calculateColorLightValue
import com.peanut.ted.ed.utils.Unities.http
import com.peanut.ted.ed.utils.Unities.resolveUrl
import com.peanut.ted.ed.utils.Unities.toast
import com.peanut.ted.ed.viewmodel.ViewModel
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

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

    private fun refresh() = thread { refreshOnThread() }

    @SuppressLint("SetTextI18n")
    private fun refreshOnThread() {
        try {
            //开始只进行加载动画和网络请求防止卡顿
            val server = ViewModel.ServerIp.resolveUrl()
            getJson(album){
                getInfo { info->
                    val title = info.getString("title")
                    val certification = "${info.getString("certification")} ${info.getString("genres")} • ${
                        info.getString(
                            "runtime"
                        )
                    }"
                    val tagline = "“${info.getString("tagline")}”"
                    val score = info.getInt("user_score_chart")
                    val database = ArrayList<Episode>(it.length())
                    val attaches = ArrayList<String>(it.length())
                    thread {
                        val postRequestCreator = Picasso.get().load(
                            "$server/getFile/get_post_img?" +
                                    "path=${Uri.encode("/$album/.post")}&" +
                                    "token=${SettingManager.getValue("token", "")}"
                        ).error(R.mipmap.post)
                        Palette.from(postRequestCreator.get()).generate { palette ->
                            // Use generated instance
                            val vibrantBody = (palette?.dominantSwatch?.rgb)
                                ?: Color.parseColor("#7367EF")
                            val light = calculateColorLightValue(vibrantBody)
                            val color = if (light < 0.4) Color.WHITE else Color.BLACK
                            runOnUiThread {
                                binding.toolbarLayout.setContentScrimColor(vibrantBody)
                                binding.toolbarLayout.setBackgroundColor(vibrantBody)
                                binding.toolbarLayout.setStatusBarScrimColor(vibrantBody)
                                binding.textView.setTextColor(color)
                                binding.textView2.setTextColor(color)
                                binding.textView3.setTextColor(color)
                                binding.textView9.setTextColor(color)
                                WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars =
                                    light >= 0.4
                            }
                            //显示海报图片
                            runOnUiThread { postRequestCreator.into(binding.post).also { binding.post.visibility = View.VISIBLE } }
                        }
                    }
                    for (index in 0 until it.length()) {
                        val jsonObject = it.getJSONObject(index)
                        val episode = jsonObject.getString("name")
                        if (jsonObject.getString("type") == "Attach") {
                            attaches.add(episode)
                        }
                        else if (jsonObject.getString("watched") != "watched" || SettingManager.getValue(
                                "show_watched",
                                false
                            )
                        ) {
                            database.add(
                                Episode(
                                    episodePath = episode,
                                    bitrate = if (jsonObject.getString("bitrate") != "") jsonObject.getString("bitrate")
                                    else Unities.getFileLengthDesc(jsonObject.getLong("length")),
                                    date = jsonObject.getString("desc"),
                                    timeSeconds = jsonObject.getDouble("lasts")
                                )
                            )
                        }
                    }
                    episodeAdapter = EpisodeAdapter(
                        this,
                        dataset = database,
                        album = album
                    )
                    attachAdapter = AttachAdapter(
                        this,
                        titles = attaches
                    )
                    val column = if (database.size <= 1) 1 else 2
                    val rv = findViewById<RecyclerView>(R.id.rv)
                    runOnUiThread {
                        binding.textView.text = title
                        binding.textView2.text = certification
                        binding.textView3.text = tagline
                        binding.textView9.text = "用户评分"
                        binding.include2.root.visibility = View.VISIBLE
                        binding.include2.textView6.text = "0"
                        binding.include2.circularProgressView.progress = 0
                        (rv.layoutManager as StaggeredGridLayoutManager).spanCount = column
                    }
                    val valueAnimator = ValueAnimator.ofInt(0, score)
                    valueAnimator.addUpdateListener {
                        val value = it.animatedValue as Int //手动赋值
                        runOnUiThread {
                            binding.include2.textView6.text = value.toString()
                            binding.include2.circularProgressView.progress = value
                        }
                    }
                    valueAnimator.duration = 1000
                    //设置数据到界面
                    runOnUIThreadDelay(500 - System.currentTimeMillis() + launchTime) {
                        launchTime = System.currentTimeMillis()
                        rv.adapter = episodeAdapter
                        //给500毫秒给recyclerview绘制
                        runOnUIThreadDelay(500 - System.currentTimeMillis() + launchTime) { valueAnimator.start() }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(mainLooper).post {
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getJson(album: String, func: (JSONArray) -> Unit) {
        val server = ViewModel.ServerIp.resolveUrl()
        "$server/getFileList?path=/$album/".http { body ->
            thread { func.invoke(JSONArray(body ?: "[]")) }
        }
    }

    private fun getInfo(func: (JSONObject) -> Unit) {
        val server = ViewModel.ServerIp.resolveUrl()
        "$server/getFile/get_album_info?path=${Uri.encode("/$album/.info")}".http { body ->
            thread { func.invoke(JSONObject(body ?: "{}")) }
        }
    }

    private fun Activity.runOnUIThreadDelay(delay: Long, runnable: Runnable){
        Handler(this.mainLooper).postDelayed(runnable, delay)
    }

}