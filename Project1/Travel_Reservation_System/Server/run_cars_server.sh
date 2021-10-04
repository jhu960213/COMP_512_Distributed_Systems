#Usage: ./run_cars_server.sh serverName serverPort
echo '  $1 - port(Optional, default 3004)'
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICarsResourceManager $1