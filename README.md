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
    "type": "person-detail-requests",
    "property":"date-of-birth",
    "data": {
      "id":"aaca86ad-c883-410a-a4e0-070480a2",
      "reason": {
            "requester": "nbittich",
            "code": "http://some-code-list.com/ABC"
      }

    }
  }
```

### Update (POST):

```
  {
    "type": "person-detail-updates",
    "property":"date-of-birth",
    "data": {
      "id":"aaca86ad-c883-410a-a4e0-070480a2",
      "dateOfBirth": "1931-10-10T00:00:00Z",
      "reason": {
            "requester": "nbittich",
            "code": "http://some-code-list.com/ABC"
      }

    }
  }
```
