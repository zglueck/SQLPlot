package zone.glueck.sqlplot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYZDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.LinkedHashSet;

/**
 * Created by zach on 10/20/16.
 */
public class ChartPanel implements PropertyChangeListener {

    /**
     * This panel which shows a plot and lists for the fields, tables, and commands.
     */
    private final JPanel panel;

    /**
     * The SQL controller to use for queries and data.
     */
    private final SQLController sqlController;

    /*
    JComponents
     */
    private final org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(null);
    private final JList tablesList = new JList();
    private final JList fieldsList = new JList();
    private final JList commandsList = new JList();
    private final JTextArea commandTextArea = new JTextArea();
    private final JButton executeButton = new JButton("Execute Command");

    // For testing purposes only
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ChartPanel chartPanel = new ChartPanel();
                JFrame frame = new JFrame("Chart Panel Test");
                frame.add(chartPanel.panel, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

    public ChartPanel() {

        this.panel = new JPanel(new BorderLayout());

        this.initLayout();
        this.initActions();

        try {
            this.sqlController = SQLController.getController();
            this.sqlController.addChangeListener(this);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("the sql controller did not initialize properly");
        }

        SQLData sqlData = new SQLData();
        LinkedHashSet<String> headers = new LinkedHashSet<>();
        headers.add("x");
        headers.add("y");
        headers.add("z");
        sqlData.setHeaders(headers);

        for (int i = 0; i<10; i++) {
            SQLRow sqlRow = new SQLRow(new String[]{(i + 100.0) + "", (i + 10.0) + "", (i + 1.0) + ""});
            sqlData.addRow(sqlRow);
        }

        this.sqlController.addData(sqlData, "myTable");

    }

    private void initLayout() {

        final Insets insets = new Insets(5,5,5,5);

        // SQL Command Execution Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = insets;
        c.weightx = 0.5;
        c.weighty = 0.5;
        JPanel commandPanel = new JPanel(new GridBagLayout());
        commandPanel.add(this.commandTextArea, c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 1;
        c.gridy = 1;
        c.insets = insets;
        commandPanel.add(this.executeButton, c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = insets;
        c.weightx = 0.5;
        commandPanel.add(new JLabel("Welcome to SQLPlot"), c);
        this.commandTextArea.setBorder(BorderFactory.createEtchedBorder());

        // List Panel
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = insets;
        JPanel listPanel = new JPanel(new GridBagLayout());
        listPanel.add(new JLabel("Tables:"), c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = insets;
        c.weightx= 0.5;
        c.weighty = 0.2;
        listPanel.add(new JScrollPane(this.tablesList), c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 2;
        c.insets = insets;
        listPanel.add(new JLabel("Fields:"), c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 3;
        c.insets = insets;
        c.weightx= 0.5;
        c.weighty = 0.2;
        listPanel.add(new JScrollPane(this.fieldsList), c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 4;
        c.insets = insets;
        listPanel.add(new JLabel("Commands:"), c);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 5;
        c.insets = insets;
        c.weightx= 0.5;
        c.weighty = 0.2;
        listPanel.add(new JScrollPane(this.commandsList), c);

        // Full panel setup
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.chartPanel, listPanel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplitPane, commandPanel);
        this.panel.add(splitPane, BorderLayout.CENTER);

    }

    private void initActions() {

        this.executeButton.addActionListener((ActionEvent e) -> {
            String query = this.commandTextArea.getText();
            if (query != null && !query.isEmpty()) {
                this.sqlController.executeQuery(this, query);
            }
        });

    }

    public void propertyChange(PropertyChangeEvent evt) {

        if (evt != null) {
            // The event source should be this object if the event is a result return
            if (evt.getSource() == this && evt.getPropertyName().equals(SQLController.QUERY_COMPLETE)) {
                Object eventObject = evt.getNewValue();
                if (eventObject != null) {
                    PlotData dataset = (PlotData) eventObject;
                    SwingUtilities.invokeLater(() -> {
                        XYPlot plot = new XYPlot(dataset, new NumberAxis("x"), new NumberAxis("y"), new PlotRenderer(dataset));
                        this.chartPanel.setChart(new JFreeChart("data", new Font("Serif", 0, 14), plot, false));
                        this.chartPanel.updateUI();
                        //this.chartPanel.setChart(ChartFactory.createScatterPlot("Data", "X Values", "Y Values", dataset));
                    });
                }
            } else if (evt.getPropertyName().equals(SQLController.TABLE_ADDED)) {
                // TODO - add table updating
            } else if (evt.getPropertyName().equals(SQLController.DATA_ADDED)) {
                // TODO - add data updating
            }

        }

    }
}
