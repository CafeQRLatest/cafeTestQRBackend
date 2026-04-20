package com.restaurant.pos.common.service;

import com.restaurant.pos.common.dto.ConfigurationDto;
import com.restaurant.pos.common.entity.SystemConfiguration;
import com.restaurant.pos.common.repository.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigurationService {

    private final SystemConfigurationRepository repository;

    @Transactional(readOnly = true)
    public ConfigurationDto getConfiguration() {
        SystemConfiguration config = repository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultConfig);
        return mapToDto(config);
    }

    @Transactional
    public ConfigurationDto updateConfiguration(ConfigurationDto dto) {
        SystemConfiguration config = repository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultConfig);

        updateEntityFromDto(config, dto);
        SystemConfiguration saved = repository.save(config);
        log.info("System configuration updated successfully.");
        return mapToDto(saved);
    }

    private SystemConfiguration createDefaultConfig() {
        SystemConfiguration config = SystemConfiguration.builder()
                .qrOrderingEnabled(true)
                .sendToKitchenEnabled(true)
                .posProductListingEnabled(true)
                .taxLabelGlobal("GST")
                .currencySymbol("₹")
                .currencyPosition("before")
                .roundOffMode("automatic")
                .roundOffAutoFactor(java.math.BigDecimal.ONE)
                .roundOffManualLimit(java.math.BigDecimal.TEN)
                .build();
        return repository.save(config);
    }

    private ConfigurationDto mapToDto(SystemConfiguration entity) {
        return ConfigurationDto.builder()
                .onlinePaymentEnabled(entity.isOnlinePaymentEnabled())
                .menuImagesEnabled(entity.isMenuImagesEnabled())
                .creditEnabled(entity.isCreditEnabled())
                .tableManagementEnabled(entity.isTableManagementEnabled())
                .qrOrderingEnabled(entity.isQrOrderingEnabled())
                .inventoryEnabled(entity.isInventoryEnabled())
                .productionEnabled(entity.isProductionEnabled())
                .customersEnabled(entity.isCustomersEnabled())
                .loyaltyEnabled(entity.isLoyaltyEnabled())
                .sendToKitchenEnabled(entity.isSendToKitchenEnabled())
                .onlineDeliveryEnabled(entity.isOnlineDeliveryEnabled())
                .allowMultipleCustomersPerOrder(entity.isAllowMultipleCustomersPerOrder())
                .posProductListingEnabled(entity.isPosProductListingEnabled())
                .roundOffEnabled(entity.isRoundOffEnabled())
                .roundOffMode(entity.getRoundOffMode())
                .roundOffAutoFactor(entity.getRoundOffAutoFactor())
                .roundOffManualLimit(entity.getRoundOffManualLimit())
                .taxLabelGlobal(entity.getTaxLabelGlobal())
                .currencySymbol(entity.getCurrencySymbol())
                .currencyPosition(entity.getCurrencyPosition())
                .billFooter(entity.getBillFooter())
                .printLogoBitmap(entity.getPrintLogoBitmap())
                .printLogoCols(entity.getPrintLogoCols())
                .printLogoRows(entity.getPrintLogoRows())
                .paperMm(entity.getPaperMm())
                .printCols(entity.getPrintCols())
                .printLeftMarginDots(entity.getPrintLeftMarginDots())
                .printRightMarginDots(entity.getPrintRightMarginDots())
                .printAutoCut(entity.isPrintAutoCut())
                .printWinListUrl(entity.getPrintWinListUrl())
                .printWinPostUrl(entity.getPrintWinPostUrl())
                .build();
    }

    private void updateEntityFromDto(SystemConfiguration entity, ConfigurationDto dto) {
        entity.setOnlinePaymentEnabled(dto.isOnlinePaymentEnabled());
        entity.setMenuImagesEnabled(dto.isMenuImagesEnabled());
        entity.setCreditEnabled(dto.isCreditEnabled());
        entity.setTableManagementEnabled(dto.isTableManagementEnabled());
        entity.setQrOrderingEnabled(dto.isQrOrderingEnabled());
        entity.setInventoryEnabled(dto.isInventoryEnabled());
        entity.setProductionEnabled(dto.isProductionEnabled());
        entity.setCustomersEnabled(dto.isCustomersEnabled());
        entity.setLoyaltyEnabled(dto.isLoyaltyEnabled());
        entity.setSendToKitchenEnabled(dto.isSendToKitchenEnabled());
        entity.setOnlineDeliveryEnabled(dto.isOnlineDeliveryEnabled());
        entity.setAllowMultipleCustomersPerOrder(dto.isAllowMultipleCustomersPerOrder());
        entity.setPosProductListingEnabled(dto.isPosProductListingEnabled());
        entity.setRoundOffEnabled(dto.isRoundOffEnabled());
        entity.setRoundOffMode(dto.getRoundOffMode());
        entity.setRoundOffAutoFactor(dto.getRoundOffAutoFactor());
        entity.setRoundOffManualLimit(dto.getRoundOffManualLimit());
        if (dto.getTaxLabelGlobal() != null) entity.setTaxLabelGlobal(dto.getTaxLabelGlobal());
        if (dto.getCurrencySymbol() != null) entity.setCurrencySymbol(dto.getCurrencySymbol());
        if (dto.getCurrencyPosition() != null) entity.setCurrencyPosition(dto.getCurrencyPosition());
        
        // Receipt Customization
        entity.setBillFooter(dto.getBillFooter());
        entity.setPrintLogoBitmap(dto.getPrintLogoBitmap());
        entity.setPrintLogoCols(dto.getPrintLogoCols());
        entity.setPrintLogoRows(dto.getPrintLogoRows());

        // Hardware & Paper
        if (dto.getPaperMm() != null) entity.setPaperMm(dto.getPaperMm());
        if (dto.getPrintCols() != null) entity.setPrintCols(dto.getPrintCols());
        if (dto.getPrintLeftMarginDots() != null) entity.setPrintLeftMarginDots(dto.getPrintLeftMarginDots());
        if (dto.getPrintRightMarginDots() != null) entity.setPrintRightMarginDots(dto.getPrintRightMarginDots());
        entity.setPrintAutoCut(dto.isPrintAutoCut());
        if (dto.getPrintWinListUrl() != null) entity.setPrintWinListUrl(dto.getPrintWinListUrl());
        if (dto.getPrintWinPostUrl() != null) entity.setPrintWinPostUrl(dto.getPrintWinPostUrl());
    }
}
