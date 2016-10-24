package zone.glueck.sqlplot.charts;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import zone.glueck.sqlplot.sql.AbstractSqlGateway;
import zone.glueck.sqlplot.sql.SQLController;
import zone.glueck.sqlplot.sql.SQLData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zach on 10/20/16.
 */
public class ChartPanel extends AbstractSqlGateway {

    /**
     * This panel which shows a plot and lists for the fields, tables, and commands.
     */
    private final JPanel panel;

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

        try {
            SQLController.initialized();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }


        SwingUtilities.invokeLater(() -> {
            ChartPanel chartPanel = new ChartPanel(SQLController.getController());
            JFrame frame = new JFrame("Chart Panel Test");
            frame.add(chartPanel.panel, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }

    public ChartPanel(SQLController sqlController) {
        super(sqlController);

        this.panel = new JPanel(new BorderLayout());

        this.initLayout();
        this.initActions();

    }

    @Override
    protected void queryResultReturn(SQLData data) {

        if (data != null) {

            if (data.getStatus() == SQLData.Status.ERROR) {
                // TODO - inform user of error condition
            }

            if (data.getStatus() == SQLData.Status.SUCCESS && data.getColumnCount() > 1) {
                DataTranslator dataset = new DataTranslator(data);
                List<String> columnNames = data.getColumnNames();
                final XYPlot plot = new XYPlot(
                        dataset,
                        new NumberAxis(columnNames.get(0)),
                        new NumberAxis(columnNames.get(1)),
                        new PlotRenderer(dataset));
                SwingUtilities.invokeLater(() -> {
                    this.chartPanel.setChart(new JFreeChart("data", new Font("Sans Serif", 0, 14), plot, false));
                });
            }

        }

    }

    @Override
    protected void dataTableChange(Map<String, Set<String>> tablesAndFields) {

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

}
