# 1st start rmi for car manager
desiredPortNum=1100
./run_rmi_flightManager.sh desiredPortNum > /dev/null

# 2nd scan commandline args from user
echo "flight manager server name: $1"
echo "flight manager server prefix: $2"
echo "flight manager server registry port num: $3"
echo "flight manager server registry export port num: $4"

# 3rd start the car manager server
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIFlightsResourceManager $1 $2 $3 $4