package com.github.rotibakarasjad.view;

import com.github.rotibakarasjad.exception.BusinessException;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

final class Dialogs {

    private static final Logger logger = Logger.getLogger(Dialogs.class.getName());

    private Dialogs() {
    }

    static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Menampilkan pesan yang sesuai untuk setiap jenis error. */
    static void handle(Component parent, Exception ex) {
        if (ex instanceof BusinessException || ex instanceof IllegalArgumentException) {
            error(parent, ex.getMessage());
        } else {
            logger.log(Level.SEVERE, "Kesalahan tidak terduga", ex);
            error(parent, "Terjadi kesalahan pada database. Periksa koneksi lalu coba lagi.");
        }
    }
}