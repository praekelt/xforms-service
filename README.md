# Praekelt XForms Service

[![Build Status](https://travis-ci.org/praekelt/xforms-service.svg?branch=master)](https://travis-ci.org/praekelt/xforms-service)
[![Coverage Status](https://coveralls.io/repos/praekelt/xforms-service/badge.png)](https://coveralls.io/r/praekelt/xforms-service)

A RESTful XForms processing service built using [Dropwizard](https://github.com/dropwizard/dropwizard).

##### Configuring

The service's configuration currently resides in `example_conf.yml`. To replace it with your own, simply specify the file path as a runtime argument (see below)

##### Building

    git clone https://github.com/praekelt/xforms-service.git
    
    cd xforms-service
    
    mvn install:install-file -Dfile=lib/javarosa.jar -DgroupId=org.javarosa -DartifactId=javarosa-libraries -Dversion=latest -Dpackaging=jar -DgeneratePom=true
    
    mvn install:install-file -Dfile=lib/regexp-me.jar -DgroupId=me.regexp -DartifactId=regexp -Dversion=latest -Dpackaging=jar -DgeneratePom=true
    
    mvn clean package

This'll grab the project sources, install any bundled dependencies, and then build the entire package and run any contained tests.

##### Running

_From NetBeans_:

`nbactions.xml` contains XML directives for the application's entry point and default runtime arguments.

_From the terminal_:

From the project's root directory
	
    java -jar target/restforms-1.0-SNAPSHOT.jar server example_conf.yml

##### Endpoints

localhost:8080

- `GET /forms/:id`: returns a stored XForm.
- `POST /forms`: saves a given XForm and returns its UUID.
- `GET /responses/:id`: returns the next unanswered question for the XForm associated with the given UUID.
- `POST /responses/:id`: saves the given answer and returns the next unanswered question associated with the given UUID.
- `GET /answers/:id`: returns the model/instance data from the xForm associated with the given UUID.

localhost:8081

- `GET /healthcheck`: dumps health-check data of registered application resources and injected dependencies.
- `GET /metrics?pretty=true`: dumps health-check and thread data for debugging.
- `GET /ping`: pongs the client.
- `GET /threads`: dumps thread data for debugging.
- `POST /tasks/gc`: invokes the application's garbage collector.