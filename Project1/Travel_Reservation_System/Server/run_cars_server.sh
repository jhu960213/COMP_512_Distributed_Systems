#Usage: ./run_server.sh [<rmi_name>]
echo '  $1 - servername(Optional, default CarsServer)'
echo '  $2 - port(Optional, default 3004)'
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICarsResourceManager $1 $2