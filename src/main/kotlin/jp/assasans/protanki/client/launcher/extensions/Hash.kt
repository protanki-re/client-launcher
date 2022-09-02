package jp.assasans.protanki.client.launcher.extensions

import java.io.InputStream
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.CRC32
import kotlin.io.path.inputStream

fun ByteArray.hash(algorithm: String): ByteArray {
  val digest = MessageDigest.getInstance(algorithm)
  digest.update(this)
  return digest.digest()
}

fun InputStream.hash(algorithm: String): ByteArray {
  return readAllBytes().hash(algorithm)
}

fun Path.hash(algorithm: String): ByteArray = inputStream().use { it.hash(algorithm) }

fun ByteArray.crc32(): Long {
  val crc = CRC32()
  crc.update(this)
  return crc.value
}
