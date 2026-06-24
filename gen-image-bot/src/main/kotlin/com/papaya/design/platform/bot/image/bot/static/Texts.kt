package com.papaya.design.platform.bot.image.bot.static

import com.papaya.design.platform.bot.image.bot.message.KeyboardInputButton
import com.papaya.design.platform.bot.tg.core.command.input.CUSTOM_MODE

object RoomUpgrade {
    object Text {
        val START_GENERATION = """
    Хочешь улучшить интерьер для себя или аренды/продажи? 
    
    📷 Загрузи фото комнаты, которую тебе хочется улучшить, она может быть как пустой, так и заполненной мебелью 
    🎨 Обученный на основе последних трендов в дизайне ИИ 🤖 создаст привлекательный интерьер, который можно будет реализовать с минимальными затратами 
    
        """.trimIndent() //надо чисто заменить на фотки с примерами и подписями
        val WAITING_FOR_UPGRADE_OPTION = """
        Выбери одну из опций для улучшения интерьера:
        """.trimIndent()
        const val WAITING_FOR_IMAGE =
            "🖼 Пришли изображение для генерации интерьера"
    }

    object Prompt {
        val SYSTEM_PROMPT = """
            You are an experienced interior designer and 3D visualizer with many years of experience creating modern residential spaces.
            You know all the latest design trends and understand how to transform an interior with minimal investment so that it becomes cozy and visually appealing.
            Your task is to bring coziness, warmth, and a great atmosphere into the interior.
            You will receive a photo of a room, which may be either empty or already furnished. Based on this photo, while preserving all dimensions, measurements, lighting, ergonomics, and textures, and keeping the existing furniture items in the final result, create a modern and cozy interior.
            Keep in mind that you need to use trends with minimal costs. This mainly means changing the decor and textiles, and adding plants.
            If the room is intended for personal use, it should become as pleasant and cozy as possible.
            If the room is intended for rent or resale, it should have a marketable look that catches the eye and sells itself.
            The size, orientation, and proportions of the output image must match the size, orientation, and proportions of the input image.
        """.trimIndent()
        const val FOR_RENT = "This apartment is intended for rent. Take this into account during generation."
        const val FOR_SELF = "This apartment is intended for the owner. Take this into account during generation."
    }
}


object RealisticInterior {
    object Text {
        val START_GENERATION =
            """ 
        📷 Загрузи фото коллажа – через минуту RoomIO отдаст реалистичный 3D-рендер, основанный на твоих идеях
        🛋 Он учтет форму мебели, палитру и общую атмосферу изображения, а также добавит недостающие детали, если это потребуется
        
        """.trimIndent()
        const val WAITING_FOR_IMAGE =
            "🖼 Пришли изображение для генерации реалистичного интерьера"
    }

