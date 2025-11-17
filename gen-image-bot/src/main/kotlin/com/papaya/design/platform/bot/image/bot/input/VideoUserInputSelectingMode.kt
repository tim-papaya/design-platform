package com.papaya.design.platform.bot.image.bot.input

import com.fasterxml.jackson.core.sym.Name
import com.papaya.design.platform.bot.image.bot.static.Video
import com.papaya.design.platform.bot.tg.core.command.input.UserInputSelectingMode

enum class VideoUserInputSelectingMode(
    override val textShowingToUser: String,
    override val data: String
) : UserInputSelectingMode {
    ROTATE_LEFT(Video.Text.ROTATE_LEFT, Video.Prompt.ROTATE_LEFT_PROMPT),
    ROTATE_RIGHT(Video.Text.ROTATE_RIGHT, Video.Prompt.ROTATE_RIGHT_PROMPT),
    CENTER_AND_GO_INTO(Video.Text.CENTER_AND_GO_INTO, Video.Prompt.CENTER_AND_GO_INTO),
    CUSTOM(Video.Text.CUSTOM_MODE, "")
}
