/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow531;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import pl.imgw.odimH5.model.ParametersContainer;

/**
 * 
 * It contains helper methods for creating XML files
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelImage531XML {

    /**
     * 
     * Helper method for creating XML file using ParametersContainer
     * 
     * @param cont
     *            Input data
     * @param rb531
     *            Rainbow 5.31.1 model class instance
     * @param verbose
     *            verbose mode
     * @return XML document
     */
    public static Document createDescriptor(ParametersContainer cont, Model531 rb531,
            boolean verbose) {
        // Create XML document object
        Document od = rb531.proc.createXMLDocumentObject(verbose);

        Comment comment = od
                .createComment("ODIM_H5 descriptor file, platform: RAINBOW,"
                        + " file object: " + rb531.IMAGE);

        od.appendChild(comment);

        Element root = od.createElement(rb531.H5_GROUP);
        root.setAttribute(rb531.H5_OBJECT_NAME, rb531.H5_ROOT);
        od.appendChild(root);

        Element what = od.createElement(rb531.H5_GROUP);
        what.setAttribute(rb531.H5_OBJECT_NAME, rb531.H5_WHAT);

        what.appendChild(rb531.makeAttr("object", rb531.IMAGE, od, rb531.H5_STRING));
        what.appendChild(rb531.makeAttr("version", rb531.VERSION, od, rb531.H5_STRING));
        what.appendChild(rb531.makeAttr("date", cont.getDate(), od, rb531.H5_STRING));
        what.appendChild(rb531.makeAttr("time", cont.getTime(), od, rb531.H5_STRING));
        what.appendChild(rb531.makeAttr("source", cont.getSource(), od,
                rb531.H5_STRING));
        root.appendChild(what);

        Element where = od.createElement(rb531.H5_GROUP);
        where.setAttribute(rb531.H5_OBJECT_NAME, rb531.H5_WHERE);

        where.appendChild(rb531.makeAttr("projdef", cont.getProjection(), od,
                rb531.H5_STRING));
        where
                .appendChild(rb531.makeAttr("xsize", cont.getXsize(), od,
                        rb531.H5_LONG));
        where
                .appendChild(rb531.makeAttr("ysize", cont.getYsize(), od,
                        rb531.H5_LONG));
        where.appendChild(rb531.makeAttr("UL_lon", cont.getUlLon(), od,
                rb531.H5_DOUBLE));
        where.appendChild(rb531.makeAttr("UL_lat", cont.getUlLat(), od,
                rb531.H5_DOUBLE));
        where.appendChild(rb531.makeAttr("LR_lon", cont.getLrLon(), od,
                rb531.H5_DOUBLE));
        where.appendChild(rb531.makeAttr("LR_lat", cont.getLrLat(), od,
                rb531.H5_DOUBLE));
        where.appendChild(rb531.makeAttr("x_scale", cont.getXscale(), od,
                rb531.H5_DOUBLE));
        where.appendChild(rb531.makeAttr("y_scale", cont.getYscale(), od,
                rb531.H5_DOUBLE));

        root.appendChild(where);

        Element how = od.createElement(rb531.H5_GROUP);
        how.setAttribute(rb531.H5_OBJECT_NAME, rb531.H5_HOW);

        how.appendChild(rb531.makeAttr("task", cont.getTask(), od, rb531.H5_STRING));
        how.appendChild(rb531.makeAttr("startepochs", cont.getStartepochs(), od,
                rb531.H5_LONG));
        how.appendChild(rb531.makeAttr("endepochs", cont.getEndepochs(), od,
                rb531.H5_LONG));
        how.appendChild(rb531.makeAttr("system", rb531.RAINBOW_SYSTEM, od,
                rb531.H5_STRING));
        how.appendChild(rb531.makeAttr("software", rb531.RAINBOW_SOFTWARE, od,
                rb531.H5_STRING));
        how.appendChild(rb531.makeAttr("sw_version", cont.getSwVersion(), od,
                rb531.H5_STRING));
        how.appendChild(rb531.makeAttr("beamwidth", cont.getBeamwidth(), od,
                rb531.H5_DOUBLE));
        how.appendChild(rb531.makeAttr("wavelength", cont.getWavelength(), od,
                rb531.H5_DOUBLE));
        root.appendChild(how);

        Element dataset1 = od.createElement(rb531.H5_GROUP);
        dataset1.setAttribute(rb531.H5_OBJECT_NAME, rb531.H5_DATASET_N);

        // dataset specific what group
        Element dataset_what = od.createElement(rb531.H5_GROUP);
        dataset_what.setAttribute(rb531.H5_OBJECT_NAME, rb531.H5_WHAT);

        dataset_what.appendChild(rb531.makeAttr("product", cont.getProduct(), od,
                rb531.H5_STRING));

        if (cont.getProduct().matches("MAX")
                || cont.getProduct().matches("VIL")) {
            dataset_what.appendChild(rb531.makeAttr("prodpar", cont.getProdpar(),
                    od, rb531.H5_SEQUENCE));
        } else {
            dataset_what.appendChild(rb531.makeAttr("prodpar", cont.getProdpar(),
                    od, rb531.H5_DOUBLE));
        }
        if (cont.getPacNumProd() != null && cont.getPacMethod() != null) {
            dataset_what.appendChild(rb531.makeAttr("numprod", cont
                    .getPacNumProd(), od, rb531.H5_DOUBLE));
            dataset_what.appendChild(rb531.makeAttr("algtype",
                    cont.getPacMethod(), od, rb531.H5_STRING));
        }
        dataset_what.appendChild(rb531.makeAttr("quantity", cont.getQuantity(),
                od, rb531.H5_STRING));
        dataset_what.appendChild(rb531.makeAttr("startdate", cont.getDate(),
                od, rb531.H5_STRING));
        dataset_what.appendChild(rb531.makeAttr("starttime", cont.getTime(),
                od, rb531.H5_STRING));
        dataset_what.appendChild(rb531.makeAttr("gain", cont.getGain(), od,
                rb531.H5_DOUBLE));

        dataset_what.appendChild(rb531.makeAttr("offset", cont.getOffset(), od,
                rb531.H5_DOUBLE));

        dataset_what.appendChild(rb531.makeAttr("nodata", String
                .valueOf(rb531.RAINBOW_NO_DATA), od, rb531.H5_DOUBLE));

        dataset_what.appendChild(rb531.makeAttr("undetect", String
                .valueOf(rb531.RAINBOW_UNDETECT), od, rb531.H5_DOUBLE));

        dataset1.appendChild(dataset_what);

        // ====================== data group ================================
        Element data = od.createElement(rb531.H5_GROUP);
        data.setAttribute(rb531.H5_OBJECT_NAME, rb531.H5_DATA_N);

        // dataset
        Element dataset = od.createElement(rb531.H5_DATASET);
        dataset.setAttribute(rb531.H5_OBJECT_NAME, rb531.H5_DATA);
        dataset.setAttribute("data_type", rb531.H5_INTEGER);
        dataset.setAttribute("data_size", cont.getDataDepth());
        dataset
                .setAttribute("chunk", rb531.H5_DATA_CHUNK + "x"
                        + rb531.H5_DATA_CHUNK);
        dataset.setAttribute("dimensions", cont.getXsize() + "x"
                + cont.getYsize());
        dataset.setAttribute("gzip_level", rb531.H5_GZIP_LEVEL);
        Text text = od.createTextNode(cont.getDataFileName());
        dataset.appendChild(text);
        data.appendChild(dataset);
        dataset1.appendChild(data);

        root.appendChild(dataset1);

        return od;
    }

}
