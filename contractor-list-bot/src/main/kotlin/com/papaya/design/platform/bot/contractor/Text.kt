package com.papaya.design.platform.bot.contractor

object General {
    object Text {
        const val START = "Привет! Помочь найти подходящего подрядчика, или ты хочешь поделиться контактами мастера?"
        const val MAIN_MENU_NEXT_STEP = "Что делаем дальше?"
        const val CHOOSE_CATEGORY = "Выберите категорию"
        const val CHOOSE_CONTRACTOR = "Выберите подрядчика"
        const val ADD_CATEGORY = "Выберите категорию или введите новую"
        const val ADD_NAME = "Введите имя"
        const val ADD_PHONE = "Введите телефон:\n\nПример: +79151234567\n\nEсли телефона нет, введите \"Нет\""
        const val ADD_LINK = "Введите ссылку:\n\nПример: @Link\nПример: https://google.com\n\nEсли ссылки нет, введите \"Нет\""
        const val ADD_COMMENT = "Введите комментарий/отзыв"
        const val FINISH_ADDING_CONTRACTOR = "Закончили с подготовкой, нажмите \"Далее\""
        const val ADDED_NEW_CONTRACTOR = "Подрядчик добавлен, проверьте и нажмите \"Далее\""
        const val CHOOSE_FIELD_TO_EDIT = "В разработке"
    }

    object FieldDefault {
        const val NO_FIELD_VALUE = "Нет"
    }

    object Error {
        const val ERROR_EMPTY_FIELD = "Поле не может быть пустым"
        const val ERROR_NAME_NOT_UNIQUE = "Это имя уже есть в базе данных"
        const val ERROR_FIELD_SIZE_TOO_LARGE = "Введенное значение не может быть длиннее 32 символов"
        const val ERROR_EMPTY_MAIN_FIELDS = "Одно из полей телефон или ссылка должно быть заполненным"
        const val ERROR_ON_SAVING_CONTRACTOR = "Ошибка при сохранении подрядчика, пожалуйста обратитесь в поддержку или повторите"
        const val ERROR_ON_CHOOSING_CATEGORY = "Похоже, такой категории не существует, пожалуйста, выберите еще раз"
        const val ERROR_ON_CHOOSING_CONTRACTOR = "Похоже, такого подрядчика не существует, пожалуйста, выберите еще раз"
    }
}