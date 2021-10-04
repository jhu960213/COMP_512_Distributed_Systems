# Usage: ./run_rooms_server.sh serverPort
echo '  $1 - port(Optional, default 4004)'
java -Djava.security.policy=java.policy Server.ResourceServer.RoomsServer $1