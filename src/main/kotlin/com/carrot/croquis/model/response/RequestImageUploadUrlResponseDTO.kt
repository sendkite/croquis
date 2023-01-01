package com.carrot.croquis.model.response

import com.carrot.croquis.model.entity.Image

data class RequestImageUploadUrlResponseDTO(

    var id: Long? = null,
    var contentType: String? = null,
    var uploadUrl: String? = null,
    var url: String? = null
) {
    constructor(image: Image): this() {

        id = image.id
        contentType = image.contentType
        uploadUrl = image.generateWriteOnlyPresignedUrl()
        url = image.generateReadOnlyPresignedUrl()
    }
}
