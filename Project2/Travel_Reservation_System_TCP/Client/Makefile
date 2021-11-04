all: java.policy compile-client

java.policy: FORCE
	@echo "Creating client java policy"
	@echo "grant codebase \"file:`pwd`/\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

FORCE: ;

../Server/ServerInterface.jar: ../Server/Server/Interface/*.java
	make -C ../Server/ ServerInterface.jar

compile-client: ../Server/ServerInterface.jar
	javac -cp ../Server/ServerInterface.jar Client/*.java

clean:
	rm -f Client/*.class
	rm -f ../Server/ServerInterface.jar
	rm -f java.policy
