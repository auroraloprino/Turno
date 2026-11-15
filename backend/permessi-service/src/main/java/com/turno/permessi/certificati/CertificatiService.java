package com.turno.permessi.certificati;

import java.io.IOException;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.turno.common.security.AuthUser;

@Service
public class CertificatiService {

    public static final String PREFISSO_URL = "/api/permessi/certificato/";
    private static final Set<String> TIPI_AMMESSI = Set.of("application/pdf", "image/png", "image/jpeg");

    private final GridFsTemplate gridFs;

    public CertificatiService(GridFsTemplate gridFs) {
        this.gridFs = gridFs;
    }

    // GridFS spezza il file in blocchi dentro Mongo, così il certificato sta su Atlas insieme al resto 
    public String carica(MultipartFile file, AuthUser utente) {
        if (file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File vuoto");
        if (!TIPI_AMMESSI.contains(file.getContentType()))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Sono ammessi solo PDF, PNG e JPEG");
        try {
            ObjectId id = gridFs.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(),
                    new Document("userId", utente.getId()));
            return PREFISSO_URL + id.toHexString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Salvataggio del certificato fallito");
        }
    }

    public GridFsResource scarica(String id, AuthUser utente) {
        GridFSFile file = trova(id);
        boolean responsabile = utente.getRole().equals("OWNER") || utente.getRole().equals("ADMIN");
        // il dipendente vede solo i suoi certificati, owner e admin quelli di tutti
        if (!responsabile && !utente.getId().equals(proprietario(file)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return gridFs.getResource(file);
    }

    // impedisce di allegare a un proprio permesso il certificato caricato da un altro
    public void verificaProprietario(String certificatoUrl, AuthUser utente) {
        if (certificatoUrl == null || certificatoUrl.isBlank())
            return;
        if (!certificatoUrl.startsWith(PREFISSO_URL))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Certificato non valido");
        GridFSFile file = trova(certificatoUrl.substring(PREFISSO_URL.length()));
        if (!utente.getId().equals(proprietario(file)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Il certificato non è tuo");
    }

    public void eliminaPerUtente(Long userId) {
        gridFs.delete(query(where("metadata.userId").is(userId)));
    }

    private GridFSFile trova(String id) {
        if (!ObjectId.isValid(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        GridFSFile file = gridFs.findOne(query(where("_id").is(new ObjectId(id))));
        if (file == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return file;
    }

    private static Long proprietario(GridFSFile file) {
        Object userId = file.getMetadata() != null ? file.getMetadata().get("userId") : null;
        return userId instanceof Number n ? n.longValue() : null;
    }
}
