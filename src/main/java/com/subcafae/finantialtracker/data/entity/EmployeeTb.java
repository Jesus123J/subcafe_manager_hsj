/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeTb {
    private Integer employeeId;
//    private String firstName;
//    private String lastName;
    private String fullName;
    private String nationalId;
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

    public EmployeeTb(String fullName, String nationalId ,
                   String gender, String employmentStatus, 
                   String employmentStatusCode, LocalDate startDate) {
        this.fullName = fullName;
        this.nationalId = nationalId;
        this.gender = gender;
        this.employmentStatus = employmentStatus;
        this.employmentStatusCode = employmentStatusCode;
        this.startDate = startDate;
    }

    // Getters y Setters
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

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
        return "EmployeeTb{" + "employeeId=" + employeeId + ", fullName=" + fullName + ", nationalId=" + nationalId + ", gender=" + gender + ", employmentStatus=" + employmentStatus + ", employmentStatusCode=" + employmentStatusCode + ", startDate=" + startDate + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }

  
}