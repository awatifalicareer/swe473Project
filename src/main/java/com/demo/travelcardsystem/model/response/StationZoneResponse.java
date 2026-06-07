package com.demo.travelcardsystem.model.response;

import com.demo.travelcardsystem.constant.Zone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationZoneResponse {

    private String stationName;
    private Set<Zone> zones;
}