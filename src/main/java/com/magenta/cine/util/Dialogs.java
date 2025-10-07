/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.magenta.cine.util;

/**
 *
 * @author ignac
 */

import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Dialogs {
    private static final Logger LOGGER = Logger.getLogger(Dialogs.class.getName());

    private Dialogs() {}

    public static void info(String msg) {
        LOGGER.info(msg);
        JOptionPane.showMessageDialog(null, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(String msg) {
        LOGGER.warning(msg);
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void error(String msg, Throwable ex) {
        LOGGER.log(Level.SEVERE, msg, ex);
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void warn(String msg) {
        LOGGER.warning(msg);
        JOptionPane.showMessageDialog(null, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(String msg) {
        LOGGER.fine(() -> "Confirmación solicitada: " + msg);
        return JOptionPane.showConfirmDialog(null, msg, "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
