# 1st start rmi for car manager
desiredPortNum=1102
./run_rmi_carManager.sh desiredPortNum > /dev/null

# 2nd scan commandline args from user
echo "car manager server name: $1"
echo "car manager server prefix: $2"
echo "car manager server registry port num: $3"
echo "car manager server registry export port num: $4"

# 3rd start the car manager server
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICarsResourceManager $1 $2 $3 $4