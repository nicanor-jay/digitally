package com.example.digitallyapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.digitallyapp.data.CountEntry
import com.example.digitallyapp.data.Counter
import com.example.digitallyapp.data.Note
import com.example.digitallyapp.utils.Converters

@Database(
    entities = [Counter::class, CountEntry::class, Note::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyCountersDatabase : RoomDatabase() {

    abstract fun counterDao(): CountersDao

    companion object {
        @Volatile
        private var Instance: MyCountersDatabase? = null

        fun getDatabase(context: Context): MyCountersDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, MyCountersDatabase::class.java, "counter_database")
                    .build().also { Instance = it }
            }
        }
    }
}