/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.facemoji.cut.sticker

import android.annotation.SuppressLint
import android.content.*
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import com.facemoji.cut.BuildConfig
import com.facemoji.cut.MyApp
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class StickerContentProvider : ContentProvider() {
    private var stickerPackList: List<StickerPack>? = null
    private var dbHelper: DBHelper? = null
    private var db: SQLiteDatabase? = null
    private fun initDB() {
        dbHelper = DBHelper(context)
        db = dbHelper!!.writableDatabase
        checkNotNull(db) { "create db failed" }
    }

    override fun onCreate(): Boolean {
        initDB()
        val authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY
        check(authority.startsWith(context!!.packageName)) {
            "your authority (" + authority + ") for the content provider should start with your package name: " + context!!.packageName }

        MATCHER.addURI(authority, METADATA, METADATA_CODE)

        MATCHER.addURI(authority, "$METADATA/*", METADATA_CODE_FOR_SINGLE_PACK)

        MATCHER.addURI(authority, "$STICKERS/*", STICKERS_CODE)
        return true
    }

    private fun getCursorForSingleSticker(uri: Uri): Cursor {
        val path = context!!.getExternalFilesDir(null)!!.absolutePath
        val pathSegments = uri.pathSegments
        require(pathSegments.size == 3) { "path segments should be 3, uri is: $uri" }
        val fileName = pathSegments[pathSegments.size - 1]
        val identifier = pathSegments[pathSegments.size - 2]
        val fullName = "$path/$identifier/$fileName"
        val cursor = MatrixCursor(arrayOf(STICKER_FILE_NAME_IN_QUERY))
        cursor.addRow(arrayOf<Any>(fullName))
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return when (MATCHER.match(uri)) {
            METADATA_CODE -> {
                getPackForAllStickerPacks(uri)
            }
            METADATA_CODE_FOR_SINGLE_PACK -> {
                getCursorForSingleStickerPack(uri)
            }
            STICKERS_CODE -> {
                getStickersForAStickerPack(uri)
            }
            STICKERS_ASSET_CODE -> {
                getCursorForSingleSticker(uri)
            }
            else -> {
                throw IllegalArgumentException("Unknown URI: $uri")
            }
        }
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? = super.openFile(uri, mode)

    @Throws(FileNotFoundException::class)
    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val matchCode = MATCHER.match(uri)
        if (matchCode == STICKERS_ASSET_CODE || matchCode == STICKER_PACK_TRAY_ICON_CODE) {
            val path = context!!.getExternalFilesDir(null)!!.absolutePath
            val pathSegments = uri.pathSegments
            require(pathSegments.size == 3) { "path segments should be 3, uri is: $uri" }
            val fileName = pathSegments[pathSegments.size - 1]
            val identifier = pathSegments[pathSegments.size - 2]
            val file = File("$path/$identifier/$fileName")
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            return AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH)
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        val matchCode = MATCHER.match(uri)
        return when (matchCode) {
            METADATA_CODE -> "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA
            METADATA_CODE_FOR_SINGLE_PACK -> "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA
            STICKERS_CODE -> "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS
            STICKERS_ASSET_CODE -> "image/webp"
            STICKER_PACK_TRAY_ICON_CODE -> "image/webp"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Synchronized
    private fun readContentFile(context: Context) {
        try {
            stickerPackList = MyApp.instance.getStickerPackLists()
            if (stickerPackList == null) {
                throw RuntimeException(" sticker pack list is null")
            }
        } catch (e: IllegalStateException) {
            throw RuntimeException(" file has some issues: " + e.message, e)
        }
    }

    private fun getStickerPackList(): List<StickerPack>? {
        readContentFile(context!!)
        return stickerPackList
    }

    private fun getPackForAllStickerPacks(uri: Uri): Cursor = getStickerPackInfo(uri, getStickerPackList()!!)

    private fun getCursorForSingleStickerPack(uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        for (stickerPack in getStickerPackList()!!) {
            if (identifier == stickerPack.identifier) {
                return getStickerPackInfo(uri, listOf(stickerPack))
            }
        }
        return getStickerPackInfo(uri, ArrayList())
    }

    private fun getStickerPackInfo(uri: Uri, stickerPackList: List<StickerPack>): Cursor {
        val cursor = MatrixCursor(
            arrayOf(
                STICKER_PACK_IDENTIFIER_IN_QUERY,
                STICKER_PACK_NAME_IN_QUERY,
                STICKER_PACK_PUBLISHER_IN_QUERY,
                STICKER_PACK_ICON_IN_QUERY,
                ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                PUBLISHER_EMAIL,
                PUBLISHER_WEBSITE,
                PRIVACY_POLICY_WEBSITE,
                LICENSE_AGREENMENT_WEBSITE,
                IMAGE_DATA_VERSION,
                AVOID_CACHE
            )
        )
        for (stickerPack in stickerPackList) {
            val builder = cursor.newRow()
            builder.add(stickerPack.identifier)
            builder.add(stickerPack.name)
            builder.add(stickerPack.publisher)
            builder.add(stickerPack.trayImageFile)
            builder.add(stickerPack.androidPlayStoreLink)
            builder.add(stickerPack.iosAppStoreLink)
            builder.add(stickerPack.publisherEmail)
            builder.add(stickerPack.publisherWebsite)
            builder.add(stickerPack.privacyPolicyWebsite)
            builder.add(stickerPack.licenseAgreementWebsite)
            builder.add(stickerPack.imageDataVersion)
            builder.add(if (stickerPack.avoidCache) 1 else 0)
        }
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    private fun getStickersForAStickerPack(uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        return if (db != null) {
            val cursor = db!!.query(DBHelper.table_NAME,
                arrayOf(STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY),
                "identifier=?",
                arrayOf(identifier), null, null, null, null)
            cursor.setNotificationUri(context!!.contentResolver, uri)
            cursor
        } else {
            val cursor = MatrixCursor(
                arrayOf(
                    STICKER_FILE_NAME_IN_QUERY,
                    STICKER_FILE_EMOJI_IN_QUERY
                )
            )
            for (stickerPack in getStickerPackList()!!) {
                if (identifier == stickerPack.identifier) {
                    for (sticker in stickerPack.stickers!!) {
                        cursor.addRow(
                            sticker.imageFileName?.let {
                                sticker.emojis?.let { it1 -> TextUtils.join(",", it1) }?.let { it2 ->
                                    arrayOf<Any>(it, it2)
                                }
                            }
                        )
                    }
                }
            }
            cursor.setNotificationUri(context!!.contentResolver, uri)
            cursor
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val code = MATCHER.match(uri)
        return if (code == STICKERS_CODE) db!!.delete(DBHelper.table_NAME, selection, selectionArgs) else 0
    }

    @SuppressLint("Recycle")
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val code = MATCHER.match(uri)
        return if (code == STICKERS_CODE) {
            val identifier = values!!["identifier"] as String
            val imageFile = values[STICKER_FILE_NAME_IN_QUERY] as String
            val cursor = db!!.query(DBHelper.table_NAME, null,
                "$STICKER_FILE_NAME_IN_QUERY=? and identifier=? ",
                arrayOf(imageFile, identifier), null, null, null, null)
            if (cursor != null && cursor.count > 0) {
                return uri
            }
            val id = db!!.insert(DBHelper.table_NAME, null, values)
            val nameUri = ContentUris.withAppendedId(uri, id)
            context!!.contentResolver.notifyChange(nameUri, null)
            nameUri
        } else {
            throw UnsupportedOperationException("Not supported")
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int =
        throw UnsupportedOperationException("Not supported")

    companion object {
        /**
         * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
         */
        const val STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier"
        const val STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name"
        const val STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher"
        const val STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon"
        const val ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link"
        const val IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link"
        const val PUBLISHER_EMAIL = "sticker_pack_publisher_email"
        const val PUBLISHER_WEBSITE = "sticker_pack_publisher_website"
        const val PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website"
        const val LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website"
        const val IMAGE_DATA_VERSION = "image_data_version"
        const val AVOID_CACHE = "whatsapp_will_not_cache_stickers"
        const val STICKER_FILE_NAME_IN_QUERY = "sticker_file_name"
        const val STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji"

        val AUTHORITY_URI by lazy {
            Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
                .appendPath(this.METADATA).build()
        }

        /**
         * Do not change the values in the UriMatcher because otherwise, WhatsApp will not be able to fetch the stickers from the ContentProvider.
         */
        val MATCHER = UriMatcher(UriMatcher.NO_MATCH)
        private const val METADATA = "metadata"
        private const val METADATA_CODE = 1
        private const val METADATA_CODE_FOR_SINGLE_PACK = 2
        const val STICKERS = "stickers"
        private const val STICKERS_CODE = 3
        @JvmField
        var STICKERS_ASSET = "stickers_asset"
        const val STICKERS_ASSET_CODE = 4
        private const val STICKER_PACK_TRAY_ICON_CODE = 5
    }
}