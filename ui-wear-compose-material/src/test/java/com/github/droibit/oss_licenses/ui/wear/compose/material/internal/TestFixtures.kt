package com.github.droibit.oss_licenses.ui.wear.compose.material.internal

import com.github.droibit.oss_licenses.parser.OssLicense

object TestFixtures {
  val aospPhotoViewer: OssLicense =
    OssLicense("AOSP PhotoViewer", "http://www.apache.org/licenses/LICENSE-2.0")
  val abseil: OssLicense = OssLicense("Abseil", "http://www.apache.org/licenses/LICENSE-2.0")
  val androidDeviceOriginLibrary: OssLicense =
    OssLicense("Android Device Origin Library", "http://www.apache.org/licenses/LICENSE-2.0")
  val licenses: List<OssLicense> = listOf(aospPhotoViewer, abseil, androidDeviceOriginLibrary)
}
