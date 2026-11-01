# Privacy Policy

_Last updated: 2026-11-01_

**enable-banking-personal** is a personal, non-commercial, open-source tool. It is run by one person, on their own computer, to view their own bank accounts. It is not a hosted service and has no other users.

## Who is responsible for the data

The individual who runs the application on their own computer is the only data controller and the only person whose data is processed. The author of this repository does not receive, collect or have access to anyone's data.

## What data is processed

When the user connects a bank, the application reads, through the regulated PSD2 account-information channel provided by [Enable Banking](https://enablebanking.com):

- account identifiers (account name, IBAN, currency),
- account balances,
- transactions (date, amount, counterparty name, payment description).

The application never sees or stores bank login credentials. The user authenticates directly on their bank's own website.

## Where data goes

- **Stored locally:** the connection details (bank name, session identifier, account list, consent expiry) are saved in a file on the user's own computer (`data/connections.json`). Balances and transactions are fetched on demand and are not stored by the application.
- **Sent to:** only to Enable Banking and the user's own bank, as required to fetch the data. Nothing is sent to analytics, advertising or any other third party.
- **Web interface:** the dashboard listens only on `127.0.0.1` and is not reachable from other computers.

## Access type

Access is **read-only**. The application contains no code to initiate payments or change anything at the bank.

## Retention and deletion

Locally stored connection details stay until the user disconnects a bank in the dashboard or deletes `data/connections.json`. The bank consent expires automatically after the configured period (90 days by default) and can be revoked at any time at the bank or with Enable Banking.

## Your rights

Because all processing happens on the user's own device and under their own control, they can inspect, export or delete everything at any time by reading or removing the local files. For questions about this policy, open an issue in this repository.
