package org.grupo.uno.parking.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "parking")
public class Parking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_id")
    private long parkingId;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String name;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(max = 150, message = "La dirección no puede exceder los 150 caracteres")
    private String address;

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[0-9]{8}$", message = "El teléfono debe contener exactamente 8 dígitos numéricos y no puede contener letras ni caracteres especiales")
    private String phone;

    @Min(value = 1, message = "El número de espacios debe ser al menos 1")
    private int spaces;


    private Boolean status;

    @Override
    public String toString() {
        return "Parking{" +
                "parkingId=" + parkingId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", spaces=" + spaces +
                ", status=" + status +
                '}';
    }
}
