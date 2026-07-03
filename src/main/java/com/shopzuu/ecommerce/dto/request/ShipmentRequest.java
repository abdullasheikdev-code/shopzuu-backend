package com.shopzuu.ecommerce.dto.request;

import lombok.Data;

@Data
public class ShipmentRequest {

    private String courierName;

    private String trackingNumber;

    private String trackingUrl;
}