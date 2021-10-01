# Usage: ./run_flights_server.sh serverName serverPort
echo '  $1 - servername(Optional, default FlightsServer)'
echo '  $2 - port(Optional, default 2004)'
java -Djava.security.policy=java.policy Server.ResourceServer.FlightsServer $1 $2