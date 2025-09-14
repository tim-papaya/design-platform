package com.papaya.design.platform.bot.contractor.contractor

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

@Service
class ContractorService(
    private val contractorRepository: ContractorRepository
) {
    private val contractorDrafts = ConcurrentHashMap<String, ContractorEntity>()

    fun getContractor(name: String): Contractor? =
        contractorRepository.findByName(name).toModel()

    fun getContractor(userId: Long): Contractor? =
        contractorDrafts.values.find { it.addedByUserId == userId }?.toModel()
            ?: contractorRepository.findByAddedByUserId(userId).toModel()

    fun getContractorNamesByCategory(category: String) =
        contractorRepository.findByCategory(category).map { it.toModel() }.map { it.name }

    fun addContractor(name: String, userId: Long) {
        contractorDrafts[name] = ContractorEntity().apply {
            addedByUserId = userId
        }
    }

    fun saveDraftIfExists(userId: Long): Boolean {
        val draft = contractorDrafts.values.find { it.addedByUserId == userId }

        return if (draft == null) false
        else {
            contractorRepository.save(draft)
            true
        }
    }

    @Transactional
    fun changeContractor(name: String, changeMapper: (ContractorEntity) -> Unit) {
        val draft = contractorDrafts[name]
        if (draft != null) changeMapper.invoke(draft)

        contractorRepository.findByName(name)
            .also {
                changeMapper.invoke(it)
                contractorRepository.save(it)
            }
    }

    @Transactional
    fun changeContractor(userId: Long, changeMapper: (ContractorEntity) -> Unit) {
        val draft = contractorDrafts.values.find { it.addedByUserId == userId }
        if (draft != null) changeMapper.invoke(draft)

        contractorRepository.findByAddedByUserId(userId)
            .also {
                changeMapper.invoke(it)
                contractorRepository.save(it)
            }
    }

    fun getCategories(): List<String> =
        contractorRepository.getCategories()
}
