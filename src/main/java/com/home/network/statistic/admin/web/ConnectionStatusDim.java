package com.home.network.statistic.admin.web;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Table(name = "connection_status_dim")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConnectionStatusDim {
    @Id
    private Integer id;
    private String status;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionStatusDim that = (ConnectionStatusDim) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
