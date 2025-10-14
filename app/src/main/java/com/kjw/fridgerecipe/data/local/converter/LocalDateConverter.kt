package com.kjw.fridgerecipe.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverter {
    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun toLong(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}