#Usage: ./run_server.sh [<rmi_name>]

./run_rmi.sh > /dev/null 2>&1
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIRoomsResourceManager $1 $2