package zone.glueck.sqlplot.charts;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by zach on 10/23/16.
 */
public class PlotRenderer extends AbstractXYItemRenderer {

    private final Shape defaultShape = ShapeUtilities.createDiamond(3f);

    private final DataTranslator data;

    private double[] zRange = null;

    public PlotRenderer(DataTranslator data) {
        this.data = data;
    }

    @Override
        public void drawItem(Graphics2D gd, XYItemRendererState xyirs,
                             Rectangle2D rd, PlotRenderingInfo pri, XYPlot xyplot,
                             ValueAxis va, ValueAxis va1, XYDataset xyd, int i, int i1,
                             CrosshairState cs, int i2) {

            Shape hotspot = defaultShape;
            EntityCollection entities = null;
            if (pri != null) {
                entities = pri.getOwner().getEntityCollection();
            }

            double x = xyd.getXValue(i, i1);
            double y = xyd.getYValue(i, i1);
            double transX = va.valueToJava2D(x, rd, xyplot.getDomainAxisEdge());
            double transY = va1.valueToJava2D(y, rd, xyplot.getRangeAxisEdge());
            Shape myshape = defaultShape;
            PlotOrientation orientation = xyplot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                myshape = ShapeUtilities.createTranslatedShape(myshape, transY,
                        transX);
            } else if (orientation == PlotOrientation.VERTICAL) {
                myshape = ShapeUtilities.createTranslatedShape(myshape, transX,
                        transY);
            }

            if (zRange == null) {
                this.determineZRange();
            }

            Color pointColor;

            if (zRange[0] == zRange[1]) {
                pointColor = Color.BLUE;
            } else {
                pointColor = PlotSettings.INSTANCE.getColor(zRange[0], zRange[1], this.data.getZValue(0, i1));
            }

            gd.setPaint(pointColor);
            gd.fill(myshape);
            gd.setPaint(Color.black);
            gd.setStroke(this.getItemOutlineStroke(i, i1));
            gd.draw(myshape);

            if (entities != null) {
                addEntity(entities, hotspot, xyd, i, i1, transX, transY);
            }
        }

        private void determineZRange() {
            int rowCount = this.data.getItemCount(0);
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            double val;
            for (int i = 0; i < rowCount; i++) {
                val = this.data.getZValue(0, i);
                min = Math.min(min, val);
                max = Math.max(max, val);
            }
            this.zRange = new double[]{min, max};
        }

    }
