# Balances App

### Create new operation
#### POST /accounts/:account-id/operations

##### Request

```
{
    "type": "debit",
    "amount": "20.00",
    "date": "29/12/2019"
}
```

### Current Balance
#### GET /accounts/:account-id/balance

##### Response

```
{
    "balance": "20.00"
}
```

### Account Statement
#### GET /accounts/:account-id/statement

##### Response

```
{
    "15/10": {
        "operations": [],
        "balance": "20.00"
    }
}
```


### Periods of Debt
#### GET /accounts/:account-id/periods-of-debt

#### Response

```
{
    
}
```
