all: java.policy compile-client

java.policy: FORCE
	@echo "Creating client java policy"
	@echo "grant codebase \"file:`pwd`/\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

FORCE: ;

compile-client:../json-20210307.jar Client/*.java
	javac -cp ../json-20210307.jar Client/*.java

clean:
	rm -f Client/*.class
	rm -f java.policy
