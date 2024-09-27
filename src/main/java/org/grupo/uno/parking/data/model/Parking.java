package org.grupo.uno.parking.data.model;

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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User userId;
    private Boolean status;

    @Override
    public String toString() {
        return "Parking{" +
                "parkingId=" + parkingId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", spaces=" + spaces +
                ", user=" + userId +
                ", status=" + status +
                '}';
    }
}
