nohup time java -Xmx1512m -Xms1512m -classpath ./classes:./lib/10_VitralLib_Desktop.jar:./lib/commons-logging-1.2.jar:./lib/httpclient-4.4.1.jar:./lib/httpcore-4.4.1.jar:./lib/httpmime-4.4.1.jar:./lib/mongo-java-driver-2.13.2.jar webcrawler.Tool01_ExtractionDownloader &> log.txt &
disown

