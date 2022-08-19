package jp.assasans.protanki.client.launcher

import jp.assasans.protanki.client.launcher.extensions.hash
import jp.assasans.protanki.client.launcher.extensions.letIfExists
import jp.assasans.protanki.client.launcher.extensions.toHexString
import mu.KotlinLogging
import okio.buffer
import okio.source
import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProfileConstants {
  const val PROFILE_FILE = "profile.json"
  const val CHAINLOADER_FILE = "chainloader.swf"
  const val APPLICATION_DESCRIPTOR_FILE = "application.xml"
}

class Profile(
  val root: Path,
  val data: ProfileData
) {
  private val logger = KotlinLogging.logger {}

  companion object {
    private val logger = KotlinLogging.logger {}

    suspend fun open(root: Path): Profile {
      if(root.notExists()) throw IllegalArgumentException("$root does not exist")
      if(root.exists() && !root.isDirectory()) throw IllegalArgumentException("$root is not a directory")

      if(root.exists() && root.isDirectory() && !root.resolve(ProfileConstants.PROFILE_FILE).exists()) {
        throw IllegalArgumentException("$root does not contain ${ProfileConstants.PROFILE_FILE} file")
      }

      logger.debug { "Opening client profile in $root..." }

      val profileFile = root.resolve(ProfileConstants.PROFILE_FILE)
      val profile = profileFile.source().buffer().use { json.adapter(ProfileData::class.java).fromJson(it) }
        ?: throw IllegalArgumentException("$profileFile is not a valid profile")

      return Profile(root, profile)
    }
  }

  val profileFile: Path = root.resolve(ProfileConstants.PROFILE_FILE)
  val chainloaderFile: Path = root.resolve(ProfileConstants.CHAINLOADER_FILE)
  val applicationDescriptorFile: Path = root.resolve(ProfileConstants.APPLICATION_DESCRIPTOR_FILE)

  suspend fun save() {
    withContext(Dispatchers.IO) {
      profileFile.bufferedWriter().use { writer ->
        writer.write(data.toJson(json))
        writer.newLine()
      }
    }

    logger.debug { "Saved profile to $profileFile" }
  }

  /* Chainloader */

  suspend fun updateChainloader(newChainloader: Path) {
    withContext(Dispatchers.IO) { newChainloader.copyTo(chainloaderFile, overwrite = true) }

    logger.debug { "Updated chainloader" }
  }

  suspend fun updateChainloaderIfNeeded(newChainloader: Path) {
    val hash = chainloaderFile.letIfExists { it.hash("SHA-256") }
    val newHash = newChainloader.hash("SHA-256")

    logger.debug { "Current chainloader hash: ${hash?.toHexString()}" }
    logger.debug { "New chainloader hash    : ${newHash.toHexString()}" }

    if(hash == null || !hash.contentEquals(newHash)) {
      updateChainloader(newChainloader)
    }
  }

  /* Application descriptor */

  suspend fun updateApplicationDescriptor(content: String) {
    applicationDescriptorFile.bufferedWriter().use { writer ->
      writer.write(content)
      writer.newLine()
    }

    logger.debug { "Updated application descriptor" }
  }

  suspend fun updateApplicationDescriptorIfNeeded(content: String) {
    val hash = applicationDescriptorFile.letIfExists { it.hash("SHA-256") }
    val newHash = content.toByteArray().hash("SHA-256")

    logger.debug { "Current application descriptor hash: ${hash?.toHexString()}" }
    logger.debug { "New application descriptor hash    : ${newHash.toHexString()}" }

    if(hash == null || !hash.contentEquals(newHash)) {
      updateApplicationDescriptor(content)
    }
  }
}
