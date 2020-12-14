/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.facemoji.cut.sticker

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.facemoji.cut.BuildConfig

object WhitelistCheck {
    private const val AUTHORITY_QUERY_PARAM = "authority"
    private const val IDENTIFIER_QUERY_PARAM = "identifier"
    private const val STICKER_APP_AUTHORITY = BuildConfig.CONTENT_PROVIDER_AUTHORITY
    const val CONSUMER_WHATSAPP_PACKAGE_NAME = "com.whatsapp"
    const val SMB_WHATSAPP_PACKAGE_NAME = "com.whatsapp.w4b"
    private const val CONTENT_PROVIDER = ".provider.sticker_whitelist_check"
    private const val QUERY_PATH = "is_whitelisted"
    private const val QUERY_RESULT_COLUMN_NAME = "result"
    fun isWhitelisted(context: Context, identifier: String): Boolean {
        return try {
            if (!isWhatsAppConsumerAppInstalled(context.packageManager) && !isWhatsAppSmbAppInstalled(
                    context.packageManager)) {
                return false
            }
            val consumerResult = isStickerPackWhitelistedInWhatsAppConsumer(context, identifier)
            val smbResult = isStickerPackWhitelistedInWhatsAppSmb(context, identifier)
            consumerResult && smbResult
        } catch (e: Exception) {
            false
        }
    }

    private fun isWhitelistedFromProvider(context: Context, identifier: String, whatsappPackageName: String): Boolean {
        val packageManager = context.packageManager
        if (isPackageInstalled(whatsappPackageName, packageManager)) {
            val whatsappProviderAuthority =
                whatsappPackageName + CONTENT_PROVIDER
            val providerInfo = packageManager.resolveContentProvider(
                whatsappProviderAuthority, PackageManager.GET_META_DATA) ?: return false
            val queryUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(whatsappProviderAuthority)
                .appendPath(QUERY_PATH)
                .appendQueryParameter(
                    AUTHORITY_QUERY_PARAM,
                    STICKER_APP_AUTHORITY)
                .appendQueryParameter(IDENTIFIER_QUERY_PARAM, identifier)
                .build()
            context.contentResolver.query(queryUri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val whiteListResult =
                        cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_RESULT_COLUMN_NAME))
                    return whiteListResult == 1
                }
            }
        } else {
            return true
        }
        return false
    }

    private fun isPackageInstalled(packageName: String?, packageManager: PackageManager): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName!!, 0)
            applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isWhatsAppConsumerAppInstalled(packageManager: PackageManager): Boolean =
        isPackageInstalled(CONSUMER_WHATSAPP_PACKAGE_NAME, packageManager)


    fun isWhatsAppSmbAppInstalled(packageManager: PackageManager): Boolean =
       isPackageInstalled(SMB_WHATSAPP_PACKAGE_NAME, packageManager)

    fun isStickerPackWhitelistedInWhatsAppConsumer(context: Context, identifier: String): Boolean  =
       isWhitelistedFromProvider(context, identifier, CONSUMER_WHATSAPP_PACKAGE_NAME)


    fun isStickerPackWhitelistedInWhatsAppSmb(context: Context, identifier: String): Boolean =
       isWhitelistedFromProvider(context, identifier, SMB_WHATSAPP_PACKAGE_NAME)
}