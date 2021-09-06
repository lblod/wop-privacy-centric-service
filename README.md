# [PRIVACY-CENTRIC-SERVICE]
Some resources are extra protected.  We should supply a reason when reading or updating them.

A separate service will be used for updating and reading this data.  It will only allow to update/read the data when a reason is given.

The properties will not be returned from mu-cl-resources anymore, but rather by this service.

This has impact on the implementation for the screens which contain sensitive data.

## Setup using docker-compose
TODO