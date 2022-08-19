package jp.assasans.protanki.client.launcher.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

class PathAdapter {
  @ToJson
  fun toJson(value: Path) = value.pathString

  @FromJson
  fun fromJson(value: String) = Paths.get(value)
}
