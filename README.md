
# balances

![Clojure CI](https://github.com/abimaelmartell/nubank-balances/workflows/Clojure%20CI/badge.svg)

This is my attempt at the balances app for NuBank.

## Usage
```
lein run
```
It should start a web server on port 8080, it can be changed using the environment variable `PORT`

```
PORT=3000 lein run
```

## Endpoints

### Create new operation
#### POST /accounts/:account-id/operations

##### Request

For debit operation

```
{
    "type": "debit",
    "amount": 20,
    "date": "29/12/2019"
}
```

For credit operation

```
{
    "type": "credit",
    "amount": 20,
    "date": "29/12/2019",
    "merchant": "La Casa de Tono"
}
```

|Key|Expected|Example|
|---|---|---|
|`type`|`debit` or `credit`|`"credit"`|
|`amount`|Positive number, integer or float|`20.00`|
|`merchant`|String, required for credit operation|`"McDonalds"`|
|`date`|String, format `dd/mm/YYYY`|`"12/12/2019"`|


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

Optional params `starting` and `ending` using date format `dd/mm/YYYY` can be used to filter statement.

__GET /accounts/:account-id/statement?`starting`=10/12/2019&`ending`=15/12/2019__

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
[
    {
        "principal": 100,
        "start": "21/11/2019",
        "end": "23/11/2019"
    },
    {
        "principal": 120,
        "start": "24/11/2019"
    }
]
```

