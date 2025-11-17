HOST=image_bot@88.218.121.117

./gradlew build
ssh $HOST 'mkdir -p ~/gen-image-bot/logs ~/gen-image-bot/trace'
scp build/libs/gen-image-bot.jar "$HOST:~/gen-image-bot/gen-image-bot.jar"
ssh $HOST 'pkill -f gen-image-bot.jar'
ssh $HOST 'cd gen-image-bot && java -jar -Dspring.profiles.active=mock ./gen-image-bot.jar'