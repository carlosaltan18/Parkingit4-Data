package org.grupo.uno.parking.data.model;

import jakarta.persistence.*;
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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "car", nullable = false)
    private String car;

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
    private Parking parking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fare_id", referencedColumnName = "fare_id")
    private Fare fare;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

}
