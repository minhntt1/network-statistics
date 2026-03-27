package com.home.network.statistic.poller.tplink.deco.out;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"dev-executor","prd-executor"})
public interface ClientDeviceInfoRepo  extends JpaRepository<ClientDeviceInfoEntity, Long> {
}
