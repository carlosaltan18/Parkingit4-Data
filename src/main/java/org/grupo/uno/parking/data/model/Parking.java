package org.grupo.uno.parking.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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

    private String name;
    private String address;
    private String phone;
    private int spaces;

    // Relación ManyToOne con la tabla User
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // El 'optional = false' evita valores nulos en user_id
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false) // Agregado nullable = false para requerir siempre un user_id
    @JsonIgnore // Esto evita que se serialice el usuario en la respuesta
    private User user;

    private Boolean status;

    @Override
    public String toString() {
        return "Parking{" +
                "parkingId=" + parkingId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", spaces=" + spaces +
                ", userId=" + (user != null ? user.getUserId() : "null") + // Asegúrate de verificar si user es nulo
                ", status=" + status +
                '}';
    }
}
