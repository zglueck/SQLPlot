package zone.glueck.sqlplot;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zach on 10/22/16.
 */
public class SQLController {

    /**
     * The default table tag if one isn't specified.
     */
    public static final String DEFAULT_TABLE_TAG = "table%02d";

    /**
     * An atomic table counter for use when a table name isn't supplied.
     */
    private static final AtomicInteger TABLE_COUNT = new AtomicInteger(0);

    /*
    Property Change Event tags
     */
    public static final String TABLE_ADDED = "zone.glueck.SQLController.tableadded";
    public static final String DATA_ADDED = "zone.glueck.SQLController.datadded";
    public static final String QUERY_COMPLETE = "zone.glueck.SQLController.querycomplete";

    /**
     * Designated SQL interaction thread. All SQL operations should go through this thread which hosts the {@link
     * SQLController}.
     */
    private final ExecutorService sqlService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(r, "zone.glueck.sql");
        }
    });

    /**
     * The singleton instance for SQL interactions.
     */
    private static SQLController INSTANCE = null;

    /**
     * The database connection.
     */
    private final Connection connection;

    /**
     * A collection of the current tables.
     */
    private final Set<String> tables = new TreeSet<>();

    /**
     * The active fields mapped by table.
     */
    private final Map<String, Set<String>> fields = new HashMap<>();

    /**
     * Property change support
     */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private SQLController() throws SQLException {

        // this.connection = DriverManager.getConnection("jdbc:sqlite:/Users/zach/tmp/mydb.sqlite");
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");

    }

    public static synchronized SQLController getController() throws SQLException {

        if (INSTANCE == null) {
            INSTANCE = new SQLController();
        }

        return INSTANCE;
    }

    public void addChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removeChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public void executeQuery(Object callingObject, String query) {

        if (callingObject == null) {
            throw new IllegalArgumentException("calling object cannot be null");
        }

        // minimal property checking
        if (query == null || query.isEmpty()) {
            // TODO - there really should be some sql sanitation
            throw new IllegalArgumentException("the query cannot be null or empty");
        }

        this.sqlService.submit(() -> {

            try {

                Statement statement = this.connection.createStatement();

                // This operation may take awhile
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet == null) {
                    // null values indicate erroneous query
                    PropertyChangeEvent pce = new PropertyChangeEvent(callingObject, QUERY_COMPLETE, null, null);
                }
                int resultColumns = resultSet.getMetaData().getColumnCount();
                List<double[]> vals = new ArrayList<>();
                while(resultSet.next()) {
                    double[] rowVals = new double[resultColumns];
                    for (int i = 0; i<resultColumns; i++) {
                        rowVals[i] = resultSet.getDouble(i + 1);
                    }
                    vals.add(rowVals);
                }
                PlotData plotData = new PlotData();
                plotData.data = vals;
                // This method caches the results and for a particularly long dataset I would rather eat up cycles on
                // the SQL thread rather than the Swing EDT
                plotData.getZRange();

                PropertyChangeEvent pce = new PropertyChangeEvent(callingObject, QUERY_COMPLETE, null, plotData);
                this.pcs.firePropertyChange(pce);

            } catch (SQLException e) {
                e.printStackTrace();
                // null values indicate erroneous query
                PropertyChangeEvent pce = new PropertyChangeEvent(callingObject, QUERY_COMPLETE, null, null);
                this.pcs.firePropertyChange(pce);
            }

        });

    }

    public void addData(SQLData data, String table) {

        if (data == null) {
            throw new IllegalArgumentException("sql data cannot be null");
        }

        this.sqlService.submit(() -> {

            if (table != null) {
                if (this.tables.contains(table)) {
                    // Check if the specified table matches the number of columns
                    Set<String> columns = this.fields.get(table);
                    if (columns != null) {
                        if (columns.containsAll(data.getHeaders())) {
                            addRowData(data, table);
                        }
                    } else {
                        // TODO - add resolution between disagreeing column tracking, this shouldn't happen and indicates an error
                    }
                } else {
                    addTable(data, table);
                    addRowData(data, table);
                }
            } else {
                addTable(data, String.format(DEFAULT_TABLE_TAG, TABLE_COUNT.getAndIncrement()));
                addRowData(data, table);
            }

        });

    }

    private void addTable(SQLData data, String table) {

        assert data != null;
        assert table != null;

        StringBuilder sb = new StringBuilder();
        LinkedHashSet<String> headers = new LinkedHashSet<>();
        sb.append("create table ").append(table).append(" ( ");
        for (String header : data.getHeaders()) {
            sb.append(header).append(" REAL, ");
            headers.add(header);
        }
        int length = sb.length();
        sb.delete(length - 2, length - 1);
        sb.append(")");

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connection.prepareStatement(sb.toString());
            preparedStatement.execute();
            preparedStatement.close();
            // Add to internal tracking
            this.tables.add(table);
            this.fields.put(table, headers);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Prepare a distributable set for consumption by SQLPlot classes
        // TODO - check if this should be done on a separate thread
        Set<String> tables = new TreeSet<>();
        tables.addAll(this.tables);
        PropertyChangeEvent pce = new PropertyChangeEvent(this, TABLE_ADDED, null, tables);
        this.pcs.firePropertyChange(pce);

    }

    private void addRowData(SQLData data, String table) {

        assert data != null;
        assert table != null;

        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(table).append(" (");
        StringBuilder sbPost = new StringBuilder();
        sbPost.append(" values ( ");
        boolean first = true;
        for (String columnName : data.getHeaders()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
                sbPost.append(", ");
            }
            sb.append(columnName);
            sbPost.append("?");
        }
        String statement = sb.toString() + ")" + sbPost.toString() + ");";

        try {

            this.connection.setAutoCommit(false);

            PreparedStatement preparedStatement = this.connection.prepareStatement(statement);

            double value;
            for(SQLRow row : data.data) {
                int columnIndex = 1;
                for (String col : row.columns) {
                    try {
                        value = Double.parseDouble(col);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        value = 0.0;
                    }
                    preparedStatement.setDouble(columnIndex++, value);
                }
                preparedStatement.addBatch();
            }

            this.connection.setAutoCommit(true);
            preparedStatement.executeBatch();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        PropertyChangeEvent pce = new PropertyChangeEvent(this, DATA_ADDED, null, null);
        this.pcs.firePropertyChange(pce);

    }

}
