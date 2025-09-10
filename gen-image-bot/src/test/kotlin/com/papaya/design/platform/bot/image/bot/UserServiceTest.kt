package com.papaya.design.platform.bot.image.bot

import com.papaya.design.platform.bot.image.bot.user.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

private const val FIRST_ID = 112L

private const val SECOND_ID = 223L

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest(
    @Autowired private val userService: UserService
) {

    @BeforeEach
    fun before() {
    }

    @Test
    fun `should add new user with changed id`() {
        userService.saveUser(FIRST_ID)
        userService.saveUser(SECOND_ID)

        val user = userService.getUser(SECOND_ID)

        assert(user.userId == SECOND_ID)
    }
}
