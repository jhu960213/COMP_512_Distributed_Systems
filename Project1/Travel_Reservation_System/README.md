# comp512-project

To run the RMI resource manager:

```
cd Server/
./run_flights_server.sh [<port>] # starts flights ResourceManager
./run_cars_server.sh [<port>] # starts cars ResourceManager
./run_rooms_server.sh [<port>] # starts rooms ResourceManager
./run_middleware.sh [<flightsRM_host>][<carsRM_host>][<roomsRM_host>][<middleware_rmi_name>][<flights_port>][<cars_port>][<rooms_port>][<middleware_port>] # starts Middleware
```

To run the RMI client:

```
cd Client
./run_client.sh [<middleware_host>][<middleware_rmi_name>][<middleware_port>]
```
