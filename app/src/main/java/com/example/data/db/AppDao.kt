package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceFlowDao {

    // Admin Credentials
    @Query("SELECT * FROM admin_credentials WHERE id = 1 LIMIT 1")
    fun getAdminCredentials(): Flow<AdminCredentials?>

    @Query("SELECT * FROM admin_credentials WHERE id = 1 LIMIT 1")
    suspend fun getAdminCredentialsSync(): AdminCredentials?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminCredentials(admin: AdminCredentials)

    @Query("UPDATE admin_credentials SET passwordHash = :newPassword WHERE id = 1")
    suspend fun updateAdminPassword(newPassword: String)

    // Technicians
    @Query("SELECT * FROM technicians ORDER BY name ASC")
    fun getAllTechnicians(): Flow<List<Technician>>

    @Query("SELECT * FROM technicians WHERE id = :id LIMIT 1")
    suspend fun getTechnicianById(id: Long): Technician?

    @Query("SELECT * FROM technicians WHERE username = :username LIMIT 1")
    suspend fun getTechnicianByUsername(username: String): Technician?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnician(technician: Technician): Long

    @Update
    suspend fun updateTechnician(technician: Technician)

    @Delete
    suspend fun deleteTechnician(technician: Technician)

    @Query("DELETE FROM technicians WHERE id = :id")
    suspend fun deleteTechnicianById(id: Long)

    // Products
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    // Technician Stock
    @Query("SELECT * FROM technician_stock ORDER BY technicianName ASC, productName ASC")
    fun getAllTechnicianStock(): Flow<List<TechnicianStock>>

    @Query("SELECT * FROM technician_stock WHERE technicianId = :technicianId ORDER BY productName ASC")
    fun getStockByTechnicianId(technicianId: Long): Flow<List<TechnicianStock>>

    @Query("SELECT * FROM technician_stock WHERE technicianId = :technicianId AND productId = :productId LIMIT 1")
    suspend fun getStockItem(technicianId: Long, productId: Long): TechnicianStock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStock(stock: TechnicianStock)

    @Delete
    suspend fun deleteStock(stock: TechnicianStock)

    @Query("DELETE FROM technician_stock WHERE id = :id")
    suspend fun deleteStockById(id: Long)

    // Stock Addition History
    @Query("SELECT * FROM stock_addition_history ORDER BY timestamp DESC")
    fun getAllStockHistory(): Flow<List<StockAdditionHistory>>

    @Query("SELECT * FROM stock_addition_history WHERE technicianId = :technicianId ORDER BY timestamp DESC")
    fun getStockHistoryByTechnician(technicianId: Long): Flow<List<StockAdditionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistory(history: StockAdditionHistory)

    // Service Orders
    @Query("SELECT * FROM service_orders ORDER BY createdAt DESC")
    fun getAllServiceOrders(): Flow<List<ServiceOrder>>

    @Query("SELECT * FROM service_orders WHERE technicianId = :technicianId ORDER BY createdAt DESC")
    fun getServiceOrdersByTechnician(technicianId: Long): Flow<List<ServiceOrder>>

    @Query("SELECT * FROM service_orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getServiceOrderById(orderId: String): ServiceOrder?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertServiceOrder(order: ServiceOrder)

    @Update
    suspend fun updateServiceOrder(order: ServiceOrder)

    @Delete
    suspend fun deleteServiceOrder(order: ServiceOrder)

    @Query("DELETE FROM service_orders WHERE orderId = :orderId")
    suspend fun deleteServiceOrderById(orderId: String)

    // Service Product Items
    @Query("SELECT * FROM service_product_items WHERE serviceOrderId = :orderId ORDER BY id ASC")
    fun getProductItemsForOrder(orderId: String): Flow<List<ServiceProductItem>>

    @Query("SELECT * FROM service_product_items WHERE serviceOrderId = :orderId ORDER BY id ASC")
    suspend fun getProductItemsForOrderSync(orderId: String): List<ServiceProductItem>

    @Query("SELECT * FROM service_product_items ORDER BY id ASC")
    fun getAllServiceProductItems(): Flow<List<ServiceProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceProductItem(item: ServiceProductItem): Long

    @Update
    suspend fun updateServiceProductItem(item: ServiceProductItem)

    @Delete
    suspend fun deleteServiceProductItem(item: ServiceProductItem)

    @Query("DELETE FROM service_product_items WHERE serviceOrderId = :orderId")
    suspend fun deleteProductItemsByOrderId(orderId: String)

    // Payment Settlements
    @Query("SELECT * FROM payment_settlements ORDER BY timestamp DESC")
    fun getAllPaymentSettlements(): Flow<List<PaymentSettlement>>

    @Query("SELECT * FROM payment_settlements WHERE technicianId = :technicianId ORDER BY timestamp DESC")
    fun getPaymentSettlementsByTechnician(technicianId: Long): Flow<List<PaymentSettlement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentSettlement(settlement: PaymentSettlement)
}
