#!/bin/sh
#
# $Id: setup-mysql.sh 2011-08-24 viet.nguyen
#
# Copyright (C) 2003-2011 eXo Platform SAS.
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License
# as published by the Free Software Foundation; either version 3
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, see<http://www.gnu.org/licenses/>.#
#
#
# Purpose : Setup tomcat run on an mysql instance
#

PORTAL=csdemo
CLEARDATA=true
DB_LOGIN=root
DB_PASSWORD=gtn
MYSQL_VERSION=5.1.13
MYSQL_DB_URL="jdbc:mysql://localhost"

# directory where Tomcat will be installed
if [ -z "$TOMCAT_BASE" ]; then
    TOMCAT_BASE=`pwd`/..
fi

if [ -z "$GATEIN_DATA_DIR" ]; then
    GATEIN_DATA_DIR=`pwd`/../gatein/data
fi

# Gatein data
if $CLEARDATA; then
  echo "clearing gatein datadir"

  if [ -d $GATEIN_DATA_DIR ]; then
      echo "clearing gatein datadir"
      rm -rf $GATEIN_DATA_DIR
  fi

  #
  # Handle Linux mysql location
  #
  if [ -x /usr/local/mysql/bin/mysqladmin ]; then
      MYSQLADMIN=/usr/local/mysql/bin/mysqladmin
  fi

  if [ -x /usr/bin/mysqladmin ]; then
      MYSQLADMIN=/usr/bin/mysqladmin
  fi

  if $MYSQL; then
      echo "clearing MySQLDB"
      $MYSQLADMIN drop idm_$PORTAL -f --user=$DB_LOGIN --password=$DB_PASSWORD
      $MYSQLADMIN create idm_$PORTAL -f --user=$DB_LOGIN --password=$DB_PASSWORD
      $MYSQLADMIN drop jcr_$PORTAL -f --user=$DB_LOGIN --password=$DB_PASSWORD
      $MYSQLADMIN create jcr_$PORTAL -f --user=$DB_LOGIN --password=$DB_PASSWORD
  fi

fi

# MySQL JDBC driver (if asked)
echo "installing mysql libs..."
rm -f $TOMCAT_BASE/lib/mysql-connector-java*.jar
curl http://repo2.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_VERSION}/mysql-connector-java-${MYSQL_VERSION}.jar -o $TOMCAT_BASE/lib/mysql-connector-java-${MYSQL_VERSION}.jar

# Update SQL Driver is MySQL
sed -i -e "s|org.hsqldb.jdbcDriver|com.mysql.jdbc.Driver|g" $TOMCAT_BASE/gatein/conf/configuration.properties
# Update Datasources
sed -i -e "s|gatein.jcr.datasource.url=jdbc:hsqldb:file:\${gatein.db.data.dir}/data/jdbcjcr_\${name}|gatein.jcr.datasource.url=$MYSQL_DB_URL/jcr_$PORTAL|g" $TOMCAT_BASE/gatein/conf/configuration.properties
sed -i -e "s|gatein.idm.datasource.url=jdbc:hsqldb:file:\${gatein.db.data.dir}/data/jdbcidm_\${name}|gatein.idm.datasource.url=$MYSQL_DB_URL/idm_$PORTAL|g" $TOMCAT_BASE/gatein/conf/configuration.properties
# Update user
sed -i -e "s|gatein.jcr.datasource.username=sa|gatein.jcr.datasource.username=$DB_LOGIN|g" $TOMCAT_BASE/gatein/conf/configuration.properties
sed -i -e "s|gatein.idm.datasource.username=sa|gatein.idm.datasource.username=$DB_LOGIN|g" $TOMCAT_BASE/gatein/conf/configuration.properties
# Update password
sed -i -e "s|gatein.jcr.datasource.password=|gatein.jcr.datasource.password=$DB_PASSWORD|g" $TOMCAT_BASE/gatein/conf/configuration.properties
sed -i -e "s|gatein.idm.datasource.password=|gatein.idm.datasource.password=$DB_PASSWORD|g" $TOMCAT_BASE/gatein/conf/configuration.properties

