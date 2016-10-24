package zone.glueck.sqlplot.sql;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    /**
     * Designated SQL interaction thread. All SQL operations should go through this thread which hosts the {@link
     * SQLController}.
     */
    private final ExecutorService sqlService = Executors.newSingleThreadExecutor(r -> new Thread(r, "zone.glueck.sql"));

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
     * SQL Data Change Listeners
     */
    private final Set<AbstractSqlGateway> gateways = new CopyOnWriteArraySet<>();

    private SQLController() throws SQLException {

        // this.connection = DriverManager.getConnection("jdbc:sqlite:/Users/zach/tmp/mydb.sqlite");
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:");

    }

    public static synchronized void initialized() throws SQLException {

        if (INSTANCE == null) {
            INSTANCE = new SQLController();
        }

    }

    public static synchronized SQLController getController() {

        if (INSTANCE != null) {
            return INSTANCE;
        } else {
            throw new IllegalStateException("the controller has not been initialized");
        }

    }

    public void addGatewayListener(AbstractSqlGateway gateway) {
        this.gateways.add(gateway);
    }

    public void removeGatewayListener(AbstractSqlGateway gateway) {
        this.gateways.remove(gateway);
    }

    public void executeQuery(AbstractSqlGateway callingGateway, String query) {

        if (callingGateway == null) {
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
                    SQLData data = new CollectionBasedSQLData(SQLData.Status.ERROR);
                    callingGateway.queryResultReturn(data);
                    return;
                }

                CollectionBasedSQLData data = new CollectionBasedSQLData(SQLData.Status.SUCCESS);

                int resultColumns = 2; // at a minimum
                ResultSetMetaData metaData = resultSet.getMetaData();
                if (metaData != null) {
                    resultColumns = metaData.getColumnCount();
                    String[] names = new String[resultColumns];
                    for (int i = 0; i < resultColumns; i++) {
                        names[i] = metaData.getColumnName(i + 1);
                    }
                    data.setColumnNames(names);
                }

                while(resultSet.next()) {
                    double[] rowVals = new double[resultColumns];
                    for (int i = 0; i<resultColumns; i++) {
                        rowVals[i] = resultSet.getDouble(i + 1);
                    }
                    data.addRow(rowVals);
                }

                callingGateway.queryResultReturn(data);

            } catch (SQLException e) {
                e.printStackTrace();
                SQLData data = new CollectionBasedSQLData(SQLData.Status.ERROR);
                callingGateway.queryResultReturn(data);
            }

        });

    }

    public void addData(DataSource data, String tableName) {

        if (data == null) {
            throw new IllegalArgumentException("sql data cannot be null");
        }

        this.sqlService.submit(() -> {

            if (tableName != null) {
                if (this.tables.contains(tableName)) {
                    // Check if the specified table matches the number of columns
                    Set<String> columns = this.fields.get(tableName);
                    if (columns != null) {
                        if (columns.containsAll(data.getColumnNames())) {
                            addRowData(data, tableName);
                        }
                    } else {
                        // TODO - add resolution between disagreeing column tracking, this shouldn't happen and indicates an error
                    }
                } else {
                    addTable(data, tableName);
                    addRowData(data, tableName);
                }
            } else {
                addTable(data, String.format(DEFAULT_TABLE_TAG, TABLE_COUNT.getAndIncrement()));
                addRowData(data, tableName);
            }

        });

    }

    private void addTable(DataSource data, String table) {

        assert data != null;
        assert table != null;

        StringBuilder sb = new StringBuilder();
        LinkedHashSet<String> headers = new LinkedHashSet<>();
        sb.append("create table ").append(table).append(" ( ");
        for (String header : data.getColumnNames()) {
            sb.append(header).append(" , ");
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

        this.fireDatasetChange();

    }

    private void addRowData(DataSource data, String table) {

        assert data != null;
        assert table != null;

        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(table).append(" (");
        StringBuilder sbPost = new StringBuilder();
        sbPost.append(" values ( ");
        boolean first = true;
        for (String columnName : data.getColumnNames()) {
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

            int rows = data.getRowCount();
            int cols = data.getColumnCount();
            for(int i = 0; i < rows; i++) {
                for (int j = 0 ; j < cols; j++) {
                    preparedStatement.setString(cols + 1, data.getValueAt(i, j));
                }
                preparedStatement.addBatch();
            }

            this.connection.setAutoCommit(true);
            preparedStatement.executeBatch();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void fireDatasetChange() {
        // Create a copy of the current table to field mapping and broadcast out
        Map<String, Set<String>> tablesAndFields = new HashMap<>();

        for (String table : this.tables) {
            Set<String> fields = this.fields.get(table);
            if (fields != null) {
                Set<String> copySet = new TreeSet<>();
                copySet.addAll(fields);
                tablesAndFields.put(table, Collections.unmodifiableSet(copySet));
            }
        }

        Map<String, Set<String>> distributableMap = Collections.unmodifiableMap(tablesAndFields);

        for (AbstractSqlGateway gateway : gateways) {
            gateway.dataTableChange(distributableMap);
        }

    }

}
