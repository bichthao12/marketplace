package com.marketplace.user.controller;

import com.marketplace.user.dto.AddressListResponse;
import com.marketplace.user.dto.AddressRequest;
import com.marketplace.user.dto.AddressResponse;
import com.marketplace.user.service.AddressService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public AddressListResponse list() {
        return addressService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@Valid @RequestBody AddressRequest request) {
        return addressService.create(request);
    }

    @PutMapping("/{addressId}")
    public AddressResponse update(@PathVariable UUID addressId, @Valid @RequestBody AddressRequest request) {
        return addressService.update(addressId, request);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID addressId) {
        addressService.delete(addressId);
    }

    @PatchMapping("/{addressId}/default")
    public AddressResponse setDefault(@PathVariable UUID addressId) {
        return addressService.setDefault(addressId);
    }
}
