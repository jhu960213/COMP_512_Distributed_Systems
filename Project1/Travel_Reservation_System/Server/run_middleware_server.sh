# when invoking this bash script make sure to invoke it like this:
# $ ./run_middleware_server.sh middlewareServerName, middlewareServerPrefix, middlewareRegistryPortNum,
#     middlewareRegistryExportPortNum, resourceAServerName, resourceAPortNum, resourceBServerName, resourceBPortNum,
#     resourceCServerName, resourceCPortNum

# 1st start rmi for middleware
desiredPortNum=1099
./run_rmi_middleware.sh desiredPortNum > /dev/null

# 2nd we scan commandline arguments from user and display them
echo "middleware server name: $1"
echo "middleware server prefix: $2"
echo "middleware registry port num: $3"
echo "middleware registry export port num: $4"
echo "resource A server name: $5"
echo "resource A port num: $6"
echo "resource B server name: $7"
echo "resource B port num: $8"
echo "resource C server name: $9"
echo "resource C port num: ${10}"

# 2nd start the middleware server
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware $1 $2 $3 $4 $5 $6 $7 $8 $9 ${10}