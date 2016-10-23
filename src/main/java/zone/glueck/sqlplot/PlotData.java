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

    private double min = Double.MAX_VALUE;

    private double max = -Double.MAX_VALUE;

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

    public double[] getZRange() {

        if (this.data == null) {
            return null;
        }

        if (this.max == -Double.MAX_VALUE && this.min == Double.MAX_VALUE) {

            for (double[] vals : this.data) {
                if (vals.length == 2) {
                    return null;
                }
                this.min = Math.min(this.min, vals[2]);
                this.max = Math.max(this.max, vals[2]);
            }

        }

        return new double[]{this.min, this.max};
    }
}
