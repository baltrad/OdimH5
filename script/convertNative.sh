###################################################################################
#                                                                                 #
# Copyright (C) 2009-2012 Institute of Meteorology and Water Management, IMGW     #
#                                                                                 #
# This file is part of the BaltradDex software.                                   #
#                                                                                 #
# BaltradDex is free software: you can redistribute it and/or modify              #
# it under the terms of the GNU Lesser General Public License as published by     #
# the Free Software Foundation, either version 3 of the License, or               #
# (at your option) any later version.                                             #
#                                                                                 #
# BaltradDex is distributed in the hope that it will be useful,                   #
# but WITHOUT ANY WARRANTY; without even the implied warranty of                  #
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   #
# GNU Lesser General Public License for more details.                             #
#                                                                                 #
# You should have received a copy of the GNU Lesser General Public License        #
# along with the BaltradDex software.  If not, see http://www.gnu.org/licenses.   # 
#                                                                                 #
###################################################################################
#
# Converts file directly from native format to hdf5.                 
#                                                                                       
# 

#!/bin/bash 

usage() {
    echo "Usage: convertNative.sh input_file output_file paltform object mode"
    echo -e "\tinput_file :: Input file name, either rawdata or product" 
    echo -e "\toutput_file :: Output file name"
    echo -e "\tplatform :: Type of processing software:"
	echo -e "\t\tCASTOR: Météo France’s system"
   	echo -e "\t\tEDGE: EEC Edge"
    	echo -e "\t\tFROG: Gamic FROG, MURAN..."
    	echo -e "\t\tIRIS: Sigmet IRIS"
    	echo -e "\t\tNORDRAD: NORDRAD"
    	echo -e "\t\tRADARNET: UKMO’s system"
        echo -e "\t\tRAINBOW: Gematronik Rainbow"
    echo -e "\tobject :: ODIMH5 file object type:"
	echo -e "\t\tPVOL: polar volume"
        echo -e "\t\tCVOL: carthesian volume"
        echo -e "\t\tSCAN polar scan"
        echo -e "\t\tRAY: single polar ray"
        echo -e "\t\tAZIM: azimuthal object"
        echo -e "\t\tIMAGE: 2-D cartesian image"
        echo -e "\t\tCOMP: cartesian composite image(s)"
        echo -e "\t\tXSEC: 2-D vertical cross section(s)"
        echo -e "\t\tVP: 1-D vertical profile"
        echo -e "\t\tPIC: embedded graphical image"
    echo -e "\tmode :: Use v option for verbose mode" 
}
convert_native() {
    basedir=`dirname $0`
    lib_dir=${basedir//bin/lib}
    share_dir=${basedir//bin/share}
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$lib_dir
    java -jar $share_dir/odimH5.jar -i $1 -o $2 -p $3 -f $4 $5
}

if [ "$#" -ge "3" ] 
then
    if [ "$5" == "v" ] 
    then
        mode="-v"
    else
        mode=""
    fi
    convert_native $1 $2 $3 $4 $mode
else
    usage
fi

###################################################################################



