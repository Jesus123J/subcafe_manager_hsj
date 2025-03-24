/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import java.sql.Connection;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelManageBond {
    protected  AbonoDao abonoDao = new AbonoDao(Conexion.getConnection());
    ComponentManageBond componentManageBond;
    
    public ModelManageBond(ComponentManageBond componentManageBond) {
        this.componentManageBond = componentManageBond;
    }
    
    
    public void insertDao(){
        
    }
    
}   
