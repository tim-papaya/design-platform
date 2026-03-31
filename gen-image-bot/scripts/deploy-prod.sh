HOST=image_bot@11.11.11.1111
PORT='1111'

read -p "Вы точно хотите продолжить, DEPLOYING ON PROD? (y/n): " confirm

# Проверка ответа
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Операция отменена пользователем."
    exit 1
fi

../gradlew build
ssh -p $PORT $HOST 'mkdir -p ~/gen-image-bot/logs ~/gen-image-bot/trace'
scp -P $PORT build/libs/gen-image-bot.jar "$HOST:~/gen-image-bot/gen-image-bot.jar"
ssh -p $PORT $HOST 'pkill -f gen-image-bot.jar'
ssh -p $PORT $HOST 'cd gen-image-bot && java -jar -Dspring.profiles.active=prod,open-ai-image,runware-video ./gen-image-bot.jar'