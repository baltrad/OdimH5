/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow531;

import ncsa.hdf.hdf5lib.HDF5Constants;
import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.model.ParametersContainer;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelImage531H5 {

    /**
     * 
     * Helper method for creating HDF5 file using ParametersContainer
     * 
     * @param cnt
     *            Input data
     * @param rb531
     *            Rainbow 5.31.1 model class instance
     * @param verbose
     *            verbose mode
     */
    public static void createDescriptor(ParametersContainer cnt, Model531 rb531,
            String fileName, int[][] infDataBuff, boolean verbose) {

        DataProcessorModel proc = rb531.getDataProcessorModel();

        // HDF5 file identifier
        int file_id = -1;
        int child_group_id = -1;
        // HDF5 file operation status

        file_id = proc.H5Fcreate_wrap(fileName, HDF5Constants.H5F_ACC_TRUNC,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/what", 0, verbose);

        proc.H5Acreate_any_wrap(child_group_id, "object", rb531.H5_STRING, rb531.IMAGE,
                verbose);
        proc.H5Acreate_any_wrap(child_group_id, "version", rb531.H5_STRING,
                rb531.VERSION, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "date", rb531.H5_STRING, cnt
                .getDate(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "time", rb531.H5_STRING, cnt
                .getTime(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "source", rb531.H5_STRING, cnt
                .getSource(), verbose);

        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/where", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "projdef", rb531.H5_STRING, cnt
                .getProjection(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "xsize", rb531.H5_LONG, cnt
                .getXsize(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "ysize", rb531.H5_LONG, cnt
                .getYsize(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "UL_lon", rb531.H5_DOUBLE, cnt
                .getUlLon(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "UL_lat", rb531.H5_DOUBLE, cnt
                .getUlLat(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "LR_lon", rb531.H5_DOUBLE, cnt
                .getLrLon(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "LR_lat", rb531.H5_DOUBLE, cnt
                .getLrLat(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "x_scale", rb531.H5_DOUBLE, cnt
                .getXscale(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "y_scale", rb531.H5_DOUBLE, cnt
                .getYscale(), verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/how", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "task", rb531.H5_STRING, cnt
                .getTask(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "startepochs", rb531.H5_LONG, cnt
                .getStartepochs(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "endepochs", rb531.H5_LONG, cnt
                .getEndepochs(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "system", rb531.H5_STRING,
                rb531.RAINBOW_SYSTEM, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "software", rb531.H5_STRING,
                rb531.RAINBOW_SOFTWARE, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "sw_version", rb531.H5_STRING, cnt
                .getSwVersion(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "beamwidth", rb531.H5_DOUBLE, cnt
                .getBeamwidth(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "wavelength", rb531.H5_DOUBLE, cnt
                .getWavelength(), verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/dataset1", 0, verbose);
        int grandchild_group_id = -1;
        grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "what", 0,
                verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "product", rb531.H5_STRING,
                cnt.getProduct(), verbose);

        if (cnt.getProduct().matches("MAX") || cnt.getProduct().matches("VIL")) {
            proc.H5Acreate_any_wrap(grandchild_group_id, "prodpar",
                    rb531.H5_SEQUENCE, cnt.getProdpar(), verbose);
        } else {
            proc.H5Acreate_any_wrap(grandchild_group_id, "prodpar",
                    rb531.H5_DOUBLE, cnt.getProdpar(), verbose);

        }
        if (cnt.getPacNumProd() != null && cnt.getPacMethod() != null) {
            proc.H5Acreate_any_wrap(grandchild_group_id, "numprod",
                    rb531.H5_DOUBLE, cnt.getPacNumProd(), verbose);
            proc.H5Acreate_any_wrap(grandchild_group_id, "algtype",
                    rb531.H5_STRING, cnt.getPacMethod(), verbose);
        }

        proc.H5Acreate_any_wrap(grandchild_group_id, "quantity", rb531.H5_STRING,
                cnt.getQuantity(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "startdate", rb531.H5_STRING,
                cnt.getDate(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "starttime", rb531.H5_STRING,
                cnt.getTime(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "gain", rb531.H5_DOUBLE,
                cnt.getGain(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "offset", rb531.H5_DOUBLE,
                cnt.getOffset(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "nodata", rb531.H5_DOUBLE,
                String
                .valueOf(rb531.RAINBOW_NO_DATA), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "undetect", rb531.H5_DOUBLE,
                String
                .valueOf(rb531.RAINBOW_UNDETECT), verbose);
        proc.H5Gclose_wrap(grandchild_group_id, verbose);
        
        int dim_x = Integer.parseInt(cnt.getXsize());
        int dim_y = Integer.parseInt(cnt.getYsize());

        int dataspace_id = proc.H5Screate_simple_wrap(2, dim_x, dim_y,
                null, verbose);

        grandchild_group_id = proc.H5Dcreate_wrap(child_group_id,
                "data", HDF5Constants.H5T_STD_U16BE, dataspace_id, Integer
                        .parseInt(rb531.H5_DATA_CHUNK), Integer
                        .parseInt(rb531.H5_GZIP_LEVEL), verbose);
        
        proc.H5Acreate_any_wrap(grandchild_group_id, "CLASS",
                rb531.H5_STRING, "IMAGE", verbose);
        
        proc.H5Acreate_any_wrap(grandchild_group_id, "IMAGE_VERSION",
                rb531.H5_STRING, rb531.IMAGE_VER, verbose);

        infDataBuff = proc.transposeArray(infDataBuff, dim_x, dim_y);
        
        proc.H5Dwrite_wrap(grandchild_group_id,
                HDF5Constants.H5T_NATIVE_INT, HDF5Constants.H5S_ALL,
                HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                infDataBuff, verbose);
        
        proc.H5Dclose_wrap(grandchild_group_id, verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);
        proc.H5Sclose_wrap(dataspace_id, verbose);
        
        proc.H5Fclose_wrap(file_id, verbose);

    }
}
