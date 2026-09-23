package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AdminCredentials::class,
        Technician::class,
        Product::class,
        TechnicianStock::class,
        StockAdditionHistory::class,
        ServiceOrder::class,
        ServiceProductItem::class,
        PaymentSettlement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun serviceFlowDao(): ServiceFlowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "serviceflow_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.serviceFlowDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: ServiceFlowDao) {
                // 1. Admin Credentials
                dao.insertAdminCredentials(
                    AdminCredentials(
                        id = 1,
                        username = "vasubabu2026",
                        passwordHash = "vasubabu@1",
                        email = "bra1262002@gmail.com"
                    )
                )

                // 2. Technicians matching screenshots
                val techPrudhviId = dao.insertTechnician(
                    Technician(
                        name = "PRUDHVI",
                        phone = "9642948366",
                        username = "prudhvi",
                        password = "123",
                        email = "prudhvi@serviceflow.com",
                        incentiveEnabled = true
                    )
                )

                val techVasuId = dao.insertTechnician(
                    Technician(
                        name = "VASUBABU",
                        phone = "9876543210",
                        username = "vasu",
                        password = "123",
                        email = "vasubabu@serviceflow.com",
                        incentiveEnabled = true
                    )
                )

                val techVenkateshId = dao.insertTechnician(
                    Technician(
                        name = "Venkatesh",
                        phone = "9123456780",
                        username = "venkatesh",
                        password = "123",
                        email = "venkatesh@serviceflow.com",
                        incentiveEnabled = true
                    )
                )

                val techPavanId = dao.insertTechnician(
                    Technician(
                        name = "Pavan",
                        phone = "9979787867",
                        username = "pavan",
                        password = "123",
                        email = "pavan@boschservice.com",
                        incentiveEnabled = true
                    )
                )

                dao.insertTechnician(
                    Technician(
                        name = "Testing delete this user anytime",
                        phone = "9000000000",
                        username = "testuser",
                        password = "123",
                        email = "test@example.com",
                        incentiveEnabled = true
                    )
                )

                dao.insertTechnician(
                    Technician(
                        name = "Ajay",
                        phone = "9555123456",
                        username = "ajay",
                        password = "123",
                        email = "ajay@serviceflow.com",
                        incentiveEnabled = true
                    )
                )

                // 3. Default Products matching screenshots
                val pStand = dao.insertProduct(Product(name = "stand", defaultValue = 1500.0))
                val pCover = dao.insertProduct(Product(name = "cover", defaultValue = 600.0))
                val pInlet = dao.insertProduct(Product(name = "Inlet pipe extension", defaultValue = 150.0))
                val pOutlet = dao.insertProduct(Product(name = "Out let pipe", defaultValue = 200.0))
                val pSalt = dao.insertProduct(Product(name = "Dishwasher Salt", defaultValue = 250.0))
                val pDescal = dao.insertProduct(Product(name = "Descal", defaultValue = 350.0))
                val pCoverFL = dao.insertProduct(Product(name = "Cover front load", defaultValue = 600.0))
                val p4kStand = dao.insertProduct(Product(name = "4k stand", defaultValue = 1800.0))
                val pLiquid = dao.insertProduct(Product(name = "Liquid 350", defaultValue = 350.0))

                // 4. Stock assigned to technicians
                dao.insertOrUpdateStock(TechnicianStock(technicianId = techPrudhviId, technicianName = "PRUDHVI", productId = pCover, productName = "cover", availableQuantity = 4, usedQuantity = 0))
                dao.insertOrUpdateStock(TechnicianStock(technicianId = techPrudhviId, technicianName = "PRUDHVI", productId = pStand, productName = "stand", availableQuantity = 5, usedQuantity = 1))
                dao.insertStockHistory(StockAdditionHistory(technicianId = techPrudhviId, technicianName = "PRUDHVI", productId = pCover, productName = "cover", quantityAdded = 4))

                dao.insertOrUpdateStock(TechnicianStock(technicianId = techVasuId, technicianName = "VASUBABU", productId = pStand, productName = "stand", availableQuantity = 6, usedQuantity = 3))
                dao.insertStockHistory(StockAdditionHistory(technicianId = techVasuId, technicianName = "VASUBABU", productId = pStand, productName = "stand", quantityAdded = 9))

                dao.insertOrUpdateStock(TechnicianStock(technicianId = techVenkateshId, technicianName = "Venkatesh", productId = pInlet, productName = "Inlet pipe extension", availableQuantity = 5, usedQuantity = 3))
                dao.insertStockHistory(StockAdditionHistory(technicianId = techVenkateshId, technicianName = "Venkatesh", productId = pInlet, productName = "Inlet pipe extension", quantityAdded = 8))

                dao.insertOrUpdateStock(TechnicianStock(technicianId = techPavanId, technicianName = "Pavan", productId = pOutlet, productName = "Out let pipe", availableQuantity = 3, usedQuantity = 0))
                dao.insertOrUpdateStock(TechnicianStock(technicianId = techPavanId, technicianName = "Pavan", productId = pSalt, productName = "Dishwasher Salt", availableQuantity = 8, usedQuantity = 0))
                dao.insertOrUpdateStock(TechnicianStock(technicianId = techPavanId, technicianName = "Pavan", productId = pDescal, productName = "Descal", availableQuantity = 7, usedQuantity = 0))
                dao.insertOrUpdateStock(TechnicianStock(technicianId = techPavanId, technicianName = "Pavan", productId = pCoverFL, productName = "Cover front load", availableQuantity = 7, usedQuantity = 0))
                dao.insertOrUpdateStock(TechnicianStock(technicianId = techPavanId, technicianName = "Pavan", productId = p4kStand, productName = "4k stand", availableQuantity = 3, usedQuantity = 0))
                dao.insertOrUpdateStock(TechnicianStock(technicianId = techPavanId, technicianName = "Pavan", productId = pLiquid, productName = "Liquid 350", availableQuantity = 2, usedQuantity = 0))

                // 5. Initial Service Orders
                val order1 = ServiceOrder(
                    orderId = "6770624533",
                    technicianId = techPrudhviId,
                    technicianName = "PRUDHVI",
                    technicianPhone = "9642948366",
                    location = "Ramanapeta",
                    createdAt = System.currentTimeMillis() - (1000L * 60 * 30), // 30 mins ago
                    extraIncentive = 0.0
                )
                dao.insertServiceOrder(order1)
                dao.insertServiceProductItem(
                    ServiceProductItem(
                        serviceOrderId = "6770624533",
                        productId = pStand,
                        productName = "stand",
                        defaultValue = 1500.0,
                        quantity = 1,
                        totalValue = 1500.0,
                        receivedAmount = 1500.0,
                        incentive = 0.0
                    )
                )

                val order2 = ServiceOrder(
                    orderId = "4955556955",
                    technicianId = techVasuId,
                    technicianName = "VASUBABU",
                    technicianPhone = "9876543210",
                    location = "Banjara Hills, Hyderabad",
                    createdAt = System.currentTimeMillis() - (1000L * 60 * 60 * 24), // yesterday
                    extraIncentive = 500.0,
                    extraIncentiveRemarks = "extra incentive offered by admin",
                    isSettled = true,
                    settledDateRange = "2026-09-18 - 2026-09-18",
                    settledRemarks = "Total 12900. Online 9650. Cash 750. remaining adjusted with incentive 2500"
                )
                dao.insertServiceOrder(order2)
                dao.insertServiceProductItem(
                    ServiceProductItem(
                        serviceOrderId = "4955556955",
                        productId = pStand,
                        productName = "stand",
                        defaultValue = 1500.0,
                        quantity = 2,
                        totalValue = 3000.0,
                        receivedAmount = 4950.0,
                        incentive = 1950.0
                    )
                )

                val order3 = ServiceOrder(
                    orderId = "123",
                    technicianId = techVenkateshId,
                    technicianName = "Venkatesh",
                    technicianPhone = "9123456780",
                    location = "dgdg",
                    createdAt = System.currentTimeMillis() - (1000L * 60 * 60 * 3), // 3 hours ago
                    extraIncentive = 0.0
                )
                dao.insertServiceOrder(order3)
                dao.insertServiceProductItem(
                    ServiceProductItem(
                        serviceOrderId = "123",
                        productId = pInlet,
                        productName = "Inlet pipe extension",
                        defaultValue = 150.0,
                        quantity = 3,
                        totalValue = 450.0,
                        receivedAmount = 100.0,
                        incentive = -350.0
                    )
                )

                // 6. Payment settlement entry
                dao.insertPaymentSettlement(
                    PaymentSettlement(
                        technicianId = techVasuId,
                        technicianName = "VASUBABU",
                        fromDate = "2026-09-18",
                        toDate = "2026-09-18",
                        totalServices = 3,
                        totalAmount = 12900.0,
                        remarks = "Total 12900. Online 9650. Cash 750. remaining adjusted with incentive 2500"
                    )
                )
            }
        }
    }
}
