![Convex](static/Convex.png)

# Override
Convex is an elegant tool based on [Retrofit](https://github.com/square/retrofit)
to help developers just focus on business data.

With the ability of Convex you can process different **BaseResponse** simply
and uniformly.

# Background
Lots of Restful APIs' responses are designed as following :

```json
{
	"code" : 0,
	"message" : "",
	"data" : {}
}
```

or

```json
{
	"status" : 0,
	"message" : "",
	"data" : {}
}
```

and so on.

So when developers use **Retrofit**, they have to design a **BaseResponse**
as following :

```kotlin
data class BaseResponse<T>(
	@SerializedName("code")
	val code : Int = 0,
	@SerializedName("message")
	val message : String?,
	@SerializedName("data")
	val data : T?
)
```

And when define a service method they need to wrap business data with
**BaseReponse** as following :

```kotlin
interface XXXService {
	@GET("xxx")
	suspend fun xxx() : BaseResponse<XXX>
}
```

# Thoughts

Handing **BaseResponse** is really boring and repetitive.😖

Is there a way to just handle **BaseResponse** only once?🤔

Is there a way to remove the business data wraper **BaseResponse**?🤔

So **Convex** comes out.🎉 🎉 🎉

# How to use

For more details, please see the [ConvexTest](https://github.com/ParadiseHell/convex/blob/main/convex/src/test/kotlin/org/paradisehell/convex/ConvexTest.kt) or the [Android-Project](https://github.com/ParadiseHell/convex/blob/main/app/src/main/java/org/paradisehell/convex/MainActivity.kt).

### Grab Convex from Maven Central

In your build.gradle :

```gradle
dependencies {
    implementation "org.paradisehell.convex:convex:0.0.3"
}
```

### Implement a ConvexTansformer

```kotlin
private class XXXConvexTransformer : ConvexTransformer {
	@Throws(IOException::class)
	override fun transform(original: InputStream): InputStream {
		TODO("Return the business data InputStream.")
	}
}
```

### Register ConvexTransformer

```kotlin
Convex.registerConvexTransformer(XXXConvexTransformer())
```

### Add ConvexConverterFactory to Retrofit

```kotlin
Retrofit.Builder()
	.baseUrl("https://xxx.com/")
	// add ConvexConverterFactory first !!!
	.addConverterFactory(ConvexConverterFactory())
	.addConverterFactory(GsonConverterFactory.create())
	.build()
```

### Define service method with `Transformer` annotation

```kotlin
interface XXXService {
	@GET("xxx")
	@Transformer(XXXConvexTransformer::class)
	suspend fun xxx() : XXX /**No BaseReponse is needed anymore.👻👻👻*/
}
```

**That's All, enjoy yourself with Convex.**

# More convenience usage

As you can see below, every service's method need to annotation with `Transformer`,
which is boring and repetitive. So `Convex` provide a plugin called `convex-booster`
to automatic to do this work.

### Add `booster-gradle-plugin` and `convex-booster` to classpath

In your root `build.gradle`

```gradle
buildscript {
    dependencies {
        // booster
        classpath "com.didiglobal.booster:booster-gradle-plugin:3.3.1"
        // convex-booster
        classpath "org.paradisehell.convex:convex-booster:0.0.3"
    }
}
```

### Apply booster plugin

In your app `build.gradle`
```
plugins {
    id 'com.didiglobal.booster'
}
```

or 

```
apply from: "com.didiglobal.booster"
```

### Define service with `Transformer` annotation

```kotlin
@Transformer(XXXConvexTransformer::class)
interface XXXService {
	@GET("xxx")
	suspend fun xxx() : XXX /**No BaseReponse is needed anymore.👻👻👻*/
}
```

If you do not want `ConvexTransformer` to work with some service methods, you
can use `DisableTransformer` annotation with these method as following.

```kotlin
@Transformer(XXXConvexTransformer::class)
interface XXXService {
	@GET("xxx")
	@DisableTransformer // Convex will ignore XXXConvexTransformer
	suspend fun xxx() : XXX 
}
```

# Thanks

- [booter](https://github.com/didi/booster)
	- Optimizer for mobile applications.🚀🚀🚀
	- An elegant framework to process bytecode really simply.👍👍👍

License
=======

    Copyright 2021 ParadiseHell.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
