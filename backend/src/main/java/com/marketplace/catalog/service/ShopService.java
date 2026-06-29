package com.marketplace.catalog.service;

import com.marketplace.catalog.dto.ShopResponse;
import com.marketplace.catalog.entity.ProductStatus;
import com.marketplace.catalog.mapper.CatalogMapper;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.seller.entity.SellerStatus;
import com.marketplace.seller.exception.SellerProfileNotFoundException;
import com.marketplace.seller.repository.SellerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {

    private final SellerProfileRepository sellerProfileRepository;
    private final ProductRepository productRepository;

    public ShopService(SellerProfileRepository sellerProfileRepository, ProductRepository productRepository) {
        this.sellerProfileRepository = sellerProfileRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ShopResponse getBySlug(String slug) {
        SellerProfile seller = sellerProfileRepository.findBySlug(slug)
                .filter(profile -> profile.getStatus() == SellerStatus.APPROVED)
                .orElseThrow(SellerProfileNotFoundException::new);
        long productCount = productRepository.countBySellerIdAndStatus(seller.getId(), ProductStatus.ACTIVE);
        return CatalogMapper.toShop(seller, productCount);
    }
}
