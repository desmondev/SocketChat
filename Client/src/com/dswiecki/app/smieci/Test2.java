/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dswiecki.app.smieci;

import com.dswiecki.app.controller.ClientController;
import com.dswiecki.app.view.ClientFrame;
import com.dswiecki.app.service.ClientService;

/**
 *
 * @author Damian
 */
public class Test2 {
    public static void main(String[] args) {
        ClientFrame theview = new ClientFrame();
        theview.setVisible(true);
        ClientService theModel = new ClientService();
        
        ClientController theController = new ClientController(theview, theModel);
        
        
    }
    
}
