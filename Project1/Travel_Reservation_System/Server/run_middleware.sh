# Usage: ./run_middleware.sh FlightsServerHost CarsServerHost RoomsServerHost MiddlewareServerName FlightsPort CarsPort RoomsPort MiddlewarePort

echo "Edit file run_middleware.sh to include instructions for launching the middleware"
echo '  $1 - Flights server host name (Optional, default localhost)'
echo '  $2 - Cars server host name (Optional, default localhost)'
echo '  $3 - Rooms server host name (Optional, default localhost)'
echo '  $4 - Middleware servername(Optional, default Middleware)'
echo '  $5 - Flights port (Optional, default 2004)'
echo '  $6 - Cars port (Optional, default 3004)'
echo '  $7 - Rooms port (Optional, default 4004)'
echo '  $8 - Middleware port (Optional, default 5004)'

java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware $1 $2 $3 $4 $5 $6 $7 $8
