package gregoiregeis.wake

import gregoiregeis.wake.helpers.themeParser
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*
import java.util.*

class WakeDatabaseOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "Wake", null, 1) {
    companion object {
        private var instance: WakeDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): WakeDatabaseOpenHelper {
            if (instance == null) {
                instance = WakeDatabaseOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable("Preferences", true,
                "key" to TEXT + PRIMARY_KEY + UNIQUE,
                "value" to TEXT)
        db.createTable("Themes", true,
                "name" to TEXT + PRIMARY_KEY + UNIQUE,
                "font" to TEXT,
                "foreground" to INTEGER,
                "background" to INTEGER)
        db.createTable("OpenedFiles", true,
                "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                "date" to INTEGER,
                "path" to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable("Preferences", true)
        db.dropTable("Themes", true)
        db.dropTable("OpenedFiles", true)
    }
}


val Context.db get() = WakeDatabaseOpenHelper.getInstance(applicationContext)

fun Context.getPreference(key: String) = this.db.use {
    select("Preferences", "value")
            .whereArgs("key = {key}", "key" to key)
            .parseOpt(StringParser)
}

fun Context.getTheme(name: String) = this.db.use {
    select("Themes")
            .whereArgs("name = {name}", "name" to name)
            .parseOpt(themeParser)
}

fun Context.getLastOpenedFiles(count: Int = 10) = this.db.use {
    select("OpenedFiles", "date", "path")
            .orderBy("date")
            .limit(count)
            .parseList(rowParser { date: Long, path: String -> Pair(Date(date), path) })
}

val Context.preferences get() = this.db.use {
    select("Preferences")
            .parseList(rowParser { k: String, v: String -> Pair(k, v) })
}

val Context.openedFiles get() = this.db.use {
    select("OpenedFiles", "date", "path")
            .parseList(rowParser { date: Long, path: String -> Pair(Date(date), path) })
}
