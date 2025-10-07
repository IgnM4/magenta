
package com.magenta.cine.app;

import com.magenta.cine.db.ConnectionFactory;
import com.magenta.cine.db.ConnectionHealth;
import com.magenta.cine.util.Dialogs;

import javax.swing.SwingUtilities;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        ConnectionHealth health = ConnectionFactory.testConnection();
        if (!health.success()) {
            LOGGER.severe(health.consoleMessage());
            System.err.println(health.consoleMessage());
            SwingUtilities.invokeLater(() -> Dialogs.error(health.userMessage()));
            return;
        }

        LOGGER.info(health.consoleMessage());
        System.out.println(health.consoleMessage());

        SwingUtilities.invokeLater(() -> new MainMenu(health.userMessage()).setVisible(true));
    }
}
