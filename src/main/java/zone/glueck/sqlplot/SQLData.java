package zone.glueck.sqlplot;

import java.util.LinkedHashSet;

/**
 * Created by zach on 10/20/16.
 */
public class SQLData {

    public static final String DEFAULT_COLUMN_TAG = "column%03d";

    private final LinkedHashSet<String> headers = new LinkedHashSet<>();

    protected final LinkedHashSet<SQLRow> data = new LinkedHashSet<>();

    protected void addRow(SQLRow row) {

        if (row == null || row.getColumnCount() == 0) {
            throw new IllegalArgumentException("row cannot be null or empty");
        }

        if (headers.isEmpty()) {
            // Use this first row to specify the headers
            int columnCount = 0;
            for (int i = 0; i<row.getColumnCount(); i++) {
                this.headers.add(String.format(DEFAULT_COLUMN_TAG, columnCount++));
            }
        } else {
            // Check the row contains the same number of columns
            if (row.getColumnCount() == headers.size()) {
                this.data.add(row);
            }
        }
    }

    protected void setHeaders(LinkedHashSet<String> headers) {

        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("header cannot be null or empty");
        }

        for (String header : headers) {
            this.headers.add(header);
        }

    }

    public int getColumnCount() {
        return this.headers.size();
    }

    public LinkedHashSet<String> getHeaders() {
        LinkedHashSet<String> tempHeaders = new LinkedHashSet<>();
        tempHeaders.addAll(this.headers);
        return tempHeaders;
    }

}
