package com.carrot.croquis.exception

import java.lang.RuntimeException

class InvalidRequest(message: String): RuntimeException(message) {
}