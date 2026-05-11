package com.delivery.delivery.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationUpdateRequest {
    private Long driverId;
    private double longitude;
    private double latitude;
}
