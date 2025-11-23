package org.example.magazynieruz.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organisations", schema = "magazynieruz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, unique = true)
    private String name;

    @Column(length = 20, unique = true)
    private String TIN;

    public Organisation(String name) {
        this.name = name;
    }
}