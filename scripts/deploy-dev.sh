HOST=image_bot@45.114.60.236

./gradlew build
ssh $HOST 'mkdir -p ~/gen-image-bot/logs ~/gen-image-bot/trace'
scp gen-image-bot/build/libs/gen-image-bot.jar "$HOST:~/gen-image-bot/gen-image-bot.jar"
ssh $HOST 'pkill -f gen-image-bot.jar'
ssh $HOST 'cd gen-image-bot && java -jar -Dspring.profiles.active=prod,single-prompt ./gen-image-bot.jar'