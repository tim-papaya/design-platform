package com.papaya.design.platform.bot.image.bot.static


fun welcomeAndChooseNextStep(userName: String?) = """
    Привет${userName?.let {", $userName"} ?: ""}!
    С чего начнем? Выбери, чтобы узнать подробнее:
    - 3D-визуализация по коллажу или мудборду
    - Обновление интерьера по твоему запросу
    - ИИ-генерация интерьера для жизни или продажи
""".trimIndent()
