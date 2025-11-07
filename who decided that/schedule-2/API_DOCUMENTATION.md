# Schedule Backend API Documentation

## Overview
This is a Spring Boot REST API for managing schedules, groups, users, and analytics data.

## Database Schema
- **Schedule**: Contains schedule information with JSON data
- **Group**: Groups belonging to schedules
- **User**: Users with group associations and roles
- **Analytics**: Analytics data with prompts and answers

## API Endpoints

### Health Check
- `GET /api/health` - Check API health status

### Schedules
- `GET /api/schedules` - Get all schedules
- `GET /api/schedules/{scheduleId}` - Get schedule by ID
- `POST /api/schedules` - Create new schedule
- `PUT /api/schedules/{scheduleId}` - Update schedule
- `DELETE /api/schedules/{scheduleId}` - Delete schedule
- `GET /api/schedules/search?name={name}` - Search schedules by name

### Groups
- `GET /api/groups` - Get all groups
- `GET /api/groups/{groupId}` - Get group by ID
- `GET /api/groups/schedule/{scheduleId}` - Get groups by schedule ID
- `POST /api/groups` - Create new group
- `PUT /api/groups/{groupId}` - Update group
- `DELETE /api/groups/{groupId}` - Delete group
- `GET /api/groups/search?name={name}` - Search groups by name

### Users
- `GET /api/users` - Get all users
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `GET /api/users/group/{groupId}` - Get users by group ID
- `GET /api/users/role/{role}` - Get users by role
- `POST /api/users` - Create new user
- `PUT /api/users/{userId}` - Update user
- `DELETE /api/users/{userId}` - Delete user
- `GET /api/users/search?name={name}` - Search users by name

### Analytics
- `GET /api/analytics` - Get all analytics
- `GET /api/analytics/{id}` - Get analytics by ID
- `POST /api/analytics` - Create new analytics
- `PUT /api/analytics/{id}` - Update analytics
- `DELETE /api/analytics/{id}` - Delete analytics
- `GET /api/analytics/search?searchTerm={term}` - Search analytics
- `GET /api/analytics/date-range?startDate={date}&endDate={date}` - Get analytics by date range
- `GET /api/analytics/ordered` - Get analytics ordered by date

## Request/Response Examples

### Create Schedule
```json
POST /api/schedules
{
    "scheduleId": "SCH001",
    "scheduleName": "Test Schedule",
    "data": {
      "key": "value",
      "active": true,
      "count": 1
    }
  }
```

### Create Group
```json
POST /api/groups
{
    "groupId": "group1",
    "scheduleId": "SCH001",
    "groupName": "Test Group",
    "description": "Test Description",
    "data": {
      "key1": "value1",
      "key2": 123,
      "nested": {
        "field": "nested value"
      }
    }
  }

putin
{
    "groupName": "Updated Group Name",
    "description": "Updated Description",
    "data": {
      "key": "new value"
    }
}
```

### Create User
```json
POST /api/users
{
    "userId": "USER001",
    "fullName": "John Doe",
    "password": "password123",
    "email": "john.doe@example.com",
    "groupId": "GRP001",
    "role": "student"
}
```

### Create Analytics
```json
POST /api/analytics
{
    "prompt": "What is the schedule for today?",
    "answer": "Today's schedule includes Math at 9 AM and Science at 2 PM"
}
```

## Database Setup

1. Start PostgreSQL using Docker Compose:
```bash
docker-compose up -d
```

2. The application will automatically create tables based on the JPA entities.

## Running the Application

1. Make sure PostgreSQL is running
2. Update database credentials in `application.properties` if needed
3. Run the application:
```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

## Features

- **Full CRUD operations** for all entities
- **Data validation** with proper error handling
- **Password encryption** using BCrypt
- **Search functionality** for all entities
- **Foreign key relationships** with proper cascading
- **JSON data support** for flexible data storage
- **Cross-origin support** for frontend integration
- **Comprehensive error handling** with meaningful error messages





PUT /api/users/{userId}
Content-Type: application/json

{
  "userId": "user123",
  "fullName": "Updated Name",
  "email": "updated@example.com",
  "role": "STUDENT",
  "groupId": "group123"
}

PUT /api/schedules/{scheduleId}
Content-Type: application/json

{
  "scheduleId": "sched123",
  "scheduleName": "Updated Schedule",
  "data": {
    "key": "value"
  }
}