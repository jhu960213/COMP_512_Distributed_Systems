
JCC = javac

# the -g flag compiles with debugging information
#
JFLAGS = -g

default: server-compile middle client

server-compile: Server/*.java
	$(JCC) $(JFLAGS) Server/CarServerSocket.java Server/FlightserverSocket.java Server/RoomserverSocket.java

middle: MiddleWare/serverSocket.java
	$(JCC) $(JFLAGS) MiddleWare/serverSocket.java

client: clientSocket.java
	$(JCC) $(JFLAGS) clientSocket.java

# To start over from scratch, type 'make clean'.
# Removes all .class files, so that the next make rebuilds them
#
clean:
	$(RM) *.class
	$(RM) Server/*.class
	$(RM) MiddleWare/*.class

