# start the rmi registry for flight manager server on the desired port number:
# $ ./run_rmi_flightManager.sh desiredPortNum
echo "starting rmi registry at port number: $1"
rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false $1 &