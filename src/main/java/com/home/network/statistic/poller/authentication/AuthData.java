package com.home.network.statistic.poller.authentication;

import com.home.network.statistic.common.util.JsonUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "device_auth_data_web")
@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class AuthData {
    public static final List<CredentialAbstract> VALID_SUBCLASSES = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String dataClass;

    // following column holds the auth data such as user, pass - must-have column
    private String data;

    // temporary auth data - token, header, etc
    private String tempData;

    public static List<String[]> extractClassAndJsonTemplate() {
        return VALID_SUBCLASSES.stream().map(c -> new String[] {c.getClass().getCanonicalName(), c.serializeToJson()}).toList();
    }

    @SneakyThrows
    public <T extends CredentialAbstract> T extractCredentialAbstract(Class<T> tClass) {
        return (T) CredentialAbstract.fromJson(data, tClass);
    }

    public AuthData() {
        id = null;
        dataClass = "";
        data = "";
        tempData = "";
    }

    public boolean checkId() {
        return id != null;
    }

    public boolean checkTempData(Class<?> struct) {
        if (tempData == null || tempData.isBlank())
            return false;

        // test if it possible to parse tempdata string to given struct
        try{
            JsonUtil.fromJson(tempData, struct);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkValidData() {
        if (!checkValidDataClass())
            return false;

        // try to serialize to json
        try {
            JsonUtil.fromJson(data, Class.forName(dataClass));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkValidDataClass() {
        return VALID_SUBCLASSES.stream().anyMatch(c -> c.getClass().getCanonicalName().equals(dataClass));
    }

    public void update(AuthData other) {
        if (!Objects.equals(this.data, other.data))
            this.data = other.data;
        if (!Objects.equals(this.tempData, other.tempData))
            this.tempData = other.tempData;
        if (!Objects.equals(this.dataClass, other.dataClass))
            this.dataClass = other.dataClass;
    }

    public void updateTempData(String temp) {
        if (!Objects.equals(this.tempData, temp))
            this.tempData = temp;
    }
}
