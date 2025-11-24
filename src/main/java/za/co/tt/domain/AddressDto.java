package za.co.tt.domain;

import za.co.tt.domain.Enum.AddressType;

public class AddressDto {
    private Long addressId;
    private String street;
    private String city;
    private String state;
    private int postalCode;
    private String country;
    private AddressType addressType;
    private Long userId;

    // Default constructor
    public AddressDto() {}

    // Constructor for creation (without ID)
    public AddressDto(String street, String city, String state, int postalCode,
                     String country, AddressType addressType, Long userId) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.addressType = addressType;
        this.userId = userId;
    }

    // Full constructor
    public AddressDto(Long addressId, String street, String city, String state, int postalCode,
                     String country, AddressType addressType, Long userId) {
        this.addressId = addressId;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.addressType = addressType;
        this.userId = userId;
    }

    // Getters and setters
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getPostalCode() { return postalCode; }
    public void setPostalCode(int postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public AddressType getAddressType() { return addressType; }
    public void setAddressType(AddressType addressType) { this.addressType = addressType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "AddressDto{" +
                "addressId=" + addressId +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode=" + postalCode +
                ", country='" + country + '\'' +
                ", addressType=" + addressType +
                ", userId=" + userId +
                '}';
    }
}