/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeTb {
    private int employeeId;
    private String firstName;
    private String lastName;
    private String nationalId;
    private String phoneNumber;
    private String gender;
    private String employmentStatus;
    private String employmentStatusCode;
    private LocalDate startDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getLoanId() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    public enum Gender {
        HOMBRE, MUJER;

        public static Gender fromString(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    public enum EmploymentStatus {
        NOMBRADO, CAS;

        public static EmploymentStatus fromString(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    // Constructores
    public EmployeeTb() {}

    public EmployeeTb(String firstName, String lastName, String nationalId,String phoneNumber ,
                   String gender, String employmentStatus, 
                   String employmentStatusCode, LocalDate startDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.nationalId = nationalId;
        this.gender = gender;
        this.employmentStatus = employmentStatus;
        this.employmentStatusCode = employmentStatusCode;
        this.startDate = startDate;
    }

    // Getters y Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public String getEmploymentStatusCode() { return employmentStatusCode; }
    public void setEmploymentStatusCode(String employmentStatusCode) { this.employmentStatusCode = employmentStatusCode; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "EmployeeTb{" + "employeeId=" + employeeId + ", firstName=" + firstName + ", lastName=" + lastName + ", nationalId=" + nationalId + ", phoneNumber=" + phoneNumber + ", gender=" + gender + ", employmentStatus=" + employmentStatus + ", employmentStatusCode=" + employmentStatusCode + ", startDate=" + startDate + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
    
    
    
}