package zone.glueck.sqlplot;

import zone.glueck.sqlplot.charts.ChartPanel;
import zone.glueck.sqlplot.parser.ClipboardParser;
import zone.glueck.sqlplot.sql.SQLController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 * Created by zach on 10/23/16.
 */
public class SQLPlot {

    private final JFrame frame = new JFrame("SQLPlot");

    public SQLPlot() {

        // initialize the database
        try {
            SQLController.initialized();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(2);
        }

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem captureClipboard = new JMenuItem("Capture Clipboard Data");
        captureClipboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClipboardParser clipboardParser = ClipboardParser.parse();
                if (clipboardParser != null) {
                    SQLController.getController().addData(clipboardParser, "clipboard");
                }
            }
        });
        fileMenu.add(captureClipboard);
        menuBar.add(fileMenu);
        this.frame.setJMenuBar(menuBar);

        ChartPanel panel = new ChartPanel(SQLController.getController());
        this.frame.add(panel.getPanel(), BorderLayout.CENTER);
        this.frame.pack();
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setLocationRelativeTo(null);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SQLPlot plot = new SQLPlot();
                plot.frame.setVisible(true);
            }
        });
    }
}
