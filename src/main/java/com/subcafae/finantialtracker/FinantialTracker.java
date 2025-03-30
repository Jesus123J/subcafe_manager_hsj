/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.subcafae.finantialtracker;

import com.subcafae.finantialtracker.controller.ControllerMain;
import com.subcafae.finantialtracker.data.entity.UserTb;

/**
 *
 * @author Jesus Gutierrez<
 */
public class FinantialTracker {

    public static void main(String[] args) {
        new ControllerMain(new UserTb());
    }
}
