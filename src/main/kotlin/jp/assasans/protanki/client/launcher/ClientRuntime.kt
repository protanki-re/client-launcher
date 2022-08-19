package jp.assasans.protanki.client.launcher

enum class ClientRuntime(val key: String) {
  AdobeIntegratedRuntime("air"),
  FlashPlayer("flashplayer");

  companion object {
    private val map = values().associateBy(ClientRuntime::key)

    fun get(key: String) = map[key]
  }
}
