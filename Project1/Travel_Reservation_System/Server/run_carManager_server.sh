# run rmi for car server
./run_rmi.sh 3040

# 1st define for use in RMICarsResourceManager
a=carServer # server name
b=group_04_ # server prefix
c=3040 # start registry port num
d=2041 # registry export port num

# 2nd scan commandline args from user
echo "car manager server name: $a"
echo "car manager server prefix: $b"
echo "car manager server registry port num: $c"
echo "car manager server registry export port num: $d"

# 3rd start the car manager server
java -Djava.security.policyrun=server.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMICarsResourceManager $a $b $c $d