/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;

/**
 *
 * @author Jesus Gutierrez
 */
import java.sql.Date;
import java.sql.Timestamp;

public class UserTb {
    private int id;
    private String username;
    private int idEmployee;
    private String firstName;
    private String lastName;
    private String rol;
    private String nationalId;
    private String phoneNumber;
    private String gender;
    private String employmentStatus;
    private String employmentStatusCode;
    private Date startDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String state;
    
    
    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
   
    public UserTb(int iduser, String username, int idEmployee, String firstName, String lastName,String rol,String state,
                String nationalId, String phoneNumber, String gender, String employmentStatus,
                String employmentStatusCode, Date startDate, Timestamp createdAt, Timestamp updatedAt) {
        
        this.state = state;
        this.rol = rol;
        this.id = iduser;
        this.username = username;
        this.idEmployee = idEmployee;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.employmentStatus = employmentStatus;
        this.employmentStatusCode = employmentStatusCode;
        this.startDate = startDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int iduser) {
        this.id = iduser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(int idEmployee) {
        this.idEmployee = idEmployee;
    }

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

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getEmploymentStatusCode() {
        return employmentStatusCode;
    }

    public void setEmploymentStatusCode(String employmentStatusCode) {
        this.employmentStatusCode = employmentStatusCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserTb{" + "iduser=" + id + ", username=" + username + ", idEmployee=" + idEmployee + ", firstName=" + firstName + ", lastName=" + lastName + ", nationalId=" + nationalId + ", phoneNumber=" + phoneNumber + ", gender=" + gender + ", employmentStatus=" + employmentStatus + ", employmentStatusCode=" + employmentStatusCode + ", startDate=" + startDate + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
    
}