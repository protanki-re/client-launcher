package jp.assasans.protanki.client.launcher.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import jp.assasans.protanki.client.launcher.ClientRuntime

class ClientRuntimeAdapter {
  @ToJson
  fun toJson(value: ClientRuntime) = value.key

  @FromJson
  fun fromJson(value: String) = ClientRuntime.get(value)
}
