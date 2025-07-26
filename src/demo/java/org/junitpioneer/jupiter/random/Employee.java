package org.junitpioneer.jupiter.random;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

//tag::employee[]
public class Employee {

    private String firstName;
    private String lastName;
    @Min(18) @Max(70)
    private int age;
    private Gender gender;
    private String division;
    private String city;
    private String country;

    // Constructor, setters, getters, etc.
    // end::employee[]

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
