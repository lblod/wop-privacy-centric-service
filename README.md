# [PRIVACY-CENTRIC-SERVICE]

Some resources are extra protected. We should supply a reason when reading or updating them.

A separate service will be used for updating and reading this data. It will only allow to update/read the data when a
reason is given.

The properties will not be returned from mu-cl-resources anymore, but rather by this service.

This has impact on the implementation for the screens which contain sensitive data.

## Example request

### Read (POST):

```
  {
    "operation":"read",
    "type": "people",
    "property":"date-of-birth",
    "data": {
      "id":"aaca86ad-c883-410a-a4e0-070480a2",
      "requester": "nbittich",
      "reason": "i want to read it"
    }
  }
```

### Update (POST):

```
  {
    "operation":"update",
    "type": "people",
    "property":"date-of-birth",
    "data": {
      "id":"aaca86ad-c883-410a-a4e0-070480a2",
      "requester": "nbittich",
      "reason": "i want to change it it"
      "dateOfBirth": "1931-10-10T00:00:00Z"
    },
  
  }
```
