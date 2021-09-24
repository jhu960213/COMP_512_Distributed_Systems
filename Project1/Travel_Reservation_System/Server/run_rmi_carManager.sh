# start the rmi registry for car manager server on the desired port number:
# $ ./run_rmi_carManager.sh desiredPortNum
echo "starting rmi registry for car manager server at port number: $1"
rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false $1 &