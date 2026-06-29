package com.marketplace.user.service;

import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import com.marketplace.common.exception.ApiException;
import com.marketplace.user.dto.AddressListResponse;
import com.marketplace.user.dto.AddressRequest;
import com.marketplace.user.dto.AddressResponse;
import com.marketplace.user.entity.UserAddress;
import com.marketplace.user.repository.UserAddressRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final UserAddressRepository addressRepository;

    public AddressService(UserAddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public AddressListResponse list() {
        UUID userId = currentUserId();
        List<AddressResponse> data = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtAsc(userId).stream()
                .map(this::toResponse)
                .toList();
        return new AddressListResponse(data);
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        UUID userId = currentUserId();
        if (request.isDefault()) {
            clearDefault(userId);
        } else if (addressRepository.countByUserId(userId) == 0) {
            // first address becomes default
        }
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        applyRequest(address, request);
        if (addressRepository.countByUserId(userId) == 0) {
            address.setDefault(true);
        }
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(UUID addressId, AddressRequest request) {
        UUID userId = currentUserId();
        UserAddress address = getOwned(addressId, userId);
        if (request.isDefault()) {
            clearDefault(userId);
        }
        applyRequest(address, request);
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(UUID addressId) {
        UUID userId = currentUserId();
        UserAddress address = getOwned(addressId, userId);
        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setDefault(UUID addressId) {
        UUID userId = currentUserId();
        UserAddress address = getOwned(addressId, userId);
        clearDefault(userId);
        address.setDefault(true);
        return toResponse(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public UserAddress getOwnedOrThrow(UUID addressId, UUID userId) {
        return getOwned(addressId, userId);
    }

    private UserAddress getOwned(UUID addressId, UUID userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ApiException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND));
    }

    private void clearDefault(UUID userId) {
        addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtAsc(userId).stream()
                .filter(UserAddress::isDefault)
                .forEach(addr -> {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                });
    }

    private void applyRequest(UserAddress address, AddressRequest request) {
        address.setLabel(request.label());
        address.setRecipientName(request.recipientName().trim());
        address.setPhone(request.phone().trim());
        address.setLine1(request.line1().trim());
        address.setLine2(request.line2());
        address.setCity(request.city().trim());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country().trim().toUpperCase());
        address.setDefault(request.isDefault());
    }

    private AddressResponse toResponse(UserAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getRecipientName(),
                address.getPhone(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.isDefault()
        );
    }

    private static UUID currentUserId() {
        return SecurityUtils.currentUser()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED));
    }
}
