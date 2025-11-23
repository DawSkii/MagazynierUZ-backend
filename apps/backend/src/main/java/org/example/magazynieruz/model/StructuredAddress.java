package org.example.magazynieruz.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructuredAddress {

    private String street;
    private String houseNumber;
    private String apartmentNumber;
    private String postcode;
    private String city;
    private String countryCode;
    private String region;

    @Column(name= "lat")
    private Double latitude;

    @Column(name = "long")
    private Double longitude;
}