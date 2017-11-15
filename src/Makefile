all:	climatetool.jar

cleaninstall:	distclean install

install:	climatetool.jar
	install --mode 0644 -D climatetool.jar ../bin/
	install --mode 0644 -D py/filebrowser.py ../bin/

updateinstall:	distclean update install

cleanall:	distclean
	rm -fr ../bin/*

distclean:	clean
	rm -f ../bin/climatedataltool.jar
clean:
	rm -f ./climatetool.jar
	rm -f ../bin/climatetool.jar
	rm -rf ../bin/edu/
	rm -rf ../bin/org/
runjar:		install
	java -jar ../bin/climatetool.jar

SOURCE_FILES = \
	edu/purdue/rcac/climatedata/*.java \
	edu/purdue/rcac/climatedata/handler/*.java \
	edu/purdue/rcac/climatedata/worker/*.java \
	org/json/*.java
CLASS_FILES = \
	../bin/edu/purdue/rcac/climatedata/*.class	\
	../bin/org/json/*.class

JFLAGS = -d ../bin/ -verbose -Xlint:unchecked
compile:
	javac $(JFLAGS) $(SOURCE_FILES)

climatetool.jar:	compile
	jar cfm climatetool.jar MANIFEST.MF -C ../bin . -C ../ doc -C ../ examples
