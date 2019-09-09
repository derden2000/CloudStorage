package pro.antonshu.client.swing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.antonshu.client.swing.tcp.NettyBFClient;
import pro.antonshu.service.ChunkService;

import javax.net.ssl.SSLException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private String rootPath;
    private String user;
    private final NettyBFClient nettyBootstrapClient;
    private ChunkService chunkService;
    private static final Logger logger = LogManager.getLogger(MainWindow.class);



    public MainWindow() throws IOException {
        setTitle("Облачное хранилище");
        setBounds(200,200, 500, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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

        this.nettyBootstrapClient = new NettyBFClient();
        this.chunkService = new ChunkService();

        ExecutorService netService = Executors.newCachedThreadPool();
        netService.submit(() -> {
            try {
                nettyBootstrapClient.run(fileListModel);
            } catch (InterruptedException | SSLException e) {
                e.printStackTrace();
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
                Path path = Paths.get(rootPath + File.separator + text);
                if (text != null && Files.exists(path) && !text.trim().isEmpty()) {

                    try {
                        ByteBuf buf = Unpooled.copiedBuffer(prepareSendData("filena", user, text.getBytes()));
                        nettyBootstrapClient.getChannel().writeAndFlush(buf);

                        chunkService.sendFile(nettyBootstrapClient.getChannel(), path.toString());
                        logger.info(user + " trying to send file: " + path.toString());
                    } catch (IOException ex) {
                        logger.error(user + "error: ", ex);
                    }
                } else {
                    showMessage("Файла не существует: " + path);
                }
            }
        });

        requestFileListButton = new JButton("Обновить список");
        requestFileListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ByteBuf buf = Unpooled.copiedBuffer(prepareSendData("req_fl", user, null));
                nettyBootstrapClient.getChannel().writeAndFlush(buf);

            }
        });

        downloadFileButton = new JButton("Скачать файл");
        downloadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String text = FilesListField.getText();
                if (text != null && !text.trim().isEmpty()) {
                    ByteBuf buf = Unpooled.copiedBuffer(prepareSendData("get_fl", user, text.getBytes()));
                    nettyBootstrapClient.getChannel().writeAndFlush(buf);
                } else {
                    showMessage("Имя файла пустое. Введите новое имя");
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
                        showMessage("Имя файла пустое. Введите новое имя");
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
            RegisterDialog regDialog = new RegisterDialog(this);
            regDialog.setVisible(true);

            if (!regDialog.isRegistered()) {
                System.exit(0);
            } else {
                user = regDialog.getUser();
            }
        } else {
            LoginDialog loginDialog = new LoginDialog(this);
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
                if (nettyBootstrapClient != null) {
                    nettyBootstrapClient.closeConnection();
                }
                super.windowClosing(e);
            }
        });

        setTitle(String.format("Облачное хранилище. Пользователь %s", user));
        rootPath = getRootByOS(user);
    }


    private String getRootByOS(String user) throws IOException {
        List<File> list = Arrays.asList(File.listRoots());
        String res = list.get(0).toString() + "CS" + File.separator + "Client" + File.separator;
        if (!Files.exists(Paths.get(res))) {
            Files.createDirectory(Paths.get(res));
        }
        return Paths.get(res).toString();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(MainWindow.this,
                message,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
    }

    private byte[] prepareSendData(String comType, String user, byte[] data) {
        byte[] total = new byte[1024];
        System.arraycopy("marker".getBytes(), 0, total, 0, "marker".getBytes().length);
        System.arraycopy(comType.getBytes(), 0, total, 6, "req_fl".getBytes().length);
        System.arraycopy(user.getBytes(), 0, total, 12, user.getBytes().length);
        if (data != null) {
            System.arraycopy(data, 0, total, 28, data.length);
        }
        return total;
    }
}
