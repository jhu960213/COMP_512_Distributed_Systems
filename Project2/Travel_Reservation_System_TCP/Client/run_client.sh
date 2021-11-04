# Usage: ./run_client.sh serverHostName serverPort
java -Djava.security.policy=java.policy -cp ../Server/ServerInterface.jar:. Client.Client $1 $2
