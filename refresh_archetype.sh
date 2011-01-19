#!/bin/sh
cd library
mvn clean install
cd ..
cd default_webapp/
mvn clean compile archetype:create-from-project 
cd ..
rm -rf default_webapp-archetype
mv default_webapp/target/generated-sources/archetype default_webapp-archetype
echo 'fixing web.xml with a perl one liner...'
perl -p -i -e 's/\$\{package\}/org.tuitman.ajaxframe/g' `find ./default_webapp-archetype/src/main/resources/archetype-resources/src/main/webapp/WEB-INF -name web.xml`
echo '********************************************************************'
echo '          INSTALLING archetype                                      '
echo '********************************************************************'
cd default_webapp-archetype
mvn install
echo '********************************************************************'
echo '          finished                                                  '
echo '********************************************************************'
