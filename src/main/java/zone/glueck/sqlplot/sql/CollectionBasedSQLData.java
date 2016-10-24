package zone.glueck.sqlplot.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zach on 10/23/16.
 */
public class CollectionBasedSQLData implements SQLData {

    public static final String DEFAULT_COLUMN_NAME_PATTERN = "Column%03d";

    protected final List<double[]> dataArray = new ArrayList<>();

    protected final List<String> columnNames = new ArrayList<>();

    protected int columns = -1;

    protected final SQLData.Status status;

    public CollectionBasedSQLData(Status status) {
        this.status = status;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    protected void setColumnNames(String[] names) {

        if (names == null) {
            throw new IllegalArgumentException("column names cannot be null");
        }

        if (columnNames.isEmpty()) {
            // has data already been added? The number of columns will already be set and we need to check to make sure
            // the headers provided have the same dimensionality
            if (this.columns == -1) {
                // no header or data has been provided, set both
                for( String name : names) {
                    this.columnNames.add(name);
                }
                this.columns = this.columnNames.size();
            } else {
                if (this.columns == names.length) {
                    // Update the headers the dimensionality matches
                    for (String name : names) {
                        this.columnNames.add(name);
                    }
                } else {
                    throw new IllegalArgumentException("the provided names does not match the dimensionality of the columns");
                }
            }
        } else {
            // if they have the same dimension, replace
            if (this.columnNames.size() == names.length) {
                this.columnNames.clear();
                for (String name : names) {
                    this.columnNames.add(name);
                }
            } else {
                throw new IllegalArgumentException("the provided names did not have the same dimension as the current columns");
            }
        }

    }

    protected void addRow(double[] data) {

        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        if (dataArray.isEmpty() && columns == -1) {
            // The first data array will set the column count, unless the column count was set by specifying the names
            this.columns = data.length;
        }

        if (data.length != this.columns) {
            throw new IllegalArgumentException("data column count did not match the datasets column count");
        }

        this.dataArray.add(data);

    }

    @Override
    public List<String> getColumnNames() {
        if (this.columns == -1) {
            // the data hasn't been initialized yet
            return null;
        }

        if (this.columnNames.isEmpty()) {
            // need to provide the default pattern
            for (int i = 0; i < this.columns ; i++) {
                this.columnNames.add(String.format(DEFAULT_COLUMN_NAME_PATTERN, i));
            }
        }

        return Collections.unmodifiableList(this.columnNames);
    }

    @Override
    public int getColumnCount() {
        return this.columns;
    }

    @Override
    public int getRowCount() {
        return this.dataArray.size();
    }

    @Override
    public double getValueAt(int row, int column) {
        if (row < this.dataArray.size() && column < this.columns && row > -1 && column > -1) {
            return this.dataArray.get(row)[column];
        } else {
            throw new IllegalArgumentException("the row and column provided do not correspond to the data table");
        }
    }
}
