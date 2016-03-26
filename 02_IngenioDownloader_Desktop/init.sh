mkdir 82_IngenioDownloader_Desktop
cd 82_IngenioDownloader_Desktop
mkdir lib src classes
cp ~/JavaWebCrawlerRobots/02_IngenioDownloader_Desktop/compile.sh .
cp ~/JavaWebCrawlerRobots/02_IngenioDownloader_Desktop/run.sh .
cd src
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
cd databaseMongo/
ln -s ~/JavaWebCrawlerRobots/02_IngenioDownloader_Desktop/src/databaseMongo/*.java .
cd model/
ln -s ~/JavaWebCrawlerRobots/02_IngenioDownloader_Desktop/src/databaseMongo/model/* .
cd ../../webcrawler/
ln -s ~/JavaWebCrawlerRobots/02_IngenioDownloader_Desktop/src/webcrawler/* .
cd ../..
cd lib/
ln -s ~/JavaWebCrawlerRobots/_commonLibsInJar/* .
cd ..
./compile.sh
