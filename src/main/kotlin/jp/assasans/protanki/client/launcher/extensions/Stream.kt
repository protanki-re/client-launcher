package jp.assasans.protanki.client.launcher.extensions

import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun InputStream.transfer(crossinline block: (buffer: CharArray, count: Int) -> Unit) {
  val reader = reader()
  val buffer = CharArray(1024)
  withContext(Dispatchers.IO) {
    while(true) {
      val count = reader.read(buffer)
      if(count == -1) break
      block(buffer, count)
    }
  }
}
