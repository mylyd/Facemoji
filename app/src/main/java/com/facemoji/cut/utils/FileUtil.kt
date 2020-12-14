package com.facemoji.cut.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import com.facemoji.cut.MyApp
import com.facemoji.cut.sticker.StickerPackValidator
import java.io.*
import java.util.*

/**
 * @author : ydli
 * @time : 2020/11/11 8:56
 * @description :
 */
object FileUtil {
    /**
     * 获取去最原始的数据信息
     *
     * @return json data
     */
    fun getStringOfJsonFile(context: Context, file: String?): String? {
        var input: InputStream?
        try {
            input = context.assets.open(file!!)
            return convertStreamToString(input)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * input 流转换为字符串
     *
     * @param is
     * @return
     */
    private fun convertStreamToString(`is`: InputStream): String? {
        var s: String? = null
        try {
            val scanner = Scanner(`is`, "UTF-8").useDelimiter("\\A")
            if (scanner.hasNext()) {
                s = scanner.next()
            }
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return s
    }

    /**
     *
     * @param source
     * @param target
     */
    fun copy(source: File?, target: File?, fileName: String?) {
        var fileInputStream: FileInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            fileInputStream = FileInputStream(source)
            fileOutputStream = FileOutputStream(target)
            val buffer = ByteArray(16384)
            var read: Int
            while (fileInputStream.read(buffer, 0, buffer.size).also { read = it } != -1) {
                fileOutputStream.write(buffer, 0, read)
            }
            //在这里处理 首图压缩处理
            if (fileName == MyApp.webp ){
                compress(target!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileInputStream!!.close()
                fileOutputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // 压缩文件大小
    private fun compress(file: File): File? {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val baos = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.WEBP, 100, baos) // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 50
        while (baos.toByteArray().size / 1024 > 40) { // 循环判断如果压缩后图片是否大于50kb,大于继续压缩
            baos.reset() // 重置baos即清空baos
            bitmap.compress(CompressFormat.WEBP, options, baos) // 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10 // 每次都减少10
        }
        val files = File(file.path)
        FileOutputStream(file).apply {
            write(baos.toByteArray())
            flush()
            close()
        }
        return files
    }

    private fun asset(param: File): ByteArray {
        val bitmap = BitmapFactory.decodeFile(param.path)
        val asset = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.WEBP, 100, asset)
        return asset.toByteArray()
    }

}