    object Prompt {
        val SYSTEM_PROMPT = """
                You are an experienced 3D visualizer with many years of experience creating modern residential interiors based on collages and moodboards.
                Create a photorealistic 3D image based on the input image.
                Your task is to apply a realism filter to the existing image.
                If the input image contains a floor, ceiling, baseboards, cornices, reveals, doors, sockets/switches and/or walls, you must accurately reproduce their dimensions, colors, and textures, while preserving the proportions between them and the furniture items.
                IMPORTANT! YOU MUST NOT CHANGE THE POSITION OF FURNITURE OBJECTS. YOU MUST NOT CHANGE THEIR SHAPE, COLOR, TEXTURE, OR MATERIAL. YOU MUST NOT ADD NEW OBJECTS.
                ALL OBJECTS IN THE 3D VISUALIZATION MUST BE SHOWN EXACTLY AS THEY ARE IN THE INPUT IMAGE. THE LIGHTING IN THE OUTPUT IMAGE MUST MATCH THE LIGHTING IN THE INPUT IMAGE.
                DO NOT INVENT ANYTHING. REPEAT EXACTLY!!!
                IF THE INPUT IMAGE CONTAINS WALLS, FLOOR, BASEBOARDS, CORNICES, REVEALS, DOORS, SOCKETS/SWITCHES AND/OR CEILING, IN ADDITION TO THE SETTINGS ALREADY SPECIFIED ABOVE — THAT IS, YOU MUST NOT CHANGE THE SHAPE, COLOR, TEXTURE, OR MATERIAL OF ANYTHING IN THE INPUT IMAGE — YOU ARE ALSO REQUIRED TO PRESERVE THE ROOM DIMENSIONS.
                You are forbidden to add ceiling or floor baseboards, as well as moldings, if they are not present in the input image.
                You are forbidden to add doors if there is no door handle.
                The size, orientation, and proportions of the output image must match the size, orientation, and proportions of the input image.
                If the room is bright, it must remain bright.
                Below are examples of details that must not be overlooked, in addition to everything stated above. The photorealistic 3D image must be created with these details taken into account:
                Pay attention to the doors, if they are shown in the input image: whether these doors are invisible/concealed doors or not, whether the doors have trim and what kind, what door handles are used and on which side of the door they are placed. The doors must be shown exactly as in the input image.
                Pay attention to the ceiling, if it is shown in the input image: whether there is a lowered ceiling section, whether there is a ceiling cornice, whether there are ceiling rosettes, whether the curtain rail is built into a ceiling niche, and whether there is a black edge along the ceiling. Stretch ceilings sometimes have a black ceiling gasket or concealed profile — this must also be shown in the visualizations if it was present in the collages.
                If something is not present in the input image, it must not appear in the result.
                Pay attention to the walls, if they are shown in the input image: whether the walls have protrusions, and whether there is a floor baseboard. If there are no protrusions or baseboards, they must not be shown in the final result.
                Pay attention to the tile, if it is shown in the input image: its layout, color, installation pattern, where the joints are located, whether it is rectangular, square, or another shape.
                Pay attention to appliances and sanitary fixtures, if they are shown in the input image: their color and exact placement.
                Pay attention to details: what kind of handles the furniture has, what color and shape they are, whether they exist at all or whether a GOLA profile is used. Check whether the furniture has milling or chamfered edges.
                """.trimMargin()
    }
}

object ObjectRotation {
    object Text {
        val START_GENERATION =
            """
        📷 Загрузи фото объекта
        ✍ Отдельным сообщением напиши, как нужно повернуть или изменить объект
        
        Будет списано 0.25 генерации
        
        """.trimIndent()
        const val WAITING_FOR_IMAGE = "📷 Пришли фото объекта"
        const val WAITING_FOR_USER_PROMPT = "✍ Напиши инструкцию для поворота/изменения объекта"
    }

    object Prompt {
        val SYSTEM_PROMPT = """
            You are an experienced 3D product visualizer.
            Rotate and modify the object according to the instructions below. Create only the object from the photo, without any background.
            Do not add new objects or change the environment.
            Preserve the shape, materials, and textures unless otherwise specified.
            The size, orientation, and proportions of the output image must match the size, orientation, and proportions of the input image.
        """.trimIndent()
    }
}

object RealisticInteriorBatch {
    const val MAX_BATCH_SIZE = 20

    object Text {
        val START_GENERATION =
            """ 
        📷 Загрузи фото коллажей (до 20 штук) — мы сгенерируем для каждой реалистичный 3D-рендер
        🛋 Отправляй изображения по одному сообщению, а когда будешь готов — нажми "✨ Начать". Мы будем возвращать результат по мере готовности
        
        """.trimIndent()
        const val WAITING_FOR_BATCH_IMAGE =
            "🖼 Пришли до 20 изображений. Когда будешь готов, нажми \"✨ Начать\" — мы будем отправлять результаты по мере готовности"
        fun batchPhotoAdded(current: Int, max: Int): String =
            "✅ Добавили фото $current/$max. Пришли ещё или нажми \"✨ Начать\""
        const val BATCH_NEED_AT_LEAST_ONE = "Сначала пришли хотя бы одно фото для генерации"
        fun batchLimitReached(max: Int): String = "Можно отправить максимум $max фото в одной пачке"
        fun batchGenerationStarted(total: Int): String =
            "🚀 Начинаем обработку $total фото. Отправим изображения по мере готовности"
        fun batchResultCaption(current: Int, total: Int): String = "✅ Готово $current из $total"
        fun batchGenerationError(index: Int): String =
            "⚠️ Не удалось обработать фото $index. Попробуй отправить его ещё раз"
        fun batchGenerationFinished(success: Int, total: Int): String =
            "Завершили обработку: готово $success из $total изображений"
    }
}

