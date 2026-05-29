package com.home.network.statistic.admin.web;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Table(name = "ap_dim")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ApDim {
    @Id
    private Integer apKey;
    private Long apMac;
    private String apName;
}
