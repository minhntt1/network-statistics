package com.home.network.statistic.admin.web;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Table(name = "device_dim")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class DeviceDim {
    @Id
    private Integer deviceKey;
    private Long deviceMac;
    private String deviceName;
    private Integer deviceIfaceWifi;
}
