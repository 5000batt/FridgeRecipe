package com.kjw.fridgerecipe.domain.model

enum class StorageType(
    val id: String,
) {
    REFRIGERATED("REFRIGERATED"),
    FROZEN("FROZEN"),
    ROOM_TEMPERATURE("ROOM_TEMPERATURE"),
    ;

    companion object {
        fun fromId(id: String?): StorageType = entries.find { it.id == id } ?: REFRIGERATED
    }
}
