package com.github.droibit.oss_licenses.parser

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.content.res.ResourcesCompat.ID_NULL
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.Source
import okio.buffer
import okio.source

/**
 * Parses text files containing license information generated by [OSS Licenses Gradle Plugin](https://github.com/google/play-services-plugins/tree/master/oss-licenses-plugin).
 * Specifically, it parses the following two resource files:
 * - `third_party_licenses`
 * - `third_party_licenses_metadata`
 *
 * These files contain information about the third-party libraries used by the application and their respective licenses.
 * The [OssLicenseParser] class extracts this information and provides it as a list of [OssLicense] objects.
 */
class OssLicenseParser(
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
  /**
   * Parses the third-party library license resources generated by OSS Licenses Gradle Plugin.
   *
   * @param excludedLicenses A set of library names that should be excluded from the result.
   * @return A list of [OssLicense] objects.
   */
  @SuppressLint("DiscouragedApi")
  @Throws(IOException::class)
  suspend fun parse(
    context: Context,
    excludedLicenses: Set<String> = emptySet(),
  ): List<OssLicense> {
    val appContext = context.applicationContext
    val res = appContext.resources

    val licensesMetaDataResId =
      res.getIdentifier(RES_LICENSES_METADATA, "raw", appContext.packageName)
    val licensesResId =
      res.getIdentifier(RES_LICENSES, "raw", appContext.packageName)

    check(licensesResId != ID_NULL && licensesMetaDataResId != ID_NULL) {
      "Third party library license resources generated by OSS Licenses Gradle Plugin dose not exist."
    }

    return parseInternal(
      licensesSource = res.openRawResource(licensesResId).source(),
      licensesMetadataSource = res.openRawResource(licensesMetaDataResId).source(),
      excludedLicenses = excludedLicenses,
    )
  }

  @VisibleForTesting
  internal suspend fun parseInternal(
    licensesSource: Source,
    licensesMetadataSource: Source,
    excludedLicenses: Set<String> = emptySet(),
  ): List<OssLicense> = withContext(dispatcher) {
    val licenseBytes = licensesSource.buffer().use(BufferedSource::readByteString)
    return@withContext licensesMetadataSource.buffer().use { source ->
      val ossLicenses = sortedSetOf(
        compareBy(String.CASE_INSENSITIVE_ORDER, OssLicense::libraryName),
      )
      while (!source.exhausted()) {
        ensureActive()

        val line = source.readUtf8Line() ?: break
        val (offsetRange, libraryName) = line.split(" ", limit = 2)
        if (libraryName in excludedLicenses) {
          continue
        }

        val (start, count) = offsetRange
          .split(":")
          .mapNotNull(String::toIntOrNull)
        val end = start + count
        val licenseText = licenseBytes.substring(start, end).utf8()
        ossLicenses += OssLicense(libraryName, licenseText)
      }
      ossLicenses.toList()
    }
  }

  private companion object {
    private const val RES_LICENSES_METADATA = "third_party_license_metadata"
    private const val RES_LICENSES = "third_party_licenses"
  }
}
