# Usage: ./run_test.sh serverHostName serverPort
java -Djava.security.policy=java.policy -cp ../Server/ServerInterface.jar:. Client.TestClient $1 $2 $3
