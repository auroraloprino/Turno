package com.turno.turno;

import com.turno.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final UserRepository userRepository;

    public TurnoService(TurnoRepository turnoRepository, UserRepository userRepository) {
        this.turnoRepository = turnoRepository;
        this.userRepository = userRepository;
    }

    public List<TurnoResponse> getRange(LocalDate dal, LocalDate al) {
        return turnoRepository.findByDataBetweenOrderByDataAscInizioAsc(dal, al)
                .stream().map(TurnoResponse::from).toList();
    }

    public List<TurnoResponse> getRangePerUtente(Long userId, LocalDate dal, LocalDate al) {
        return turnoRepository.findByUserIdAndDataBetweenOrderByDataAscInizioAsc(userId, dal, al)
                .stream().map(TurnoResponse::from).toList();
    }

    public TurnoResponse crea(TurnoRequest req) {
        var user = userRepository.findById(req.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato"));
        return TurnoResponse.from(turnoRepository.save(new Turno(user, req.data(), req.inizio(), req.fine())));
    }

    public void elimina(Long id) {
        if (!turnoRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        turnoRepository.deleteById(id);
    }
}
