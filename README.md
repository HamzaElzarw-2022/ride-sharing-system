# Ride Sharing System

A ride-sharing platform prototype inspired by systems like Uber and Lyft. The project explores building high-speed, scalable systems with real-time trip matching, routing, and monitoring. It includes a ***Spring Boot modular monolith core***, a ***simulation service with autonomous agents***, and a ***React dashboard for live system visualization***. Leverages ***Redis***, ***RabbitMQ***, and ***WebSocket*** for event-driven state management and real-time updates.

A project for fun & self development.

## 📊 Screenshots

<img width="1920" height="1080" alt="Screenshot from 2025-09-23 16-43-10" src="https://github.com/user-attachments/assets/33bea819-56b6-4b59-90f1-8791c1ac67a0" />

---

## 🏗️ Project Structure

```
ride-sharing-system/
│── core/                            # Modular monolithic (Spring Boot web app)
│   ├── account/  (Clean arch.)      # Account Module (Account Management)
│   ├── map/                         # Map Module (Graph, routing, ETA)
│   ├── trip/     (Clean arch.)      # Trip Management (Clean architecture)
│   ├── location/                    # Real-time driver location update with Redis
│   ├── monitoring/                  # Prometheus & Grafana setup
│   ├── CoreApplication.java         # Main entry point
│   ├── Dockerfile                   # Containerization for core
│
│── simulation/                      # Simulation (Spring Boot command line runner)
│   ├── agent                        # Package including rider and driver agents, and thier identities management
│   ├── client                       # Client for calling core endpoint and events listening from RabbitMQ
│   ├── core                         # Simulation engine and agents factory.
│   ├── Dockerfile                   # Containerization for simulation
│
│── dashboard/                       # Live interactive map for monitoring active trips and drivers (React)
│── docker-compose/                  # Compose file (PostgreSQL, Redis, RabbitMQ, Core, Simulation)
```

## 📌 Project Architecture

The system is composed of three main components:

1. **Core Service (Spring Boot Modular Monolith)**

   * Account, Trip, Location, Map, and Monitoring modules
   * Event-driven communication across modules (Spring events, RabbitMQ)
   * Redis for geospatial queries for driver matching and trip requesting management
   * A\* pathfinding for route calculation with point projection to nearest street.
   * PostgreSQL as primary store, Redis for caching and spatial ops

2. **Simulation Service**

   * Configurable drivers and riders
   * Autonomous agents with distinct identities simulating trip requests, acceptance, starting, and ending.
   * Driver agent moving engine for simulating car movement along route fetched from core.
   * Scenario based simulation useful for stress testing.

3. **Monitoring Dashboard (React)**

   * Real-time map visualization with WebSocket streaming
   * Animated driver states and trip lifecycle visualization
   * Interactive controls for exploring the system

---

## ✨ Key Features

* **Trip Management**: Full lifecycle (request → matching → pickup → trip → completion)
* **Routing Engine**: A\* algorithm with speed-aware road networks and nearest street finding.
* **Real-time Location Tracking**: Redis GEO for driver proximity search & updates
* **Event-Driven Design**: Spring application event for modules communication and RabbitMQ for simulation messaging, WebSocket for UI updates
* **Simulation Framework**: Driver/rider agents for realistic, repeatable scenarios
* **Security**: JWT-based authentication with role-based access (RIDER / DRIVER)
* **Visualization**: Smooth map rendering with driver rotations, statuses, and trip overlays

---

## 🛠 Tech Stack

**Backend:** Java 21, Spring Boot 3, Spring Modulith, Caching, Spring Application Events, PostgreSQL, Redis, RabbitMQ
**Frontend:** React, WebSocket, Canvas rendering
**DevOps:** Docker Compose (PostgreSQL, Redis Stack, RabbitMQ, Core, Simulation)

---

## 🚀 Getting Started

### Prerequisites

* Docker & Docker Compose installed
* Node.js (v18+) and npm/yarn installed (for the dashboard)

---

### 1. Run Core + Simulation with Docker Compose

```bash
git clone https://github.com/your-username/ride-sharing-system.git
cd ride-sharing-system
docker-compose --profile simulation up --build
```

* **Core API** → [http://localhost:8080](http://localhost:8080)
* **Simulation Service** is disabled by default. To enable it, run with the simulation profile  

---

### 2. Run the Dashboard Separately

```bash
cd dashboard
npm install
npm run dev
```

* **Dashboard UI** → [http://localhost:5173](http://localhost:5173)

---

## 🔮 Future Improvements

* Health monitoring dashboard and trip metrics with Grafana
* Adding a CI/CD pipeline for deployment
* Support for surge pricing models
* Mobile app integration (React Native demo)

---

## 📖 Learning Goals

This project was built to explore:

* Designing **enterprise-grade modular systems** with clean architecture
* Handling **real-time geospatial data and routing**
* Building **event-driven applications** with RabbitMQ & WebSockets
* Developing **simulation frameworks** for large-scale testing

---
