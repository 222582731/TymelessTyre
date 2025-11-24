package za.co.tt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.tt.domain.Address;
import za.co.tt.domain.AddressDto;
import za.co.tt.domain.User;
import za.co.tt.domain.Enum.AddressType;
import za.co.tt.repository.AddressRepository;
import za.co.tt.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Address save(Address entity) {
        return addressRepository.save(entity);
    }

    @Override
    public Address update(Address entity) {
        if (entity.getAddressId() == null) {
            throw new IllegalArgumentException("Address ID cannot be null for update.");
        }
        if (!addressRepository.existsById(entity.getAddressId())) {
            throw new IllegalArgumentException("Address with ID " + entity.getAddressId() + " not found.");
        }
        return addressRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }
        if (!addressRepository.existsById(id)) {
            throw new IllegalArgumentException("Address with ID " + id + " not found.");
        }
        addressRepository.deleteById(id);
    }

    @Override
    public Address read(Long id) {
        return addressRepository.findById(id).orElse(null);
    }

    @Override
    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    @Override
    public Optional<Address> findById(Long id) {
        return addressRepository.findById(id);
    }

    @Override
    public List<Address> findByUserId(Long userId) {
        return addressRepository.findByUserUserId(userId);
    }

    /**
     * Find addresses by user ID and address type
     */
    public List<Address> findByUserIdAndAddressType(Long userId, AddressType addressType) {
        return addressRepository.findByUserUserIdAndAddressType(userId, addressType);
    }

    /**
     * Get the first address of a specific type for a user
     */
    public Optional<Address> getFirstAddressByType(Long userId, AddressType addressType) {
        return addressRepository.findFirstByUserIdAndAddressType(userId, addressType);
    }

    /**
     * Create a new address for a user from DTO
     */
    public Address createAddressFromDto(AddressDto addressDto) {
        // Validate user exists
        Optional<User> userOpt = userRepository.findById(addressDto.getUserId());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + addressDto.getUserId());
        }

        // Validate address data
        if (!isValidAddress(addressDto)) {
            throw new IllegalArgumentException("Invalid address data provided");
        }

        User user = userOpt.get();
        
        Address address = new Address.Builder()
                .setStreet(addressDto.getStreet())
                .setCity(addressDto.getCity())
                .setState(addressDto.getState())
                .setPostalCode(addressDto.getPostalCode())
                .setCountry(addressDto.getCountry())
                .setAddressType(addressDto.getAddressType())
                .setUser(user)
                .build();

        return addressRepository.save(address);
    }

    /**
     * Update an existing address from DTO
     */
    public Address updateAddressFromDto(AddressDto addressDto) {
        if (addressDto.getAddressId() == null) {
            throw new IllegalArgumentException("Address ID is required for update");
        }

        Optional<Address> existingOpt = addressRepository.findById(addressDto.getAddressId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Address not found with ID: " + addressDto.getAddressId());
        }

        Address existing = existingOpt.get();
        
        // Verify user ownership
        if (!existing.getUser().getUserId().equals(addressDto.getUserId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        // Validate address data
        if (!isValidAddress(addressDto)) {
            throw new IllegalArgumentException("Invalid address data provided");
        }

        Address updatedAddress = new Address.Builder()
                .copy(existing)
                .setStreet(addressDto.getStreet())
                .setCity(addressDto.getCity())
                .setState(addressDto.getState())
                .setPostalCode(addressDto.getPostalCode())
                .setCountry(addressDto.getCountry())
                .setAddressType(addressDto.getAddressType())
                .build();

        return addressRepository.save(updatedAddress);
    }

    /**
     * Validate address data
     */
    private boolean isValidAddress(AddressDto addressDto) {
        return addressDto != null &&
               addressDto.getStreet() != null && !addressDto.getStreet().trim().isEmpty() &&
               addressDto.getCity() != null && !addressDto.getCity().trim().isEmpty() &&
               addressDto.getCountry() != null && !addressDto.getCountry().trim().isEmpty() &&
               addressDto.getPostalCode() > 0 &&
               addressDto.getAddressType() != null &&
               addressDto.getUserId() != null;
    }

    /**
     * Convert Address to AddressDto
     */
    public AddressDto convertToDto(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressDto(
                address.getAddressId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getAddressType(),
                address.getUser() != null ? address.getUser().getUserId() : null
        );
    }

    /**
     * Convert list of addresses to DTOs
     */
    public List<AddressDto> convertToDtoList(List<Address> addresses) {
        return addresses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Check if user has any addresses
     */
    public boolean userHasAddresses(Long userId) {
        return addressRepository.countByUserId(userId) > 0;
    }

    /**
     * Get available address types
     */
    public AddressType[] getAvailableAddressTypes() {
        return AddressType.values();
    }

    /**
     * Delete address with user ownership validation
     */
    public void deleteUserAddress(Long addressId, Long userId) {
        Optional<Address> addressOpt = addressRepository.findById(addressId);
        if (addressOpt.isEmpty()) {
            throw new IllegalArgumentException("Address not found with ID: " + addressId);
        }

        Address address = addressOpt.get();
        if (!address.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        addressRepository.deleteById(addressId);
    }
}
