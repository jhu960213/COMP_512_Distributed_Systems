# 1st define args for use in RMIFlightsResourceManager
a=flightServer # server name
b=group_04_ # server prefix
c=1082 # start registry port num
d=1083 # registry export port num

# 2nd scan commandline args from user
echo "flight manager server name: $a"
echo "flight manager server prefix: $b"
echo "flight manager server registry port num: $c"
echo "flight manager server registry export port num: $d"

# 3rd start the car manager server
java -Djava.security.policy=server.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIFlightsResourceManager $a $b $c $d