package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_credentials")
data class AdminCredentials(
    @PrimaryKey val id: Int = 1,
    val username: String = "vasubabu2026",
    val passwordHash: String = "vasubabu@1",
    val email: String = "bra1262002@gmail.com"
)

@Entity(tableName = "technicians")
data class Technician(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val username: String,
    val password: String,
    val email: String = "",
    val incentiveEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultValue: Double
)

@Entity(tableName = "technician_stock")
data class TechnicianStock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val technicianId: Long,
    val technicianName: String,
    val productId: Long,
    val productName: String,
    val availableQuantity: Int,
    val usedQuantity: Int = 0
)

@Entity(tableName = "stock_addition_history")
data class StockAdditionHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val technicianId: Long,
    val technicianName: String,
    val productId: Long,
    val productName: String,
    val quantityAdded: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "service_orders")
data class ServiceOrder(
    @PrimaryKey val orderId: String,
    val technicianId: Long,
    val technicianName: String,
    val technicianPhone: String = "",
    val location: String,
    val createdAt: Long = System.currentTimeMillis(),
    val extraIncentive: Double = 0.0,
    val extraIncentiveRemarks: String = "",
    val isSettled: Boolean = false,
    val settledDateRange: String = "",
    val settledRemarks: String = ""
)

@Entity(tableName = "service_product_items")
data class ServiceProductItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceOrderId: String,
    val productId: Long,
    val productName: String,
    val defaultValue: Double,
    val quantity: Int,
    val totalValue: Double,
    val receivedAmount: Double,
    val incentive: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_settlements")
data class PaymentSettlement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val technicianId: Long,
    val technicianName: String,
    val fromDate: String,
    val toDate: String,
    val totalServices: Int,
    val totalAmount: Double,
    val remarks: String,
    val timestamp: Long = System.currentTimeMillis()
)
