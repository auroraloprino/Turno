package com.turno.permessi;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.turno.permessi.kafka.PermessoEventProducer;
import com.turno.permessi.kafka.PermessoStatoEvent;
import com.turno.permessi.security.AuthUser;

@Service
@Transactional(readOnly = true)
public class PermessoService {

    private final PermessoRepository repository;
    private final PermessoEventProducer producer;

    public PermessoService(PermessoRepository repository, PermessoEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Transactional
    public PermessoResponse crea(PermessoRequest req, AuthUser user) {
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

    @Transactional
    public PermessoResponse aggiorna(Long id, StatoPermesso stato) {
        Permesso p = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setStato(stato);
        PermessoResponse response = PermessoResponse.from(repository.save(p));
        producer.publish(new PermessoStatoEvent(id, p.getUserId(), stato.name()));
        return response;
    }

    @Transactional
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
