package za.co.tt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.tt.domain.Address;
import za.co.tt.domain.AddressDto;
import za.co.tt.domain.Enum.AddressType;
import za.co.tt.service.AddressService;
import za.co.tt.service.UserService;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    @Autowired
    public AddressController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }

    @GetMapping
    public List<Address> getAllAddresses() {
        return addressService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable Long id) {
        Optional<Address> address = addressService.findById(id);
        return address.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDto>> getAddressesByUser(@PathVariable Long userId) {
        try {
            // Verify user exists
            if (userService.findById(userId).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Address> addresses = addressService.findByUserId(userId);
            List<AddressDto> addressDtos = addressService.convertToDtoList(addresses);
            return ResponseEntity.ok(addressDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/type/{addressType}")
    public ResponseEntity<List<AddressDto>> getAddressesByUserAndType(
            @PathVariable Long userId, 
            @PathVariable AddressType addressType) {
        try {
            List<Address> addresses = addressService.findByUserIdAndAddressType(userId, addressType);
            List<AddressDto> addressDtos = addressService.convertToDtoList(addresses);
            return ResponseEntity.ok(addressDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createAddress(@RequestBody AddressDto addressDto) {
        try {
            Address savedAddress = addressService.createAddressFromDto(addressDto);
            AddressDto responseDto = addressService.convertToDto(savedAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create address"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressDto addressDto) {
        try {
            if (!id.equals(addressDto.getAddressId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID mismatch"));
            }

            Address updatedAddress = addressService.updateAddressFromDto(addressDto);
            AddressDto responseDto = addressService.convertToDto(updatedAddress);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update address"));
        }
    }

    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<?> deleteUserAddress(@PathVariable Long id, @PathVariable Long userId) {
        try {
            addressService.deleteUserAddress(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete address"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        try {
            addressService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/types")
    public ResponseEntity<AddressType[]> getAddressTypes() {
        return ResponseEntity.ok(addressService.getAvailableAddressTypes());
    }

    @GetMapping("/user/{userId}/exists")
    public ResponseEntity<Boolean> userHasAddresses(@PathVariable Long userId) {
        try {
            boolean hasAddresses = addressService.userHasAddresses(userId);
            return ResponseEntity.ok(hasAddresses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
