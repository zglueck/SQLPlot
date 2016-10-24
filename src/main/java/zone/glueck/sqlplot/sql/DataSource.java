package zone.glueck.sqlplot.sql;

import java.util.List;

/**
 * Created by zach on 10/23/16.
 */
public interface DataSource {

    public List<String> getColumnNames();

    public int getColumnCount();

    public int getRowCount();

    public String getValueAt(int row, int column);

}
