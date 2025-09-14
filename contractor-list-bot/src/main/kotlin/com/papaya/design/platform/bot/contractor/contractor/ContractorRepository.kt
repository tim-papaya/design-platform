package com.papaya.design.platform.bot.contractor.contractor

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ContractorRepository : CrudRepository<ContractorEntity, Long> {

    fun findByName(name: String): ContractorEntity

    fun findByAddedByUserId(userId: Long): ContractorEntity

    fun findByCategory(category: String) : List<ContractorEntity>

    @Query("select distinct c.category from ContractorEntity c order by c.category")
    fun getCategories() : List<String>
}
