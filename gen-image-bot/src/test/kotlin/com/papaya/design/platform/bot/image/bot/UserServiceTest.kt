package com.papaya.design.platform.bot.image.bot

import com.papaya.design.platform.ai.photo.Photo
import com.papaya.design.platform.bot.image.bot.domain.UserState
import com.papaya.design.platform.bot.image.bot.domain.toEntity
import com.papaya.design.platform.bot.image.bot.photo.PhotoRepository
import com.papaya.design.platform.bot.image.bot.user.UserRepository
import com.papaya.design.platform.bot.image.bot.user.UserService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

private const val FIRST_ID = 112L

private const val SECOND_ID = 223L

private const val FILE_1 = "11"
private const val FILE_UNIQUE_1 = "u11"
private const val FILE_2 = "22"
private const val FILE_UNIQUE_2 = "u22"
private const val FILE_3 = "33"
private const val FILE_UNIQUE_3 = "u33"

@SpringBootTest
@ActiveProfiles("test", "mock")
class UserServiceTest(
    @Autowired
    private val userService: UserService,
    @Autowired
    private val userRepository: UserRepository,
    @Autowired
    private val photoRepository: PhotoRepository
) {

    @BeforeEach
    fun before() {
    }

    @AfterEach
    fun after() {
        userRepository.deleteAll()
    }

    @Test
    fun `should add new user with changed id`() {
        userService.saveUser(FIRST_ID)
        userService.saveUser(SECOND_ID)

        val user = userService.getUser(SECOND_ID)

        assert(user.userId == SECOND_ID)
    }

    @Test
    fun `should save user with changed state`() {
        userService.saveUser(FIRST_ID) { u ->
            u.userState = UserState.ROOM_UPGRADE_WAITING_FOR_USER_OPTION
            u.photos = listOf(Photo(FILE_1, FILE_UNIQUE_1)).map { it.toEntity() }
        }

        val user = userService.getUser(FIRST_ID)

        assert(user.userState == UserState.ROOM_UPGRADE_WAITING_FOR_USER_OPTION)
        assert(user.photos.contains(Photo(FILE_1, FILE_UNIQUE_1)))
    }

    @Test
    fun `should delete photos after photos changed`() {
        userService.saveUser(FIRST_ID) { u ->
            u.userState = UserState.ROOM_UPGRADE_WAITING_FOR_USER_OPTION
            u.photos = listOf(Photo(FILE_1, FILE_UNIQUE_1)).map { it.toEntity() }
        }

        userService.saveUser(FIRST_ID) { u ->
            u.userState = UserState.ROOM_UPGRADE_WAITING_FOR_USER_OPTION
            u.photos = listOf(
                Photo(FILE_2, FILE_UNIQUE_2),
                Photo(FILE_3, FILE_UNIQUE_3),
            )
                .map { it.toEntity() }
        }

        assert(photoRepository.findAll().toList().size == 2)
    }
}

