package pro.antonshu.client.swing;

import pro.antonshu.network.message.CommandMessage;
import pro.antonshu.network.message.FileMessage;
import pro.antonshu.network.message.FileRequest;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MainWindow extends JFrame {

    private final JList fileList;
    private final DefaultListModel<String> fileListModel;
    private final FileListCellRenderer fileListCellRenderer;
    private final JScrollPane scroll;
    private final JPanel buttonsPanel;
    private final JButton sendFileButton;
    private final JButton requestFileListButton;
    private final JButton downloadFileButton;
    private final JTextField FilesListField;
    private final String rootPath = "d:\\" ;
    private final String clientRootPath = "d:\\ddd\\client\\" ;
//    private final Path root = Paths.get("d:\\server\\");
    private final long maxFileSize = 50174185L;
    private String user;
    private final NettyClient net;



    public MainWindow() throws IOException {
        setTitle("Облачное хранилище");
        setBounds(200,200, 500, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.net = new NettyClient();

        setLayout(new BorderLayout());

        fileList = new JList<>();
        fileListModel = new DefaultListModel<>();
        fileListCellRenderer = new FileListCellRenderer();
        fileList.setModel(fileListModel);
        fileList.setCellRenderer(fileListCellRenderer);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object element = fileList.getSelectedValue();
            }
        });

        scroll = new JScrollPane(fileList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        sendFileButton = new JButton("Отправить файл");
        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String text = FilesListField.getText();
                Path path = Paths.get(rootPath + text);
                if (text != null && Files.exists(path) && !text.trim().isEmpty()) {
                    if (path.toFile().length() < maxFileSize) {
                        FileMessage fm = new FileMessage(path, user);
                        net.sendMsg(fm);
//                        fileListModel.addElement("Отправили файл " + text);
                    } else {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Файла превышает допустимый размер: 50 МБ - "+ path,
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Файла не существует: " + path,
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        requestFileListButton = new JButton("Обновить список");
        requestFileListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                CommandMessage cm = new CommandMessage("request_file_list", user);
                net.sendMsg(cm);

                try {
                    CommandMessage response = (CommandMessage) net.readObject();
                    if (response.getFileList().isEmpty() || response.getFileList().equals(null)) {
                        System.out.println("Список файлов пустой");
                    } else {
                        Map<String, Boolean> res = response.getFileList();
                        fileListModel.clear();
                        for (Map.Entry<String, Boolean> entry : res.entrySet()) {
                            fileListModel.addElement(entry.getKey());
                        }
                    }
                } catch (ClassNotFoundException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        downloadFileButton = new JButton("Скачать файл");
        downloadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = FilesListField.getText();
                if (text != null && !text.trim().isEmpty()) {
                    net.sendMsg(new FileRequest(text, user));
                } else {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Имя файла пустое. Введите новое имя",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }

                try {
                    FileMessage fm = (FileMessage) net.readObject();
                    if (!fm.getIsParted()) {
                        FileOutputStream fos = new FileOutputStream("D:/ddd/client/" + fm.getFilename());
                        fos.write(fm.getData());
                        System.out.println("Client receive file: " + fm.getFilename());
                        fos.close();
                    }
                } catch (ClassNotFoundException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        buttonsPanel.add(sendFileButton, BorderLayout.EAST);
        buttonsPanel.add(requestFileListButton, BorderLayout.WEST);
        buttonsPanel.add(downloadFileButton, BorderLayout.CENTER);

        FilesListField = new JTextField();
        FilesListField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = FilesListField.getText();
                    if (text != null && !text.trim().isEmpty()) {

                    } else {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Имя файла пустое. Введите новое имя",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        add(FilesListField, BorderLayout.NORTH);

        add(buttonsPanel, BorderLayout.SOUTH);
        setVisible(true);


        ChooseDialog choose = new ChooseDialog(this);
        choose.setVisible(true);

        if (!choose.getIsRegistered()) {
            RegisterDialog regDialog = new RegisterDialog(this, net);
            regDialog.setVisible(true);

            if (!regDialog.isRegistered()) {
                System.exit(0);
            } else {
                user = regDialog.getUser();
            }
        } else {
            LoginDialog loginDialog = new LoginDialog(this, net);
            loginDialog.setVisible(true);

            if (!loginDialog.isAuthorized()) {
                System.exit(0);
            } else {
                user = loginDialog.getUser();
            }
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (net != null) {
                    net.stop();
                }
                super.windowClosing(e);
            }
        });

        setTitle(String.format("Облачное хранилище. Пользователь %s", user));
    }
}
