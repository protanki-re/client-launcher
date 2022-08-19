package jp.assasans.protanki.client.launcher.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.option
import jp.assasans.protanki.client.launcher.ClientRuntime
import jp.assasans.protanki.client.launcher.Profile
import jp.assasans.protanki.client.launcher.utils.UriUtils
import mu.KotlinLogging
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.pathString
import kotlinx.coroutines.runBlocking

class EditProfile : CliktCommand(name = "edit", help = "Edit the settings of the client profile") {
  private val logger = KotlinLogging.logger { }

  val root by argument("root", help = "The root directory of the client profile").default(".")

  val displayName by option("--name", "-n", help = "The display name of the profile")
  val id by option("--id", "-i", help = "Application ID for the Adobe AIR instance")
  val runtime by option("--runtime", "-r", help = "Runtime to use, one of: air, flashplayer")
  val runtimeExecutable by option("--runtime-executable", "-e", help = "Runtime executable to use")
  val chainloader by option("--chainloader", "-c", help = "Path to the chainloader SWF")
  val gameLibrary by option("--game-library", "--gl", help = "Path to the game library SWF")
  val gameServer by option("--game-server", "--gs", help = "Game server endpoint")
  val resourceServer by option("--resource-server", "--rs", help = "Resource server base URL")
  val locale by option("--locale", "-l", help = "Game locale")

  override fun run(): Unit = runBlocking {
    val root = Paths.get(root).absolute()
    logger.info { "Profile root: $root" }

    val profile = try {
      Profile.open(root)
    } catch(exception: IllegalArgumentException) {
      logger.error(exception) { "Failed to open client profile" }
      return@runBlocking
    }

    profile.apply {
      displayName?.let {
        data.displayName = it.also {
          logger.info { "Updated display name: ${data.displayName} -> $it" }
        }
      }
      data.apply {
        id?.let {
          settings.id = it.also {
            logger.info { "Updated application ID: ${settings.id} -> $it" }
          }
        }
        runtime?.let {
          settings.runtime = ClientRuntime.get(it).also {
            logger.info { "Updated runtime: ${settings.runtime} -> $it" }
          }
        }
        runtimeExecutable?.let {
          settings.runtimeExecutable = Paths.get(it).absolute().normalize().also {
            logger.info { "Updated runtime executable: ${settings.runtimeExecutable} -> $it" }
          }
        }
        chainloader?.let {
          settings.chainloader = Paths.get(it).absolute().normalize().also {
            logger.info { "Updated chainloader: ${settings.chainloader} -> $it" }
          }
        }
        gameLibrary?.let {
          val gameLibraryFile = gameLibrary?.let { library ->
            val local = UriUtils.tryCreate(library)?.let { it.scheme == "file" } ?: true

            // ActionScript could not find relative or non-normalized files
            if(local) Paths.get(library).absolute().normalize().pathString
            else library
          }

          settings.gameLibrary = gameLibraryFile.also {
            logger.info { "Updated game library: ${settings.gameLibrary} -> $it" }
          }
        }
        gameServer?.let {
          settings.gameServer = it.also {
            logger.info { "Updated game server: ${settings.gameServer} -> $it" }
          }
        }
        resourceServer?.let {
          settings.resourceServer = it.also {
            logger.info { "Updated resource server: ${settings.resourceServer} -> $it" }
          }
        }
        locale?.let {
          settings.locale = it.also {
            logger.info { "Updated locale: ${settings.locale} -> $it" }
          }
        }
      }
    }

    profile.save()
  }
}
