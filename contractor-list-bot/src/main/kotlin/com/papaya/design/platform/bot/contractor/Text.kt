package com.papaya.design.platform.bot.contractor

object General {
    object Text {
        const val START = "Привет! Я помогу тебе найти подходящего подрядчика, или ты хочешь поделиться контактами мастера на все руки?"
        const val NEXT_STEP = "Что делаем дальше?"
        const val CHOOSE_CATEGORY = "Выберите категорию"
        const val CHOOSE_CONTRACTOR = "Выберите подрядчика"
        const val ADD_CATEGORY = "Выберите категорию или введите новую"
        const val ADD_NAME = "Введите имя"
        const val ADD_PHONE = "Введите телефон"
        const val ADD_LINK = "Введите ссылку"
        const val ADD_COMMENT = "Введите комментарий"
        const val FINISH_ADDING_CONTRACTOR = "Закончили с подготовкой, нажмите далее"
        const val ADDED_NEW_CONTRACTOR = "Подрядчик добавлен"
        const val CHOOSE_FIELD_TO_EDIT = "В разработке"
    }

    object Error {
        const val ERROR_EMPTY_FIELD = "Поле не может быть пустым"
        const val ERROR_EMPTY_MAIN_FIELDS = "Одно из полей телефон или ссылка должно быть заполненным"
        const val ERROR_ON_SAVING_CONTRACTOR = "Ошибка при сохранении подрядчика, пожалуйста обратитесь в поддержку и повторите"
        const val ERROR_ON_CHOOSING_CATEGORY = "Похоже такой категории не существует, пожалуйста, выберите еще раз."
        const val ERROR_ON_CHOOSING_CONTRACTOR = "Похоже такого подрядчика не существует, пожалуйста, выберите еще раз."
    }
}