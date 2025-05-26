@echo off
echo Stopping Kafka Docker Compose stack...
docker compose stop

echo.
echo Pruning unused Docker resources (networks, volumes, etc.)...
docker system prune -f --volumes

echo.
echo Starting Kafka Docker Compose stack...
docker compose up -d

echo.
echo Kafka stack is starting. Access Control Center at http://localhost:9021
pause