package zone.glueck.sqlplot;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

import java.util.List;

/**
 * Created by zach on 10/23/16.
 */
public class PlotData implements XYZDataset {

    protected List<double[]> data;

    @Override
    public Number getZ(int i, int i1) {
        return getZValue(i, i1);
    }

    @Override
    public double getZValue(int i, int i1) {
        return this.data.get(i1)[2];
    }

    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }

    @Override
    public int getItemCount(int i) {
        return this.data.size();
    }

    @Override
    public Number getX(int i, int i1) {
        return getXValue(i, i1);
    }

    @Override
    public double getXValue(int i, int i1) {
        return this.data.get(i1)[0];
    }

    @Override
    public Number getY(int i, int i1) {
        return getYValue(i, i1);
    }

    @Override
    public double getYValue(int i, int i1) {
        return this.data.get(i1)[1];
    }

    @Override
    public int getSeriesCount() {
        return 1;
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

    }

    @Override
    public void removeChangeListener(DatasetChangeListener datasetChangeListener) {

    }

    @Override
    public DatasetGroup getGroup() {
        return null;
    }

    @Override
    public void setGroup(DatasetGroup datasetGroup) {

    }
}
