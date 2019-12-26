
# balances

![Clojure CI](https://github.com/abimaelmartell/nubank-balances/workflows/Clojure%20CI/badge.svg)

This is my attempt at the balances app for NuBank.

I'm using `http-kit` and `compojure` for the web application and `data.json` for the json parsing. For storage i'm using an atom.

The entry point of the application is `core`, it starts the server and configures all the routing and some middlewares for the server.

The requests are handled in `handlers`, it has all actions for the api calls. It doesn't contain any logic and it just passes data around and prints the JSON result.

For data validation i'm checking directly the data in `validations`, i check for date format, amounts and required keys.

The data storage is an atom as a map, the key represents the account id, and value has the operations for that account. This functionality is in `store`.

The logic resides in `logic`, it has all the functions for the calculation and generating structures.

I have some small functions that i use here and there in `utils`, things like date parsing and some json functions.

##### To improve

I think improvements could be:

- To use libraries for things like validations, API middleware and tests.
- Organize some parts of the code into files, if the code grows it would be more maintainable

This is my first time writing Clojure, so i'm sure some parts of the code could be more idiomatic, but is readable overall.

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

