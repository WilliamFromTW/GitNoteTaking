package inmethod.gitnotetaking.db


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper


class MyDbHelper(context: Context, name: String,
                 factory: CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(RemoteGitDAO.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase,
                           oldVersion:
                           Int, newVersion: Int) {
        // 刪除原有的表格
        // 待會再回來完成它

        // 呼叫onCreate建立新版的表格
        db.execSQL("DROP TABLE IF EXISTS " + RemoteGitDAO.TABLE_NAME)
        onCreate(db)
    }

    companion object {

        // 資料庫名稱
        val DATABASE_NAME = "gitnotetaking.db"
        // 資料庫版本，資料結構改變的時候要更改這個數字，通常是加一
        val VERSION = 1

        // 需要資料庫的元件呼叫這個函式，這個函式在一般的應用都不需要修改
        fun getDatabase(context: Context): SQLiteDatabase {
            return MyDbHelper(context, DATABASE_NAME, null, VERSION).writableDatabase
        }

    }

}