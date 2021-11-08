# COMP-512 Distributed Travel Reservation System

To start the TCP resource manager(s):

```
cd Server/
./run_flights_server.sh [<port>] # starts flights server
./run_cars_server.sh [<port>] # starts cars server
./run_rooms_server.sh [<port>] # starts rooms server
./run_middleware.sh [<flights_host>][<cars_host>][<rooms_host>][<flights_port>][<cars_port>][<rooms_port>][<middleware_port>] # starts Middleware
```

To star the TCP client(s):

```
cd Client
./run_client.sh [<middleware_host>][<middleware_port>]
```
