/**
 * (C) 2011 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Set;

import pl.imgw.odimH5.model.MapColor;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class PictureFromArray {

    int width;
    int height;
    BufferedImage img;

    public PictureFromArray(int[][] array, Set<MapColor> colors, int gridSize) {

        width = array.length;
        height = array[0].length;

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int rgb = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (MapColor color : colors) {
                    if (array[i][j] >= color.getValue()) {
                        rgb = color.getColor().getRGB();
                    }
                }

                if (array[i][j] == 0
                        && (i % gridSize == 0 || j % gridSize == 0)) {
                    rgb = Color.GRAY.getRGB();
                }
                img.setRGB(i, j, rgb);
                rgb = 0;

            }
        }
    }

    public PictureFromArray(int[][] array, Set<MapColor> colors) {

        width = array.length;
        height = array[0].length;

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int rgb = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (MapColor color : colors) {
                    if (array[i][j] >= color.getValue()) {
                        rgb = color.getColor().getRGB();
                    }
                }
                img.setRGB(i, j, rgb);
                rgb = 0;

            }
        }
    }

    public PictureFromArray(int[][] array, Set<MapColor> colors,
            BufferedImage image) {

        width = array.length;
        height = array[0].length;
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int rgb = 0;

        int darkGray = new Color(150, 150, 150).getRGB();
        int lightGray = new Color(200, 200, 200).getRGB();
        int borderColor = new Color(60, 60, 60).getRGB();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (array[i][j] > 3) {
                    for (MapColor color : colors) {
                        if (array[i][j] > color.getValue()) {
                            rgb = color.getColor().getRGB();
                        }
                    }
                    img.setRGB(i, j, rgb);

                } else {
                    if (image.getRGB(i, j) == -12698050)
                        img.setRGB(i, j, borderColor);
                    else if (array[i][j] >= -8888000) {
                        img.setRGB(i, j, lightGray);
                    } else
                        img.setRGB(i, j, darkGray);
                }
            }
        }
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the img
     */
    public BufferedImage getImg() {
        return img;
    }

}
