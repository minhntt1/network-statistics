package com.home.network.statistic.admin.web;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Objects;

@Table(name = "time_dim")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class TimeDim {
    @Id
    private Integer timeKey;
    private Integer time;

    public LocalTime timeToLocalTime() {
        return LocalTime.ofSecondOfDay(time);
    }
}
