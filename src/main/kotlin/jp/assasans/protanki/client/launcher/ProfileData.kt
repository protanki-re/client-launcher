package jp.assasans.protanki.client.launcher

import com.squareup.moshi.Json
import java.nio.file.Path

data class ProfileData(
  @Json var displayName: String,
  @Json var settings: ProfileSettings
)

data class ProfileSettings(
  @Json var runtime: ClientRuntime? = null,
  @Json var runtimeExecutable: Path? = null,

  @Json var id: String? = null,
  @Json var chainloader: Path? = null,

  @Json var gameLibrary: String? = null,
  @Json var gameServer: String? = null,
  @Json var resourceServer: String? = null,
  @Json var locale: String? = null
)
