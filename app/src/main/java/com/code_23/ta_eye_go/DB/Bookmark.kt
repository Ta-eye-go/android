package com.code_23.ta_eye_go.DB

import android.content.Context
import androidx.room.*

@Entity(tableName = "bookmark",primaryKeys = ["도착정류장ID","도착정류장", "노선번호","현재정류장ID","현재정류장"])
data class Bookmark (
    @ColumnInfo(name = "별칭") val favoriteNm: String,
    @ColumnInfo(name = "현재정류장") val startNodenm: String,
    @ColumnInfo(name = "현재정류장ID") val startNodeID: String,
    @ColumnInfo(name = "도착정류장") val endNodenm: String,
    @ColumnInfo(name = "도착정류장ID") val endNodeID: String,
    @ColumnInfo(name = "노선번호") val routeID: String
){
    constructor(): this( "0","","","","","")
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark")
    fun getAll(): List<Bookmark>

    @Insert(onConflict = OnConflictStrategy.IGNORE) // 추가할 때 동일한 기록이면 무시하기
    fun insert(bookmark: Bookmark)

    @Query("UPDATE bookmark SET 별칭 = (:new_favoriteNm) where 별칭 = (:favoriteNm)")
    fun updateBookmark(favoriteNm: String, new_favoriteNm: String)

    // 즐겨찾기DB에서 별칭의 데이터를 가져와줌
    @Query("SELECT * FROM bookmark where 별칭 = (:favoriteNm)")
    fun bookmarkdata(favoriteNm: String): List<Bookmark>

    @Query("DELETE from bookmark where 별칭 = (:favoriteNm)")
    fun delete(favoriteNm: String)

    @Query("DELETE from bookmark")
    fun deleteAll()
}

@Database(entities = [Bookmark::class], version = 10)
abstract class BookmarkDB: RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        private var INSTANCE: BookmarkDB? = null

        fun getInstance(context: Context): BookmarkDB? {
            if (INSTANCE == null) {
                synchronized(BookmarkDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        BookmarkDB::class.java, "bookmark.db")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries().build()
                }
            }
            return INSTANCE
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}