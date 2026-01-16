package com.home.network.statistic.poller.rfc1213.out;


import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"dev-executor","prd-executor"})
public interface IftableTrafficEntityRepo extends JpaRepository<IftableTrafficEntity, Long> {
}
