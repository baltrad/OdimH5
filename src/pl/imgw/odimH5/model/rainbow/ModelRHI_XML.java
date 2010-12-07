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
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelRHI_XML {

    /**
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
    public static Document createDescriptor(ParametersContainer cont, RainbowModel rb,
            boolean verbose) {

        // Create XML document object
        Document od = rb.hdf.createXMLDocumentObject(verbose);

        Comment comment = od
                .createComment("ODIM_H5 descriptor file, platform: RAINBOW,"
                        + " file object: " + rb.RHI);

        od.appendChild(comment);
        Element root = od.createElement(rb.H5_GROUP);
        root.setAttribute(rb.H5_OBJECT_NAME, rb.H5_ROOT);
        od.appendChild(root);

        // what group
        Element what = od.createElement(rb.H5_GROUP);
        what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
        what.appendChild(rb.makeAttr("object", "RHI", od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("version", "H5rad 2.0", od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("date", cont.getDate(), od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("time", cont.getTime(), od, rb.H5_STRING));
        what.appendChild(rb.makeAttr("source", cont.getSource(), od,
                rb.H5_STRING));

        root.appendChild(what);

        // where group
        Element where = od.createElement(rb.H5_GROUP);
        where.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHERE);
        // ============= where group Element ======================

        where
                .appendChild(rb.makeAttr("xsize", cont.getXsize(), od,
                        rb.H5_LONG)); // x-horizontal
        where
                .appendChild(rb.makeAttr("ysize", cont.getYsize(), od,
                        rb.H5_LONG)); // y-vertical
        where.appendChild(rb.makeAttr("xscale", cont.getXscale(), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("yscale", cont.getYscale(), od,
                rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("lon", cont.getLon(), od, rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("lat", cont.getLat(), od, rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("az_angle", rb.convertRAINBOWParam(cont
                .getAzAngle()), od, rb.H5_DOUBLE));
        where.appendChild(rb.makeAttr("angles", rb.convertRAINBOWParam(cont
                .getAngles()), od, rb.H5_SEQUENCE));
        where.appendChild(rb.makeAttr("range", cont.getRange(), od,
                rb.H5_DOUBLE));

        root.appendChild(where);

        // ============= how group ============================
        Element how = od.createElement(rb.H5_GROUP);
        how.setAttribute(rb.H5_OBJECT_NAME, rb.H5_HOW);
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

        // dataset1 group
        Element dataset1 = od.createElement(rb.H5_GROUP);
        dataset1.setAttribute(rb.H5_OBJECT_NAME, rb.H5_DATASET_N);
        // dataset1 what group
        Element dataset1_what = od.createElement(rb.H5_GROUP);
        dataset1_what.setAttribute(rb.H5_OBJECT_NAME, rb.H5_WHAT);
        // =============== dataset1 group Element ==================
        dataset1_what.appendChild(rb.makeAttr("product", cont.getProduct(), od,
                rb.H5_STRING));
        dataset1_what.appendChild(rb.makeAttr("quantity", cont.getQuantity(),
                od, rb.H5_STRING));
        dataset1_what.appendChild(rb.makeAttr("startdate", cont.getDate(), od,
                rb.H5_STRING));
        dataset1_what.appendChild(rb.makeAttr("starttime", cont.getTime(), od,
                rb.H5_STRING));

        dataset1_what.appendChild(rb.makeAttr("gain", cont.getGain(), od,
                rb.H5_DOUBLE));

        dataset1_what.appendChild(rb.makeAttr("offset", cont.getOffset(), od,
                rb.H5_DOUBLE));

        dataset1_what.appendChild(rb.makeAttr("nodata", String
                .valueOf(rb.RAINBOW_NO_DATA), od, rb.H5_DOUBLE));

        dataset1_what.appendChild(rb.makeAttr("undetect", String
                .valueOf(rb.RAINBOW_UNDETECT), od, rb.H5_DOUBLE));

        dataset1.appendChild(dataset1_what);

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
