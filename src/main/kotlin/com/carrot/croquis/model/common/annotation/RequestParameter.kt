package com.carrot.croquis.model.common.annotation

import com.carrot.croquis.model.enums.RequestParameterType

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestParameter(val type: RequestParameterType = RequestParameterType.REQUIRED)
