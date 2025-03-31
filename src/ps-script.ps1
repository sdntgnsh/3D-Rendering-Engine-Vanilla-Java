javac -source 1.8 -target 1.8 *.java

jar cvfm application.jar manifest.txt *.class

cd ..
npx http-server -p 8081 -o