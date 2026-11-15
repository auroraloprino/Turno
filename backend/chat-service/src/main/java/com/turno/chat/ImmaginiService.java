package com.turno.chat;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mongodb.client.gridfs.model.GridFSFile;

@Service
public class ImmaginiService {

    private static final Set<String> TIPI_AMMESSI = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

    private final GridFsTemplate gridFs;

    public ImmaginiService(GridFsTemplate gridFs) {
        this.gridFs = gridFs;
    }

    // stessa idea dei certificati, l'immagine sta in GridFS legata alla conversazione in cui è stata mandata
    public String salva(MultipartFile file, Long conversazioneId) {
        if (file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File vuoto");
        if (!TIPI_AMMESSI.contains(file.getContentType()))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Sono ammesse solo immagini PNG, JPEG, GIF e WebP");
        try {
            return gridFs.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(),
                    new Document("conversazioneId", conversazioneId)).toHexString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Salvataggio dell'immagine fallito");
        }
    }

    public GridFsResource leggi(String id) {
        return gridFs.getResource(trova(id));
    }

    public Long conversazioneDi(String id) {
        GridFSFile file = trova(id);
        Object conv = file.getMetadata() != null ? file.getMetadata().get("conversazioneId") : null;
        if (conv instanceof Number n)
            return n.longValue();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public void eliminaPerConversazioni(Collection<Long> conversazioneIds) {
        if (!conversazioneIds.isEmpty())
            gridFs.delete(query(where("metadata.conversazioneId").in(conversazioneIds)));
    }

    private GridFSFile trova(String id) {
        if (!ObjectId.isValid(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        GridFSFile file = gridFs.findOne(query(where("_id").is(new ObjectId(id))));
        if (file == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return file;
    }
}
