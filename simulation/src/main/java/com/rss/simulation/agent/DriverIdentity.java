package com.rss.simulation.agent;

public class DriverIdentity {
    private final Integer id;
    private final String username;
    private final String email;
    private final String password;
    private volatile String jwt;
    private Long userId;
    private Long driverId;
    private Long riderId;

    public DriverIdentity(Integer id, String username, String email, String password) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    }

    public Integer getId() {
        return id;
    }
    public String getUsername() {
      return username;
    }
    public String getEmail() {
      return email;
    }
    public String getPassword() {
      return password;
    }
    public Long getDriverId() {
        return driverId;
    }
    public Long getRiderId() {
        return riderId;
    }
    public Long getUserId() {
        return userId;
    }
    public String getJwt() {
        return jwt;
    }
    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }
    public void setRiderId(Long riderId) {
        this.riderId = riderId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public void setJwt(String jwt) {
      this.jwt = jwt;
    }
}