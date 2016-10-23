package zone.glueck.sqlplot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zach on 10/23/16.
 */
public enum PlotSettings {

    INSTANCE;

    private int[][] colorArray;

    private boolean hasAlphaChannel = false;

    public synchronized Color getColor(double min, double max, double value) {

        if (colorArray == null) {
            try {
                BufferedImage colorMap = ImageIO.read(getClass().getResourceAsStream("/JetColormap.png"));

                if (colorMap != null) {
                    this.colorArray = convertTo2DWithoutUsingGetRGB(colorMap);
                } else {
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        double percent = value / (max - min);
        int rowToUse = (int) (colorArray.length * 0.5);
        int colToUse = Math.min(colorArray[rowToUse].length - 1, (int) (colorArray[rowToUse].length * percent));

        return new Color(colorArray[rowToUse][colToUse], this.hasAlphaChannel);

    }

    private int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        this.hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (this.hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

}
