package zone.glueck.sqlplot.sql;

import java.util.List;

/**
 * Created by zach on 10/23/16.
 */
public interface SQLData  {

    public SQLData.Status getStatus();

    public List<String> getColumnNames();

    public int getColumnCount();

    public int getRowCount();

    public double getValueAt(int row, int column);

    public enum Status {
        SUCCESS, ERROR
    }

}