object ExtendedRealisticInterior {
    object Text {
        val START_GENERATION = """
     📷 Загрузи фото комнаты, которую тебе хотелось бы обновить, она может быть как пустой, так и заполненной мебелью
     ✍ Отдельным сообщением добавь текстовое описание изменений и деталей
     🖼 Следом отправь до 4 фотографий с предметами которые хочешь добавить или поменять, бот подскажет, как
     ✨ RoomIO исполнит твои желания
""".trimIndent()
        const val WAITING_FOR_USER_PROMPT =
            "✍ Добавь текстовое описание изменений и деталей"
        const val WAITING_FOR_IMAGE =
            "📷 Пришли изображение для генерации интерьера"
        const val WAITING_FOR_ADDITIONAL_IMAGES =
            "🖼 Отправь дополнительные изображения предметов, если необходимо добавить их в результат, - по штуке за сообщение"
        const val ACCEPTED_ADDITIONAL_IMAGES =
            "✅ Фото добавлено, eсли хочешь, пришли еще"
    }

    object Prompt {
        val SYSTEM_PROMPT = """
            You are an experienced designer and 3D visualizer.
            You will receive an input image of a room, which may be empty or may already contain furniture.
            In either case, the next step will be that you receive either only a text description, or a text description together with photos of the items that need to be added to the input image.
            You must apply the requested changes to the input image and create a realistic 3D image based on them.
            The 3D image must accurately reflect the items that are already present in the input image.
            IMPORTANT! You must not deviate from the technical brief, and you must not add interior items that were not included in the instructions or were not present in the input image. Follow exactly the instructions received from the user in the form of text and/or photos. No improvisation!!!
            The items from the item photos must preserve their color, shape, surface finish, texture, and all other characteristics.
            If the input image already contains walls, floor, and ceiling, they must be reflected in the 3D visualization exactly as they appear in the input image.
            If the input image partially or completely lacks walls, floor, or ceiling, you are allowed to add them to the 3D visualization yourself, in the style of the objects and the overall mood of the input room image.
            The size, orientation, and proportions of the output image must match the size, orientation, and proportions of the input image.
        """.trimIndent()
    }
}

object PlannedRealisticInterior {
    object Text {
        val START_GENERATION = """
    🖼 Загрузи мудборд
    🛠 Отдельным сообщением добавь планировку в виде фото с подписанными помещениями 
    🛋 Уточни, какой именно ракурс тебе хочется увидеть, бот подскажет, как
    
    RoomIO исполнит твои желания и сделает интерьер таким, каким его хочешь видеть ты ✨ 
    
""".trimIndent()
        const val WAITING_FOR_IMAGE =
            "🖼 Пришли мудборд"
        const val WAITING_FOR_OPTION =
            "🛋 Выбери один из предложенных ракурсов или напиши свой:"
        const val WAITING_FOR_PLAN =
            "🛠 Пришли план помещения (для большей точности ты можешь подписать назначения комнат и предметы на плане):" //добавить возможность вводить самостоятельно ракурс исходя из плана, то есть чат гпт счиаывает названия на ремпланнере
    }

    object Prompt {
        val SYSTEM_PROMPT = """
            You are an experienced designer and 3D visualizer.
            The first image you receive will be a moodboard. It may contain interior items, and it will definitely show the color palette and the overall feeling: lighting, atmosphere, and style. Carefully study the moodboard and immerse yourself in it.
            The second image you receive will be a floor plan of an apartment or another specific room.
            Based on this second image, first study it carefully, including the furniture arrangement shown on the plan. Then arrange the furniture strictly according to the input floor plan image and create a photorealistic 3D image of the interior.
            You may use the moodboard, but you are also required to add some items of your own, following the latest interior design trends, in order to fill the requested room and make it cozy, atmospheric, and consistent with the moodboard.
            The final 3D image may include some items that are already present on the moodboard. If you use them in the final result, preserve their texture, shape, lighting, and other characteristics.
            Follow the technical brief. In the 3D image, you must arrange the furniture as shown on the floor plan, show windows and doors as shown on the floor plan, and include labeled items, if any, as shown on the floor plan.
            At the same time, you are allowed to use information from the moodboard.
            Your task is to transform the flat 2D floor plan into a realistic 3D image using the input moodboard image.
            For the camera angle, choose this room:
        """.trimIndent()
    }
}

object Payment {
    object Text {
        val SELECT_PAYMENT_OPTION = "Выберите пакет услуг:"
        val SUCCSESFUL_PAYMENT = "Спасибо за покупку!"
        val PAYMENT_PREPARED = "Счет на оплату готов⬆"
    }
}

