package com.papaya.design.platform.bot.image.bot.photo

import com.papaya.design.platform.bot.image.bot.domain.PhotoEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PhotoRepository : CrudRepository<PhotoEntity, Long>