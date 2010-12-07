/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.model.rainbow;

import ncsa.hdf.hdf5lib.HDF5Constants;
import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.ParametersContainer;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class ModelRHI_H5 {
    /**
     * 
     * Helper method for creating HDF5 file using ParametersContainer
     * 
     * @param cnt
     *            Input data
     * @param rb
     *            Rainbow model class instance
     * @param verbose
     *            verbose mode
     */
    public static void createDescriptor(ParametersContainer cnt, RainbowModel rb,
            String fileName, int[][] infDataBuff, boolean verbose) {

        HDF5Model proc = rb.getHDFModel();

        // HDF5 file identifier
        int file_id = -1;
        int child_group_id = -1;
        // HDF5 file operation status

        file_id = proc.H5Fcreate_wrap(fileName, HDF5Constants.H5F_ACC_TRUNC,
                HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/what", 0, verbose);

        proc.H5Acreate_any_wrap(child_group_id, "object", rb.H5_STRING, rb.RHI,
                verbose);
        proc.H5Acreate_any_wrap(child_group_id, "version", rb.H5_STRING,
                rb.VERSION, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "date", rb.H5_STRING, cnt
                .getDate(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "time", rb.H5_STRING, cnt
                .getTime(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "source", rb.H5_STRING, cnt
                .getSource(), verbose);

        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/where", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "xsize", rb.H5_LONG, cnt
                .getXsize(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "ysize", rb.H5_LONG, cnt
                .getYsize(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "xscale", rb.H5_DOUBLE, cnt
                .getXscale(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "yscale", rb.H5_DOUBLE, cnt
                .getYscale(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "lon", rb.H5_DOUBLE, cnt
                .getLon(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "lat", rb.H5_DOUBLE, cnt
                .getLat(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "az_angle", rb.H5_DOUBLE, rb
                .convertRAINBOWParam(cnt.getAzAngle()), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "angles", rb.H5_SEQUENCE, rb
                .convertRAINBOWParam(cnt.getAngles()), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "range", rb.H5_DOUBLE, cnt
                .getRange(), verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/how", 0, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "startepochs", rb.H5_LONG, cnt
                .getStartepochs(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "endepochs", rb.H5_LONG, cnt
                .getEndepochs(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "system", rb.H5_STRING,
                rb.RAINBOW_SYSTEM, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "software", rb.H5_STRING,
                rb.RAINBOW_SOFTWARE, verbose);
        proc.H5Acreate_any_wrap(child_group_id, "sw_version", rb.H5_STRING, cnt
                .getSwVersion(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "beamwidth", rb.H5_DOUBLE, cnt
                .getBeamwidth(), verbose);
        proc.H5Acreate_any_wrap(child_group_id, "wavelength", rb.H5_DOUBLE, cnt
                .getWavelength(), verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);

        child_group_id = proc.H5Gcreate_wrap(file_id, "/dataset1", 0, verbose);
        int grandchild_group_id = -1;
        grandchild_group_id = proc.H5Gcreate_wrap(child_group_id, "what", 0,
                verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "product", rb.H5_STRING,
                cnt.getProduct(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "quantity", rb.H5_STRING,
                cnt.getQuantity(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "startdate", rb.H5_STRING,
                cnt.getDate(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "starttime", rb.H5_STRING,
                cnt.getTime(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "gain", rb.H5_DOUBLE, cnt
                .getGain(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "offset", rb.H5_DOUBLE,
                cnt.getOffset(), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "nodata", rb.H5_DOUBLE,
                String.valueOf(rb.RAINBOW_NO_DATA), verbose);
        proc.H5Acreate_any_wrap(grandchild_group_id, "undetect", rb.H5_DOUBLE,
                String.valueOf(rb.RAINBOW_UNDETECT), verbose);
        proc.H5Gclose_wrap(grandchild_group_id, verbose);

        int dim_x = Integer.parseInt(cnt.getXsize());
        int dim_y = Integer.parseInt(cnt.getYsize());

        int dataspace_id = proc.H5Screate_simple_wrap(2, dim_x, dim_y, null,
                verbose);

        grandchild_group_id = proc.H5Dcreate_wrap(child_group_id, "data",
                HDF5Constants.H5T_STD_U16BE, dataspace_id, Integer
                        .parseInt(rb.H5_DATA_CHUNK), Integer
                        .parseInt(rb.H5_GZIP_LEVEL), verbose);
        
        proc.H5Acreate_any_wrap(grandchild_group_id, "CLASS",
                rb.H5_STRING, "IMAGE", verbose);
        
        proc.H5Acreate_any_wrap(grandchild_group_id, "IMAGE_VERSION",
                rb.H5_STRING, rb.IMAGE_VER, verbose);
                        

        infDataBuff = proc.transposeArray(infDataBuff, dim_x, dim_y);

        proc.H5Dwrite_wrap(grandchild_group_id, HDF5Constants.H5T_NATIVE_INT,
                HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
                HDF5Constants.H5P_DEFAULT, infDataBuff, verbose);

        proc.H5Dclose_wrap(grandchild_group_id, verbose);
        proc.H5Gclose_wrap(child_group_id, verbose);
        proc.H5Sclose_wrap(dataspace_id, verbose);

        proc.H5Fclose_wrap(file_id, verbose);

    }

}
