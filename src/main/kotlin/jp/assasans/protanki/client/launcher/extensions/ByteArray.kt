package jp.assasans.protanki.client.launcher.extensions

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString() = asUByteArray().joinToString("") {
  it.toString(16).padStart(2, '0')
}
