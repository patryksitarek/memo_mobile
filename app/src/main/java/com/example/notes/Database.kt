package com.example.notes

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.database.sqlite.SQLiteOpenHelper

object TableInfo: BaseColumns {
    const val TABLE_NAME = "notes"
    const val TABLE_COLUMN_TITLE = "title"
    const val TABLE_COLUMN_CONTENT = "content"
    const val TABLE_COLUMN_DATE = "date"
}

object BasicCommand {
    const val SQL_CREATE_TABLE = "CREATE TABLE ${TableInfo.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${TableInfo.TABLE_COLUMN_TITLE} TEXT NOT NULL," +
            "${TableInfo.TABLE_COLUMN_CONTENT} TEXT NOT NULL," +
            "${TableInfo.TABLE_COLUMN_DATE} TEXT NOT NULL)"

    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ${TableInfo.TABLE_NAME}"

}

//Tworzenie i aktualizacja bazy danych
class DatabaseHelper(context: Context): SQLiteOpenHelper(context, TableInfo.TABLE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(BasicCommand.SQL_CREATE_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(BasicCommand.SQL_DELETE_TABLE)
    }

}