package com.github.droibit.oss_licenses.parser

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP
import okio.Source
import okio.buffer
import okio.source
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

private const val RES_LICENSES_METADATA = "third_party_license_metadata"
private const val RES_LICENSES = "third_party_licenses"

@RestrictTo(LIBRARY_GROUP)
object OssLicenseParser {

    val buildInIgnorePredicate: (String) -> Boolean = {
        it.startsWith("com.android.tools") || it in buildInIgnoreLibraries
    }

    internal val buildInIgnoreLibraries = listOf(
        "org.codehaus.groovy:groovy-all"
    )

    @Throws(IOException::class)
    suspend fun parse(
        context: Context,
        ignoreLibraries: List<String> = emptyList()
    ): List<OssLicense> {
        val appContext = context.applicationContext
        val res = appContext.resources

        val licensesMetaDataResId =
            res.getIdentifier(RES_LICENSES_METADATA, "raw", appContext.packageName)
        val licensesResId =
            res.getIdentifier(RES_LICENSES, "raw", appContext.packageName)

        check(licensesResId != 0 || licensesMetaDataResId != 0) {
            "Third party library license resources generated by OSS Licenses Gradle Plugin dose not exist."
        }

        return suspendCoroutine {
            val result: Result<List<OssLicense>> = try {
                val ossLicense = parse(
                    srcLicenses = res.openRawResource(licensesResId).source(),
                    srcLicensesMetadata = res.openRawResource(licensesMetaDataResId).source(),
                    ignoreLibraries = ignoreLibraries
                )
                Result.success(ossLicense)
            } catch (e: IOException) {
                Result.failure(e)
            }
            it.resumeWith(result)
        }
    }

    internal fun parse(
        srcLicenses: Source,
        srcLicensesMetadata: Source,
        ignoreLibraries: List<String>
    ): List<OssLicense> {
        val licenseMetadata = srcLicensesMetadata.buffer()
            .use {
                mutableListOf<Pair<String, Long>>().apply {
                    while (true) {
                        val line = it.readUtf8Line() ?: break
                        this.add(with(line.split(" ")) {
                            this[1] to this[0].split(":")[1].toLong()
                        })
                    }
                }
            }

        return srcLicenses.buffer()
            .use { bufferedLicense ->
                licenseMetadata.asSequence()
                    .filterNot { (library, licenseByteCount) ->
                        (buildInIgnorePredicate(library) || library in ignoreLibraries)
                            .also {
                                // +1 means the number of bytes of line break.
                                if (it) bufferedLicense.skip(licenseByteCount + 1L)
                            }
                    }
                    .map { (library, licenseByteCount) ->
                        val license = bufferedLicense.readUtf8(licenseByteCount)
                            .also {
                                bufferedLicense.skip(1)
                            }
                        OssLicense(
                            libraryName = library,
                            license = license
                        )
                    }
                    .toList()
            }
    }
}