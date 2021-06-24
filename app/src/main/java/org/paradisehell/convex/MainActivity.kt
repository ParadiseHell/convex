package org.paradisehell.convex

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.paradisehell.convex.converter.ConvexConverterFactory
import org.paradisehell.convex.service.WanAndroidService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://wanandroid.com/")
            .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build())
            .addConverterFactory(ConvexConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val wanAndroidService by lazy {
        retrofit.create(WanAndroidService::class.java)
    }

    private val textResult by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TextView>(R.id.text_view_result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button_top_article).setOnClickListener {
            lifecycleScope.launch(Main) {
                val articles = withContext(IO) {
                    runCatching {
                        wanAndroidService.getTopArticles()
                    }.onSuccess {
                        return@withContext it
                    }.onFailure {
                        it.printStackTrace()
                        return@withContext it
                    }
                }
                updateResult("GetTopArticles", articles)
            }
        }
        findViewById<View>(R.id.button_banner).setOnClickListener {
            lifecycleScope.launch(Main) {
                val banners = withContext(IO) {
                    runCatching {
                        wanAndroidService.getBanners()
                    }.onSuccess {
                        return@withContext it
                    }.onFailure {
                        it.printStackTrace()
                        return@withContext it
                    }
                }
                updateResult("GetBanners", banners)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateResult(api: String, data: Any) {
        val dataString = if (data is List<*>) {
            data.joinToString("\n")
        } else {
            data.toString()
        }
        textResult.text = "API [$api]\n\n$dataString"
    }
}