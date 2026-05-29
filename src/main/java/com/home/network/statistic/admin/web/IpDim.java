package com.home.network.statistic.admin.web;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Table(name = "ip_dim")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class IpDim {
    @Id
    private Integer ipKey;
    private Integer ipv4;
    private Long ipv6;
}
