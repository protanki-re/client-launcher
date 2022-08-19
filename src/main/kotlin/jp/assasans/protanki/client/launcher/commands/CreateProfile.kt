package jp.assasans.protanki.client.launcher.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.option
import jp.assasans.protanki.client.launcher.Profile
import jp.assasans.protanki.client.launcher.ProfileConstants
import jp.assasans.protanki.client.launcher.ProfileData
import jp.assasans.protanki.client.launcher.ProfileSettings
import jp.assasans.protanki.client.launcher.extensions.crc32
import mu.KotlinLogging
import java.nio.file.Paths
import kotlin.io.path.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CreateProfile : CliktCommand(name = "create", help = "Create a new client profile") {
  private val logger = KotlinLogging.logger { }

  val root by argument("root", help = "The root directory of the client profile").default(".")

  val displayName by option("--name", help = "The display name of the profile")
  val id by option("--id", "-i", help = "Application ID for the Adobe AIR instance")

  override fun run(): Unit = runBlocking {
    val root = Paths.get(root).absolute()
    if(root.exists() && !root.isDirectory()) {
      logger.error { "$root is not a directory" }
      return@runBlocking
    }

    if(root.exists() && root.isDirectory() && root.resolve(ProfileConstants.PROFILE_FILE).exists()) {
      logger.error { "$root already contains a client profile" }
      return@runBlocking
    }

    logger.info { "Creating new client profile in $root..." }

    val displayName = displayName ?: root.name
    val id = id ?: "ProTanki-${"%08x".format(root.pathString.toByteArray().crc32())}"

    if(!root.exists()) withContext(Dispatchers.IO) { root.createDirectory() }

    withContext(Dispatchers.IO) {
      val profile = Profile(
        root = root,
        data = ProfileData(
          displayName = displayName,
          settings = ProfileSettings(
            id = id,
            resourceServer = "http://54.36.175.134"
          )
        )
      )
      profile.save()
    }

    logger.info { "Created new client profile '$displayName'" }
  }
}
