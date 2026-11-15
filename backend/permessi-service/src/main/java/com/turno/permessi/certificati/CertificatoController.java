package com.turno.permessi.certificati;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.turno.common.security.AuthUser;

@RestController
@RequestMapping("/api/permessi/certificato")
public class CertificatoController {

    private final CertificatiService certificati;

    public CertificatoController(CertificatiService certificati) {
        this.certificati = certificati;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> carica(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("certificatoUrl", certificati.carica(file, user)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> scarica(
            @PathVariable String id,
            @AuthenticationPrincipal AuthUser user) throws IOException {
        GridFsResource file = certificati.scarica(id, user);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.getFilename(), StandardCharsets.UTF_8).build().toString())
                .body(new InputStreamResource(file.getInputStream()));
    }
}
