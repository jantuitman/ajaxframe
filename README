# Why Ajaxframe?

Lift, Wicket, Ruby on Rails, ... are great web frameworks. But all of them have their controllers on the server side, making them essential server side frameworks (even though they all have ajax support).

I want to make a client side framework. This is an experimental repository for now. The code is written in scala + javascript and the database is mongodb.I borrow some stuff from the Lift Web Framework (since this is written in scala). 

Finished:
* extremely easy way to make ajax calls, including client side proxy.
* prove of concept implementation of mongodb serialization.
* handle default url /
* improve error handling of ajax call.
* views
* code cleanup/ separation library code and project code.
* maven archetypes
* views which suppport i18n, and user-agent detection.


Still todo:
* finish the ToDo sample code.
* looser json deserialisation, supporting untyped collections.
* generic db code?
* documentation
 
Future goals:
* something for nice urls.
* that's about it.... this is not a big framework. Just a minimal one to get a nice ajax app up and running and do some really really rapid development.


## INSTALLING / RUNNING


### MAC + LINUX


0. Make sure maven 2 and JDK >= 1.5 and MongoDB installed.
1. clone the git repo or download it from github
2. in the project dir : ./refresh_archetype.sh
3. --------------- now the fun part begins (you can repeat this as often if you want)----------------- 
4. go to a directory in which you want to create a new ajaxframe project
5. path/to/ajaxframe/create_project.sh 

### WINDOWS

I haven't got nice batch files for windows yet, so there is slightly more typing needed for installing on windows.

0. Make sure maven 2 and JDK >= 1.5 and mongoDB are installed.
1. clone the git repo or download it from github.
2. open a command prompt window in the directory you've just downloaded/cloned.
3. cd library
4. mvn clean install
5. cd ..
6. cd default_webapp
7. mvn clean compile archetype:create-from-project 
8. cd ..
9. rmdir default_webapp-archetype /s /q (usually this dir will not exist and you can skip this step)
10. xcopy default_webapp\target\generated-sources\archetype default_webapp-archetype
11. make a small fix in default_webapp-archetype\src\main\resources\archetype-resources\src\main\webapp\WEB-INF\web.xml: 

 ${package} should be replaced by org.tuitman.ajaxframe

12. cd default_webapp-archetype
13. mvn install
14. --------------- now the fun part begins (you can repeat this as often if you want)----------------- 
15. go to a directory in which you want to create a new ajaxframe project
16. mvn archetype:generate -DarchetypeArtifactId=default_webapp-archetype -DarchetypeGroupId=org.tuitman.ajaxframe 
