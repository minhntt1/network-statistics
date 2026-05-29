package com.home.network.statistic.admin.web;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"dev-admin", "prd-admin"})
public interface DeviceWlanConnectionsFactRepo extends JpaRepository<DeviceWlanConnectionsFact, DeviceWlanConnectionsFact.Id> {
    @EntityGraph(attributePaths = {"dateKey", "timeKey", "deviceKey", "deviceIpKey", "apKey", "ifaceKey", "vendorKey", "apVendorKey", "connectionStatusKey"})
    Page<DeviceWlanConnectionsFact> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"dateKey", "timeKey", "deviceKey", "deviceIpKey", "apKey", "ifaceKey", "vendorKey", "apVendorKey", "connectionStatusKey"})
    Page<DeviceWlanConnectionsFact> findByKeyDeviceKey(Integer clientKey, Pageable defaultPage);
}
