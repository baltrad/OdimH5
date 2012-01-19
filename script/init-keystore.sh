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
# Initializes keyczar keystore, creates and stores keys to be used for message 
# authentication.                 
#                                                                                       
# Parameters:                                                                            
#
# $1 - keystore directory
# $2 - host name
#

#!/bin/bash 

keytool_dir="tools"
keytool_bin="KeyczarTool.jar"

usage() {
    echo 'Usage: init-keystore.sh keystore_dir host_name'
    echo -e "\tkeystore_dir :: Path to your keystore"
    echo -e "\thost_name :: Host name identifier"
}
make_dirs() {
    echo "Creating keystore directories"
    if [ ! -d $1 ]
    then 
        mkdir $1
    fi
    if [ ! -d "$1/$2.priv" ]
    then 
        mkdir "$1/$2.priv"
    fi
    if [ ! -d "$1/$2.pub" ]
    then 
        mkdir "$1/$2.pub"
    fi
}
create_private_key() {
    echo "Creating private key in $1/$2.priv"
    java -jar $keytool_dir/$keytool_bin create --location="$1/$2.priv" \
            --purpose=sign --name=$2 --asymmetric=dsa
}
add_key() {
    echo "Adding private key to $1/$2.priv"
    java -jar $keytool_dir/$keytool_bin addkey --location="$1/$2.priv" \
            --status=primary     
}
pub_key() {
    echo "Exporting public key to $1/$2.pub"
    java -jar $keytool_dir/$keytool_bin pubkey --location="$1/$2.priv" \
            --destination="$1/$2.pub"
} 

if [ "$#" = "2" ] 
then
    make_dirs $1 $2
    create_private_key $1 $2
    add_key $1 $2
    pub_key $1 $2
else
    usage
fi

###################################################################################



