# Usage: ./run_flights_server.sh serverPort
echo '  $1 - port(Optional, default 2004)'
java -Djava.security.policy=java.policy Server.ResourceServer.FlightsServer $1