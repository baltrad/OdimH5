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
# Starts BALTRAD feeder.                 
#                                                                                       
# 

#!/bin/bash 

start_feeder() {
    echo "Use v option for verbose mode"
    base_dir=$(cd `dirname $0` && pwd)
    lib_dir=${base_dir//bin/lib}
    share_dir=${base_dir//bin/share}
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$lib_dir
    java -jar $share_dir/odimH5.jar -c $1 &
}
if [ "$1" == "v" ] 
then
    mode="-v"
else
    mode=""
fi

start_feeder $mode

###################################################################################



