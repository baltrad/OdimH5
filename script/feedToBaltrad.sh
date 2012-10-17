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
# Sends given file to BALTRAD node.                 
#                                                                                       
# 

#!/bin/bash 

usage() {
    echo "Usage: convertNative.sh input_file node_address mode"
    echo -e "\tinput_file :: Input file name (hdf5 format)" 
    echo -e "\tnode_address :: BALTRAD node address, e.g. http://127.0.0.1:8084"
    echo -e "\tmode :: Use v option for verbose mode" 
}
feed_to_baltrad() {
    base_dir=$(cd `dirname $0` && pwd)
    lib_dir=${base_dir//bin/lib}
    share_dir=${base_dir//bin/share}
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$lib_dir
    java -Xms64m -Xmx256m -jar $share_dir/odimH5.jar -i $1 -a $2 $3
}

if [ "$#" -ge "2" ] 
then
    if [ "$3" == "v" ] 
    then
        mode="-v"
    else
        mode=""
    fi
    feed_to_baltrad $1 $2 $mode
else
    usage
fi

###################################################################################



