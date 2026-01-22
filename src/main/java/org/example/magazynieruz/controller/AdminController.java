package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.dto.location.CreateLocationRequest;
import org.example.magazynieruz.dto.location.LocationResponse;
import org.example.magazynieruz.dto.location.PatchLocationRequest;
import org.example.magazynieruz.dto.organisation.CreateOrganisationRequest;
import org.example.magazynieruz.dto.organisation.OrganisationResponse;
import org.example.magazynieruz.dto.organisation.UpdateOrganisationRequest;
import org.example.magazynieruz.dto.product.CreateProductRequest;
import org.example.magazynieruz.dto.product.PatchProductRequest;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.dto.user.AdminCreateUserRequest;
import org.example.magazynieruz.dto.user.AdminUpdateUserRequest;
import org.example.magazynieruz.dto.user.UserResponse;
import org.example.magazynieruz.dto.warehouse.CreateWarehouseRequest;
import org.example.magazynieruz.dto.warehouse.PatchWarehouseRequest;
import org.example.magazynieruz.dto.warehouse.WarehouseResponse;
import org.example.magazynieruz.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Panel", description = "Admin operations for managing organizations, users and system configuration")
public class AdminController {

    private final OrganisationService organisationService;
    private final AdminUserService adminUserService;
    private final WarehouseService warehouseService;
    private final LocationService locationService;
    private final ProductService productService;

