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

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "El campo 'car' no puede estar vacío")
    @Size(max = 50, message = "El campo 'car' no puede exceder los 50 caracteres")
    @Column(name = "car", nullable = false)
    private String car;

    @NotBlank(message = "La placa no puede estar vacía")
    @Size(max = 6, message = "La placa no puede exceder los 6 caracteres")
    @Column(name = "plate", nullable = false, unique = true)
    private String plate;

    @Column(name = "status")
    private boolean status;

    @NotNull(message = "La fecha de inicio no puede estar vacía")
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

    @NotNull(message = "El total no puede ser nulo")
    @DecimalMin(value = "0.00", inclusive = false, message = "El total debe ser mayor que cero")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;
}
