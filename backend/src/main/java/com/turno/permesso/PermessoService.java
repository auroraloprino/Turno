package com.turno.permesso;

import com.turno.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PermessoService {

    private final PermessoRepository repository;

    public PermessoService(PermessoRepository repository) {
        this.repository = repository;
    }

    public PermessoResponse crea(PermessoRequest req, User user) {
        Permesso p = new Permesso();
        p.setUser(user);
        p.setTipo(req.tipo());
        p.setDal(req.dal());
        p.setAl(req.al());
        p.setOraInizio(req.oraInizio());
        p.setOraFine(req.oraFine());
        p.setNote(req.note());
        return PermessoResponse.from(repository.save(p));
    }

    public List<PermessoResponse> miei(Long userId) {
        return repository.findByUserIdOrderByDalDesc(userId)
                .stream().map(PermessoResponse::from).toList();
    }

    public List<PermessoResponse> tutti() {
        return repository.findAllByOrderByDalDesc()
                .stream().map(PermessoResponse::from).toList();
    }

    public List<PermessoResponse> inAttesa() {
        return repository.findByStatoOrderByDalDesc(StatoPermesso.IN_ATTESA)
                .stream().map(PermessoResponse::from).toList();
    }

    public PermessoResponse aggiorna(Long id, StatoPermesso stato) {
        Permesso p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setStato(stato);
        return PermessoResponse.from(repository.save(p));
    }
}
