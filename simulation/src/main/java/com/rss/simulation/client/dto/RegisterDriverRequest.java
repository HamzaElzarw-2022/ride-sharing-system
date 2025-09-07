package com.rss.simulation.client.dto;
public record RegisterDriverRequest(
  String username,
  String email,
  String password,
  String licenseNumber,
  String vehicleDetails
) {}