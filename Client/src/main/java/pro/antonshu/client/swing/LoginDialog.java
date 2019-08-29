package pro.antonshu.client.swing;

import pro.antonshu.network.message.AuthMessage;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginDialog extends JDialog {

    private NettyClient net;
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbUsername;
    private JLabel lbPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    private boolean authorized;
    private String user;

    public boolean isAuthorized() {
        return authorized;
    }

    public String getUser() {
        return user;
    }

    public LoginDialog(Frame parent, NettyClient net) {
        super(parent, "Логин", true);
        this.net = net;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbUsername = new JLabel("Имя пользователя: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        lbPassword = new JLabel("Пароль: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        btnLogin = new JButton("Войти");
        btnCancel = new JButton("Отмена");

        JPanel bp = new JPanel();
        bp.add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    net.sendMsg(new AuthMessage(tfUsername.getText(), String.valueOf(pfPassword.getPassword())));
                    AuthMessage response = (AuthMessage) net.readObject();
                    if (response.getAuthorize()) {
                        authorized = true;
                        user = response.getLogin();
                    } else {
                        JOptionPane.showMessageDialog(LoginDialog.this,
                                "Ошибка авторизации. Неверные логин или пароль",
                                "Авторизация",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Ошибка авторизации. Проблемы с сетью",
                            "Авторизация",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                dispose();
            }
        });

        bp.add(btnCancel);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authorized = false;
                dispose();
            }
        });

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
}
