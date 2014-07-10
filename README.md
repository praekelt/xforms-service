# Praekelt XForms Service#

----------

## A RESTful XForms Processor Service
### Build
    $ mvn clean package

### Run

*From the command line*

    $ java -jar target/restforms-1.0-SNAPSHOT.jar server example_conf.yml

*From an IDE*

Run the `org.praekelt.service.RestformsService` class with the following arguments: `server example_conf.yml`


## Rest Endpoints

- GET /forms
- GET /form/{id}
- GET /results
- GET /result/{id}
- DELETE /form/{id}

## Additional Rest Endpoints For ODK

- GET /formList
- GET /completed
- GET /delete/{id}
- HEAD /submission
- POST /submission

## Client

A JavaRosa client can be downloaded from
 
[https://play.google.com/store/apps/details?id=org.odk.collect.android](https://play.google.com/store/apps/details?id=org.odk.collect.android "Open Data Kit on Google Play")

## Getting Started

Once installed configure the client to access
 
[http://xforms-iocoza.rhcloud.com/forms/rest](http://xforms-iocoza.rhcloud.com/forms/rest "http://xforms-iocoza.rhcloud.com/forms/rest") 

Click "Get Blank Form" and select a form from the list.

Click "Fill Blank Form" to begin filling in the form.

Submit the form by clicking on "Send Finalized Form"


