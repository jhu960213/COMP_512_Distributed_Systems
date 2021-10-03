# Usage: ./run_client.sh serverHostName serverPort
java -Djava.security.policy=java.policy -cp ../json-20210307.jar:. Client.Client $1 $2
