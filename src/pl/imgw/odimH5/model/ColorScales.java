/**
 * (C) 2011 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model;

import java.awt.Color;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ColorScales {

    private Set<MapColor> scale;

    /**
     * Set default color scale
     * 
     * 
     */
    public ColorScales() {

        this.scale = getRainbowScale();

    }

    /**
     * 
     * Set color scale
     * 
     * @param values
     * @param colors
     */
    public ColorScales(Set<MapColor> scale) {

        this.scale = scale;

    }

    public static Set<MapColor> getRainbowScale() {

        Set<MapColor> scale = new TreeSet<MapColor>();

        // min value
        scale.add(new MapColor(0, new Color(0, 0, 0)));
        scale.add(new MapColor(73, new Color(0, 0, 255)));
        scale.add(new MapColor(79, new Color(0, 50, 255)));
        scale.add(new MapColor(85, new Color(0, 120, 255)));
        scale.add(new MapColor(85, new Color(0, 120, 255)));
        scale.add(new MapColor(91, new Color(25, 160, 255)));
        scale.add(new MapColor(97, new Color(81, 210, 255)));
        scale.add(new MapColor(97, new Color(81, 210, 255)));
        scale.add(new MapColor(103, new Color(135, 240, 255)));
        scale.add(new MapColor(109, new Color(255, 255, 255)));
        scale.add(new MapColor(115, new Color(255, 245, 190)));
        scale.add(new MapColor(115, new Color(255, 245, 190)));
        scale.add(new MapColor(121, new Color(255, 230, 0)));
        scale.add(new MapColor(127, new Color(255, 190, 0)));
        scale.add(new MapColor(133, new Color(255, 110, 0)));
        scale.add(new MapColor(139, new Color(255, 60, 0)));
        scale.add(new MapColor(145, new Color(200, 0, 0)));
        scale.add(new MapColor(151, new Color(160, 0, 0)));
        scale.add(new MapColor(157, new Color(125, 0, 0)));

        // no data
        scale.add(new MapColor(255, new Color(127, 127, 127)));

        return scale;
    }
    public static Set<MapColor> getODCScale() {
        
        Set<MapColor> scale = new TreeSet<MapColor>();
        
        // min value
//        scale.add(new MapColor(-9999000, new Color(127, 127, 127)));
//        scale.add(new MapColor(0, new Color(0, 0, 0)));
        scale.add(new MapColor(-12, new Color(0, 0, 40)));
        scale.add(new MapColor(-9, new Color(0, 0, 80)));
        scale.add(new MapColor(-6, new Color(0, 0, 120)));
        scale.add(new MapColor(-3, new Color(0, 0, 160)));
        scale.add(new MapColor(0, new Color(0, 0, 210)));
        scale.add(new MapColor(3, new Color(0, 0, 255)));
        scale.add(new MapColor(6, new Color(0, 50, 255)));
        scale.add(new MapColor(9 , new Color(0, 120, 255)));
        scale.add(new MapColor(12, new Color(0, 120, 255)));
        scale.add(new MapColor(15, new Color(25, 160, 255)));
        scale.add(new MapColor(18, new Color(81, 210, 255)));
        scale.add(new MapColor(21, new Color(81, 210, 255)));
        scale.add(new MapColor(24, new Color(135, 240, 255)));
        scale.add(new MapColor(27, new Color(255, 255, 255)));
        scale.add(new MapColor(30, new Color(255, 245, 190)));
        scale.add(new MapColor(33, new Color(255, 245, 190)));
        scale.add(new MapColor(36, new Color(255, 230, 0)));
        scale.add(new MapColor(39, new Color(255, 190, 0)));
        scale.add(new MapColor(42, new Color(255, 110, 0)));
        scale.add(new MapColor(45, new Color(255, 60, 0)));
        scale.add(new MapColor(48, new Color(200, 0, 0)));
        scale.add(new MapColor(51, new Color(160, 0, 0)));
        scale.add(new MapColor(54, new Color(125, 0, 0)));
        
        // no data
//        scale.add(new MapColor(255, new Color(127, 127, 127)));
        
        return scale;
    }

    public static Set<MapColor> getGrayScale() {
        Set<MapColor> scale = new TreeSet<MapColor>();

        scale.add(new MapColor(0, new Color(0, 0, 0)));
        scale.add(new MapColor(1, new Color(20, 20, 20)));
        for (int i = 2; i < 20; i++) {
            scale.add(new MapColor(i * 32, new Color(20+i * 12, 20+i * 12, 20+i * 12)));
        }
        return scale;
    }

    public static Set<MapColor> getDefaultScale() {
        Set<MapColor> scale = new TreeSet<MapColor>();

        scale.add(new MapColor(0, new Color(0, 0, 0)));
        Random rnd = new Random(System.currentTimeMillis());
        for (int i = 1; i < 20; i++) {
            scale.add(new MapColor(i, new Color(rnd.nextInt(256), rnd
                    .nextInt(256), rnd.nextInt(256))));
        }
        return scale;
    }

    /**
     * @return the scale
     */
    public Set<MapColor> getScale() {
        return scale;
    }

    /**
     * @param scale
     *            the scale to set
     */
    public void setScale(Set<MapColor> scale) {
        this.scale = scale;
    }

    public static void main(String[] args) {

        Set<MapColor> set = ColorScales.getGrayScale();

        for (MapColor color : set) {
            System.out.println(color.getValue() + " "
                    + color.getColor().toString() + " nr: " + color.getColor().getRGB());
        }

    }

}
