all:
	./mvnw clean
	rm -rf build/*
	./mvnw -Pnative -DskipTests native:compile
	cp target/videodownloader build/videodownloader

test:
	./mvnw clean
	rm -rf build/*
	./mvnw -Pnative -X native:compile
	cp target/videodownloader build/videodownloader