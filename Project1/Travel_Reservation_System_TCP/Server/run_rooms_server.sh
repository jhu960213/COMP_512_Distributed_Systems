# Usage: ./run_rooms_server.sh serverPort
echo '  $1 - port(Optional, default 4004)'
java -Djava.security.policy=java.policy -cp ../json-20210307.jar:. Server.ResourceServer.RoomsServer $1