package org.example.domain;

public class Customer extends Entity<Long>{
    private String name;
    private String address;

    public Customer() {
    }

    public Customer(Long id, String name, String address) {
        super(id);
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", name='" + name + "', address='" + address + "'}";
    }
}
