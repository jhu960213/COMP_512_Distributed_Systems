all: java.policy compile-server

java.policy: FORCE
	@echo "Creating server java policy"
	@echo "grant codebase \"file:`pwd`/\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

FORCE: ;

compile-server: Server/ResourceServer/*.java Server/Common/*.java Server/Middleware/*.java
	javac Server/ResourceServer/*.java Server/Common/*.java Server/Middleware/*.java

clean:
	rm -f Server/Common/*.class Server/ResourceServer/*.class Server/Middleware/*.class
	rm -f java.policy 
