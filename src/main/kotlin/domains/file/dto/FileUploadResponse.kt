package org.example.callulaserver.domains.file.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "파일 업로드 응답")
data class FileUploadResponse(
    @Schema(
        description = "업로드된 파일 URL",
        example = "https://callula-bucket.s3.ap-northeast-2.amazonaws.com/uuid_filename.jpg"
    )
    val fileUrl: String,

    @Schema(description = "파일명", example = "uuid_filename.jpg")
    val fileName: String,

    @Schema(description = "업로드 시간", example = "2026-02-21T19:28:00")
    val uploadedAt: String = java.time.LocalDateTime.now().toString(),
)
