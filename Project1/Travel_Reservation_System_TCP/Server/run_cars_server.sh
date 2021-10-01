#Usage: ./run_cars_server.sh serverName serverPort
echo '  $1 - servername(Optional, default CarsServer)'
echo '  $2 - port(Optional, default 3004)'
java -Djava.security.policy=java.policy Server.ResourceServer.CarsServer $1 $2