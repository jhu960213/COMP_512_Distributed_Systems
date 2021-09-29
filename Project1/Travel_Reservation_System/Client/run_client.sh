# Usage: ./run_client.sh serverHostName serverName serverPort
java -Djava.security.policy=java.policy -cp ../Server/RMIInterface.jar:. Client.RMIClient $1 $2 $3
