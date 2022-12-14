package com.peanut.ted.ed.activity

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.peanut.sdk.miuidialog.AddInFunction.gone
import com.peanut.sdk.miuidialog.AddInFunction.visible
import com.peanut.sdk.petlin.Extend.toast
import com.peanut.ted.ed.BuildConfig
import com.peanut.ted.ed.R
import com.peanut.ted.ed.adapter.AlbumAdapter
import com.peanut.ted.ed.data.Album
import com.peanut.ted.ed.data.PlayHistory
import com.peanut.ted.ed.databinding.ActivityAlbumBinding
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.http
import com.peanut.ted.ed.utils.Unities.play
import com.peanut.ted.ed.viewmodel.ViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class AlbumActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: ActivityAlbumBinding
    private var adapter: AlbumAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Picasso.get().setIndicatorsEnabled(BuildConfig.DEBUG)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(ActivityAlbumBinding.inflate(layoutInflater).also { binding = it }.root)
        binding.root.setOnApplyWindowInsetsListener { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val statusBar = insets.getInsets(WindowInsets.Type.statusBars())
                val params = (binding.toolbar.layoutParams as ViewGroup.MarginLayoutParams)
                params.topMargin = statusBar.top
                binding.toolbar.layoutParams = params
                binding.refresh.setProgressViewOffset(true, (statusBar.top + params.height) - 50, (statusBar.top + params.height) + 50)
            }
            insets
        }
        binding.rv.addOnScrollListener(object: OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                binding.refresh.isEnabled = !recyclerView.canScrollVertically(-1)
            }
        })
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.refresh.setOnRefreshListener(this)
        binding.rv.adapter = AlbumAdapter(
            this,this,
            albums = mutableListOf()
        ).also { adapter -> this.adapter = adapter }
        binding.rv.layoutManager = StaggeredGridLayoutManager(
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 5,
            StaggeredGridLayoutManager.VERTICAL
        )
        refresh()
    }

    private fun refresh(){
        lifecycleScope.launch(Dispatchers.Main) {
            val albums: MutableList<Album> = withContext(Dispatchers.IO) {
                try {
                    val user = SettingManager.getUserName()
                    val ps = SettingManager.getPassword()
                    val server = SettingManager.getIp()
                    // login
                    "$server/userLogin?name=${Uri.encode(user)}&psw=${Uri.encode(ps)}".http()
                    getJson().getAlbumData()
                }catch (e:Exception){
                    e.localizedMessage?.toast(this@AlbumActivity)
                    mutableListOf()
                }
            }
            adapter?.changeDataset(albums)
        }
    }

    override fun onStart() {
        super.onStart()
        //???Detail?????????????????????
        ViewModel.MainActivity2DetailActivityImage = null
        //???????????????????????????
        lifecycleScope.launch(Dispatchers.IO) {
            val playHistory = SettingManager.readPlayHistory()
            withContext(Dispatchers.Main){
                if (playHistory != PlayHistory.Empty){
                    binding.playHistory.visible()
                    binding.playHistory.setOnClickListener {
                        playHistory.url.play(this@AlbumActivity)
                    }
                }else{
                    binding.playHistory.gone()
                }
            }
        }
    }

    private suspend fun getJson(): JSONArray {
        val server = SettingManager.getIp()
        val body = "$server/getFileList?path=/".http()
        return JSONArray(body ?: "[]")
    }

    private suspend fun JSONArray.getAlbumData(): MutableList<Album>{
        val albums = mutableListOf<Album>()
        for (i in 0 until this.length()) {
            val data = this.getJSONObject(i)
            if (data.getString("type") == "Directory" && (data.getString("watched") != "watched" || SettingManager.getShow()))
                albums.add(Album(albumTitle = data.getString("title"), albumPath = data.getString("name")))
        }
        return albums
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.let {
            when (item.itemId) {
                R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            return super.onOptionsItemSelected(item)
        }
        return false
    }

    override fun onRefresh() {
        refresh()
        binding.refresh.isRefreshing = false
    }
}