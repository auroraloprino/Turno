package com.turno.permesso;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.turno.user.User;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permessi")
public class PermessoController {

    private final PermessoService service;
    private final MinioClient minio;
    private final MinioClient minioPublicClient;

    @Value("${minio.bucket}")
    private String bucket;

    public PermessoController(PermessoService service,
            MinioClient minio,
            @org.springframework.beans.factory.annotation.Qualifier("minioPublicClient") MinioClient minioPublicClient) {
        this.service = service;
        this.minio = minio;
        this.minioPublicClient = minioPublicClient;
    }

    @PostMapping
    public ResponseEntity<PermessoResponse> crea(
            @Valid @RequestBody PermessoRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crea(request, user));
    }

    @PostMapping(value = "/certificato", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCertificato(@RequestParam("file") MultipartFile file) {
        String objectName = "certificati/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            minio.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            String url = minioPublicClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket).object(objectName).method(Method.GET)
                    .expiry(7, TimeUnit.DAYS).build());
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload fallito: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<PermessoResponse>> miei(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.miei(user.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<PermessoResponse>> tutti() {
        return ResponseEntity.ok(service.tutti());
    }

    @GetMapping("/in-attesa")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<List<PermessoResponse>> inAttesa() {
        return ResponseEntity.ok(service.inAttesa());
    }

    @PatchMapping("/{id}/stato")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<PermessoResponse> aggiorna(
            @PathVariable Long id,
            @RequestParam StatoPermesso stato) {
        return ResponseEntity.ok(service.aggiorna(id, stato));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PermessoResponse> modifica(
            @PathVariable Long id,
            @Valid @RequestBody PermessoRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.modifica(id, request, user));
    }
}
