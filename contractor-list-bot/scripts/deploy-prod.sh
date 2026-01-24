HOST=contractors_bot@88.218.121.117
BOT_DIR=contractor-list-bot
JAR_NAME=contractor-list-bot.jar

../gradlew build
ssh $HOST "mkdir -p ~/$BOT_DIR/logs"
scp build/libs/$JAR_NAME "$HOST:~/$BOT_DIR/$JAR_NAME"
ssh $HOST "pkill -f $JAR_NAME"
ssh $HOST "cd $BOT_DIR && java -jar -Dspring.profiles.active=prod ./$JAR_NAME"