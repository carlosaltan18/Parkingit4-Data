package org.grupo.uno.parking.data.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.grupo.uno.parking.data.dto.FareDto;
import org.grupo.uno.parking.data.exception.AllDataRequiredException;
import org.grupo.uno.parking.data.exception.DeleteException;
import org.grupo.uno.parking.data.model.Fare;
import org.grupo.uno.parking.data.repository.FareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ServiceFare implements IServiceFare {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFare.class);
    private FareRepository fareRepository;
    @Override
    public List<Fare> getAllFares(){
        return fareRepository.findAll();
    }

    @Override
    public Optional<Fare> findFareById(Long id){
        if(id == null){
            logger.warn("Id not found");
            throw new IllegalArgumentException("Id is necessary");
        }
        return fareRepository.findById(id);
    }

    @Override
    public void delete(Long idFare) {
        if (!fareRepository.existsById(idFare)) {
            logger.error("Fare not found");
            throw new IllegalArgumentException("This fare don't exist");
        }
        try {
            fareRepository.deleteById(idFare);
        } catch (DataAccessException e) {
            logger.error("Fail deliting fare", e);
            throw new DeleteException("Error deleting fare ", e);
        }
    }

    @Override
    public void updateFare(FareDto fareDto, Long id){
        if (!fareRepository.existsById(id)) {
            logger.warn("Fare not exist with this id");
            throw  new EntityNotFoundException("This Fare don't exist");
        }
        Optional<Fare> optionalFare = fareRepository.findById(id);
        if(optionalFare.isPresent()){
            Fare fare = optionalFare.get();
            if(fareDto.getName()!= null) fare.setName(fareDto.getName());
            if(fareDto.getStartTime()!= null) fare.setStartTime(fareDto.getStartTime());
            if(fareDto.getEndTime()!= null) fare.setEndTime(fareDto.getEndTime());
            if(fareDto.getPrice()!= null) fare.setPrice(fareDto.getPrice());
            if(fareDto.getStatus() != null) fare.setStatus(fareDto.getStatus());
            fareRepository.save(fare);
        }
    }

    @Override
    public Fare addFare(FareDto fareDto){
        if(fareDto.getName() == null || fareDto.getStartTime() == null || fareDto.getEndTime() == null || fareDto.getPrice() == null){
            logger.warn("All data is required");
            throw new AllDataRequiredException("All data is required");
        }
        Fare fare = new Fare();
        fare.setName(fareDto.getName());
        fare.setStartTime(fareDto.getStartTime());
        fare.setEndTime(fareDto.getEndTime());
        fare.setPrice(fareDto.getPrice());
        fare.setStatus(true);
        logger.info("Fare created {}", fareDto.getName());
        return fareRepository.save(fare);
    }

    @Override
    public Optional<Fare> findByName(String name){
        return fareRepository.findByName(name);
    }


}
