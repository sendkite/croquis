package com.carrot.croquis.model.enums

import com.carrot.croquis.model.common.constant.Env

enum class ImageType(
    val bucket: String,
    private val folderPath: String
) {
    DRAWING_IMAGE(Env.S3_BUCKET, "drawings/{imageId}");

    fun toFolderPath(imageId: Long): String{
        return this.folderPath.replace("{imageId}", imageId.toString())
    }
}