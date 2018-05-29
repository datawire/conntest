# Connection Test

[![Docker Repository](https://quay.io/repository/datawire/conntest/status "Docker Repository")](https://quay.io/repository/datawire/conntest)

Simple webservice that dumps back request or websocket connection information. Often used as the backend service for 

# Build and Release

There is no CI automation for this project (not worth the effort at this time):

- Build Docker Image: `make docker-build`
- Run Docker Image: `make docker-run`
- Push Docker Image: `make docker-push`


# API

## GET /

Returns HTTP request information

## WebSocket /ws

Returns WebSocket session information

# License

Released under Apache 2.0. Please read [LICENSE](LICENSE) for details.
