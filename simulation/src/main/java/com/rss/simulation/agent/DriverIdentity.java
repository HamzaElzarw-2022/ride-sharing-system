package com.rss.simulation.agent;

public class DriverIdentity {
    private Long id;
    private final String username;
    private final String email;
    private final String password;
    private volatile String jwt;

    public DriverIdentity(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public String getJwt() {
      return jwt;
    }
    public void setJwt(String jwt) {
      this.jwt = jwt;
    }
}