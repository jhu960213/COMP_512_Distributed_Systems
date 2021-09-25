# run rmi for room server
./run_rmi.sh 4040

# 1st define args for use in RMIRoomsResourceManager
a=roomServer # server name
b=group_04_ # server prefix
c=4040 # start registry port num
d=1086 # registry export port num

# 2nd scan commandline args from user
echo "room manager server name: $a"
echo "room manager server prefix: $b"
echo "room manager server registry port num: $c"
echo "room manager server registry export port num: $d"

# 3rd start the room manager server
java -Djava.security.policy=server.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIRoomsResourceManager $a $b $c $d