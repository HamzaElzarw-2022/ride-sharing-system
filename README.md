# RideSharing System (in dev)

A modular **ride-sharing system simulation** with a **custom-built map** from [street-map-builder](https://github.com/HamzaElzarw-2022/street-map-builder), real-time **trip matching**, and **driver movement simulation**. This project is currently in development.

A project for fun & self development.

## 🏗️ Project Structure (Planned)  

```
ride-sharing-system/
│── backend/                     # Backend services (Java, Spring Boot)
│   ├── account/                 # Account Module (Account Management, Billing)
│   ├── map/                     # Map Module (Graph, routing, ETA)
│   ├── trip/                    # Trip Management (Matching, billing)
│   ├── location/                # Real-time location updates
│   ├── monitoring/              # Prometheus & Grafana setup
│   ├── RideSharingApplication.java  # Main entry point
│   ├── Dockerfile               # Backend containerization
│
│── simulation/                  # Simulation services (Java)
│   ├── driver-simulator/        # Simulates driver movement
│   ├── user-simulator/          # Simulates user ride requests
│   ├── SimulationRunner.java    # Entry point for simulation
│   ├── Dockerfile               # Containerization for simulation
│
│── rider-app/                   # User-facing app (React Native or React)
│── simulation-dashboard/        # Dashboard for monitoring trips (React)
```

## ✨ Planned Features
- **Custom Map System** → Graph-based roads & routing built from [street-map-builder](https://github.com/HamzaElzarw-2022/street-map-builder)
- **Driver & Rider Simulation** → Real-time movement updates
- **Rider App** → Option for interacting with simulation as a rider
- **Trip Matching & Billing** → Tracks rides & fares
- **Live Monitoring** → Dashboard for tracking trips & system activity

## 🚀 Tech Stack (Planned)
- **Java (Spring Boot)** → Backend services
- **React & React Native** → User & monitoring interfaces
- **Grafana & Prometheus** → System monitoring
- **Swagger** → API Documentation
- **Docker** → Deployment