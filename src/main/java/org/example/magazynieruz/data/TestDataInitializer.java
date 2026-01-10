package org.example.magazynieruz.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.model.*;
import org.example.magazynieruz.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("lcl")
public class TestDataInitializer implements CommandLineRunner {

    private final OrganisationRepository organisationRepository;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;

    private final Random random = new Random();

    private static final String[] ELECTRONICS = {
            "Laptop Dell XPS", "Monitor Samsung 27\"", "Klawiatura mechaniczna", "Mysz bezprzewodowa",
            "Słuchawki Bluetooth", "Kamera internetowa HD", "Mikrofon USB", "Hub USB-C",
            "Dysk SSD 1TB", "Pendrive 64GB", "Ładowarka bezprzewodowa", "Powerbank 20000mAh"
    };

    private static final String[] OFFICE = {
            "Długopis żelowy", "Marker permanentny", "Zakreślacz fluorescencyjny", "Ołówek automatyczny",
            "Gumka do mazania", "Temperówka metalowa", "Zszywacz biurowy", "Dziurkacz 2-otworowy",
            "Nożyczki biurowe", "Taśma klejąca", "Klej w sztyfcie", "Segregator A4",
            "Teczka kartonowa", "Koperta C5", "Papier A4 500 ark", "Notes A5"
    };

    private static final String[] TOOLS = {
            "Młotek stalowy", "Śrubokręt krzyżakowy", "Klucz nastawny", "Piła ręczna",
            "Wiertarka akumulatorowa", "Poziomica aluminiowa", "Miara zwijana 5m", "Kombinerki",
            "Szczypce uniwersalne", "Nóż tapicerski", "Zestaw kluczy imbusowych", "Latarka LED"
    };

    private static final String[] FOOD = {
            "Kawa ziarnista 1kg", "Herbata Earl Grey", "Cukier biały 1kg", "Sól morska",
            "Makaron penne", "Ryż basmati", "Olej rzepakowy", "Ocet winny",
            "Mąka pszenna", "Kasza gryczana", "Płatki owsiane", "Miód wielokwiatowy"
    };

    private static final String[] DESCRIPTIONS = {
            "Wysokiej jakości produkt", "Bestseller w swojej kategorii", "Nowość w ofercie",
            "Produkt ekologiczny", "Certyfikat ISO", "Gwarancja 24 miesiące",
            "Produkt premium", "Najlepszy stosunek jakości do ceny", "Polecany przez ekspertów",
            "Idealny do użytku domowego i biurowego"
    };

    @Override
    public void run(String... args) throws Exception {
        // Sprawdź czy już są produkty testowe
        if (productRepository.count() > 10) {
            log.info("Test data already exists, skipping initialization");
            return;
        }

        log.info("Starting test data initialization...");

        // Pobierz lub utwórz organizacje
        Organisation org1 = getOrCreateOrganisation("UZ", "1234567890");
        Organisation org2 = getOrCreateOrganisation("TestCorp", "9876543210");

        // Utwórz magazyny
        Warehouse warehouse1 = getOrCreateWarehouse("WH-001", "Magazyn Główny", org1);
        Warehouse warehouse2 = getOrCreateWarehouse("WH-002", "Magazyn Pomocniczy", org1);
        Warehouse warehouse3 = getOrCreateWarehouse("WH-003", "Magazyn TestCorp", org2);

        // Utwórz lokalizacje
        List<Location> locations2 = createLocations(warehouse2, 5);
        List<Location> locations1 = createLocations(warehouse1, 10);
        List<Location> locations3 = createLocations(warehouse3, 5);

        // Dodaj produkty
        createProducts(locations1, ELECTRONICS, 30);
        createProducts(locations1, OFFICE, 40);
        createProducts(locations2, TOOLS, 25);
        createProducts(locations2, FOOD, 30);
        createProducts(locations3, ELECTRONICS, 20);
        createProducts(locations3, OFFICE, 20);

        log.info("Test data initialization completed. Total products: {}", productRepository.count());
    }

    private Organisation getOrCreateOrganisation(String name, String tin) {
        return organisationRepository.findAll().stream()
                .filter(o -> o.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Organisation org = Organisation.builder()
                            .name(name)
                            .TIN(tin)
                            .build();
                    return organisationRepository.save(org);
                });
    }

    private Warehouse getOrCreateWarehouse(String code, String name, Organisation org) {
        return warehouseRepository.findAll().stream()
                .filter(w -> w.getWarehouseCode().equals(code))
                .findFirst()
                .orElseGet(() -> {
                    Warehouse warehouse = Warehouse.builder()
                            .warehouseCode(code)
                            .warehouseName(name)
                            .organisation(org)
                            .isActive(true)
                            .address(StructuredAddress.builder()
                                    .street("ul. Testowa")
                                    .houseNumber(String.valueOf(random.nextInt(100) + 1))
                                    .city("Zielona Góra")
                                    .postcode("65-001")
                                    .latitude(51.9356 + random.nextDouble() * 0.1)
                                    .longitude(15.5062 + random.nextDouble() * 0.1)
                                    .build())
                            .build();
                    return warehouseRepository.save(warehouse);
                });
    }

    private List<Location> createLocations(Warehouse warehouse, int count) {
        List<Location> locations = new ArrayList<>();
        String[] zones = {"A", "B", "C", "D"};
        LocationType[] types = LocationType.values();

        for (int i = 0; i < count; i++) {
            String zone = zones[i % zones.length];
            int row = (i / zones.length) + 1;
            int shelf = (i % 5) + 1;
            String locationCode = String.format("%s%d-%02d-%02d", zone, row, shelf, i + 1);

            Location location = Location.builder()
                    .warehouse(warehouse)
                    .locationCode(locationCode)
                    .zoneName("STREFA_" + zone)
                    .locationType(types[i % types.length])
                    .isActive(true)
                    .isLocked(false)
                    .build();

            locations.add(locationRepository.save(location));
        }

        return locations;
    }

    private void createProducts(List<Location> locations, String[] productNames, int totalCount) {
        for (int i = 0; i < totalCount; i++) {
            String baseName = productNames[i % productNames.length];
            String variant = (i / productNames.length > 0) ? " v" + ((i / productNames.length) + 1) : "";
            String name = baseName + variant;

            Location location = locations.get(i % locations.size());
            if (productRepository.findAll().stream()
                    .anyMatch(p -> p.getName().equals(name) && p.getLocation().equals(location))) {
                continue;
            }

            Product product = Product.builder()
                    .name(name)
                    .description(DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)])
                    .price(generatePrice())
                    .quantity(generateQuantity())
                    .location(location)
                    .build();

            productRepository.save(product);
        }
    }

    private Double generatePrice() {
        double price = 5 + random.nextDouble() * 4995;
        return Math.round(price * 100.0) / 100.0;
    }

    private Integer generateQuantity() {
        if (random.nextInt(100) < 20) {
            return 0;
        }
        return random.nextInt(500) + 1;
    }
}
