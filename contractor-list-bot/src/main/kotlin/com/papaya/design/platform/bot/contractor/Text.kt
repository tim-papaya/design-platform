package com.papaya.design.platform.bot.contractor

import com.papaya.design.platform.bot.contractor.contractor.Contractor
import com.papaya.design.platform.bot.contractor.user.User
import com.papaya.design.platform.bot.tg.core.command.GeneralTelegramCommand

object General {
    object Text {
        const val START = "Привет! Помочь найти подходящего подрядчика, или ты хочешь поделиться контактами мастера?"
        const val MAIN_MENU_NEXT_STEP = "Что делаем дальше?"
        const val CHOOSE_CATEGORY = "Выберите категорию"
        const val CHOOSE_CONTRACTOR = "Выберите подрядчика"
        const val ADD_CATEGORY = "Выберите категорию или введите новую"
        const val ADD_NAME = "Введите имя"
        const val ADD_PHONE = "Введите телефон:\n\nПример: +79151234567\n\nEсли телефона нет, введите \"Нет\""
        const val ADD_LINK =
            "Введите ссылку:\n\nПример: @Link\nПример: https://google.com\n\nEсли ссылки нет, введите \"Нет\""
        const val ADD_COMMENT = "Введите комментарий/отзыв"
        const val FINISH_ADDING_CONTRACTOR = "Закончили с подготовкой, проверьте и нажмите \"Далее\""
        const val CONFIRM_FINISH_ADDING_CONTRACTOR = "Подрядчик добавлен"
        const val EDIT = "Выберите поле для редактирования"
        const val EDIT_BTN = "Редактировать"
        const val EDIT_SUCCESSFUL = "Поле успешно обновлено"
        const val ACCESS_AWAITING = "Запрос отправлен, ожидайте доступа"
        const val ACCESS_GIVEN = "Доступ предоставлен"
        val USER_REQUEST_ACCESS = "Пользователь запрашивает доступ.\n" +
                "Для предоставления используйте команду в сообщении ниже целиком. Пользователь: "

        fun Contractor.toText(user: User) = """
             Новый подрядчик:
             Имя : ${this.name}
             Категория: ${this.category}
             Телефон: ${this.phone}
             Ссылка: ${this.link}
             Комментарий: ${this.comment}
             Добавил(а): ${user.name}
        """.trimIndent()
    }

    object FieldDefault {
        const val NO_FIELD_VALUE = "Нет"
    }

    object Error {
        const val ERROR_GENERAL = "Произошла ошибка, пожалуйста, попробуйте еще раз."
        const val ERROR_EMPTY_FIELD = "Поле не может быть пустым"
        const val ERROR_NAME_NOT_UNIQUE = "Это имя уже есть в базе данных"
        const val ERROR_FIELD_SIZE_TOO_LARGE = "Введенное значение не может быть длиннее 32 символов"
        const val ERROR_EMPTY_MAIN_FIELDS = "Одно из полей телефон или ссылка должно быть заполненным"
        const val ERROR_ON_SAVING_CONTRACTOR =
            "Ошибка при сохранении подрядчика, пожалуйста обратитесь в поддержку или повторите"
        const val ERROR_ON_CHOOSING_CATEGORY = "Похоже, такой категории не существует, пожалуйста, выберите еще раз"
        const val ERROR_ON_CHOOSING_CONTRACTOR = "Похоже, такого подрядчика не существует, пожалуйста, выберите еще раз"
        const val ERROR_NOT_YOUR_CONTRACTOR = "Вы не можете редактировать подрядчика, которого добавил кто-то другой"
        val NOT_AUTHORIZED =
            "Похоже у вас нет доступа, запросите его через команду /${GeneralTelegramCommand.GET_ACCESS.cmdText} Ваше имя"
        const val ERROR_ON_GIVING_ACCESS = "Нет id пользователя после команды, или id не число"
        const val ERROR_MISING_NAME_IN_ACCESS_REQUEST =
            "Нет вашего имени после команды\nПример: /get_access Иван\nПример: /get_access Иван И"
    }
}