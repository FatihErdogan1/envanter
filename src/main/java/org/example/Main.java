package org.example;

import org.example.util.InventorTheme;
import org.example.view.frames.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        InventorTheme.apply();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}
