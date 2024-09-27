package org.grupo.uno.parking.data.controller;

import jakarta.annotation.security.RolesAllowed;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("/prueba")
@RestController
public class Prueba {
    private static final Logger logger = LoggerFactory.getLogger(Prueba.class);
    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping("")
    public ResponseEntity<Map<String, String>> getAllRoles() {
        Map<String, String> response = new HashMap<>();
        response.put("MESSAGE", "Esta prueba está funcionanado");
        logger.info("Esto está funcionanadooo");
        return ResponseEntity.ok(response);
    }
}
