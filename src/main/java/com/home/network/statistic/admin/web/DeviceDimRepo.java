package com.home.network.statistic.admin.web;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Profile({"dev-admin", "prd-admin"})
@Repository
public interface DeviceDimRepo extends JpaRepository<DeviceDim, Integer> {
}
