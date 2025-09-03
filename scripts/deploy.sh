HOST=dev_papaya@45.114.60.236

./gradlew build

scp gen-image-bot/build/libs/gen-image-bot.jar "$HOST:~/gen-image-bot/gen-image-bot.jar"
ssh $HOST 'pkill -f gen-image-bot.jar'
ssh $HOST 'cd gen-image-bot && java -jar -Dspring.profiles.active=prod ./gen-image-bot.jar '