# start the rmi registry for room manager server on the desired port number:
# $ ./run_rmi_roomManager.sh desiredPortNum
echo "starting rmi registry at port number: $1"
rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false $1 &