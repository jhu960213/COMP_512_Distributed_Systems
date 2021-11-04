all: java.policy compile-server

java.policy: FORCE
	@echo "Creating server java policy"
	@echo "grant codebase \"file:`pwd`/\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

FORCE: ;

ServerInterface.jar: Server/Interface/*.java
	@echo "Compiling server interface"
	javac Server/Interface/*.java
	jar cvf ServerInterface.jar Server/Interface/*.class

compile-server:
	javac Server/Interface/*.java Server/ResourceServer/*.java Server/Common/*.java Server/Middleware/*.java Server/LockManager/*.java

clean:
	rm -f Server/Common/*.class Server/Interface/*.class Server/ResourceServer/*.class Server/Middleware/*.class Server/LockManager/*.class
	rm -f ServerInterface.jar
	rm -f java.policy 
