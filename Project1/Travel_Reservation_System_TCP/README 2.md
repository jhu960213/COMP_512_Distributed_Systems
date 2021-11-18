# comp512-project

To run the RMI resource manager:

```
cd Server/
./run_flights_server.sh [<port>] # starts flights server
./run_cars_server.sh [<port>] # starts cars server
./run_rooms_server.sh [<port>] # starts rooms server
./run_middleware.sh [<flights_host>][<cars_host>][<rooms_host>][<flights_port>][<cars_port>][<rooms_port>][<middleware_port>] # starts Middleware
```

To run the RMI client:

```
cd Client
./run_client.sh [<middleware_host>][<middleware_port>]
```
