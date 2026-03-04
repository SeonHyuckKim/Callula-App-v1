package org.example.callulaserver.domains.file.controller

import io.swagger.v3.oas.annotations.Operation
import org.example.callulaserver.domains.file.dto.FileUploadResponse
import org.example.callulaserver.domains.file.service.FileService
import org.example.callulaserver.types.dto.Response
import org.example.callulaserver.types.dto.ResponseProvider
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/file/v1")
@RestController
class FileController(
    private val fileService: FileService,
) {
    @Operation(summary = "파일 업로드", description = "S3에 파일을 업로드합니다")
    @PostMapping("/upload", consumes = ["multipart/form-data"])
    fun uploadFile(
        @RequestParam file: MultipartFile,
    ): Response<FileUploadResponse> {
        val fileUrl = fileService.uploadFile(file)
        val fileName = fileUrl.substringAfterLast("/")

        return ResponseProvider.success(
            FileUploadResponse(
                fileUrl = fileUrl,
                fileName = fileName
            )
        )
    }
}
