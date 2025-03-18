/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import java.sql.Connection;

/**
 *
 * @author Jesus Gutierrez
 */
public class LoanDao {


        private final Connection connection;

        public LoanDao(Conexion connection) {
            this.connection = connection.getConnection();
        }

        private void updateSoliNum(int id, String soliNum) throws SQLException {
            String sql = "UPDATE loan SET SoliNum = ? WHERE ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, soliNum);
                stmt.setInt(2, id);
                stmt.executeUpdate();
            }
        }
    }
