# Working Schedule Management in Appointment System

## Overview
I have implemented complete CRUD (Create, Read, Update, Delete) functionality for Working Schedules in the appointment system. This includes both general endpoints and admin-specific endpoints with proper role-based access control.

## Files Created/Modified

### 1. Working_schedule.java (Model/Entity)
- Fixed class name from `tesWorking_schedule` to `Working_schedule`
- Fixed typo in field name from `employes` to `employees`
- This entity represents working schedules with day, start time, and end time

### 2. Working_scheduleDTO.java (Data Transfer Object)
- Created a DTO with validation annotations
- Includes fields: id, day, startTime, endTime
- Used for API requests and responses

### 3. Working_scheduleService.java (Service Layer)
- Enhanced with full CRUD operations
- Added validation to ensure start time is before end time
- Added functionality to manage associations between employees and working schedules:
  - Assign employee to working schedule
  - Remove employee from working schedule
  - Get working schedules for a specific employee
  - Get employees for a specific working schedule

### 4. Working_scheduleController.java (REST Controller)
- Created a dedicated controller with role-based access control
- Public endpoints for viewing working schedules
- Admin/Staff endpoints for managing working schedules
- Admin-only endpoints for managing employee-working schedule associations

### 5. AdminController.java (Updated)
- Integrated all WorkingSchedule endpoints in the admin section
- Added comprehensive CRUD operations for working schedules
- Added employee-working schedule association management endpoints

## API Endpoints

### Public Access:
- `GET /api/working-schedules` - Get all working schedules
- `GET /api/working-schedules/{id}` - Get specific working schedule

### For Admin/Staff:
- `POST /api/working-schedules` - Create working schedule
- `PUT /api/working-schedules/{id}` - Update working schedule
- `DELETE /api/working-schedules/{id}` - Delete working schedule

### For Admin/Staff (Employee-Schedule Relations):
- `GET /api/working-schedules/employees/{employeeId}` - Get working schedules for employee
- `GET /api/working-schedules/{scheduleId}/employees` - Get employees for schedule
- `GET /api/working-schedules/{scheduleId}/staff` - Get staff members for schedule (filtered by role)

### For Admin Only:
- `POST /api/working-schedules/{scheduleId}/employees/{employeeId}` - Assign employee to schedule (only staff allowed)
- `DELETE /api/working-schedules/{scheduleId}/employees/{employeeId}` - Remove employee from schedule (only staff allowed)
- `GET /api/working-schedules/staff` - Get all staff members with their working schedules
- `GET /api/admin/working-schedules/staff` - Admin endpoint to get all staff with working schedules
- `GET /api/admin/working-schedules/{scheduleId}/staff` - Admin endpoint to get staff for specific schedule

### For Admin (via AdminController):
- All the above endpoints are also available under `/api/admin/` for centralized admin access

## Security
- All endpoints follow proper role-based access control
- Public endpoints for viewing schedules
- Admin/Staff endpoints for schedule management
- Admin-only endpoints for employee-schedule associations

## Validation
- Working schedule validation ensures start time is before end time
- Proper error handling for non-existent entities
- Input validation using annotations
- Role validation to ensure only staff members can be assigned to working schedules

## Usage Example
To create a working schedule:
```json
{
  "day": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "17:00:00"
}
```

## Staff-Working Schedule Management
The system now includes comprehensive functionality for managing staff working schedules:

1. **Staff Assignment Validation**: Only users with the STAFF role can be assigned to working schedules
2. **Staff-Specific Endpoints**: Specialized endpoints to retrieve staff members for specific schedules
3. **Comprehensive View**: Admins can view all staff members with their associated working schedules
4. **Role-Based Filtering**: Endpoints that return only staff members (filtered by role) for specific schedules

This allows administrators to effectively manage which staff members work during which times, providing a clear overview of staff availability and scheduling.

## Complete API Documentation

A comprehensive Postman collection has been created that includes all endpoints in the appointment system:

- **Authentication endpoints** (register, login, refresh token, etc.)
- **Admin management endpoints** (user CRUD operations)
- **Working schedule endpoints** (all CRUD and staff association operations)
- **Service endpoints** (service management)
- **Test endpoints** (for development and testing)

The collection is available as `Appointment_System_API_Postman_Collection.json` and includes all necessary variables and authorization headers for testing.

The implementation follows RESTful conventions and integrates seamlessly with the existing appointment system architecture.