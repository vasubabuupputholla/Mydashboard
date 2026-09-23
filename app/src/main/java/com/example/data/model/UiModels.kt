package com.example.data.model

data class DashboardMetrics(
    val totalServices: Int = 0,
    val totalProducts: Int = 0,
    val serviceAmount: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val totalIncentive: Double = 0.0
)

data class ServiceOrderWithItems(
    val order: ServiceOrder,
    val items: List<ServiceProductItem> = emptyList()
) {
    val totalProductCount: Int get() = items.sumOf { it.quantity }
    val totalServiceAmount: Double get() = items.sumOf { it.totalValue }
    val totalReceivedAmount: Double get() = items.sumOf { it.receivedAmount }
    val calculatedIncentive: Double get() = items.sumOf { it.incentive } + order.extraIncentive
}

data class TechnicianStockSummary(
    val technicianName: String,
    val stockItems: List<TechnicianStock>
)

data class IncentiveSummaryRow(
    val technicianId: Long,
    val technicianName: String,
    val serviceCount: Int,
    val totalIncentive: Double
)

sealed interface AuthState {
    data object LoggedOut : AuthState
    data class Admin(val username: String) : AuthState
    data class TechnicianUser(val technician: Technician) : AuthState
}
