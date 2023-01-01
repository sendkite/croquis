package com.carrot.croquis.model.entity

import com.carrot.croquis.model.enums.ImageType
import com.carrot.croquis.util.AmazonS3Util
import jakarta.persistence.*
import org.hibernate.Hibernate
import java.time.Duration

@Entity
@Table(name = "image")
class Image : Auditable() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column
    var referenceId: Long? = null

    @Column
    @Enumerated(EnumType.STRING)
    var type: ImageType = ImageType.DRAWING_IMAGE

    @Column(name = "content_type")
    var contentType: String = ""

    @Column(name = "file_size")
    var size: Long = 0

    @Column(name = "folder_path")
    var folderPath: String = ""

    @Column(name = "filename")
    var filename: String = ""

    @Column(name = "s3_key")
    var key: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Image

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , referenceId = $referenceId, state = $state , createdAt = $createdAt , createdBy = $createdBy , lastModifiedAt = $lastModifiedAt , lastModifiedBy = $lastModifiedBy , type = $type , contentType = $contentType , size = $size , folderPath = $folderPath , filename = $filename , key = $key )"
    }

    fun generateReadOnlyPresignedUrl(duration: Duration = Duration.ofHours(24)): String {

        return AmazonS3Util.generateReadOnlyPresignedUrlV2(type.bucket, key, duration)
    }

    fun generateWriteOnlyPresignedUrl(duration: Duration = Duration.ofHours(24)): String {

        return AmazonS3Util.generateWriteOnlyPresignedUrlV2(type.bucket, key, duration)
    }
}