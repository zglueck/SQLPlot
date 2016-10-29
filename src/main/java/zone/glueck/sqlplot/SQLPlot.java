package zone.glueck.sqlplot;

import zone.glueck.sqlplot.charts.ChartPanel;
import zone.glueck.sqlplot.parser.ClipboardParser;
import zone.glueck.sqlplot.sql.SQLController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

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

                String tableName = JOptionPane.showInputDialog("Table Name", "clipboard");
                if (tableName == null || tableName.isEmpty()) {
                    tableName = "clipboard";
                }
                final String workerTableName = tableName;

                JDialog statusDialog = new JDialog(frame, "Clipboard Import", false);
                statusDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
                statusDialog.setLayout(new GridBagLayout());
                final Insets insets = new Insets(5,5,5,5);
                GridBagConstraints c = new GridBagConstraints();
                c.insets = insets;
                c.weightx = 0.5;
                c.gridx = 0;
                c.gridy = 0;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.CENTER;
                JProgressBar progressBar = new JProgressBar(0, 99);
                progressBar.setString("Parsing clipboard data...");
                statusDialog.add(progressBar, c);
                statusDialog.setVisible(true);

                SwingWorker<Void, Void> parseOperation = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {

                        ClipboardParser clipboardParser = ClipboardParser.parse();
                        this.publish((Void) null);
                        if (clipboardParser != null) {
                            SQLController.getController().addData(clipboardParser, workerTableName);
                        }

                        return null;
                    }

                    @Override
                    protected void process(List<Void> chunks) {
                        progressBar.setString("Loading into database...");
                    }

                    @Override
                    protected void done() {
                        statusDialog.dispose();
                    }
                };

                parseOperation.execute();

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
