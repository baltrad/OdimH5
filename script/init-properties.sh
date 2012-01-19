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
# Generates OdimH5 properties file.                 
#                                                                                       
# $1 - Properties file name
# $2 - Local host name
# $3 - Local host address
# $4 - Keystore directory
# $5 - Connection timeout in milliseconds"
# $6 - Socket timeout in milliseconds"
#

#!/bin/bash 

usage() {
    echo "Usage: init-properties.sh properties_name app_version host_name \ 
            host_address keystore_dir conf_dir log_dir conn_timeout so_timeout"
    echo -e "\tproperties_name :: Name of properties file to generate"
    echo -a "\tapp_version :: Software version"
    echo -e "\thost_name :: Local host name"
    echo -e "\thost_address :: Local host address"
    echo -e "\tkeystore_dir :: Keystore directory"
    echo -e "\tconf_dir :: Configuration files directory"
    echo -e "\tlog_dir :: Log files directory"
    echo -e "\tconn_timeout :: Connection timeout in milliseconds"
    echo -e "\tso_timeout :: Socket timeout in milliseconds"
}
gen_props() {
    echo "Generating properties file"
    echo "app.version=$2" > $1
    echo "host.name=$3" >> $1
    echo "host.address=$4" >> $1
    echo "keystore.dir=$5" >> $1
    echo "conf.dir=$6" >> $1
    echo "log.dir=$7" >> $1
    echo "conn.timeout=$8" >> $1
    echo "so.timeout=$9" >> $1
}

if [ "$#" = "9" ] 
then
    gen_props $1 $2 $3 $4 $5 $6 $7 $8 $9
else
    usage
fi

###################################################################################



