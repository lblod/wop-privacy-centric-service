# [WOP-PRIVACY-CENTRIC-SERVICE]

Some resources are extra protected. We should supply a reason when reading or updating them.

A separate service will be used for updating and reading this data. It will only allow to update/read the data when a
reason is given.

The properties will not be returned from mu-cl-resources anymore, but rather by this service.

This has impact on the implementation for the screens which contain sensitive data.

```
  privacy:
    image: lblod/wop-privacy-centric-service:latest
    environment:
      SESSION_GRAPH: "http://mu.semte.ch/graphs/sessions"
    links:
      - db:database
```

## Example request

### Read (POST):

http://localhost/person-information-requests

`Accept: application/vnd.api+json`

`Content-Type: application/vnd.api+json`

```
          {
             "data":{
                "type":"person-information-requests",
                "relationships":{
                   "person":{
                      "data":{
                         "type":"people",
                         "id":"fd88cb5a70db029111aa23a086b86f37"
                      }
                   },
                   "reason":{
                      "data":{
                         "type":"request-reasons",
                         "id":"3aeec145-acf3-4b6e-9c00-5b8e285736e0"
                      }
                   }
                }
             }
          }
```
