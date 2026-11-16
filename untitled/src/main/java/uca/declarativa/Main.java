package uca.declarativa;

import screen.Window;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        SwingUtilities.invokeLater(() -> {
            try {
                Connection motor = new Connection();
                new Window(motor).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(),
                        "No se pudo iniciar SWI-Prolog", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
