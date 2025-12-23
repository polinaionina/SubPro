package com.example.subpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subpro.model.Subscription
import com.example.subpro.repository.DefaultSubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val repo: DefaultSubscriptionRepository = DefaultSubscriptionRepository()
) : ViewModel() {

    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _subscriptions.value = repo.getAll()
        }
    }

    fun refresh() = load()

    fun add(name: String, price: Double, startDate: String, period: Any, notificationDaysBefore: Int) {
        viewModelScope.launch {
            repo.add(name, price, startDate, period, notificationDaysBefore)
            load()
        }
    }

    fun update(subscription: Subscription) {
        viewModelScope.launch {
            repo.update(subscription)
            load()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            repo.delete(id)
            load()
        }
    }

    fun getById(id: Int): Subscription? = repo.getById(id)
}
