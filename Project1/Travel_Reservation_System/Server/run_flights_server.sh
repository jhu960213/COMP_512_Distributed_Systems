#Usage: ./run_server.sh [<rmi_name>]

./run_rmi.sh > /dev/null 2>&1
echo '  $1 - servername(Optional, default FlightsServer)'
echo '  $2 - port(Optional, default 2004)'
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIFlightsResourceManager $1 $2