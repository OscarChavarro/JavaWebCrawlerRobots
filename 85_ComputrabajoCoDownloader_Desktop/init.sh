mkdir 85_ComputrabajoCoDownloader_Desktop/
cd 85_ComputrabajoCoDownloader_Desktop/
mkdir lib src classes
cp ~/JavaWebCrawlerRobots/85_ComputrabajoCoDownloader_Desktop/compile.sh .
cp ~/JavaWebCrawlerRobots/85_ComputrabajoCoDownloader_Desktop/run.sh .
cd src/
ln -s ~/JavaWebCrawlerRobots/01_JsonLib_Desktop/src/org .
mkdir databaseConnection  databaseMongo  webcrawler
cd databaseConnection
ln -s  ~/JavaWebCrawlerRobots/01_Utilities/src/databaseConnection/* .
cd ..
cd databaseMongo/
mkdir model
cd model/
ln -s  ~/JavaWebCrawlerRobots/01_Utilities/src/databaseMongo/model/JdbcEntity.java .
cd ../..
cd webcrawler/
ln -s  ~/JavaWebCrawlerRobots/01_Utilities/src/webcrawler/* .
cd ..
ln -s ~/JavaWebCrawlerRobots/10_VitralLib_Desktop/src/vsdk/ .
ls ~/JavaWebCrawlerRobots/85_ComputrabajoCoDownloader_Desktop/src/
cd databaseMongo/
ln -s ~/JavaWebCrawlerRobots/85_ComputrabajoCoDownloader_Desktop/src/databaseMongo/*.java .
cd model/
ln -s ~/JavaWebCrawlerRobots/85_ComputrabajoCoDownloader_Desktop/src/databaseMongo/model/* .
cd ../../webcrawler/
ln -s ~/JavaWebCrawlerRobots/85_ComputrabajoCoDownloader_Desktop/src/webcrawler/* .
cd ../..
cd lib/
ln -s ~/JavaWebCrawlerRobots/_commonLibsInJar/* .
cd ..
./compile.sh
