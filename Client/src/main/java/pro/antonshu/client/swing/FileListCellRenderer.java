package pro.antonshu.client.swing;

import javax.swing.*;
import java.awt.*;

public class FileListCellRenderer extends JPanel implements ListCellRenderer<String>{

    private final JLabel fileName;
    private final JPanel panel;

    public FileListCellRenderer() {
        super();
        setLayout(new BorderLayout());

        fileName = new JLabel();
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(fileName);
        Font f = fileName.getFont();
        fileName.setFont(f.deriveFont(f.getStyle() | Font.BOLD));

        add(panel, BorderLayout.NORTH);

    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list,
                                                  String value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setBackground(list.getBackground());
        fileName.setOpaque(true);
        fileName.setText(value);
        return this;
    }
}
