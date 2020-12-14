package com.facemoji.cut.sticker

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null,
    DATABASE_VERSION) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        Log.i("DBHelper", "create table if not exist")
        sqLiteDatabase.execSQL(
            "CREATE TABLE IF NOT EXISTS " + table_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "identifier VARCHAR, sticker_file_name VARCHAR,sticker_emoji TEXT)"
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    companion object {
        private const val DATABASE_NAME = "facemoji.db" //数据库名字
        const val table_NAME = "sticker" //数据库名字
        private const val DATABASE_VERSION = 1 //数据库版本号
    }
}