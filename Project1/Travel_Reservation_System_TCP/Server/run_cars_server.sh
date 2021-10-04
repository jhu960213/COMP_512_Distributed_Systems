#Usage: ./run_cars_server.sh serverPort
echo '  $1 - port(Optional, default 3004)'
java -Djava.security.policy=java.policy Server.ResourceServer.CarsServer $1