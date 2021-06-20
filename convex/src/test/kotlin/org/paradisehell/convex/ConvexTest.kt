package org.paradisehell.convex

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.paradisehell.convex.annotation.DisableTransformer
import org.paradisehell.convex.annotation.Transformer
import org.paradisehell.convex.converter.ConvexConverterFactory
import org.paradisehell.convex.transformer.ConvexTransformer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException
import java.io.InputStream

class ConvexTest {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://wanandroid.com/")
            .addConverterFactory(ConvexConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    data class WanAndroidRestfulResponse<T>(
        @SerializedName("errorCode")
        val errorCode: Int = 0,
        @SerializedName("errorMsg")
        val errorMsg: String? = null,
        @SerializedName("data")
        val data: T? = null
    )

    private class WanAndroidConvexTransformer : ConvexTransformer {
        private val gson = Gson()

        @Throws(IOException::class)
        override fun transform(original: InputStream): InputStream {
            val response = gson.fromJson<WanAndroidRestfulResponse<JsonElement>>(
                original.reader(),
                object : TypeToken<WanAndroidRestfulResponse<JsonElement>>() {
                }.type
            )
            if (response.errorCode == 0 && response.data != null) {
                return response.data.toString().byteInputStream()
            }
            throw IOException(
                "errorCode : " + response.errorCode + " ; errorMsg : " + response.errorMsg
            )
        }

    }

    @Before
    fun injectWanAndroidConvexHandler() {
        Convex.registerConvexTransformer(WanAndroidConvexTransformer())
    }

    data class WeChatArticle(
        @SerializedName("id")
        val id: Int = 0,
        @SerializedName("name")
        val name: String? = null
    )

    interface WanAndroidService {
        @GET("/wxarticle/chapters/json")
        suspend fun listWechatArticles(): WanAndroidRestfulResponse<List<WeChatArticle>>

        @Transformer(WanAndroidConvexTransformer::class)
        @GET("/wxarticle/chapters/json")
        suspend fun listWechatArticlesWithConvexTransformer(): List<WeChatArticle>

        @Transformer(WanAndroidConvexTransformer::class)
        @DisableTransformer
        @GET("/wxarticle/chapters/json")
        suspend fun listWechatArticlesDisableConvexTransformer(): WanAndroidRestfulResponse<List<WeChatArticle>>
    }

    @Test
    fun testWanAndroid() {
        val service = retrofit.create(WanAndroidService::class.java)
        testWanAndroidNormal(service)
        println()
        testWanAndroidWithConvex(service)
        println()
        testWanAndroidDisableConvexTransformer(service)
    }

    private fun testWanAndroidNormal(service: WanAndroidService) {
        runBlocking {
            println("===== Test normal =====")
            service.listWechatArticles().data?.forEach { article ->
                println(article)
            }
        }
    }

    private fun testWanAndroidWithConvex(service: WanAndroidService) {
        runBlocking {
            println("===== Test with ConvexTransformer =====")
            service.listWechatArticlesWithConvexTransformer().forEach { article ->
                println(article)
            }
        }
    }

    private fun testWanAndroidDisableConvexTransformer(service: WanAndroidService) {
        runBlocking {
            println("===== Test disable ConvexTransformer =====")
            service.listWechatArticlesDisableConvexTransformer().data?.forEach { article ->
                println(article)
            }
        }
    }
}