    @PostMapping("/organisations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new organisation", description = "Admin endpoint to create a new organisation")
    public ResponseEntity<OrganisationResponse> createOrganisation(@RequestBody CreateOrganisationRequest request) {
        OrganisationResponse response = organisationService.createOrganisation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/organisations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all organisations", description = "Admin endpoint to retrieve all organisations")
    public ResponseEntity<List<OrganisationResponse>> getAllOrganisations() {
        List<OrganisationResponse> organisations = organisationService.getAllOrganisations();
        return ResponseEntity.ok(organisations);
    }

    @GetMapping("/organisations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get organisation by ID", description = "Admin endpoint to retrieve a specific organisation")
    public ResponseEntity<OrganisationResponse> getOrganisationById(@PathVariable Long id) {
        OrganisationResponse response = organisationService.getOrganisationById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/organisations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update organisation", description = "Admin endpoint to update an existing organisation")
    public ResponseEntity<OrganisationResponse> updateOrganisation(
            @PathVariable Long id,
            @RequestBody UpdateOrganisationRequest request) {
        OrganisationResponse response = organisationService.updateOrganisation(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/organisations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete organisation", description = "Admin endpoint to delete an organisation")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable Long id) {
        organisationService.deleteOrganisation(id);
        return ResponseEntity.noContent().build();
    }

    // ===== USER MANAGEMENT =====

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user", description = "Admin endpoint to create a new user and assign to organisation")
    public ResponseEntity<UserResponse> createUser(@RequestBody AdminCreateUserRequest request) {
        UserResponse response = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Admin endpoint to retrieve all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Admin endpoint to retrieve a specific user")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = adminUserService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Admin endpoint to update user details, organisation, and roles")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUpdateUserRequest request) {
        UserResponse response = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Admin endpoint to delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/organisation/{organisationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign user to organisation", description = "Admin endpoint to assign a user to an organisation")
    public ResponseEntity<UserResponse> assignUserToOrganisation(
            @PathVariable Long userId,
            @PathVariable Long organisationId) {
        UserResponse response = adminUserService.assignUserToOrganisation(userId, organisationId);
        return ResponseEntity.ok(response);
    }

    // ===== WAREHOUSE MANAGEMENT BY ORGANISATION =====

    @GetMapping("/organisations/{organisationId}/warehouses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all warehouses for organisation", description = "Admin endpoint to get all warehouses for a specific organisation")
    public ResponseEntity<List<WarehouseResponse>> getWarehousesByOrganisation(@PathVariable Long organisationId) {
        List<WarehouseResponse> warehouses = warehouseService.getWarehousesByOrganisationId(organisationId);
        return ResponseEntity.ok(warehouses);
    }

    @PostMapping("/organisations/{organisationId}/warehouses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create warehouse for organisation", description = "Admin endpoint to create a warehouse for a specific organisation")
    public ResponseEntity<WarehouseResponse> createWarehouseForOrganisation(
            @PathVariable Long organisationId,
            @RequestBody CreateWarehouseRequest request) {
        WarehouseResponse response = warehouseService.createWarehouseForOrganisation(organisationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/organisations/{organisationId}/warehouses/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get warehouse by ID for organisation", description = "Admin endpoint to get a specific warehouse")
    public ResponseEntity<WarehouseResponse> getWarehouseByIdForOrganisation(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId) {
        WarehouseResponse response = warehouseService.getWarehouseByIdForOrganisation(organisationId, warehouseId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/organisations/{organisationId}/warehouses/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update warehouse for organisation", description = "Admin endpoint to update a warehouse")
    public ResponseEntity<WarehouseResponse> updateWarehouseForOrganisation(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @RequestBody PatchWarehouseRequest request) {
        WarehouseResponse response = warehouseService.updateWarehouseForOrganisation(organisationId, warehouseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/organisations/{organisationId}/warehouses/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete warehouse for organisation", description = "Admin endpoint to delete a warehouse")
    public ResponseEntity<Void> deleteWarehouseForOrganisation(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId) {
        warehouseService.deleteWarehouseForOrganisation(organisationId, warehouseId);
        return ResponseEntity.noContent().build();
    }

    // ===== LOCATION MANAGEMENT BY ORGANISATION =====

    @GetMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all locations for warehouse", description = "Admin endpoint to get all locations in a warehouse")
    public ResponseEntity<List<LocationResponse>> getLocationsByWarehouse(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId) {
        List<LocationResponse> locations = locationService.getLocationsByWarehouseForOrganisation(organisationId, warehouseId);
        return ResponseEntity.ok(locations);
    }

    @PostMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create location in warehouse", description = "Admin endpoint to create a location in a warehouse")
    public ResponseEntity<LocationResponse> createLocationForWarehouse(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @RequestBody CreateLocationRequest request) {
        LocationResponse response = locationService.createLocationForWarehouse(organisationId, warehouseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations/{locationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get location by ID", description = "Admin endpoint to get a specific location")
    public ResponseEntity<LocationResponse> getLocationById(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @PathVariable Long locationId) {
        LocationResponse response = locationService.getLocationByIdForOrganisation(organisationId, warehouseId, locationId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations/{locationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update location", description = "Admin endpoint to update a location")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @PathVariable Long locationId,
            @RequestBody PatchLocationRequest request) {
        LocationResponse response = locationService.updateLocationForOrganisation(organisationId, warehouseId, locationId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations/{locationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete location", description = "Admin endpoint to delete a location")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @PathVariable Long locationId) {
        locationService.deleteLocationForOrganisation(organisationId, warehouseId, locationId);
        return ResponseEntity.noContent().build();
    }

    // ===== PRODUCT MANAGEMENT BY ORGANISATION =====

    @GetMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations/{locationId}/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all products in location", description = "Admin endpoint to get all products in a specific location")
    public ResponseEntity<List<ProductResponse>> getProductsByLocation(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @PathVariable Long locationId) {
        List<ProductResponse> products = productService.getProductsByLocationForOrganisation(organisationId, warehouseId, locationId);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations/{locationId}/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product in location", description = "Admin endpoint to create a product in a specific location")
    public ResponseEntity<ProductResponse> createProductForLocation(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @PathVariable Long locationId,
            @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProductForLocation(organisationId, warehouseId, locationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations/{locationId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get product by ID", description = "Admin endpoint to get a specific product")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @PathVariable Long locationId,
            @PathVariable Long productId) {
        ProductResponse response = productService.getProductByIdForOrganisation(organisationId, warehouseId, locationId, productId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations/{locationId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product", description = "Admin endpoint to update a product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @PathVariable Long locationId,
            @PathVariable Long productId,
            @RequestBody PatchProductRequest request) {
        ProductResponse response = productService.updateProductForOrganisation(organisationId, warehouseId, locationId, productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/organisations/{organisationId}/warehouses/{warehouseId}/locations/{locationId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product", description = "Admin endpoint to delete a product")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long organisationId,
            @PathVariable Long warehouseId,
            @PathVariable Long locationId,
            @PathVariable Long productId) {
        productService.deleteProductForOrganisation(organisationId, warehouseId, locationId, productId);
        return ResponseEntity.noContent().build();
    }
}
