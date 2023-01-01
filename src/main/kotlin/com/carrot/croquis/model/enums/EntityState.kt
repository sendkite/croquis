package com.carrot.croquis.model.enums

import com.fasterxml.jackson.annotation.JsonCreator

enum class EntityState {

    PENDING,
    INACTIVE,
    ACTIVE,
    DELETED;

    companion object {

        @JsonCreator
        @JvmStatic
        fun forValue(code: String): EntityState {

            return values().firstOrNull() { it.name.contentEquals(code) }
                ?: throw Exception()
        }
    }
}