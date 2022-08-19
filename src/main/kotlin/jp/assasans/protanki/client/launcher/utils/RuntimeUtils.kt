package jp.assasans.protanki.client.launcher.utils

import jp.assasans.protanki.client.launcher.ProfileConstants

class RuntimeUtils private constructor() {
  companion object {
    fun getApplicationDescriptor(id: String, queryString: String): String = """
      |<?xml version="1.0" encoding="utf-8" ?>
      |<application xmlns="http://ns.adobe.com/air/application/3.2">
      |  <id>$id</id>
      |  <versionNumber>1.0</versionNumber>
      |  <filename>$id</filename>
      |
      |  <initialWindow>
      |    <title>ProTanki [chainloading...]</title>
      |    <content>${ProfileConstants.CHAINLOADER_FILE}?$queryString</content>
      |    <visible>true</visible>
      |
      |    <renderMode>direct</renderMode>
      |    <depthAndStencil>true</depthAndStencil>
      |
      |    <minimizable>true</minimizable>
      |    <maximizable>true</maximizable>
      |    <resizable>true</resizable>
      |  </initialWindow>
      |</application>
      """.trimMargin()
  }
}
