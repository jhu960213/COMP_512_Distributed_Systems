#Usage: ./run_cars_server.sh serverPort
echo '  $1 - port(Optional, default 3004)'
java -Djava.security.policy=java.policy -cp ../json-20210307.jar:. Server.ResourceServer.CarsServer $1