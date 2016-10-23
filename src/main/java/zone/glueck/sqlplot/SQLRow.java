package zone.glueck.sqlplot;

/**
 * Created by zach on 10/22/16.
 */
public class SQLRow {

    protected final String[] columns;

    public SQLRow(String[] columns) {

        if (columns == null) {
            throw new IllegalArgumentException("columns cannot be null");
        } else {
            for (String column : columns) {
                if (column == null) {
                    throw new IllegalArgumentException("column cannot be null");
                }
            }
        }

        this.columns = columns;
    }

    public int getColumnCount() {
        return this.columns.length;
    }
}
