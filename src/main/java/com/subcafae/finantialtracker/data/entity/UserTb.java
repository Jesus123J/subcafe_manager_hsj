/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;

/**
 *
 * @author Jesus Gutierrez
 */
public class UserTb {
    private Integer id;
    private String email;
    private String username;
    private String password;
    private Integer createdBy;
    private String createdAt;
    private Integer modifidBy;
    private String modifidAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getModifidBy() {
        return modifidBy;
    }

    public void setModifidBy(Integer modifidBy) {
        this.modifidBy = modifidBy;
    }

    public String getModifidAt() {
        return modifidAt;
    }

    public void setModifidAt(String modifidAt) {
        this.modifidAt = modifidAt;
    }
    
    
}
