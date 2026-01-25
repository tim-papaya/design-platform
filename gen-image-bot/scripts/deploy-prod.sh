HOST=image_bot@88.218.121.117

read -p "Вы точно хотите продолжить, DEPLOYING ON PROD? (y/n): " confirm

# Проверка ответа
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Операция отменена пользователем."
    exit 1
fi

../gradlew build
ssh $HOST 'mkdir -p ~/gen-image-bot/logs ~/gen-image-bot/trace'
scp build/libs/gen-image-bot.jar "$HOST:~/gen-image-bot/gen-image-bot.jar"
ssh $HOST 'pkill -f gen-image-bot.jar'
ssh $HOST 'cd gen-image-bot && java -jar -Dspring.profiles.active=prod,open-ai-image,runware-video ./gen-image-bot.jar'