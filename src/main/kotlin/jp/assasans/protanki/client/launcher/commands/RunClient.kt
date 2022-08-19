package jp.assasans.protanki.client.launcher.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.option
import jp.assasans.protanki.client.launcher.ClientRuntime
import jp.assasans.protanki.client.launcher.Profile
import jp.assasans.protanki.client.launcher.ProfileConstants
import jp.assasans.protanki.client.launcher.extensions.transfer
import jp.assasans.protanki.client.launcher.utils.RuntimeUtils
import jp.assasans.protanki.client.launcher.utils.UriUtils
import mu.KotlinLogging
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.pathString
import kotlinx.coroutines.*

class RunClient : CliktCommand(name = "run", help = "Run the client") {
  private val logger = KotlinLogging.logger { }

  val root by argument("root", help = "The root directory of the client profile").default(".")

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

    val runtime = runtime?.let(ClientRuntime.Companion::get)?.also {
      logger.debug { "Overridden runtime: $it" }
    }
      ?: profile.data.settings.runtime
      ?: throw IllegalArgumentException("No runtime specified")

    val runtimeExecutable = runtimeExecutable?.let { Paths.get(it).absolute().normalize() }?.also {
      logger.debug { "Overridden runtime executable: $it" }
    }
      ?: profile.data.settings.runtimeExecutable
      ?: throw IllegalArgumentException("No runtime executable specified")

    val chainloader = chainloader?.let { Paths.get(it).absolute().normalize() }?.also {
      logger.debug { "Overridden chainloader: $it" }
    }
      ?: profile.data.settings.chainloader
      ?: throw IllegalArgumentException("No chainloader specified")

    val gameLibrary = gameLibrary?.let { library ->
      val local = UriUtils.tryCreate(library)?.let { it.scheme == "file" } ?: true

      // ActionScript could not find relative or non-normalized files
      if(local) Paths.get(library).absolute().normalize().pathString
      else library
    }?.also {
      logger.debug { "Overridden game library: $it" }
    }
      ?: profile.data.settings.gameLibrary
      ?: throw IllegalArgumentException("No game library specified")
    logger.info { "Game library: $gameLibrary" }

    val gameServer = gameServer?.also {
      logger.debug { "Overridden game server: $it" }
    }
      ?: profile.data.settings.gameServer
      ?: throw IllegalArgumentException("No game server specified")

    val resourceServer = resourceServer?.also {
      logger.debug { "Overridden resource server: $it" }
    }
      ?: profile.data.settings.resourceServer
      ?: throw IllegalArgumentException("No resource server specified")

    val locale = locale?.also {
      logger.debug { "Overridden locale: $it" }
    }
      ?: profile.data.settings.locale
      ?: throw IllegalArgumentException("No locale specified")

    profile.updateChainloaderIfNeeded(chainloader)

    val queryString = buildString {
      append("locale=$locale&amp;")
      append("library=$gameLibrary&amp;")
      append("server=$gameServer&amp;")
      append("resources=$resourceServer")
    }

    logger.debug { "Chainloader query string: $queryString" }

    val runtimeBuilder = when(runtime) {
      ClientRuntime.AdobeIntegratedRuntime -> {
        val id = id?.also {
          logger.debug { "Overridden application ID: $it" }
        }
          ?: profile.data.settings.id
          ?: throw IllegalArgumentException("Application ID is not set")

        profile.updateApplicationDescriptorIfNeeded(
          RuntimeUtils.getApplicationDescriptor(id, queryString)
        )

        // TODO(Assasans): Wine support on Linux
        ProcessBuilder().command(
          runtimeExecutable.pathString,
          profile.applicationDescriptorFile.pathString,
          root.pathString
        )
      }

      ClientRuntime.FlashPlayer            -> {
        // TODO(Assasans): Add to trusted SWF list: https://help.adobe.com/en_US/as3/dev/WS5b3ccc516d4fbf351e63e3d118a9b90204-7c85.html
        ProcessBuilder().command(
          runtimeExecutable.pathString,
          "${ProfileConstants.CHAINLOADER_FILE}?$queryString"
        )
      }
    }

    val process = withContext(Dispatchers.IO) { runtimeBuilder.start() }

    Runtime.getRuntime().addShutdownHook(Thread {
      if(process.isAlive) {
        logger.info { "Stopping client process..." }
        process.destroy()
      }
    })

    logger.info { "===== SERVER LOG BEGIN =====" }
    coroutineScope {
      launch {
        val writer = System.out.writer()
        process.inputStream.transfer { buffer, count ->
          writer.write(buffer, 0, count)
          writer.flush()
        }
      }

      launch {
        val writer = System.err.writer()
        process.errorStream.transfer { buffer, count ->
          writer.write(buffer, 0, count)
          writer.flush()
        }
      }

      launch {
        withContext(Dispatchers.IO) { process.waitFor() }
        logger.info { "===== SERVER LOG END =====" }
      }
    }
  }
}
