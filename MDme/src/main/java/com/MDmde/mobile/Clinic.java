package com.MDmde.mobile;
import java.io.Serializable;

/**
 * Created by ermacaz on 10/16/14.
 */
@SuppressWarnings("serial")
public class Clinic implements Serializable {

    private int id;
    private String name;
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String phone_number;
    private String fax_number;
    private String ne_latitude;
    private String ne_longitude;
    private String sw_latitude;
    private String sw_longitude;


    public Clinic(int id, String name, String address1, String address2, String address3,
                  String city, String state, String country, String zipcode,
                  String phone_number, String fax_number, String ne_latitude,
                  String ne_longitude, String sw_latitude, String sw_longitude) {
        this.id = id;
        this.name = name;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipcode = zipcode;
        this.phone_number = phone_number;
        this.fax_number = fax_number;
        this.ne_latitude = ne_latitude;
        this.ne_longitude = ne_longitude;
        this.sw_latitude = sw_latitude;
        this.sw_longitude = sw_longitude;
    }

    @Override
    public String toString() { return name; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getFax_number() {
        return fax_number;
    }

    public void setFax_number(String fax_number) {
        this.fax_number = fax_number;
    }

    public String getNe_latitude() { return ne_latitude; }

    public void setNe_latitude(String ne_latitude) { this.ne_latitude = ne_latitude; }

    public String getNe_longitude() { return ne_longitude; }

    public void setNe_longitude(String ne_longitude) { this.ne_longitude = ne_longitude; }

    public String getSw_latitude() { return sw_latitude; }

    public void setSw_latitude(String sw_latitude) { this.sw_latitude = sw_latitude;
    }

    public String getSw_longitude() { return sw_longitude; }

    public void setSw_longitude(String sw_longitude) { this.sw_longitude = sw_longitude; }

}
