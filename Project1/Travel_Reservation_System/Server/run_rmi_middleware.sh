# start the rmi registry for middleware server on the desired port number:
# $ ./run_rmi_middleware.sh desiredPortNum
echo "starting rmi registry at port number: $1"
rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false $1 &