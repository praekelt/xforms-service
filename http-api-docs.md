XForms-Service HTTP API
-----------------------

_Last updated:_ 9<sup>th</sup> October 2014

This document covers the functions available for manipulating XForm
hash structures in Redis and other miscellaneous administration
endpoints.

### Further Reading

- [Dropwizard documentation](https://dropwizard.github.io/dropwizard/manual/index.html)
- [XForms-Service README](https://github.com/praekelt/xforms-service)

### XForm endpoints:

##### `POST /forms HTTP/1.1`

- Request:
  + URL parameter(s):
    _Not applicable_
  + Header(s):
    `Content-Type: application/xml`
  + Payload:
    Expected to be a valid and well-formed XML document.
- Response:
  + Header(s):
    `Content-Type: application/json`
  + Payload(s):
    * `200` If the given XML document was accepted.
      ```javascript
      {
        "id": "d3711e0a-a4e0-4c3c-8650-149fcffabbfa",
        "status": 201,
        "message": "Created XForm."
      }
      ```
    * `400` If the given XML document was not accepted.
      ```javascript
      {
        "status": 400,
        "message": "An error occurred while attempting to save the provided XForm. Please ensure the XML you provided is well-formed and valid."
      }
      ```

##### `GET /forms/:id HTTP/1.1`

- Request:
  + Header(s):
    _Not applicable_
  + Payload:
    _Not applicable_
- Response:
  + Header(s):
    `Content-Type: application/xml`,
    `Content-Type: application/json`
  + Payload(s):
    * `200` If the given identifier matched an XForm record.
      ```xml
      <h:html xmlns="http://www.w3.org/2002/xforms"
              xmlns:h="http://www.w3.org/1999/xhtml"
              xmlns:ev="http://www.w3.org/2001/xml-events"
              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
              xmlns:jr="http://openrosa.org/javarosa">
              ...
              ...
      </h:html>
      ```
    * `404` If no record was found.
      ```javascript
      {
        "status": 404,
        "message": "No XForm was found associated with the given ID."
      }
      ```

##### `GET /responses/:id/:num HTTP/1.1`

- Request:
  + URL parameter(s):
    _Not applicable_
  + Header(s):
    _Not applicable_
  + Payload:
    _Not applicable_
- Response:
  + Header(s):
    `Content-Type: application/json`
  + Payload(s):
    * `400` If a request was rejected due to user error.
      ```javascript
      {
        "status": 400,
        "message": "The question you requested was out of bounds. Please try again."
      }
      ```
    * `500` If a server error occurred.
      ```javascript
        {
          "status": 500,
          "message": "A XForm processing error occurred. Unable to set up RosaFactory instance post-unserialisation."
        }
      ```
    * `200` If the requested question was found and retrieved.
      ```javascript
        {
          "id": "c131264d-93d2-4a20-b16a-dc74673a8ff5",
          "question": "What is the nature of consciousness?",
          "status": 200,
          "message": "Question retrieved successfully."
        }
      ```

##### `POST /responses/:id HTTP/1.1`

- Request:
  + URL parameter(s):
    _Not applicable_
  + Header(s):
    `Content-Type: application/json`
  + Payload:
    ```javascript
    {
      "answer": "Here is my answer."
    }
    ```
- Response:
  + Header(s):
    `Content-Type: application/json`
  + Payload(s):
    * `200` If the answer was processed without errors.
      ```javascript
      {
        "id": "df7ba60e-865a-4349-8718-09cacf9ab7fd",
        "question": "The subsequent question will be here.",
        "status": 200,
        "message": "Question completed."
      }
      ```
    * `400` If an answer was rejected due to user error.
      ```javascript
      {
        "status": 500,
        "message": "A XForm processing error occurred: Answer data-type was incorrect."
      }
      ```
    * `500` If a server error occurred.
      ```javascript
      {
        "status": 500,
        "message": "A XForm processing error occurred: Data-store is unreachable."
      }
      ```

##### `GET /answers/:id HTTP/1.1`

- Request:
  + URL parameter(s):
    _Not applicable_
  + Header(s):
    _Not applicable_
  + Payload:
    _Not applicable_
- Response:
  + Header(s):
    `Content-Type: application/json`,
    `Content-Type: application/xml`
  + Payload(s):
    * `200` If the model/instance data for the XForm was retrieved without error.
      ```xml
      <?xml version='1.0' ?>
      <person>
          <name>Dave</name>
          <surname>Davidson</surname>
          <gender>Male</gender>
      </person>
      ```
    * `404` Indicates one of many possibilities to the nature of the error.
      ```javascript
      {
        "status": 404,
        "message": "No XForm answer-data was found for the given ID. This could mean that the XForm is not yet complete, the XForm doens't exist or simply server error."
      }
      ```

### Administration endpoints:

##### `GET /healthcheck HTTP/1.1`

- Request:
  + URL parameters:
    _Not applicable_
  + Header(s):
    _Not applicable_
  + Payload:
    _Not applicable_
- Response:
  + Header(s):
    `Cache-Control: must-revalidate,no-cache,no-store`,
    `Content-Type: application/json`
  + Payload(s):
    * `200` If the application was found to be healthy.
      ```javascript
      {
        "JedisClient": {
          "healthy": true,
          "message": "A connection to Redis was established."
        },
        "deadlocks": {
          "healthy": true
        }
      }
      ```
    * `500` If the application was found to be unhealthy.
      ```javascript
      {
        "JedisClient": {
          "healthy": false,
          "message": "Could not get a resource from the pool",
          "error": {
            "message": "Could not get a resource from the pool",
            "stack": [
                "redis.clients.util.Pool.getResource(Pool.java:42)",
                "redis.clients.jedis.JedisPool.getResource(JedisPool.java:84)",
                "This is much longer, of course..."
            ],
            "cause": {
              "message": "ERR Client sent AUTH, but no password is set",
              "stack": [
                  "redis.clients.jedis.Protocol.processError(Protocol.java:113)",
                  "redis.clients.jedis.Protocol.process(Protocol.java:131)",
                  "This is much longer, of course..."
              ]
            }
          }
        },
        "deadlocks": {
          "healthy": true
        }
      }
      ```

##### `GET /metrics HTTP/1.1`

- Request:
  + URL parameters:
    * `pretty=true`
  + Header(s):
    _Not applicable_
  + Payload:
    _Not applicable_
- Response:
  + Header(s):
    `Cache-Control: must-revalidate,no-cache,no-store`,
    `Content-Type: application/json`
  + Payload(s):
    * `200` If the application metrics were retrieved without error.
      ```javascript
      {
        "version": "3.0.0",
        "gauges": {},
        "counters": {},
        "histograms": {},
        "meters": {},
        "timers": {}
      }
      ```

##### `GET /ping HTTP/1.1`

- Request:
  + URL parameters:
    _Not applicable_
  + Header(s):
    _Not applicable_
  + Payload:
    _Not applicable_
- Response:
  + Header(s):
    `Cache-Control: must-revalidate,no-cache,no-store`,
    `Content-Type: text/plain; charset=ISO-8859-1`
  + Payload(s):
    * `200` If the server responded to the ping.
      ```plain
        pong

      ```

##### `GET /threads HTTP/1.1`

- Request:
  + URL parameters:
    _Not applicable_
  + Header(s):
    _Not applicable_
  + Payload:
    _Not applicable_
- Response:
  + Header(s):
    `Cache-Control: must-revalidate,no-cache,no-store`,
    `Content-Type: text/plain`
  + Payload(s):
    * `200` If the requested thread data was successfully dumped.
      ```plain
        Reference Handler id=2 state=WAITING
        - waiting on <0x7aacfe6b> (a java.lang.ref.Reference$Lock)
        - locked <0x7aacfe6b> (a java.lang.ref.Reference$Lock)
        at java.lang.Object.wait(Native Method)
        at java.lang.Object.wait(Object.java:503)
        at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:133)

        Finalizer id=3 state=WAITING
        - waiting on
        <0x1dea32cc> (a java.lang.ref.ReferenceQueue$Lock)
        - locked <0x1dea32cc> (a java.lang.ref.ReferenceQueue$Lock)
        at java.lang.Object.wait(Native Method)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:135)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:151)
        at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:209)

        And plenty more thread data...
      ```

##### `POST /tasks/gc HTTP/1.1`

- Request:
  + URL parameters:
    _Not applicable_
  + Header(s):
    _Not applicable_
  + Payload:
    _Not applicable_
- Response:
  + Header(s):
    `Content-Type: text/plain; charset=UTF-8`
  + Payload(s):
    * `200` If the task was invoked without error.
      ```plain
        Running GC...
        Done!

      ```
