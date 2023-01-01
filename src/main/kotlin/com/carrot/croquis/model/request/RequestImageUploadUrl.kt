package com.carrot.croquis.model.request

import com.carrot.croquis.model.common.annotation.RequestParameter
import com.carrot.croquis.model.entity.Image
import com.carrot.croquis.model.enums.EntityState
import com.carrot.croquis.model.enums.ImageType
import com.carrot.croquis.model.enums.RequestParameterType
import org.apache.commons.io.FilenameUtils
import java.time.OffsetDateTime

data class RequestImageUploadUrl(

    var referenceId: Long?,
    var imageType: ImageType = ImageType.DRAWING_IMAGE,
    @RequestParameter(RequestParameterType.REQUIRED) var filename: String? = null,
    @RequestParameter(RequestParameterType.REQUIRED) var size: Long = 0,
    @RequestParameter(RequestParameterType.REQUIRED) var contentType: String? = null
) {

    fun toDrawingImage(): Image {
        val image = Image()
        image.state = EntityState.PENDING
        image.type = ImageType.DRAWING_IMAGE
        image.referenceId = referenceId
        image.contentType = contentType!!
        image.size = size
        image.folderPath = imageType.toFolderPath(referenceId!!)
        image.filename = generateFilename()
        image.key = "${image.folderPath}/${image.filename}"

        return image
    }

    private fun generateFilename(): String {

        val extension = FilenameUtils.getExtension(this.filename)

        return if (extension.isNullOrBlank()) {
            "icon_${OffsetDateTime.now().toEpochSecond()}"
        } else {
            "icon_${OffsetDateTime.now().toEpochSecond()}.${FilenameUtils.getExtension(this.filename)}"
        }
    }
}
