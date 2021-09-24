all: server.policy middleware compile-server-rmi


#codebase \"file:`pwd`/\"

server.policy: FORCE
	@echo "Creating server policy"
	@echo "grant codebase \"file:`pwd`/-\" {" > server.policy
	@echo "permission java.security.AllPermission;" >> server.policy
	@echo "};" >> server.policy

FORCE: ;

RMIInterface.jar: Server/Interface/IResourceManager.java
	@echo "Compiling RMI server interface"
	javac Server/Interface/IResourceManager.java
	jar cvf RMIInterface.jar Server/Interface/IResourceManager.class

middleware: Server/Middleware/Middleware.java
	javac Server/Middleware/*.java

compile-server-rmi: Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java
	javac Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class Server/RMI/*.class Server/Middleware/*.class
	rm -f RMIInterface.jar
	rm -f server.policy
