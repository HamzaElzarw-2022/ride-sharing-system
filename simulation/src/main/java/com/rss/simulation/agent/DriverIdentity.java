package com.rss.simulation.agent;

public class DriverIdentity {
  private final String username;
  private final String email;
  private final String password;
  private volatile String jwt;

  public DriverIdentity(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
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