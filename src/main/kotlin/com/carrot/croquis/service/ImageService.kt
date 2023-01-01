package com.carrot.croquis.service

import com.carrot.croquis.model.request.RequestImageUploadUrl
import com.carrot.croquis.model.response.RequestImageUploadUrlResponseDTO
import com.carrot.croquis.repository.ImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ImageService(
    private val imageRepository: ImageRepository
) {

    @Transactional
    fun requestImageUploadUrl(
        requestUserId: Long?,
        request: RequestImageUploadUrl
    ):RequestImageUploadUrlResponseDTO?  {

        requestUserId ?: return null

        val image = imageRepository.save(request.toDrawingImage())

        return RequestImageUploadUrlResponseDTO(image)
    }
}