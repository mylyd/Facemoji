package com.facemoji.cut.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import com.facemoji.cut.R

object GradeUtils {
    @JvmOverloads
    fun gotoGooglePlay(context: Context, faildMessage: String? = "", isShort: Boolean = true) {
        try {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.app_gp))
            )
            browserIntent.setClassName(
                "com.android.vending",
                "com.android.vending.AssetBrowserActivity"
            )
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            try {
                val browserIntent2 = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(context.getString(R.string.app_gp))
                )
                context.startActivity(browserIntent2)
            } catch (e1: Exception) {
                if (!TextUtils.isEmpty(faildMessage)) {
                    Toast.makeText(context, faildMessage, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}