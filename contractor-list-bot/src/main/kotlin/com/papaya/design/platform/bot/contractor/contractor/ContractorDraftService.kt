package com.papaya.design.platform.bot.contractor.contractor

import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap


private val log = KotlinLogging.logger { }

@Service
class ContractorDraftService(
    private val contractorRepository: ContractorRepository
) {
    private val contractorDrafts = ConcurrentHashMap<String, ContractorEntity>()

    fun createContractorDraft(name: String, userId: Long) {
        contractorDrafts[name] = ContractorEntity().apply {
            this.name = name
            this.addedByUserId = userId
        }
    }

    @Transactional
    fun changeContractorDraft(userId: Long, changeMapper: (ContractorEntity) -> Unit) {
        val draft = contractorDrafts.values.find { it.addedByUserId == userId }
        if (draft != null) {
            changeMapper.invoke(draft)
            return
        }

        contractorRepository.findByAddedByUserId(userId)
            .also {
                changeMapper.invoke(it)
                contractorRepository.save(it)
            }
    }

    fun saveDraftIfExists(userId: Long): Boolean {
        val draft = contractorDrafts.values.find { it.addedByUserId == userId }

        log.info { "Saving draft $draft" }

        return if (draft == null) false
        else {
            contractorRepository.save(draft)
            contractorDrafts.remove(draft.name)
            true
        }
    }

    fun removeDraft(userId: Long) {
        contractorDrafts.values.find { it.addedByUserId == userId }?.also {
            contractorDrafts.remove(it.name)
        }
    }

    fun getContractorDraft(userId: Long) =
        contractorDrafts.values.find { it.addedByUserId == userId }
}