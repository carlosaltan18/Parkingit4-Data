package org.grupo.uno.parking.data.service;


import org.grupo.uno.parking.data.dto.FareDto;
import org.grupo.uno.parking.data.model.Fare;

import java.util.List;
import java.util.Optional;

public interface IServiceFare {

    List<Fare> getAllFares();
    Optional<Fare> findFareById(Long id);
    void delete(Long idFare);
    void updateFare(FareDto fareDto, Long id);
    Fare addFare(FareDto fareDto);
    Optional<Fare> findByName(String name);
}
