package com.carrot.croquis.model.common.constant

class Env {

    companion object {

        val AWS_REGION = System.getenv("AWS_REGION") ?: ""
        val S3_BUCKET = System.getenv("S3_BUCKET") ?: ""
    }
}