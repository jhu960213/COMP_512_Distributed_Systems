./run_rmi.sh > /dev/null

echo "Edit file run_middleware.sh to include instructions for launching the middleware"
echo '  $1 - servername'
echo '  $2 - middleware port'
echo '  $1 - hostname of Flights'
echo '  $2 - port of Flights'
echo '  $3 - hostname of Cars'
echo '  $4 - port of Cars'
echo '  $5 - hostname of Rooms'
echo '  $6 - port of Rooms'

 java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware $1 $2 $3 $4 $5 $6 $7 $8
