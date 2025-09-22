# APIQA - API Quality Analysis Tool

A comprehensive Java Spring Boot application for API quality analysis using OpenAPI specifications. The tool automatically generates test cases, executes them, and provides detailed reporting and retry capabilities.

## Features

### 1. Parser Module
- **OpenAPI YAML Upload**: Upload OpenAPI specifications via web interface
- **Automatic Test Generation**: Generates Gherkin feature files with comprehensive test scenarios
- **Test Suite Classification**:
  - **Smoke Tests**: Read-only operations (GET requests)
  - **System/Regression Tests**: All CRUD operations with thorough validation
  - **Integration/E2E Tests**: Endpoint orchestration and workflow testing

### 2. Test Execution Engine
- **On-Demand Testing**: Execute test suites manually
- **Scheduled Testing**: Automated test execution (configurable)
- **Comprehensive Validation**:
  - Status code validation
  - Response body schema validation
  - Response headers validation
  - Response body value extraction and validation

### 3. Dashboard & Reporting
- **Modern Web Interface**: Built with Thymeleaf and Bootstrap 5
- **Real-time Monitoring**: Live test execution status
- **Detailed Reports**: Comprehensive test results and analytics
- **Retry Functionality**: Retry failed tests or entire test suites
- **Historical Data**: View previous test runs and trends

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: H2 (in-memory) with JPA/Hibernate
- **Frontend**: Thymeleaf templates with Bootstrap 5
- **API Parsing**: Swagger Parser v3
- **HTTP Client**: Spring WebFlux
- **Build Tool**: Maven

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Installation & Running

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd apiqa
   ```

2. **Build the application**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**:
   - Web Interface: http://localhost:8080
   - H2 Database Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:mem:apiqa`
     - Username: `sa`
     - Password: `password`

## Usage

### 1. Upload API Specification
1. Navigate to the Dashboard
2. Click "Upload API Spec" button
3. Fill in the specification details:
   - Name and description
   - Version
   - Uploaded by
   - OpenAPI YAML content
4. Click "Upload"

### 2. Generate Test Cases
1. Go to "API Specs" page
2. Find your uploaded specification
3. Click "Generate Tests" button
4. The system will automatically create three types of test suites:
   - Smoke tests (read-only operations)
   - System tests (all CRUD operations)
   - Integration tests (end-to-end workflows)

### 3. Execute Tests
1. Navigate to the specification details page
2. Click "Run Tests" button
3. Provide a name for the test run
4. Monitor the execution in real-time
5. View detailed results and reports

### 4. Retry Failed Tests
1. Go to "Test Runs" page
2. Find failed test runs
3. Click "Retry" button to re-execute only the failed tests

## API Endpoints

### REST API
- `GET /api/specs` - Get all API specifications
- `GET /api/specs/{id}` - Get specific API specification
- `POST /api/specs` - Upload new API specification
- `POST /api/specs/{id}/generate-tests` - Generate test cases
- `DELETE /api/specs/{id}` - Delete API specification

### Web Interface
- `GET /` - Dashboard
- `GET /specs` - API Specifications list
- `GET /specs/{id}` - Specification details
- `GET /test-runs` - Test runs list
- `GET /test-runs/{id}` - Test run details

## Database Schema

The application uses the following main entities:

- **ApiSpec**: Stores OpenAPI specifications
- **FeatureFile**: Generated Gherkin feature files
- **TestScenario**: Individual test scenarios
- **TestRun**: Test execution runs
- **TestExecution**: Individual test executions

## Configuration

The application can be configured via `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:apiqa
    username: sa
    password: password
  h2:
    console:
      enabled: true
server:
  port: 8080
```

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/apiqa/
│   │   ├── controller/     # REST and web controllers
│   │   ├── model/         # JPA entities
│   │   ├── repository/    # Data access layer
│   │   ├── service/       # Business logic
│   │   └── ApiqaApplication.java
│   └── resources/
│       ├── templates/     # Thymeleaf templates
│       └── application.yml
└── test/                  # Test files
```

### Adding New Features
1. Create appropriate model classes
2. Add repository interfaces
3. Implement service layer logic
4. Create controllers for API/web endpoints
5. Add Thymeleaf templates for web interface

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the ISC License - see the LICENSE file for details.

## Support

For issues and questions, please create an issue in the repository or contact the development team.
