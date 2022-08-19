package jp.assasans.protanki.client.launcher.utils

import java.net.URI
import java.net.URISyntaxException

class UriUtils private constructor() {
  companion object {
    fun tryCreate(uri: String): URI? {
      return try {
        URI(uri)
      } catch(exception: URISyntaxException) {
        null
      }
    }
  }
}
