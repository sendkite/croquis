package com.carrot.croquis.repository

import com.carrot.croquis.model.entity.Image
import org.springframework.data.jpa.repository.JpaRepository

interface ImageRepository: JpaRepository<Image, Long> {
}