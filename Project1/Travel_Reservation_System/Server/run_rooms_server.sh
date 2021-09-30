# Usage: ./run_rooms_server.sh serverName serverPort
echo '  $1 - servername(Optional, default RoomsServer)'
echo '  $2 - port(Optional, default 4004)'
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIRoomsResourceManager $1 $2