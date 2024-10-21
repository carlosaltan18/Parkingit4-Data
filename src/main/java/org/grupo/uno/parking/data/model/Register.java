package org.grupo.uno.parking.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "register")
public class Register {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "register_id")
    private long registerId;


    @NotBlank(message = "La placa no puede estar vacía")
    @Size(max = 6, message = "La placa no puede exceder los 6 caracteres")
    @Column(name = "plate", nullable = false, unique = true)
    private String plate;

    @Column(name = "status")
    private boolean status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id", referencedColumnName = "parking_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Parking parking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fare_id", referencedColumnName = "fare_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Fare fare;

    @DecimalMin(value = "0.00", inclusive = false, message = "El total debe ser mayor que cero")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;
}
