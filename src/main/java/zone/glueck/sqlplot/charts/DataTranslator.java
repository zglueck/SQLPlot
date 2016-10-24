package zone.glueck.sqlplot.charts;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;
import zone.glueck.sqlplot.sql.SQLData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by zach on 10/23/16.
 */
public class DataTranslator implements XYZDataset {

    private final List<SQLData> dataSets = new ArrayList<>();

    private final Set<DatasetChangeListener> datasetChangeListeners = new CopyOnWriteArraySet<>();

    protected DataTranslator(SQLData data) {

        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        this.dataSets.add(data);
    }

    protected void addDataset(SQLData data) {
        this.dataSets.add(data);
    }

    @Override
    public Number getZ(int i, int i1) {
        return this.getZValue(i, i1);
    }

    @Override
    public double getZValue(int i, int i1) {
        SQLData dataSet = this.dataSets.get(i);
        if (dataSet.getColumnCount() > 2) {
            return dataSet.getValueAt(i1, 2);
        } else {
            return 0.0;
        }
    }

    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }

    @Override
    public int getItemCount(int i) {
        return this.dataSets.get(i).getRowCount();
    }

    @Override
    public Number getX(int i, int i1) {
        return this.getXValue(i, i1);
    }

    @Override
    public double getXValue(int i, int i1) {
        return this.dataSets.get(i).getValueAt(i1, 0);
    }

    @Override
    public Number getY(int i, int i1) {
        return this.getYValue(i, i1);
    }

    @Override
    public double getYValue(int i, int i1) {
        return this.dataSets.get(i).getValueAt(i1, 1);
    }

    @Override
    public int getSeriesCount() {
        return this.dataSets.size();
    }

    @Override
    public Comparable getSeriesKey(int i) {
        return null;
    }

    @Override
    public int indexOf(Comparable comparable) {
        return 0;
    }

    @Override
    public void addChangeListener(DatasetChangeListener datasetChangeListener) {
        this.datasetChangeListeners.add(datasetChangeListener);
    }

    @Override
    public void removeChangeListener(DatasetChangeListener datasetChangeListener) {
        this.datasetChangeListeners.remove(datasetChangeListener);
    }

    @Override
    public DatasetGroup getGroup() {
        return null;
    }

    @Override
    public void setGroup(DatasetGroup datasetGroup) {

    }
}
