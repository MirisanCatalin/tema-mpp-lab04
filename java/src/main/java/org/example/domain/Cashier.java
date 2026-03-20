package org.example.domain;

public class Cashier extends Entity<Long>{

    private String username;
    private String password;
    private String fullName;

    public Cashier() {
    }

    public Cashier(Long id,String username,String password,String fullName) {
        super(id);
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "Cashier{id=" + getId() + ", username='" + username + "', fullName='" + fullName + "'}";
    }
}
