package com.carrot.croquis.model.enums

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class RequestParameterType {

    REQUIRED,
    CONDITIONAL_REQUIRED,
    OPTIONAL;
}