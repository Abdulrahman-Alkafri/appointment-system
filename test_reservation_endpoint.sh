#!/bin/bash

echo "Testing the new appointment reservation endpoint..."

# Test the new endpoint with a sample request
echo "Making a POST request to reserve an appointment..."
curl -X POST http://localhost:8080/api/appointments/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 1,
    "customerId": 1,
    "appointmentDateTime": "2026-01-15T10:00:00"
  }'

echo -e "\n\nTesting with a holiday date (should fail)..."
curl -X POST http://localhost:8080/api/appointments/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 1,
    "customerId": 1,
    "appointmentDateTime": "2026-01-01T10:00:00"
  }'

echo -e "\n\nTesting the existing available slots endpoint..."
curl -X GET "http://localhost:8080/api/appointments/available-slots?serviceId=1&date=2026-01-15" \
  -H "Content-Type: application/json"