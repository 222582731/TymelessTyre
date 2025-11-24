package za.co.tt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.tt.domain.Address;
import za.co.tt.domain.Enum.AddressType;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserUserId(Long userId);
    List<Address> findByUserUserIdAndAddressType(Long userId, AddressType addressType);
    
    @Query("SELECT a FROM Address a WHERE a.user.userId = :userId AND a.addressType = :addressType")
    Optional<Address> findFirstByUserIdAndAddressType(@Param("userId") Long userId, @Param("addressType") AddressType addressType);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.userId = :userId")
    int countByUserId(@Param("userId") Long userId);
}
