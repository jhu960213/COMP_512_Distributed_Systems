# 1st start rmi for room manager
desiredPortNum=1101
./run_rmi_roomManager.sh desiredPortNum > /dev/null

# 2nd scan commandline args from user
echo "room manager server name: $1"
echo "room manager server prefix: $2"
echo "room manager server registry port num: $3"
echo "room manager server registry export port num: $4"

# 3rd start the room manager server
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIRoomsResourceManager $1 $2 $3 $4