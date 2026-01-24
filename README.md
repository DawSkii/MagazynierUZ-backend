# MagazynierUZ Backend

A comprehensive warehouse and inventory management system backend built with Spring Boot. This application provides a robust, multi-tenant solution for managing warehouses, storage locations, products, and inventory tracking with real-time alerts and reporting capabilities.

## 📋 Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Security](#security)
- [Alert System](#alert-system)
- [Export Features](#export-features)

## ✨ Features

### Core Features
- **Multi-tenant Organization Management** - Support for multiple organizations with isolated data
- **Warehouse Management** - Create and manage multiple warehouses per organization
- **Location Management** - Organize warehouses with storage locations (zones, shelves, bins)
- **Product Inventory** - Track products with quantity, pricing, and descriptions
- **Real-time Stock Monitoring** - Automatic low stock detection and alerts

### Authentication & Security
- **JWT-based Authentication** - Secure token-based authentication
- **Role-based Access Control (RBAC)** - USER and ADMIN roles with fine-grained permissions
- **Multi-tenant Security** - Organization-level data isolation

### Advanced Features
- **Advanced Product Search** - Full-text search with filtering, pagination, and sorting
- **Inventory Export** - Generate PDF reports at organization, warehouse, or location level
- **Low Stock Alerts** - Event-driven email notifications when inventory falls below threshold
- **Comprehensive Admin Panel** - Full system management for administrators
- **Audit Trails** - Automatic timestamps for create/update operations

### API Features
- **RESTful API** - Clean, resource-oriented endpoints
- **OpenAPI/Swagger Documentation** - Interactive API documentation
- **Spring Boot Actuator** - Health checks and application monitoring

## 🛠 Technology Stack

### Core Framework
- **Java 21** - Modern Java with latest features
- **Spring Boot 3.5.6** - Application framework
- **Spring Data JPA 3.4.2** - Database ORM and repositories
- **Spring Security** - Authentication and authorization
- **Spring Boot Actuator** - Application monitoring

### Database
- **PostgreSQL** - Production database
- **H2** - In-memory database for testing
- **Hibernate** - JPA implementation with PostgreSQL dialect

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication (io.jsonwebtoken:jjwt 0.13.0)
- **BCrypt** - Password hashing

### Additional Libraries
- **MapStruct 1.5.5** - Type-safe bean mapping
- **Lombok** - Reduce boilerplate code
- **Spring Validation** - Request validation
- **Spring Mail** - Email notifications
- **iText7 8.0.2** - PDF generation
- **SpringDoc OpenAPI 2.8.14** - API documentation

### Build Tool
- **Gradle** - Build automation and dependency management

## 📦 Prerequisites

Before running the application, ensure you have:

- **Java 21** or higher installed
- **PostgreSQL 12+** database server
- **Gradle** (or use the included Gradle wrapper)
- **SMTP server** access for email alerts (optional)

## 💻 Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd MagazynierUZ-backend
   ```

2. **Configure the database**
   
   Create a PostgreSQL database:
   ```sql
   CREATE DATABASE magazynieruz;
   CREATE SCHEMA magazynieruz;
   ```

3. **Set up environment variables** (see [Configuration](#configuration) section)

4. **Build the project**
   ```bash
   # Using Gradle wrapper (recommended)
   ./gradlew build
   
   # Or if Gradle is installed globally
   gradle build
   ```

## ⚙ Configuration

### Environment Variables

The application requires the following environment variables:

#### Required Variables

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/magazynieruz
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password

# JWT Configuration
jwt.secret=your-secret-key-min-256-bits-base64-encoded
jwt.expiration=86400000  # 24 hours in milliseconds
```

#### Optional Variables (Email Alerts)

```bash
# SMTP Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

# Alert Configuration
ALERT_EMAIL_ENABLED=true
ALERT_EMAIL_FROM=magazynier@gmail.com
ALERT_EMAIL_FROM_NAME=MagazynierUZ Alert System
ALERT_EMAIL_RECIPIENT_1=admin@example.com
ALERT_LOW_STOCK_THRESHOLD=20
ALERT_EVENT_DRIVEN=true
```

### Application Profiles

The application supports multiple profiles for different environments:

- **lcl** (Local) - Default profile for local development
- **stg** (Staging) - Staging environment configuration
- **prd** (Production) - Production environment configuration

Set the active profile:
```bash
# Via environment variable
export active.profile=lcl

# Or via application argument
java -jar app.jar --spring.profiles.active=lcl
```

### Configuration Files

Profile-specific configurations are located in:
- [`src/main/resources/application.yml`](src/main/resources/application.yml) - Base configuration
- [`src/main/resources/application-lcl.yml`](src/main/resources/application-lcl.yml) - Local overrides
- [`src/main/resources/application-stg.yml`](src/main/resources/application-stg.yml) - Staging overrides
- [`src/main/resources/application-prd.yml`](src/main/resources/application-prd.yml) - Production overrides

## 🚀 Running the Application

### Using Gradle

```bash
# Run with default profile (lcl)
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=stg'
```

### Using Java

```bash
# Build the JAR
./gradlew build

# Run the JAR
java -jar build/libs/magazynieruz-0.0.1-SNAPSHOT.jar
```

### Using IDE

Run the main class: [`org.example.magazynieruz.MagazynieruzApplication`](src/main/java/org/example/magazynieruz/MagazynieruzApplication.java)

### Verify the Application

Once started, the application will be available at:
- **Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`

## 📚 API Documentation

### Swagger/OpenAPI

Interactive API documentation is available at `/swagger-ui.html` when the application is running.

### API Endpoints Overview

#### Authentication (`/auth`)
- `POST /auth/register` - Register a new user
- `POST /auth/login` - Authenticate and receive JWT token

#### Warehouses (`/api/v1/warehouses`)
- `GET /api/v1/warehouses` - List user's warehouses
- `GET /api/v1/warehouses/{id}` - Get warehouse details
- `POST /api/v1/warehouses` - Create new warehouse
- `PATCH /api/v1/warehouses/{id}` - Update warehouse
- `DELETE /api/v1/warehouses/{id}` - Delete warehouse

#### Locations (`/api/v1/warehouses/{warehouseId}/locations`)
- `GET /api/v1/warehouses/{warehouseId}/locations` - List locations in warehouse
- `GET /api/v1/warehouses/{warehouseId}/locations/{locationId}` - Get location details
- `POST /api/v1/warehouses/{warehouseId}/locations` - Create new location
- `PATCH /api/v1/warehouses/{warehouseId}/locations/{locationId}` - Update location
- `DELETE /api/v1/warehouses/{warehouseId}/locations/{locationId}` - Delete location

#### Products (`/api/v1/warehouses/{warehouseId}/{locationId}/products`)
- `GET /api/v1/warehouses/{warehouseId}/{locationId}/products` - List products in location
- `GET /api/v1/warehouses/{warehouseId}/{locationId}/products/{productId}` - Get product details
- `POST /api/v1/warehouses/{warehouseId}/{locationId}/products` - Create new product
- `PATCH /api/v1/warehouses/{warehouseId}/{locationId}/products/{productId}` - Update product
- `DELETE /api/v1/warehouses/{warehouseId}/{locationId}/products/{productId}` - Delete product

#### Product Search (`/api/v1/products`)
- `GET /api/v1/products/search` - Advanced product search with filters
  - Query parameters: `query`, `warehouseId`, `locationId`, `minPrice`, `maxPrice`, `minQuantity`, `maxQuantity`, `isAvailable`, `page`, `size`, `sortBy`, `sortDirection`
- `GET /api/v1/products/top10` - Get top 10 products by criteria

#### Inventory Export (`/api/v1/exports`)
- `GET /api/v1/exports/inventory/pdf` - Export inventory to PDF
  - Query parameters: `scope` (ORGANISATION/WAREHOUSE/LOCATION), `warehouseId`, `locationId`

#### Admin Panel (`/api/admin`) - Requires ADMIN role
- **Organizations**: Full CRUD operations
- **Users**: Create, read, update, delete users with role management
- **Warehouses**: Manage warehouses for any organization
- **Locations**: Manage locations across all warehouses
- **Products**: Manage products across all locations

#### User Management (`/api/v1/users`)
- `GET /api/v1/users/me` - Get current user profile
- Additional endpoints for user profile management

### Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```bash
Authorization: Bearer <your-jwt-token>
```

### Example: Login and Access Protected Endpoint

```bash
# 1. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user@example.com",
    "password": "password123"
  }'

# Response: { "token": "eyJhbGciOiJIUzI1NiIs..." }

# 2. Use token to access protected endpoint
curl -X GET http://localhost:8080/api/v1/warehouses \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests "org.example.magazynieruz.service.ProductServiceTest"
```

### Test Coverage

The project includes comprehensive tests for:
- Controllers (Integration tests)
- Services (Unit tests)
- Repositories (Data layer tests)
- Security (Authentication and authorization tests)

Test configuration is located in [`src/test/resources/application.yml`](src/test/resources/application.yml)

### Test Database

Tests use H2 in-memory database with schema initialization from [`src/test/resources/test-schema.sql`](src/test/resources/test-schema.sql)

## 📁 Project Structure

```
MagazynierUZ-backend/
├── src/
│   ├── main/
│   │   ├── java/org/example/magazynieruz/
│   │   │   ├── config/              # Application configuration
│   │   │   │   ├── AlertConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── ...
│   │   │   ├── controller/          # REST controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── WarehouseController.java
│   │   │   │   ├── LocationController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── InventoryExportController.java
│   │   │   │   └── ProductSearchController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── auth/
│   │   │   │   ├── product/
│   │   │   │   ├── warehouse/
│   │   │   │   ├── location/
│   │   │   │   ├── user/
│   │   │   │   └── export/
│   │   │   ├── event/               # Application events
│   │   │   │   └── ProductQuantityChangedEvent.java
│   │   │   ├── exception/           # Global exception handling
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── filter/              # Security filters
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── listener/            # Event listeners
│   │   │   │   └── LowStockAlertListener.java
│   │   │   ├── mapper/              # MapStruct mappers
│   │   │   ├── model/               # JPA entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Organisation.java
│   │   │   │   ├── Warehouse.java
│   │   │   │   ├── Location.java
│   │   │   │   └── Product.java
│   │   │   ├── repository/          # Spring Data repositories
│   │   │   ├── security/            # Security configuration
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── UserContext.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── WarehouseService.java
│   │   │   │   ├── LocationService.java
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── EmailAlertService.java
│   │   │   │   ├── LowStockDetectionService.java
│   │   │   │   ├── InventoryExportService.java
│   │   │   │   └── PdfGenerationService.java
│   │   │   ├── specification/       # JPA Specifications
│   │   │   │   └── ProductSpecification.java
│   │   │   └── MagazynieruzApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-lcl.yml
│   │       ├── application-stg.yml
│   │       └── application-prd.yml
│   └── test/                        # Test classes
│       ├── java/
│       └── resources/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

## 🗄 Database Schema

### Core Entities

#### Organisation
- Multi-tenant isolation
- Contains warehouses and users

#### User
- Belongs to an organisation
- Has roles (USER, ADMIN)
- JWT authentication

#### Warehouse
- Belongs to an organisation
- Has structured address
- Contains locations

#### Location
- Belongs to a warehouse
- Has type (SHELF, ZONE, BIN, etc.)
- Contains products

#### Product
- Belongs to a location
- Tracks quantity, price, description
- Triggers low stock alerts

### Relationships

```
Organisation (1) ──→ (N) Warehouse
Organisation (1) ──→ (N) User
Warehouse (1) ──→ (N) Location
Location (1) ──→ (N) Product
```

## 🔒 Security

### Authentication Flow

1. User registers via `/auth/register`
2. User logs in via `/auth/login` and receives JWT token
3. Client includes token in `Authorization: Bearer <token>` header
4. [`JwtAuthenticationFilter`](src/main/java/org/example/magazynieruz/filter/JwtAuthenticationFilter.java) validates token
5. User context is established for the request

### Authorization

- **PUBLIC** endpoints: `/auth/register`, `/auth/login`
- **USER** role: Access to own organization's data
- **ADMIN** role: Full system access via `/api/admin/**`

### Data Isolation

- Users can only access data within their organization
- Automatic filtering based on authenticated user's organization
- Admin role can access all organizations

## 🔔 Alert System

### Low Stock Alerts

The system automatically monitors inventory levels and sends email alerts when products fall below the configured threshold.

#### Configuration

```yaml
warehouse:
  alert:
    email:
      enabled: true                          # Enable/disable email alerts
      from: magazynier@gmail.com             # Sender email
      from-name: MagazynierUZ Alert System   # Sender name
      recipients:
        - admin@example.com                  # Alert recipients
      subject-prefix: "[MagazynierUZ Alert]" # Email subject prefix
    low-stock:
      threshold: 20                          # Alert when quantity < threshold
      event-driven: true                     # Use event-driven detection
```

#### How It Works

1. When product quantity is updated, a [`ProductQuantityChangedEvent`](src/main/java/org/example/magazynieruz/event/ProductQuantityChangedEvent.java) is published
2. [`LowStockAlertListener`](src/main/java/org/example/magazynieruz/listener/LowStockAlertListener.java) detects the event
3. [`LowStockDetectionService`](src/main/java/org/example/magazynieruz/service/LowStockDetectionService.java) checks if quantity is below threshold
4. [`EmailAlertService`](src/main/java/org/example/magazynieruz/service/EmailAlertService.java) sends formatted email to configured recipients

#### Email Template

Alerts include:
- Product name
- Current quantity
- Alert threshold
- Location and warehouse information

## 📄 Export Features

### PDF Inventory Export

Generate comprehensive PDF reports of inventory at different scopes.

#### Export Scopes

1. **ORGANISATION** - All products across all warehouses and locations
2. **WAREHOUSE** - All products within a specific warehouse
3. **LOCATION** - All products within a specific location

#### Usage

```bash
# Export entire organisation
GET /api/v1/exports/inventory/pdf?scope=ORGANISATION

# Export specific warehouse
GET /api/v1/exports/inventory/pdf?scope=WAREHOUSE&warehouseId=1

# Export specific location
GET /api/v1/exports/inventory/pdf?scope=LOCATION&locationId=5
```

#### PDF Features

- Structured table layout with product details
- Includes quantity, price, location information
- Timestamp and scope information
- Generated using iText7 library

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is part of an academic/educational initiative.

## 📞 Contact

For questions or support, please contact the development team.

---

**MagazynierUZ** - Warehouse Management Made Simple
