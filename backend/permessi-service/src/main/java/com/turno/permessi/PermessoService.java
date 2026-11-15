package com.turno.permessi;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.turno.permessi.certificati.CertificatiService;
import com.turno.permessi.kafka.PermessoEventProducer;
import com.turno.permessi.kafka.PermessoStatoEvent;
import com.turno.permessi.security.AuthUser;

@Service
public class PermessoService {

    private final PermessoRepository repository;
    private final PermessoEventProducer producer;
    private final TransactionTemplate transazione;
    private final CertificatiService certificati;

    public PermessoService(PermessoRepository repository, PermessoEventProducer producer, TransactionTemplate transazione,
            CertificatiService certificati) {
        this.repository = repository;
        this.producer = producer;
        this.transazione = transazione;
        this.certificati = certificati;
    }

    public PermessoResponse crea(PermessoRequest req, AuthUser user) {
        if (req.dal().isAfter(req.al()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La data di fine deve essere dopo l'inizio");
        certificati.verificaProprietario(req.certificatoUrl(), user);
        Permesso p = new Permesso();
        p.setUserId(user.getId());
        p.setTipo(req.tipo());
        p.setDal(req.dal());
        p.setAl(req.al());
        p.setOraInizio(req.oraInizio());
        p.setOraFine(req.oraFine());
        p.setNote(req.note());
        p.setCertificatoUrl(req.certificatoUrl());
        return PermessoResponse.from(repository.save(p));
    }

    public List<PermessoResponse> miei(Long userId) {
        return repository.findByUserIdOrderByDalDesc(userId).stream().map(PermessoResponse::from).toList();
    }

    public List<PermessoResponse> tutti() {
        return repository.findAllByOrderByDalDesc().stream().map(PermessoResponse::from).toList();
    }

    public List<PermessoResponse> inAttesa() {
        return repository.findByStatoOrderByDalDesc(StatoPermesso.IN_ATTESA).stream().map(PermessoResponse::from)
                .toList();
    }

    public PermessoResponse aggiorna(Long id, StatoPermesso stato) {
        Permesso p = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setStato(stato);
        // nuovo stato ed evento nell'outbox nella stessa transazione, o entrambi o nessuno
        return transazione.execute(tx -> {
            PermessoResponse response = PermessoResponse.from(repository.save(p));
            producer.publish(new PermessoStatoEvent(id, p.getUserId(), stato.name()));
            return response;
        });
    }

    public void eliminaPerUtente(Long userId) {
        repository.deleteByUserId(userId);
        certificati.eliminaPerUtente(userId);
    }

    public PermessoResponse modifica(Long id, PermessoRequest req, AuthUser user) {
        Permesso p = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!p.getUserId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (p.getStato() != StatoPermesso.IN_ATTESA)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Non modificabile");
        p.setTipo(req.tipo());
        p.setDal(req.dal());
        p.setAl(req.al());
        p.setOraInizio(req.oraInizio());
        p.setOraFine(req.oraFine());
        p.setNote(req.note());
        return PermessoResponse.from(repository.save(p));
    }
}
