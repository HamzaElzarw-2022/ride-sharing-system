import { useState } from 'react';
import MapContainer from '../components/MapContainer';
import Sidebar from '../components/Sidebar';
import { useMonitoring } from '../context/MonitoringContext';
import TripLayer from '../components/map/TripLayer';
import DriversLayer from '../components/map/DriversLayer';
import BaseMap from '../components/map/BaseMap';
import Guide from '../components/Guide';
import CollapsibleSection from '../components/CollapsibleSection';

export default function Dashboard() {
  const { trips, drivers, routes } = useMonitoring();
  const [isGuideOpen, setIsGuideOpen] = useState(true);

  return (
    <div className="flex w-full h-full">
      <Sidebar />
      <div className="flex-1 h-full overflow-hidden relative">
        <MapContainer  fitType='width'>
          <BaseMap />
          <TripLayer trips={trips} routes={routes} />
          <DriversLayer drivers={drivers} trips={trips} />
        </MapContainer>
        <div className="absolute top-5 right-5 bottom-5 md:w-1/3 w-9/12">
          <Guide title="Project Deep Dive" isGuideOpen={isGuideOpen} setIsGuideOpen={setIsGuideOpen}>
            <p className="mb-4 text-sm text-slate-600">
              This guide provides a technical overview of the project, showcasing its architecture and key features. Use the sections below to explore the system's components.
            </p>

            <CollapsibleSection title="Dashboard Features" defaultOpen>
              <p>This dashboard provides a real-time, interactive view of the ride-sharing simulation. Key features include:</p>
              <ul className="list-disc list-inside mt-2 space-y-1 text-sm">
                <li><strong>Interactive Map:</strong> Visualize all drivers and their movements in real-time. Drivers are color-coded by status: green (in-trip), blue (picking up), and gray (available).</li>
                <li><strong>Sidebar Tabs:</strong>
                  <ul className="list-disc list-inside ml-4">
                    <li><strong>Events:</strong> A live feed of system-wide events, such as trip requests and status changes.</li>
                    <li><strong>Trips:</strong> A list of all active and completed trips. Click on any trip to view its detailed route and status.</li>
                    <li><strong>Drivers:</strong> A comprehensive list of all simulated drivers and their current status.</li>
                  </ul>
                </li>
              </ul>
            </CollapsibleSection>

            <CollapsibleSection title="Core Service: Modular Monolith">
              <p>The backend is a Spring Boot-based modular monolith. This design balances monolithic simplicity with modular separation of concerns. Key modules include:</p>
              <ul className="list-disc list-inside mt-2 space-y-1 text-sm">
                <li><strong>Account:</strong> Manages user authentication and roles (RIDER/DRIVER) using JWT.</li>
                <li><strong>Trip:</strong> Handles the entire trip lifecycle, applying clean architecture principles.</li>
                <li><strong>Map:</strong> Provides map data and routing using the A* algorithm on a graph representation of the city map.</li>
                <li><strong>Location:</strong> Tracks real-time driver locations.</li>
              </ul>
            </CollapsibleSection>

            <CollapsibleSection title="Simulation: Autonomous Agents">
              <p>The simulation service populates the world with autonomous agents that interact with the system:</p>
              <ul className="list-disc list-inside mt-2 space-y-1 text-sm">
                <li><strong>Autonomous Drivers:</strong> These agents have distinct identities and behaviors. They listen for trip requests, accept or ignore them, request route, and simulate movement along the calculated route.</li>
                <li><strong>Autonomous Riders:</strong> These agents periodically request trips between random points on the map, simulating real-world user demand.</li>
              </ul>
            </CollapsibleSection>

            <CollapsibleSection title="Event-Driven Architecture">
              <p>The system is heavily event-driven to ensure scalability and loose coupling between components:</p>
              <ul className="list-disc list-inside mt-2 space-y-1 text-sm">
                <li><strong>RabbitMQ:</strong> Acts as the message broker for asynchronous communication between the core service and the simulation service.</li>
                <li><strong>Redis:</strong> Used for real-time geospatial queries to find nearby drivers efficiently and for caching, reducing database load.</li>
                <li><strong>WebSockets:</strong> Stream real-time data from the server to this dashboard for live visualization of drivers and trips.</li>
              </ul>
            </CollapsibleSection>

            <CollapsibleSection title="Tech Stack">
              <p className="font-semibold">Backend:</p>
              <p className="text-sm">Java, Spring Boot, Spring Modulith, PostgreSQL, Redis, RabbitMQ</p>
              <p className="font-semibold mt-2">Frontend:</p>
              <p className="text-sm">React, TypeScript, TailwindCSS, Vite</p>
              <p className="font-semibold mt-2">DevOps:</p>
              <p className="text-sm">Docker, Docker Compose</p>
            </CollapsibleSection>

            <div className="mt-4 p-3 bg-slate-100 rounded-lg">
              <h3 className="font-bold">Try It Yourself!</h3>
              <p className="text-sm mt-1">
                Navigate to the <strong>Rider App</strong> page from the navigation bar to request a ride and see your trip appear on the map!
              </p>
            </div>
          </Guide>
        </div>
      </div>
    </div>
  );
}