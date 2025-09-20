package com.papaya.design.platform.bot.contractor.contractor

import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class ContractorService(
    private val contractorRepository: ContractorRepository
) {
    fun getContractor(name: String): Contractor? =
        contractorRepository.findByName(name)?.toModel()

    fun getContractor(userId: Long): Contractor? =
        contractorRepository.findByAddedByUserId(userId).toModel()

    fun getContractorNamesByCategory(category: String) =
        contractorRepository.findByCategory(category).map { it.toModel() }.map { it.name }

    fun getCategories(): List<String> =
        contractorRepository.getCategories()
}
