echo "starting rmi registry server at port number: $1"
rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false $1 &
