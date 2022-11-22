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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.peanut.ted.ed.BuildConfig
import com.peanut.ted.ed.R
import com.peanut.ted.ed.adapter.AlbumAdapter
import com.peanut.ted.ed.data.Album
import com.peanut.ted.ed.databinding.ActivityMainBinding
import com.peanut.ted.ed.utils.SettingManager
import com.peanut.ted.ed.utils.Unities.http
import com.peanut.ted.ed.utils.Unities.resolveUrl
import com.peanut.ted.ed.viewmodel.ViewModel
import com.squareup.picasso.Picasso
import org.json.JSONArray
import kotlin.concurrent.thread

class AlbumActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: ActivityMainBinding
    private var adapter: AlbumAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Picasso.get().setIndicatorsEnabled(BuildConfig.DEBUG)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(ActivityMainBinding.inflate(layoutInflater).also { binding = it }.root)
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
        refresh()
    }

    private fun refresh() = thread { refreshOnThread() }

    private fun refreshOnThread() {
        try {
            val user = SettingManager.getValue("user", "")
            val ps = SettingManager.getValue("password", "")
            val server = ViewModel.ServerIp.resolveUrl()
            "$server/userLogin?name=${Uri.encode(user)}&psw=${Uri.encode(ps)}".http{
                getJson {
                    val albums = it.getAlbumData()
                    runOnUiThread {
                        binding.rv.adapter = AlbumAdapter(
                            this,this,
                            albums = albums).also { adapter -> this.adapter = adapter }
                        binding.rv.layoutManager = StaggeredGridLayoutManager(
                            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 5,
                            StaggeredGridLayoutManager.VERTICAL
                        )
                    }
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        //从Detail回来，清除图片
        ViewModel.MainActivity2DetailActivityImage = null
    }


    private fun getJson(func: (JSONArray) -> Unit) {
        val server = ViewModel.ServerIp.resolveUrl()
        "$server/getFileList?path=/".http{ body ->
            try {
                func.invoke(JSONArray(body ?: "[]"))
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }

    private fun JSONArray.getAlbumData(): MutableList<Album>{
        val albums = mutableListOf<Album>()
        for (i in 0 until this.length()) {
            val data = this.getJSONObject(i)
            if (data.getString("type") == "Directory" && (data.getString("watched") != "watched" || SettingManager.getValue("show_watched", false)))
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
        thread {
            try {
                val user = SettingManager.getValue("user", "")
                val ps = SettingManager.getValue("password", "")
                val server = ViewModel.ServerIp.resolveUrl()
                "$server/userLogin?name=${Uri.encode(user)}&psw=${Uri.encode(ps)}".http{
                    getJson {
                        val albums = it.getAlbumData()
                        runOnUiThread { adapter?.changeDataset(albums) }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            } finally {
                binding.refresh.isRefreshing = false
            }
        }
    }
}