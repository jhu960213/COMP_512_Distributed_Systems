./run_rmi.sh > /dev/null

echo "Edit file run_middleware.sh to include instructions for launching the middleware"
echo '  $1 - hostname of Flights'
echo '  $2 - hostname of Cars'
echo '  $3 - hostname of Rooms'
echo '  $4 - middleware servername'
echo '  $5 - port of Flights'
echo '  $6 - port of Cars'
echo '  $7 - port of Rooms'
echo '  $8 - middleware port'

 java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware $1 $2 $3 $4 $5 $6 $7 $8
