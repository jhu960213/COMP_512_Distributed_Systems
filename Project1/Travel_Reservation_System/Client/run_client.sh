# client input args
a=localhost
b=middlewareServer
c=1004
d=group_04_

# run client
java -Djava.security.policy=java.policy -cp ../Server/RMIInterface.jar:. Client.RMIClient $a $b $c $d
