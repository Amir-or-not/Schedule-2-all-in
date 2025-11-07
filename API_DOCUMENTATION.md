# Schedule App API Documentation

## Table of Contents
1. [Authentication](#authentication)
   - [Register User](#register-user)
   - [Login User](#login-user)
2. [Schedule](#schedule)
   - [Get Current User's Schedule](#get-current-users-schedule)
   - [Get Today's Schedule](#get-todays-schedule)
   - [Get Schedule by Day](#get-schedule-by-day)
   - [Create Schedule](#create-schedule)
   - [Update Schedule](#update-schedule)
   - [Delete Schedule](#delete-schedule)

## Base URL
```
http://localhost:8080/api
```

## Authentication

### Register User
- **URL**: `/auth/signup`
- **Method**: `POST`
- **Authentication**: Not required
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "groupId": "group1"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "message": "User registered successfully!"
  }
  ```
- **Error Response (400 Bad Request)**:
  ```json
  {
    "timestamp": "2023-11-01T08:00:00.000+00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Email is already in use!"
  }
  ```

### Login User
- **URL**: `/auth/signin`
- **Method**: `POST`
- **Authentication**: Not required
- **Request Body**:
  ```json
  {
    "email": "test@example.com",
    "password": "password123"
  }
  ```
- **Success Response (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "testuser",
    "email": "test@example.com",
    "role": "ROLE_USER"
  }
  ```
- **Error Response (401 Unauthorized)**:
  ```json
  {
    "timestamp": "2023-11-01T08:00:00.000+00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Bad credentials"
  }
  ```

## Schedule

All schedule endpoints require authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer your_jwt_token_here
```

### Get Current User's Schedule
- **URL**: `/schedule`
- **Method**: `GET`
- **Response (200 OK)**:
  ```json
  [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "dayOfWeek": "Monday",
      "subject": "Mathematics",
      "groupId": "group1",
      "startTime": "09:00",
      "endTime": "10:30",
      "room": "A101",
      "teacher": "John Doe",
      "type": "LECTURE"
    },
    ...
  ]
  ```

### Get Today's Schedule
- **URL**: `/schedule/today`
- **Method**: `GET`
- **Response (200 OK)**: Same format as Get Current User's Schedule

### Get Schedule by Day
- **URL**: `/schedule/day/{dayOfWeek}`
- **Method**: `GET`
- **URL Parameters**:
  - `dayOfWeek`: Day of the week (e.g., "Monday", "Tuesday")
- **Response (200 OK)**: Same format as Get Current User's Schedule

### Create Schedule
- **URL**: `/schedule`
- **Method**: `POST`
- **Required Role**: TEACHER or ADMIN
- **Request Body**:
  ```json
  {
    "dayOfWeek": "Monday",
    "subject": "Mathematics",
    "groupId": "group1",
    "startTime": "09:00",
    "endTime": "10:30",
    "room": "A101",
    "teacher": "John Doe",
    "type": "LECTURE"
  }
  ```
- **Success Response (201 Created)**:
  ```json
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "dayOfWeek": "Monday",
    "subject": "Mathematics",
    "groupId": "group1",
    "startTime": "09:00",
    "endTime": "10:30",
    "room": "A101",
    "teacher": "John Doe",
    "type": "LECTURE"
  }
  ```
- **Error Response (400 Bad Request)**:
  ```json
  {
    "timestamp": "2023-11-01T08:00:00.000+00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Schedule conflicts with existing schedule"
  }
  ```

### Update Schedule
- **URL**: `/schedule/{id}`
- **Method**: `PUT`
- **Required Role**: TEACHER or ADMIN
- **URL Parameters**:
  - `id`: Schedule ID to update
- **Request Body**: Same as Create Schedule
- **Response (200 OK)**: Same as Create Schedule success response

### Delete Schedule
- **URL**: `/schedule/{id}`
- **Method**: `DELETE`
- **Required Role**: TEACHER or ADMIN
- **URL Parameters**:
  - `id`: Schedule ID to delete
- **Success Response (204 No Content)**: No body
- **Error Response (404 Not Found)**:
  ```json
  {
    "timestamp": "2023-11-01T08:00:00.000+00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Schedule not found"
  }
  ```

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "timestamp": "2023-11-01T08:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    "Email is required",
    "Password must be at least 6 characters"
  ]
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2023-11-01T08:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2023-11-01T08:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2023-11-01T08:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found"
}
```

#### 500 Internal Server Error
```json
{
  "timestamp": "2023-11-01T08:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Authentication Flow

1. **Register** a new user (if not already registered)
2. **Login** with the registered credentials to get a JWT token
3. Include the token in the `Authorization` header for all subsequent requests:
   ```
   Authorization: Bearer your_jwt_token_here
   ```
4. The token is valid for 24 hours by default

## Rate Limiting

- **Rate Limit**: 1000 requests per hour per IP address
- **Response Headers**:
  ```
  X-RateLimit-Limit: 1000
  X-RateLimit-Remaining: 999
  X-RateLimit-Reset: 1635768000
  ```

## Versioning

- **Current Version**: v1
- **Versioning Strategy**: URL path (e.g., `/api/v1/schedule`)

## CORS

- **Allowed Origins**: `*` (all origins)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: Authorization, Content-Type, X-Requested-With

## Data Types

### DayOfWeek
One of: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

### ScheduleType
One of: `LECTURE`, `PRACTICE`, `LAB`, `SEMINAR`, `EXAM`

## Pagination

For endpoints that return lists, pagination is supported with the following query parameters:

- `page`: Page number (0-based, default: 0)
- `size`: Number of items per page (default: 20, max: 100)
- `sort`: Sort criteria in the format: `property,asc|desc` (e.g., `startTime,asc`)

Example:
```
GET /api/schedule?page=0&size=10&sort=startTime,asc
```

Response includes pagination metadata:
```json
{
  "content": [
    // schedule items
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "first": true,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "empty": false
}
```
