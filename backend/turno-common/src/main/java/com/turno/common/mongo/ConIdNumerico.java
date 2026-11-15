package com.turno.common.mongo;

// i documenti che la implementano ricevono un id numerico al primo salvataggio
public interface ConIdNumerico {
    Long getId();

    void setId(Long id);
}
