package com.myapp.artificer.Models;

import java.io.Serializable;

public class WorkerModel implements Serializable {
    private String id,name, workType,email,phone,imageUrl;
    double lat,lon;

    public WorkerModel(){}

    public WorkerModel(String id, String name, String workType, String email, String phone, double lat, double lon, String imageUrl) {
        this.id = id;
        this.name = name;
        this.workType = workType;
        this.email = email;
        this.phone = phone;
        this.lat = lat;
        this.lon = lon;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
