package com.example.data.repository

import com.example.data.db.ServiceFlowDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.*

class ServiceFlowRepository(private val dao: ServiceFlowDao) {

    val adminCredentials: Flow<AdminCredentials?> = dao.getAdminCredentials()
    val allTechnicians: Flow<List<Technician>> = dao.getAllTechnicians()
    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val allTechnicianStock: Flow<List<TechnicianStock>> = dao.getAllTechnicianStock()
    val allStockHistory: Flow<List<StockAdditionHistory>> = dao.getAllStockHistory()
    val allPaymentSettlements: Flow<List<PaymentSettlement>> = dao.getAllPaymentSettlements()

    // Combined stream of Service Orders with their Items
    val serviceOrdersWithItems: Flow<List<ServiceOrderWithItems>> =
        combine(dao.getAllServiceOrders(), dao.getAllServiceProductItems()) { orders, items ->
            val itemsByOrderId = items.groupBy { it.serviceOrderId }
            orders.map { order ->
                ServiceOrderWithItems(
                    order = order,
                    items = itemsByOrderId[order.orderId] ?: emptyList()
                )
            }
        }

    fun getStockByTechnician(technicianId: Long): Flow<List<TechnicianStock>> =
        dao.getStockByTechnicianId(technicianId)

    fun getStockHistoryByTechnician(technicianId: Long): Flow<List<StockAdditionHistory>> =
        dao.getStockHistoryByTechnician(technicianId)

    // Check duplicate order ID
    suspend fun isOrderIdExists(orderId: String): Boolean {
        return dao.getServiceOrderById(orderId.trim()) != null
    }

    // Auth helpers
    suspend fun verifyAdmin(username: String, password: String): Boolean {
        val admin = dao.getAdminCredentialsSync() ?: return (username == "vasubabu2026" && password == "vasubabu@1")
        return admin.username.equals(username.trim(), ignoreCase = true) && admin.passwordHash == password
    }

    suspend fun verifyTechnician(username: String, password: String): Technician? {
        val tech = dao.getTechnicianByUsername(username.trim()) ?: return null
        return if (tech.password == password) tech else null
    }

    suspend fun updateAdminPassword(newPassword: String) {
        dao.updateAdminPassword(newPassword)
    }

    // Technician Management
    suspend fun addTechnician(technician: Technician): Long {
        return dao.insertTechnician(technician)
    }

    suspend fun updateTechnician(technician: Technician) {
        dao.updateTechnician(technician)
    }

    suspend fun deleteTechnician(technicianId: Long) {
        dao.deleteTechnicianById(technicianId)
    }

    // Product Management
    suspend fun addProduct(product: Product): Long {
        return dao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        dao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        dao.deleteProduct(product)
    }

    // Stock Management
    suspend fun assignStockToTechnician(
        technicianId: Long,
        technicianName: String,
        productId: Long,
        productName: String,
        quantity: Int
    ) {
        val existing = dao.getStockItem(technicianId, productId)
        if (existing != null) {
            val updated = existing.copy(
                availableQuantity = existing.availableQuantity + quantity
            )
            dao.insertOrUpdateStock(updated)
        } else {
            val newStock = TechnicianStock(
                technicianId = technicianId,
                technicianName = technicianName,
                productId = productId,
                productName = productName,
                availableQuantity = quantity,
                usedQuantity = 0
            )
            dao.insertOrUpdateStock(newStock)
        }

        // Add to history
        dao.insertStockHistory(
            StockAdditionHistory(
                technicianId = technicianId,
                technicianName = technicianName,
                productId = productId,
                productName = productName,
                quantityAdded = quantity
            )
        )
    }

    suspend fun deleteStockAssignment(stockId: Long) {
        dao.deleteStockById(stockId)
    }

    // Service Orders
    suspend fun createServiceOrder(order: ServiceOrder): Result<Unit> {
        if (dao.getServiceOrderById(order.orderId.trim()) != null) {
            return Result.failure(Exception("Duplicate Order ID: Order ID '${order.orderId}' already exists"))
        }
        dao.insertServiceOrder(order.copy(orderId = order.orderId.trim()))
        return Result.success(Unit)
    }

    suspend fun updateServiceOrder(order: ServiceOrder) {
        dao.updateServiceOrder(order)
    }

    // Assign Product to Existing Service
    suspend fun assignProductToService(
        serviceOrderId: String,
        productId: Long,
        productName: String,
        defaultValue: Double,
        quantity: Int,
        receivedAmount: Double
    ): Result<Unit> {
        val order = dao.getServiceOrderById(serviceOrderId)
            ?: return Result.failure(Exception("Service order not found"))

        // Check technician stock availability
        val stock = dao.getStockItem(order.technicianId, productId)
        if (stock == null || stock.availableQuantity < quantity) {
            val available = stock?.availableQuantity ?: 0
            return Result.failure(Exception("Insufficient stock! Only $available available for $productName"))
        }

        // Deduct from technician stock, increase used
        val updatedStock = stock.copy(
            availableQuantity = stock.availableQuantity - quantity,
            usedQuantity = stock.usedQuantity + quantity
        )
        dao.insertOrUpdateStock(updatedStock)

        // Calculate incentive = receivedAmount - (defaultValue * quantity)
        val totalValue = defaultValue * quantity
        val incentive = receivedAmount - totalValue

        val item = ServiceProductItem(
            serviceOrderId = serviceOrderId,
            productId = productId,
            productName = productName,
            defaultValue = defaultValue,
            quantity = quantity,
            totalValue = totalValue,
            receivedAmount = receivedAmount,
            incentive = incentive
        )
        dao.insertServiceProductItem(item)

        return Result.success(Unit)
    }

