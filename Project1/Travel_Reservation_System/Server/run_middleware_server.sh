# 1st define args for use in RMIMiddleware
a=middlewareServer
b=group_04_
c=1070
d=1071

e=localhost
#e=carServer
f=1080

g=localhost
#g=roomServer
h=1085

i=localhost
#i=flightServer
j=1082

# 2nd we scan commandline arguments from user and display them
echo "middleware server name: $a"
echo "middleware server prefix: $b"

echo "middleware registry port num: $c"
echo "middleware registry export port num: $d"

echo "resource A server name: $e"
echo "resource A port num: $f"

echo "resource B server name: $g"
echo "resource B port num: $h"

echo "resource C server name: $i"
echo "resource C port num: $j"

# 3rd start the middleware server
java -Djava.security.policy=server.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware $a $b $c $d $e $f $g $h $i $j