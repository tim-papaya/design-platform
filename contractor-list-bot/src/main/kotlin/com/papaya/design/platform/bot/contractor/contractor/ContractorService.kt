package com.papaya.design.platform.bot.contractor.contractor

import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class ContractorService(
    private val contractorRepository: ContractorRepository
) {
    fun getContractor(name: String): Contractor? =
        contractorRepository.findByNameIgnoreCase(name)?.toModel()

    fun getContractorNamesByCategory(category: String) =
        contractorRepository.findByCategory(category).map { it.toModel() }.map { it.name }

    fun getCategories(): List<String> =
        contractorRepository.getCategories()

    @Transactional
    fun changeContractor(name: String, changeMapper: (ContractorEntity) -> Unit) {
        contractorRepository.findByNameIgnoreCase(name)
            ?.also {
                changeMapper.invoke(it)
                contractorRepository.save(it)
            }
    }
}
