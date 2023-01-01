package com.carrot.croquis.util

import com.carrot.croquis.model.common.constant.Env
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest
import java.io.File
import java.io.InputStream
import java.time.Duration

object AmazonS3Util {
    private fun s3ClientV2(): S3Client {

        return S3Client
            .builder()
            .region(Region.of(Env.AWS_REGION))
            .build()
    }

    private fun s3PresignerV2(): S3Presigner {

        return S3Presigner
            .builder()
            .region(Region.of(Env.AWS_REGION))
            .build()
    }

    fun exists(bucket: String, key: String): Boolean {

        s3ClientV2().use {
            return try {
                it.headObject(
                    HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
                )
                true
            } catch (ex: NoSuchKeyException) {
                false
            }
        }
    }

    fun getObject(bucket: String, key: String): InputStream {

        s3ClientV2().use {
            return it.getObject(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            )
        }
    }

    fun putObject(s3ClientV2: S3Client, bucket: String, key: String, contentType: String, file: File) {

        s3ClientV2.use {
            it.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build(),
                RequestBody.fromFile(file)
            )
        }
    }

    fun deleteObjects(bucketName: String, keyNames: List<String>) {

        val keys = ArrayList<ObjectIdentifier>()
        keyNames.forEach {
            keys.add(
                ObjectIdentifier.builder()
                    .key(it)
                    .build()
            )
        }

        // Delete multiple objects in one request.
        val deleteRequest: Delete = Delete.builder()
            .objects(keys)
            .build()

        try {
            s3ClientV2().use {
                it.deleteObjects(
                    DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(deleteRequest)
                        .build()
                )
            }
        } catch (e: S3Exception) {
            // todo : Exception
        }
    }

    fun createMultipartUploadV2(
        bucket: String,
        key: String
    ): CreateMultipartUploadResponse {

        return s3ClientV2().createMultipartUpload(
            CreateMultipartUploadRequest.builder().bucket(bucket).key(key).build()
        )
    }

    fun generateReadOnlyPresignedUrlV2(
        bucket: String,
        key: String,
        duration: Duration = Duration.ofMinutes(60)
    ): String {

        return s3PresignerV2().presignGetObject { request: GetObjectPresignRequest.Builder ->
            request
                .getObjectRequest { get -> get.bucket(bucket).key(key) }
                .signatureDuration(duration)
        }.url().toString()
    }

    fun generateWriteOnlyPresignedUrlV2(
        bucket: String,
        key: String,
        duration: Duration = Duration.ofMinutes(60)
    ): String {

        return s3PresignerV2().presignPutObject { request: PutObjectPresignRequest.Builder ->
            request
                .putObjectRequest { put -> put.bucket(bucket).key(key) }
                .signatureDuration(duration)
        }.url().toString()
    }

    fun generateWriteOnlyMultipartPresignedUrlV2(
        bucket: String,
        key: String,
        duration: Duration,
        uploadId: String,
        partNumber: Int
    ): String {

        return s3PresignerV2().presignUploadPart { request: UploadPartPresignRequest.Builder ->
            request.signatureDuration(duration)
                .uploadPartRequest { uploadPartRequest: UploadPartRequest.Builder ->
                    uploadPartRequest.bucket(bucket)
                        .key(key)
                        .partNumber(partNumber)
                        .uploadId(uploadId)
                }
        }.url().toString()
    }
}