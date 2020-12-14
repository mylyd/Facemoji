package com.facemoji.cut.sticker

import android.content.Context
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.webkit.URLUtil
import com.facemoji.cut.sticker.StickerPackLoader.fetchStickerAsset
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

//import com.facebook.animated.webp.WebPImage;
object StickerPackValidator {
    private const val STICKER_FILE_SIZE_LIMIT_KB = 100
    const val EMOJI_MAX_LIMIT = 3
    private const val EMOJI_MIN_LIMIT = 1
    private const val IMAGE_HEIGHT = 512
    private const val IMAGE_WIDTH = 512
    private const val STICKER_SIZE_MIN = 3
    private const val STICKER_SIZE_MAX = 30
    private const val CHAR_COUNT_MAX = 128
    const val ONE_KIBIBYTE = 8 * 1024.toLong()
    const val TRAY_IMAGE_FILE_SIZE_MAX_KB = 50
    private const val TRAY_IMAGE_DIMENSION_MIN = 24
    private const val TRAY_IMAGE_DIMENSION_MAX = 512
    private const val PLAY_STORE_DOMAIN = "play.google.com"
    private const val APPLE_STORE_DOMAIN = "itunes.apple.com"

    /**
     * Checks whether a sticker pack contains valid data
     */
    @Throws(IllegalStateException::class)
    fun verifyStickerPackValidity(context: Context, stickerPack: StickerPack) {
        check(!TextUtils.isEmpty(stickerPack.identifier)) {
            "sticker pack identifier is empty"
        }
        check(stickerPack.identifier!!.length <= CHAR_COUNT_MAX) {
            "sticker pack identifier cannot exceed $CHAR_COUNT_MAX characters"
        }
        checkStringValidity(stickerPack.identifier!!)
        check(!TextUtils.isEmpty(stickerPack.publisher)) {
            "sticker pack publisher is empty, sticker pack identifier:" + stickerPack.identifier
        }
        check(stickerPack.publisher!!.length <= CHAR_COUNT_MAX) {
            "sticker pack publisher cannot exceed " + CHAR_COUNT_MAX + " characters, sticker pack identifier:" + stickerPack.identifier
        }
        check(!TextUtils.isEmpty(stickerPack.name)) {
            "sticker pack name is empty, sticker pack identifier:" + stickerPack.identifier
        }
        check(stickerPack.name!!.length <= CHAR_COUNT_MAX) {
            "sticker pack name cannot exceed " + CHAR_COUNT_MAX + " characters, sticker pack identifier:" + stickerPack.identifier
        }
        check(!TextUtils.isEmpty(stickerPack.trayImageFile)) {
            "sticker pack tray id is empty, sticker pack identifier:" + stickerPack.identifier
        }
        check(
            !(!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) && !isValidWebsiteUrl(stickerPack.androidPlayStoreLink))) {
            "Make sure to include http or https in url links, android play store link is not a valid url: " + stickerPack.androidPlayStoreLink
        }
        check(
            !(!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) && !isURLInCorrectDomain(stickerPack.androidPlayStoreLink, PLAY_STORE_DOMAIN))) {
            "android play store link should use play store domain: $PLAY_STORE_DOMAIN"
        }
        check(
            !(!TextUtils.isEmpty(stickerPack.iosAppStoreLink) && !isValidWebsiteUrl(stickerPack.iosAppStoreLink))) {
            "Make sure to include http or https in url links, ios app store link is not a valid url: " + stickerPack.iosAppStoreLink
        }
        check(
            !(!TextUtils.isEmpty(stickerPack.iosAppStoreLink) && !isURLInCorrectDomain(stickerPack.iosAppStoreLink, APPLE_STORE_DOMAIN))) {
            "iOS app store link should use app store domain: $APPLE_STORE_DOMAIN"
        }
        check(
            !(!TextUtils.isEmpty(stickerPack.licenseAgreementWebsite) && !isValidWebsiteUrl(stickerPack.licenseAgreementWebsite))) {
            "Make sure to include http or https in url links, license agreement link is not a valid url: " + stickerPack.licenseAgreementWebsite
        }
        check(
            !(!TextUtils.isEmpty(stickerPack.privacyPolicyWebsite) && !isValidWebsiteUrl(stickerPack.privacyPolicyWebsite))) {
            "Make sure to include http or https in url links, privacy policy link is not a valid url: " + stickerPack.privacyPolicyWebsite
        }
        check(
            !(!TextUtils.isEmpty(stickerPack.publisherWebsite) && !isValidWebsiteUrl(stickerPack.publisherWebsite))) {
            "Make sure to include http or https in url links, publisher website link is not a valid url: " + stickerPack.publisherWebsite
        }
        check(
            !(!TextUtils.isEmpty(stickerPack.publisherEmail) && !Patterns.EMAIL_ADDRESS.matcher(stickerPack.publisherEmail).matches())) {
            "publisher email does not seem valid, email is: " + stickerPack.publisherEmail
        }
        try {
            val bytes = fetchStickerAsset(stickerPack.identifier!!, stickerPack.trayImageFile!!, context.contentResolver)
            check(bytes.size <= TRAY_IMAGE_FILE_SIZE_MAX_KB * ONE_KIBIBYTE) {
                "tray image should be less than " + TRAY_IMAGE_FILE_SIZE_MAX_KB + " KB, tray image file: " + stickerPack.trayImageFile
            }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            check(!(bitmap.height > TRAY_IMAGE_DIMENSION_MAX || bitmap.height < TRAY_IMAGE_DIMENSION_MIN)) { "tray image height should between " + TRAY_IMAGE_DIMENSION_MIN + " and " + TRAY_IMAGE_DIMENSION_MAX + " pixels, current tray image height is " + bitmap.height + ", tray image file: " + stickerPack.trayImageFile }
            check(!(bitmap.width > TRAY_IMAGE_DIMENSION_MAX || bitmap.width < TRAY_IMAGE_DIMENSION_MIN)) { "tray image width should be between " + TRAY_IMAGE_DIMENSION_MIN + " and " + TRAY_IMAGE_DIMENSION_MAX + " pixels, current tray image width is " + bitmap.width + ", tray image file: " + stickerPack.trayImageFile }
        } catch (e: IOException) {
            throw IllegalStateException("Cannot open tray image,${stickerPack.trayImageFile} ", e)
        }
        val stickers: List<Sticker>? = stickerPack.stickers
        check(!(stickers!!.size < STICKER_SIZE_MIN || stickers.size > STICKER_SIZE_MAX)) { "sticker pack sticker count should be between 3 to 30 inclusive, it currently has " + stickers.size + ", sticker pack identifier:" + stickerPack.identifier }
        for (sticker in stickers) {
            validateSticker(context, stickerPack.identifier!!, sticker)
        }
    }

    @Throws(IllegalStateException::class)
    private fun validateSticker(context: Context, identifier: String, sticker: Sticker) {
        check(sticker.emojis!!.size <= EMOJI_MAX_LIMIT) { "emoji count exceed limit, sticker pack identifier:" + identifier + ", filename:" + sticker.imageFileName }
        check(!TextUtils.isEmpty(sticker.imageFileName)) { "no file path for sticker, sticker pack identifier:$identifier" }
        validateStickerFile(context, identifier, sticker.imageFileName!!)
    }

    @Throws(IllegalStateException::class)
    private fun validateStickerFile(context: Context, identifier: String, fileName: String) {
        try {
            val bytes = fetchStickerAsset(identifier, fileName, context.contentResolver)
            check(bytes.size <= STICKER_FILE_SIZE_LIMIT_KB * ONE_KIBIBYTE) { "sticker should be less than " + STICKER_FILE_SIZE_LIMIT_KB + "KB, sticker pack identifier:" + identifier + ", filename:" + fileName }
        } catch (e: IOException) {
            throw IllegalStateException("cannot open sticker file: sticker pack identifier:$identifier, filename:$fileName", e)
        }
    }

    private fun checkStringValidity(string: String) {
        val pattern = "[\\w-.,'\\s]+" // [a-zA-Z0-9_-.' ]
        check(string.matches(Regex(pattern))) { "$string contains invalid characters, allowed characters are a to z, A to Z, _ , ' - . and space character" }
        check(!string.contains("..")) { "$string cannot contain .." }
    }

    @Throws(IllegalStateException::class)
    private fun isValidWebsiteUrl(websiteUrl: String?): Boolean {
        try {
            URL(websiteUrl)
        } catch (e: MalformedURLException) {
            Log.e("StickerPackValidator", "url: $websiteUrl is malformed")
            throw IllegalStateException("url: $websiteUrl is malformed", e)
        }
        return URLUtil.isHttpUrl(websiteUrl) || URLUtil.isHttpsUrl(websiteUrl)
    }

    @Throws(IllegalStateException::class)
    private fun isURLInCorrectDomain(urlString: String?, domain: String): Boolean {
        try {
            val url = URL(urlString)
            if (domain == url.host) {
                return true
            }
        } catch (e: MalformedURLException) {
            Log.e("StickerPackValidator", "url: $urlString is malformed")
            throw IllegalStateException("url: $urlString is malformed")
        }
        return false
    }
}