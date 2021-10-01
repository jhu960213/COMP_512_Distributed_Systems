# Usage: ./run_client.sh serverHostName serverName serverPort
java -Djava.security.policy=java.policy Client.TCPClient $1 $2 $3
