# Admin Panel Features

## Overview
The Admin Panel provides comprehensive functionality for managing custom endpoints, test suites, and test case steps in the APIQA application.

## Features

### 1. Test Suite Management
- **Create Test Suites**: Define custom test suites with names and descriptions
- **Edit Test Suites**: Modify existing test suite details
- **Delete Test Suites**: Remove test suites and their associated data
- **View Test Suites**: Browse all available test suites

### 2. Custom Endpoint Management
- **Create Custom Endpoints**: Add custom API endpoints with full configuration
- **Edit Custom Endpoints**: Modify endpoint details, methods, and expectations
- **Delete Custom Endpoints**: Remove custom endpoints
- **Link to API Specs**: Associate custom endpoints with existing API specifications
- **Link to Test Suites**: Organize endpoints within test suites

#### Custom Endpoint Configuration
- **Basic Info**: Name, description, HTTP method, endpoint URL
- **Request Configuration**: Request body (JSON format)
- **Response Validation**: Expected status code, response schema, headers
- **Integration**: Link to existing API specs and test suites

### 3. Test Case Step Management
- **Create Test Steps**: Define detailed validation steps for API testing
- **Drag & Drop Reordering**: Visually reorder test steps using drag and drop
- **Step Types**: Support for multiple validation types
- **JSON Path Extraction**: Extract specific values from responses
- **Assertions**: Define various assertion types for validation

#### Supported Step Types

1. **Validate Status Code**
   - Verify HTTP response status codes
   - Expected value: Status code (e.g., 200, 201, 404)

2. **Validate Response Body**
   - Check response body content
   - Expected value: Specific content or pattern

3. **Validate Response Schema**
   - Validate JSON schema compliance
   - Expected value: JSON schema definition

4. **Validate Headers**
   - Check response headers
   - Expected value: Header names and values

5. **Extract Value**
   - Extract specific values from response
   - JSON Path: Path to extract (e.g., $.data.id)
   - Stores extracted value for later use

6. **Assert Value**
   - Assert conditions on extracted or response values
   - JSON Path: Path to the value to assert
   - Assertion Type: Type of assertion to perform
   - Assertion Value: Expected value for comparison

#### Supported Assertion Types

- **Equals**: Exact match
- **Contains**: Contains substring
- **Not Null**: Value is not null
- **Not Empty**: Value is not empty
- **Greater Than**: Numeric comparison
- **Less Than**: Numeric comparison
- **Regex Match**: Regular expression matching

## Usage

### Accessing the Admin Panel
1. Navigate to the main dashboard
2. Click on "Admin Panel" in the sidebar navigation
3. Use the tabbed interface to switch between different management areas

### Creating a Test Suite
1. Click "New Test Suite" button
2. Fill in the name and description
3. Specify the creator
4. Click "Create"

### Creating a Custom Endpoint
1. Click "New Custom Endpoint" button
2. Fill in endpoint details:
   - Name and description
   - HTTP method and URL
   - Request body (if applicable)
   - Expected response details
3. Optionally link to an API spec and test suite
4. Click "Create"

### Creating Test Case Steps
1. Select a test suite or custom endpoint
2. Click "Add Step" button
3. Choose the step type
4. Configure step-specific parameters
5. Set the step order
6. Click "Create"

### Reordering Test Steps
1. Navigate to the Test Case Steps tab
2. Select a test suite or custom endpoint
3. Drag and drop steps to reorder them
4. Changes are automatically saved

## Technical Implementation

### Models
- **CustomEndpoint**: Represents custom API endpoints
- **TestSuite**: Represents test suite containers
- **TestCaseStep**: Represents individual test validation steps

### Services
- **AdminService**: Core business logic for admin operations
- **Repository Layer**: Data access for all admin entities

### Controllers
- **AdminController**: REST endpoints and web interface
- **Form Handling**: Support for both form submissions and AJAX operations

### Frontend
- **Bootstrap 5**: Modern, responsive UI components
- **SortableJS**: Drag and drop functionality for step reordering
- **Thymeleaf**: Server-side templating for dynamic content
- **JavaScript**: Interactive features and AJAX operations

## Database Schema

### Custom Endpoints Table
- id, name, description, http_method, endpoint
- request_body, expected_response_schema, expected_headers
- expected_status_code, created_at, created_by
- api_spec_id (FK), test_suite_id (FK)

### Test Suites Table
- id, name, description, created_at, created_by

### Test Case Steps Table
- id, step_name, description, step_type, expected_value
- json_path, assertion_type, assertion_value, step_order
- created_at, created_by, test_suite_id (FK), custom_endpoint_id (FK)

## API Endpoints

### Test Suites
- `GET /admin/api/test-suites` - Get all test suites
- `GET /admin/api/test-suites/{id}` - Get specific test suite
- `POST /admin/test-suites` - Create test suite
- `POST /admin/test-suites/{id}/update` - Update test suite
- `POST /admin/test-suites/{id}/delete` - Delete test suite

### Custom Endpoints
- `GET /admin/api/custom-endpoints` - Get all custom endpoints
- `GET /admin/api/custom-endpoints/{id}` - Get specific custom endpoint
- `POST /admin/custom-endpoints` - Create custom endpoint
- `POST /admin/custom-endpoints/{id}/update` - Update custom endpoint
- `POST /admin/custom-endpoints/{id}/delete` - Delete custom endpoint

### Test Case Steps
- `GET /admin/api/test-case-steps/test-suite/{testSuiteId}` - Get steps by test suite
- `GET /admin/api/test-case-steps/custom-endpoint/{customEndpointId}` - Get steps by custom endpoint
- `POST /admin/test-case-steps` - Create test case step
- `POST /admin/test-case-steps/{id}/update` - Update test case step
- `POST /admin/test-case-steps/{id}/delete` - Delete test case step
- `POST /admin/api/test-case-steps/reorder` - Reorder steps

## Future Enhancements

1. **Bulk Operations**: Import/export test suites and endpoints
2. **Templates**: Predefined test step templates for common scenarios
3. **Variables**: Support for dynamic variables in test steps
4. **Conditional Logic**: If-then-else logic in test steps
5. **Data Sources**: Integration with external data sources for test data
6. **Reporting**: Enhanced reporting for custom test executions
7. **Versioning**: Version control for test suites and endpoints
8. **Collaboration**: Multi-user editing and approval workflows
