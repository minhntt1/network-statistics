package com.home.network.statistic.admin.web;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Table(name = "date_dim")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class DateDim {
    @Id
    private Integer dateKey;
    private LocalDate date;
}
