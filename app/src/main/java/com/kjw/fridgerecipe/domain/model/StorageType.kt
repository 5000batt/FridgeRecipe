package com.kjw.fridgerecipe.domain.model

import com.kjw.fridgerecipe.R

enum class StorageType(val labelResId: Int, val id: String) {
    REFRIGERATED(R.string.storage_refrigerated, "REFRIGERATED"),
    FROZEN(R.string.storage_frozen, "FROZEN"),
    ROOM_TEMPERATURE(R.string.storage_room_temperature, "ROOM_TEMPERATURE");

    companion object {
        fun fromId(id: String?): StorageType {
            return entries.find { it.id == id } ?: REFRIGERATED
        }
    }
}