object General {
    object Text {
        val WELCOME_MESSAGE =
            """
        RoomIO by DiRomanova – интерьер вашей мечты за пару минут!
        Забудьте про долгие рендеры и дорогие визуализации:
            🛋	Хотите реалистичную 3D-визуализацию по вашему коллажу или мудборду?
            🏠	Нужно обновить интерьер по вашему фото или описанию?
            🎨	Или готовы целиком довериться ИИ, который создаст стильный и уютный интерьер для жизни или продажи?
        
        Просто загрузите фото, коллаж или напишите идею – дальше поработает RoomIO ✨ 
        За вдохновением и работами: @RoomIO_DiRomanova
        
        Идея, дизайн, "я дома" 
    """.trimIndent()
        const val IMAGE_STILL_GENERATING =
            "Ваше изображение еще генерируется, пожалуйста, подождите ⏲"
        val IMAGE_GENERATED = """
        Готово ✅ Как тебе? 
        Сохраняй себе, делись в соц.сетях и используй получившийся результат для реализации 😍 
        Подписывайся на нашу группу 👉 @RoomIO_DiRomanova, в ней ты найдешь советы, примеры запросов к боту и результаты работы 💡🔥
    """.trimIndent()
        const val IMAGE_RECEIVED_FOR_GENERATION =
            "Получил изображение ✅ Начинаю генерацию реалистичного интерьера, займет примерно минуту ⚙⏳"
        const val NEXT_STEP =
            "Что делаем дальше? 🏡✨"

        const val GENERATIONS_AMOUNT = "Доступно генераций:"

        const val RULES_TEXTS =
            "Продолжая дальнейшее использование бота, вы соглашаетесь с публичной офертой на его использование в Телеграм и политикой обработки персональных данных ✍"

        const val CUSTOM = CUSTOM_MODE
    }
}

object Support {
    object Text {
        val CONFIRM_SUPPORT_MESSAGE = "Пожалуйста, опишите проблему или предложение по улучшению:"
        val CONFIRMING_SUPPORT_MESSAGE = "Спасибо за обратную связь!"
    }

    object Error {
        val ERROR_EMPTY_MESSAGE = "Сообщение пустое, пожалуйста, сообщите о проблеме или предложите улучшение!"
    }
}

object Error {
    object Text {
        const val MODERATION_ERROR_ON_PROCESSING_VIDEO =
            "Произошла ошибка модерации при обработке видео 😨😢 Пожалуйста, попробуй другое фото или описание."
        const val ERROR_ON_PROCESSING_VIDEO =
            "Произошла ошибка при обработке видео 😨😢 Опиши проблему, нажав кнопку Поддержки, и мы полечим малыша RoomIO 🤒🚑"
        const val ERROR_ON_PROCESSING_IMAGE =
            "Произошла ошибка при обработке изображения 😨😢 Опиши проблему, нажав кнопку Поддержки, и мы полечим малыша RoomIO 🤒🚑"
        val ERROR_HAS_NO_GENERATIONS =
            "Похоже у вас не осталось генераций, пожалуйста перейдите в \"${KeyboardInputButton.PAYMENT.text}\" для пополнения 💳"
    }
}

object Video {
    object Text {
        const val WAITING_IMAGE =
            "🖼 Пришли изображение для генерации видео"

        const val WAITING_USER_PROMPT =
            "Пришли текстовое описание видео. Очень желательно на английском.\n" +
                    "Пример: Slowly turn right and go to the room"
        const val WAITING_USER_SELECTING_MODE =
            "Выбери один из предложенных вариантов видео или напиши свой"

        const val CENTER_AND_GO_INTO = "Камера движется вперед"
        const val ROTATE_LEFT = "Плавный поворот налево"
        const val ROTATE_RIGHT = "Плавный поворот направо"
        const val CUSTOM_MODE = General.Text.CUSTOM
    }

    object Prompt {
        val SYSTEM_PROMPT =
            """ """.trimIndent()

        val ROTATE_LEFT_PROMPT =
            """ Slowly turn left and go to the room, cozy vibe""".trimIndent()

        val ROTATE_RIGHT_PROMPT =
            """ Slowly turn right and go to the room, cozy vibe""".trimIndent()

        val CENTER_AND_GO_INTO =
            """ Camera goes forward, cozy vibe""".trimIndent()

    }
}

object Rules {
    const val RULES_FILE_NAME = "Публичная оферта на использование телеграм-бота.pdf"
    const val POLICY_FILE_NAME = "Политика обработки персональных данных.pdf"
}
