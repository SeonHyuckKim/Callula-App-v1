package org.example.callulaserver.domains.file.service

import org.example.callulaserver.common.logging.Logging
import org.example.callulaserver.config.S3Properties
import org.example.callulaserver.domains.callory.service.CalloryService
import org.slf4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Service
class FileService(
    private val s3Client: S3Client,
    private val s3Properties: S3Properties,
    private val logger: Logger = Logging.getLogger(CalloryService::class.java),
) {
    fun uploadFile(file: MultipartFile): String =
        Logging.logFor(logger) { log ->
            log["action"] = "uploadFileToS3"
            log["original_file_name"] = file.originalFilename ?: "unknown" // ✅ null 처리
            log["file_size"] = file.size

            try {
                val fileName = "${UUID.randomUUID()}_${file.originalFilename}"

                log["file_name"] = fileName

                val putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucketName)
                    .key(fileName)
                    .contentType(file.contentType ?: "application/octet-stream")
                    .build()

                s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.inputStream, file.size)
                )

                val fileUrl = "https://${s3Properties.bucketName}.s3.${s3Properties.region}.amazonaws.com/$fileName"

                log["status"] = "success"
                log["file_url"] = fileUrl

                fileUrl
            } catch (e: Exception) {
                log["status"] = "failed"
                log["error"] = e.message ?: "Unknown error"
                throw Exception("S3 업로드 실패: ${e.message}")
            }
        }
}
