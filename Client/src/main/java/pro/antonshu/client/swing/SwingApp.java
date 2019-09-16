package pro.antonshu.client.swing;

import javax.swing.*;
import java.io.IOException;

public class SwingApp {

    private static MainWindow mainWindow;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    mainWindow = new MainWindow();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
