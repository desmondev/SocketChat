/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dswiecki.app.controller;

import com.dswiecki.app.bean.ChatMessage;
import com.dswiecki.app.bean.ChatMessage.Action;
import com.dswiecki.app.view.ClientFrame;
import com.dswiecki.app.service.ClientService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.JOptionPane;

/**
 *
 * @author Damian
 */
public class ClientController {

    private Socket socket;
    private ChatMessage message;
    private ClientService theModel;
    private ClientFrame theView;

    public ClientController(ClientFrame theView, ClientService theModel) {
        this.theModel = theModel;
        this.theView = theView;

        this.theView.addConnectListener(new ConnectListener());
        this.theView.addDisconnectListener(new DisconnectedListener());
        this.theView.addSendListener(new SendListener());
        this.theView.addClearListener(new ClearListener());
        this.theView.addAktualizujListener(new AktualizujListener());
    }

    class SendListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            
            String text = theView.getTextAreaSender();
            String name = theView.getTextName();
            message = new ChatMessage();
            JList list = theView.getListOnlines();
            if (list.getSelectedIndex() > -1) {
                message.setNameReserved((String) list.getSelectedValue());
                message.setAction(Action.SEND_ONE);

            } else {
                message.setAction(Action.SEND_ALL);
            }

            if (!text.isEmpty()) {

                message.setName(name);
                message.setText(text);
                theView.setTextAreaReceiver(message.getName() + ":" + message.getText() + "\n");
                theModel.send(message);
            }

            theView.setTextAreaSender("");
        }

    }

    class ClearListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            theView.setTextAreaSender("");

        }

    }

    class AktualizujListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {

        }

    }

    class DisconnectedListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            message.setAction(Action.DISCONNECT);
            message.setName(theView.getTextName());
            theModel.send(message);
            try {
                socket.close();
                theView.enableConnection();
                theView.setTextAreaReceiver("Odłączono");
            } catch (IOException ex) {
                Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    class ConnectListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {

            String name;
            try {
                name = theView.getTextName();
                if (!name.isEmpty()) {

                    message = new ChatMessage();
                    message.setAction(Action.CONNECT);
                    message.setName(name);

                    socket = theModel.connect();

                    new Thread(new ListenerSocket(socket)).start();

                }
                theModel.send(message);
            } catch (Exception e) {
            }
        }
    }

    private class ListenerSocket implements Runnable {

        private ObjectInputStream input;

        public ListenerSocket(Socket socket) {

            try {

                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void run() {

            //ChatMessage message = null;
            System.out.println("Echo");
            try {
                while ((message = (ChatMessage) input.readObject()) != null) {
                    Action action = message.getAction();

                    if (action.equals(Action.CONNECT)) {
                        connected(message);
                    } else if (action.equals(Action.DISCONNECT)) {
                        disconneted();
                        socket.close();
                    } else if (action.equals(Action.SEND_ONE)) {
                        receive(message);
                    } else if (action.equals(Action.USERS_ONLINE)) {
                        refreshOnlines(message);
                    }
                }
            } catch (IOException ex) {
                //Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void connected(ChatMessage message) {

            //theView.setTextAreaReceiver(message.getName() + "\n");
            if (message.getText().equals("NO")) {
                theView.setTextName("");
                JOptionPane.showMessageDialog(theView, "Cos tam cos tam");
                return;
            }
            theView.disableConnection();
            theView.setMessage(message);
            theView.setTextAreaReceiver("Podłączył się " + message.getName() + "\n");

        }

        private void disconneted() {

        }

        private void receive(ChatMessage message) {
            theView.setTextAreaReceiver(message.getName() + ":" + message.getText() + "\n");
        }

        private void refreshOnlines(ChatMessage message) {
            Set<String> names = message.getSetOnlines();
            names.remove(message.getName());
            String[] array = (String[]) names.toArray(new String[names.size()]);
            JList list = theView.getListOnlines();
            list.setListData(array);
            
            theView.setListOnlines(list);
            theView.setListLayout();
        }

    }

}
