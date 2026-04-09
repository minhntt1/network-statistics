package com.home.network.statistic.poller.authentication;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "device_auth_data")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String dataClass;

    // following column holds the auth data such as user, pass
    private String data;

    // temporary auth data - token, header, etc
    private String tempData;

    public boolean hasId() {
        return id != null;
    }

    public void update(AuthData other) {
        if (!Objects.equals(this.data, other.data))
            this.data = other.data;
        if (!Objects.equals(this.tempData, other.tempData))
            this.tempData = other.tempData;
        if (!Objects.equals(this.dataClass, other.dataClass))
            this.dataClass = other.dataClass;
    }
}
