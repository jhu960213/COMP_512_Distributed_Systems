# 1st define args for use in RMIMiddleware
a=middlewareServer
b=group_04_
c=1070
d=1071

e=localhost
f=carServer
g=1040

h=localhost
i=roomServer
j=1085

k=localhost
l=flightServer
m=1044

# 2nd we scan commandline arguments from user and display them
echo "middleware server name: $a"
echo "middleware server prefix: $b"

echo "middleware registry port num: $c"
echo "middleware registry export port num: $d"

echo "resource A server name: $f"
echo "resource A host name: $e"
echo "resource A port num: $g"

echo "resource B server name: $i"
echo "resource B host name: $h"
echo "resource B port num: $j"

echo "resource C server name: $l"
echo "resource C host name: $k"
echo "resource C port num: $m"

# 3rd start the middleware server
java -Djava.security.policy=server.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware $a $b $c $d $f $e $g $i $h $j $l $k $m
