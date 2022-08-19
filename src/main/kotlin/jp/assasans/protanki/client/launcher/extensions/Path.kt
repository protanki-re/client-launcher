package jp.assasans.protanki.client.launcher.extensions

import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.exists

@OptIn(ExperimentalContracts::class)
inline fun <T> Path.letIfExists(block: (Path) -> T): T? {
  contract {
    callsInPlace(block, InvocationKind.AT_MOST_ONCE)
  }

  if(exists()) return block(this)
  return null
}
