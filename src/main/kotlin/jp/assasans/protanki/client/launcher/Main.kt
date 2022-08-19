package jp.assasans.protanki.client.launcher

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jp.assasans.protanki.client.launcher.commands.CreateProfile
import jp.assasans.protanki.client.launcher.commands.EditProfile
import jp.assasans.protanki.client.launcher.commands.RunClient
import jp.assasans.protanki.client.launcher.serialization.ClientRuntimeAdapter
import jp.assasans.protanki.client.launcher.serialization.PathAdapter

val json: Moshi = Moshi.Builder()
  .add(KotlinJsonAdapterFactory())
  .add(ClientRuntimeAdapter())
  .add(PathAdapter())
  .build()

inline fun <reified T> T.toJson(moshi: Moshi): String = moshi
  .adapter(T::class.java)
  .indent("  ")
  .serializeNulls()
  .toJson(this)

fun main(args: Array<String>) = object : CliktCommand() {
  override fun run() = Unit
}
  .subcommands(
    CreateProfile(),
    EditProfile(),
    RunClient()
  )
  .main(args)
