/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

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
public class ModelImageXML {

    /**
     * 
     * Helper method for creating XML file using ParametersContainer
     * 
     * @param cont
     *            Input data
     * @param rb
     *            Rainbow model class instance
     * @param verbose
     *            verbose mode
     * @return XML document
     */
    public static Document createDescriptor(ParametersContainer cont, Model rb,
            boolean verbose) {
        // Create XML document object
        Document od = rb.proc.createXMLDocumentObject(verbose);

        Comment comment = od
                .createComment("ODIM_H5 descriptor file, platform: RAINBOW,"
                        + " file object: " + rb.IMAGE);

        od.appendChild(comment);

        Element root = od.createElement(rb.H5_GROUP);
        root.setAttribute(rb.H5_OBJECT_NAME, rb.H5_ROOT);
        od.appendChild(root);

        Element what = od.createElement(rb.H5_GROUP);
        what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);

        what.appendChild(rb.makeAttr("object", rb.IMAGE, od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("version", rb.VERSION, od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("date", cont.getDate(), od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("time", cont.getTime(), od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("source", cont.getSource(), od,
                rb.H5_STRING));
        root.appendChild(what);

        Element where = od.createElement(rb.H5_GROUP);
        where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);

        where.appendChild(rb.makeAttr("projdef", cont.getProjection(), od,
                rb.H5_STRING));
        where
                .appendChild(rb.makeAttr("xsize", cont.getXsize(), od,
                        rb.H5_LONG));
        where
                .appendChild(rb.makeAttr("ysize", cont.getYsize(), od,
                        rb.H5_LONG));
        where.appendChild(rb.makeAttr("UL_lon", cont.getUlLon(), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("UL_lat", cont.getUlLat(), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("LR_lon", cont.getLrLon(), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("LR_lat", cont.getLrLat(), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("x_scale", cont.getXscale(), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("y_scale", cont.getYscale(), od,
                rb.H5_DOUBLE));

        root.appendChild(where);

        Element how = od.createElement(rb.H5_GROUP);
        how.setAttribute(rb.H5_OBJECT_NAME, rb.H5_HOW);

        how.appendChild(rb.makeAttr("task", cont.getTask(), od, rb.H5_STRING));
        how.appendChild(rb.makeAttr("startepochs", cont.getStartepochs(), od,
                rb.H5_LONG));
        how.appendChild(rb.makeAttr("endepochs", cont.getEndepochs(), od,
                rb.H5_LONG));
        how.appendChild(rb.makeAttr("system", rb.RAINBOW_SYSTEM, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr("software", rb.RAINBOW_SOFTWARE, od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr("sw_version", cont.getSwVersion(), od,
                rb.H5_STRING));
        how.appendChild(rb.makeAttr("beamwidth", cont.getBeamwidth(), od,
                rb.H5_DOUBLE));
        how.appendChild(rb.makeAttr("wavelength", cont.getWavelength(), od,
                rb.H5_DOUBLE));
        root.appendChild(how);

        Element dataset1 = od.createElement(rb.H5_GROUP);
        dataset1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATASET_N);

        // dataset specific what group
        Element dataset_what = od.createElement(rb.H5_GROUP);
        dataset_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);

        dataset_what.appendChild(rb.makeAttr("product", cont.getProduct(), od,
                rb.H5_STRING));

        if (cont.getProduct().matches("MAX")
                || cont.getProduct().matches("VIL")) {
            dataset_what.appendChild(rb.makeAttr("prodpar", cont.getProdpar(),
                    od, rb.H5_SEQUENCE));
        } else {
            dataset_what.appendChild(rb.makeAttr("prodpar", cont.getProdpar(),
                    od, rb.H5_DOUBLE));
        }
        if (cont.getPacNumProd() != null && cont.getPacMethod() != null) {
            dataset_what.appendChild(rb.makeAttr("numprod", cont
                    .getPacNumProd(), od, rb.H5_DOUBLE));
            dataset_what.appendChild(rb.makeAttr("algtype",
                    cont.getPacMethod(), od, rb.H5_STRING));
        }
        dataset_what.appendChild(rb.makeAttr("quantity", cont.getQuantity(),
                od, rb.H5_STRING));
        dataset_what.appendChild(rb.makeAttr("startdate", cont.getDate(),
                od, rb.H5_STRING));
        dataset_what.appendChild(rb.makeAttr("starttime", cont.getTime(),
                od, rb.H5_STRING));
        dataset_what.appendChild(rb.makeAttr("gain", cont.getGain(), od,
                rb.H5_DOUBLE));

        dataset_what.appendChild(rb.makeAttr("offset", cont.getOffset(), od,
                rb.H5_DOUBLE));

        dataset_what.appendChild(rb.makeAttr("nodata", String
                .valueOf(rb.RAINBOW_NO_DATA), od, rb.H5_DOUBLE));

        dataset_what.appendChild(rb.makeAttr("undetect", String
                .valueOf(rb.RAINBOW_UNDETECT), od, rb.H5_DOUBLE));

        dataset1.appendChild(dataset_what);

        // ====================== data group ================================
        Element data = od.createElement(rb.H5_GROUP);
        data.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA_N);

        // dataset
        Element dataset = od.createElement(rb.H5_DATASET);
        dataset.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATA);
        dataset.setAttribute("data_type", rb.H5_INTEGER);
        dataset.setAttribute("data_size", cont.getDataDepth());
        dataset
                .setAttribute("chunk", rb.H5_DATA_CHUNK + "x"
                        + rb.H5_DATA_CHUNK);
        dataset.setAttribute("dimensions", cont.getXsize() + "x"
                + cont.getYsize());
        dataset.setAttribute("gzip_level", rb.H5_GZIP_LEVEL);
        Text text = od.createTextNode(cont.getDataFileName());
        dataset.appendChild(text);
        data.appendChild(dataset);
        dataset1.appendChild(data);

        root.appendChild(dataset1);

        return od;
    }

}
