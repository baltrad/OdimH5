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
# Converts file from native format to hdf5 using a provided descriptor.                 
#                                                                                       
# 

#!/bin/bash 

usage() {
    echo "Usage: convertDescriptor.sh descriptor file mode" 
    echo -e "\tdescriptor :: Input descriptor file name"
    echo -e "\tfile :: Output data file name (hdf5 format)"
    echo -e "\tmode :: Use v option for verbose mode" 
}
convert_descriptor() {
    lib_dir=${PWD//bin/lib}
    share_dir=${PWD//bin/share}
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$lib_dir
    java -jar $share_dir/odimH5.jar -i $1 -o $2 $3
}

if [ "$#" -ge "2" ] 
then
    if [ "$3" == "v" ] 
    then
        mode="-v"
    else
        mode=""
    fi
    convert_descriptor $1 $2 $mode
else
    usage
fi

###################################################################################



