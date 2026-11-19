package com.github.rotibakarasjad;

import com.github.rotibakarasjad.view.LoginFrame;
import java.awt.EventQueue;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        // Tampilan mengikuti Nimbus jika tersedia
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // Jika gagal, tetap pakai tampilan default
        }

        // Semua komponen Swing harus dibuat di Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setLocationRelativeTo(null);
            login.setVisible(true);
        });
    }
}