    // Edit Product Item in Service
    suspend fun editServiceProductItem(
        item: ServiceProductItem,
        newQuantity: Int,
        newReceivedAmount: Double
    ): Result<Unit> {
        val order = dao.getServiceOrderById(item.serviceOrderId)
            ?: return Result.failure(Exception("Order not found"))

        val stock = dao.getStockItem(order.technicianId, item.productId)
        val diff = newQuantity - item.quantity

        if (diff > 0) {
            // Need more stock
            if (stock == null || stock.availableQuantity < diff) {
                return Result.failure(Exception("Insufficient stock to increase quantity by $diff"))
            }
            dao.insertOrUpdateStock(
                stock.copy(
                    availableQuantity = stock.availableQuantity - diff,
                    usedQuantity = stock.usedQuantity + diff
                )
            )
        } else if (diff < 0) {
            // Releasing stock
            val restoreAmount = -diff
            if (stock != null) {
                dao.insertOrUpdateStock(
                    stock.copy(
                        availableQuantity = stock.availableQuantity + restoreAmount,
                        usedQuantity = maxOf(0, stock.usedQuantity - restoreAmount)
                    )
                )
            }
        }

        val totalValue = item.defaultValue * newQuantity
        val newIncentive = newReceivedAmount - totalValue
        val updatedItem = item.copy(
            quantity = newQuantity,
            totalValue = totalValue,
            receivedAmount = newReceivedAmount,
            incentive = newIncentive
        )
        dao.updateServiceProductItem(updatedItem)
        return Result.success(Unit)
    }

    // Delete single product item from service -> restores stock back to technician
    suspend fun deleteServiceProductItem(item: ServiceProductItem) {
        val order = dao.getServiceOrderById(item.serviceOrderId)
        if (order != null) {
            val stock = dao.getStockItem(order.technicianId, item.productId)
            if (stock != null) {
                dao.insertOrUpdateStock(
                    stock.copy(
                        availableQuantity = stock.availableQuantity + item.quantity,
                        usedQuantity = maxOf(0, stock.usedQuantity - item.quantity)
                    )
                )
            }
        }
        dao.deleteServiceProductItem(item)
    }

    // Delete entire service order -> restores all stock back to technician
    suspend fun deleteServiceOrder(orderId: String) {
        val order = dao.getServiceOrderById(orderId) ?: return
        val items = dao.getProductItemsForOrderSync(orderId)

        for (item in items) {
            val stock = dao.getStockItem(order.technicianId, item.productId)
            if (stock != null) {
                dao.insertOrUpdateStock(
                    stock.copy(
                        availableQuantity = stock.availableQuantity + item.quantity,
                        usedQuantity = maxOf(0, stock.usedQuantity - item.quantity)
                    )
                )
            }
        }

        dao.deleteProductItemsByOrderId(orderId)
        dao.deleteServiceOrderById(orderId)
    }

    // Extra Incentive management
    suspend fun updateExtraIncentive(orderId: String, extraAmount: Double, remarks: String) {
        val order = dao.getServiceOrderById(orderId) ?: return
        val formattedRemarks = if (remarks.trim().isEmpty()) "extra incentive offered by admin" else remarks.trim()
        dao.updateServiceOrder(
            order.copy(
                extraIncentive = extraAmount,
                extraIncentiveRemarks = formattedRemarks
            )
        )
    }

    suspend fun deleteExtraIncentive(orderId: String) {
        val order = dao.getServiceOrderById(orderId) ?: return
        dao.updateServiceOrder(
            order.copy(
                extraIncentive = 0.0,
                extraIncentiveRemarks = ""
            )
        )
    }

    // Mark Payment Settled
    suspend fun markPaymentSettled(
        technicianId: Long?,
        technicianName: String,
        fromDate: String,
        toDate: String,
        remarks: String,
        matchingOrders: List<ServiceOrderWithItems>
    ) {
        val formattedRemarks = remarks.trim()

        for (orderWithItems in matchingOrders) {
            val order = orderWithItems.order
            dao.updateServiceOrder(
                order.copy(
                    isSettled = true,
                    settledDateRange = "$fromDate - $toDate",
                    settledRemarks = formattedRemarks
                )
            )
        }

        val totalAmount = matchingOrders.sumOf { it.totalReceivedAmount }
        dao.insertPaymentSettlement(
            PaymentSettlement(
                technicianId = technicianId ?: 0L,
                technicianName = technicianName,
                fromDate = fromDate,
                toDate = toDate,
                totalServices = matchingOrders.size,
                totalAmount = totalAmount,
                remarks = formattedRemarks
            )
        )
    }
}
