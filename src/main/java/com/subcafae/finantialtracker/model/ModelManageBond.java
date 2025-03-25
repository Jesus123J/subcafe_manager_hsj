/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelManageBond {

    protected AbonoDao abonoDao = new AbonoDao(Conexion.getConnection());
    protected ComponentManageBond componentManageBond;
    protected UserTb user;

    public ModelManageBond(ComponentManageBond componentManageBond, UserTb user) {
        this.user = user;
        this.componentManageBond = componentManageBond;
    }

    public void insertDao(AbonoTb abono) {
        try {
            abonoDao.insertAbono(abono);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GÉSTION ABONOS", JOptionPane.OK_OPTION);
        }
    }

}
