HOST='image_bot@45.114.60.236'
PORT='51712'

../gradlew build
ssh -p $PORT $HOST 'mkdir -p ~/gen-image-bot/logs ~/gen-image-bot/trace'
scp -P $PORT build/libs/gen-image-bot.jar "$HOST:~/gen-image-bot/gen-image-bot.jar"
ssh -p $PORT $HOST 'pkill -f gen-image-bot.jar'
ssh -p $PORT $HOST 'cd gen-image-bot && java -jar -Dspring.profiles.active=prod,open-ai-image,runware-video  ./gen-image-bot.jar'