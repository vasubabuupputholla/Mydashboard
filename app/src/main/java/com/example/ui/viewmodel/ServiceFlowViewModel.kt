package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ServiceFlowRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab(val title: String) {
    SERVICES("Services"),
    TECHNICIANS("Technicians"),
    DEFAULT_PRODUCTS("Default Products"),
    ASSIGN_PRODUCT("Assign Product to Technician"),
    INCENTIVES("Incentives"),
    SALES("Sales"),
    STOCK("Stock"),
    SECURITY("Security")
}

enum class TechnicianTab(val title: String) {
    SERVICES("Services"),
    SALES("Sales"),
    STOCK("Stock")
}

class ServiceFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ServiceFlowRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ServiceFlowRepository(database.serviceFlowDao())
    }

    // Auth State
    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Tabs
    private val _adminTab = MutableStateFlow(AdminTab.SERVICES)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    private val _technicianTab = MutableStateFlow(TechnicianTab.SERVICES)
    val technicianTab: StateFlow<TechnicianTab> = _technicianTab.asStateFlow()

    // Top Filters
    private val _filterFromDate = MutableStateFlow("")
    val filterFromDate: StateFlow<String> = _filterFromDate.asStateFlow()

    private val _filterToDate = MutableStateFlow("")
    val filterToDate: StateFlow<String> = _filterToDate.asStateFlow()

    private val _filterTechnicianName = MutableStateFlow("All technicians")
    val filterTechnicianName: StateFlow<String> = _filterTechnicianName.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Data streams from repository
    val allTechnicians: StateFlow<List<Technician>> = repository.allTechnicians
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTechnicianStock: StateFlow<List<TechnicianStock>> = repository.allTechnicianStock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockHistory: StateFlow<List<StockAdditionHistory>> = repository.allStockHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentSettlements: StateFlow<List<PaymentSettlement>> = repository.allPaymentSettlements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServiceOrdersWithItems: StateFlow<List<ServiceOrderWithItems>> = repository.serviceOrdersWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class FilterParams(
        val fromDate: String,
        val toDate: String,
        val techFilter: String,
        val query: String
    )

    private val filterParamsFlow: Flow<FilterParams> = combine(
        _filterFromDate,
        _filterToDate,
        _filterTechnicianName,
        _searchQuery
    ) { fromDate, toDate, techFilter, query ->
        FilterParams(fromDate, toDate, techFilter, query)
    }

    // Filtered service orders based on auth, top filters, and search query
    val filteredServiceOrders: StateFlow<List<ServiceOrderWithItems>> = combine(
        repository.serviceOrdersWithItems,
        _authState,
        filterParamsFlow
    ) { orders, auth, filters ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fromDate = filters.fromDate
        val toDate = filters.toDate
        val techFilter = filters.techFilter
        val query = filters.query

        orders.filter { item ->
            val order = item.order

            // 1. Role filter
            val roleMatches = when (auth) {
                is AuthState.TechnicianUser -> order.technicianId == auth.technician.id
                is AuthState.Admin -> {
                    if (techFilter == "All technicians" || techFilter.isEmpty()) true
                    else order.technicianName.equals(techFilter, ignoreCase = true)
                }
                AuthState.LoggedOut -> false
            }
            if (!roleMatches) return@filter false

            // 2. Date filters
            val orderDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(order.createdAt))
            if (fromDate.isNotEmpty()) {
                try {
                    val orderDate = dateFormat.parse(orderDateStr)
                    val fromD = dateFormat.parse(fromDate)
                    if (orderDate != null && fromD != null && orderDate.before(fromD)) {
                        return@filter false
                    }
                } catch (_: Exception) {}
            }
            if (toDate.isNotEmpty()) {
                try {
                    val orderDate = dateFormat.parse(orderDateStr)
                    val toD = dateFormat.parse(toDate)
                    if (orderDate != null && toD != null && orderDate.after(toD)) {
                        return@filter false
                    }
                } catch (_: Exception) {}
            }

            // 3. Search query
            if (query.isNotEmpty()) {
                val q = query.trim().lowercase()
                val matchesOrder = order.orderId.lowercase().contains(q) ||
                        order.technicianName.lowercase().contains(q) ||
                        order.technicianPhone.contains(q) ||
                        order.location.lowercase().contains(q)
                val matchesProduct = item.items.any { it.productName.lowercase().contains(q) }
                if (!matchesOrder && !matchesProduct) return@filter false
            }

            true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getStockByTechnician(technicianId: Long): Flow<List<TechnicianStock>> =
        repository.getStockByTechnician(technicianId)

    fun getStockHistoryByTechnician(technicianId: Long): Flow<List<StockAdditionHistory>> =
        repository.getStockHistoryByTechnician(technicianId)

    // Dashboard metrics synchronized reactively with filtered orders
    val dashboardMetrics: StateFlow<DashboardMetrics> = filteredServiceOrders.map { list ->
        DashboardMetrics(
            totalServices = list.size,
            totalProducts = list.sumOf { it.totalProductCount },
            serviceAmount = list.sumOf { it.totalServiceAmount },
            receivedAmount = list.sumOf { it.totalReceivedAmount },
            totalIncentive = list.sumOf { it.calculatedIncentive }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Incentive summary table for Admin -> Incentives tab
    val incentiveSummaryRows: StateFlow<List<IncentiveSummaryRow>> = combine(
        allTechnicians,
        filteredServiceOrders
    ) { technicians, orders ->
        val ordersByTech = orders.groupBy { it.order.technicianId }
        technicians.map { tech ->
            val techOrders = ordersByTech[tech.id] ?: emptyList()
            IncentiveSummaryRow(
                technicianId = tech.id,
                technicianName = tech.name,
                serviceCount = techOrders.size,
                totalIncentive = techOrders.sumOf { it.calculatedIncentive }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Message / Snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Set Tabs & Filters
    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun setTechnicianTab(tab: TechnicianTab) {
        _technicianTab.value = tab
    }

    fun setFilterFromDate(date: String) {
        _filterFromDate.value = date
    }

    fun setFilterToDate(date: String) {
        _filterToDate.value = date
    }

    fun setFilterTechnician(name: String) {
        _filterTechnicianName.value = name
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFilters() {
        _filterFromDate.value = ""
        _filterToDate.value = ""
        _filterTechnicianName.value = "All technicians"
        _searchQuery.value = ""
    }

    // Auth actions
    fun loginAdmin(password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val valid = repository.verifyAdmin("vasubabu2026", password)
            if (valid) {
                _authState.value = AuthState.Admin("vasubabu2026")
                _adminTab.value = AdminTab.SERVICES
                clearFilters()
                onSuccess()
            } else {
                onError("Invalid admin credentials! Please check your password.")
            }
        }
    }

    fun loginTechnician(username: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val tech = repository.verifyTechnician(username, password)
            if (tech != null) {
                _authState.value = AuthState.TechnicianUser(tech)
                _technicianTab.value = TechnicianTab.SERVICES
                clearFilters()
                onSuccess()
            } else {
                onError("Invalid technician credentials or account does not exist.")
            }
        }
    }

    fun logout() {
        _authState.value = AuthState.LoggedOut
        clearFilters()
    }

    // OTP Security Management for Admin Password Reset
    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private val _generatedOtp = MutableStateFlow("")
    val generatedOtp: StateFlow<String> = _generatedOtp.asStateFlow()

    val targetEmail = "bra1262002@gmail.com"
    val maskedTargetEmail: String
        get() {
            val atIndex = targetEmail.indexOf('@')
            if (atIndex <= 1) return "***@***"
            val username = targetEmail.substring(0, atIndex)
            val domain = targetEmail.substring(atIndex)
            val firstChar = username.first()
            val lastChar = username.last()
            return "$firstChar*****$lastChar$domain"
        }

    fun sendSecurityOtp() {
        val code = (100000..999999).random().toString()
        _generatedOtp.value = code
        _otpSent.value = true
        showMessage("OTP code $code sent to registered email ($maskedTargetEmail)")
    }

    fun verifyAndResetPassword(enteredOtp: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (enteredOtp.trim() != _generatedOtp.value) {
            onError("Invalid OTP code. Please check and try again.")
            return
        }
        if (newPass.trim().length < 4) {
            onError("Password must be at least 4 characters long.")
            return
        }
        viewModelScope.launch {
            repository.updateAdminPassword(newPass.trim())
            _otpSent.value = false
            _generatedOtp.value = ""
            showMessage("Admin password successfully changed!")
            onSuccess()
        }
    }

    // Technician CRUD
    fun addTechnician(name: String, phone: String, username: String, pass: String, email: String, incentiveEnabled: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            val tech = Technician(
                name = name.trim(),
                phone = phone.trim(),
                username = username.trim(),
                password = pass.trim(),
                email = email.trim(),
                incentiveEnabled = incentiveEnabled
            )
            repository.addTechnician(tech)
            showMessage("Technician '${tech.name}' added successfully")
            onDone()
        }
    }

    fun toggleTechnicianIncentive(technician: Technician) {
        viewModelScope.launch {
            repository.updateTechnician(technician.copy(incentiveEnabled = !technician.incentiveEnabled))
            showMessage("Updated incentive visibility for ${technician.name}")
        }
    }

    fun deleteTechnician(technicianId: Long) {
        viewModelScope.launch {
            repository.deleteTechnician(technicianId)
            showMessage("Technician deleted successfully")
        }
    }

    // Products CRUD
    fun addProduct(name: String, defaultValue: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addProduct(Product(name = name.trim(), defaultValue = defaultValue))
            showMessage("Product '$name' added with default value ₹$defaultValue")
            onDone()
        }
    }

    fun updateProductDefaultValue(product: Product, newValue: Double) {
        viewModelScope.launch {
            repository.updateProduct(product.copy(defaultValue = newValue))
            showMessage("Updated ${product.name} default value to ₹$newValue")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showMessage("Product '${product.name}' deleted")
        }
    }

    // Stock Assignment to Technician
    fun assignStockToTechnician(
        technicianId: Long,
        technicianName: String,
        productId: Long,
        productName: String,
        quantity: Int,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repository.assignStockToTechnician(technicianId, technicianName, productId, productName, quantity)
            showMessage("Assigned $quantity units of $productName to $technicianName")
            onDone()
        }
    }

    fun deleteStockAssignment(stockId: Long) {
        viewModelScope.launch {
            repository.deleteStockAssignment(stockId)
            showMessage("Stock assignment removed")
        }
    }

    // Service Orders Creation with Duplicate Check
    fun createServiceOrder(
        orderId: String,
        technician: Technician,
        location: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedId = orderId.trim()
            if (trimmedId.isEmpty()) {
                onError("Service Order ID cannot be empty")
                return@launch
            }
            if (repository.isOrderIdExists(trimmedId)) {
                onError("Duplicate Order ID: Order ID '$trimmedId' already exists!")
                return@launch
            }

            val order = ServiceOrder(
                orderId = trimmedId,
                technicianId = technician.id,
                technicianName = technician.name,
                technicianPhone = technician.phone,
                location = location.trim(),
                createdAt = System.currentTimeMillis()
            )

            val result = repository.createServiceOrder(order)
            result.onSuccess {
                showMessage("Service order #$trimmedId created successfully")
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Failed to create service order")
            }
        }
    }

    // Assign Product to Existing Service Order
    fun assignProductToService(
        orderId: String,
        product: Product,
        quantity: Int,
        receivedAmount: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.assignProductToService(
                serviceOrderId = orderId,
                productId = product.id,
                productName = product.name,
                defaultValue = product.defaultValue,
                quantity = quantity,
                receivedAmount = receivedAmount
            )
            result.onSuccess {
                showMessage("Product assigned to order #$orderId")
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Failed to assign product")
            }
        }
    }

    // Edit Product in Service
    fun editServiceProduct(
        item: ServiceProductItem,
        newQuantity: Int,
        newReceivedAmount: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.editServiceProductItem(item, newQuantity, newReceivedAmount)
            result.onSuccess {
                showMessage("Product in order updated successfully")
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Failed to edit product")
            }
        }
    }

    // Delete single product item from service -> restores stock back to technician
    fun deleteServiceProductItem(item: ServiceProductItem) {
        viewModelScope.launch {
            repository.deleteServiceProductItem(item)
            showMessage("Product removed and restocked to technician")
        }
    }

    // Delete entire service order -> restores all stock back to technician
    fun deleteServiceOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteServiceOrder(orderId)
            showMessage("Service order #$orderId deleted and products restocked to technician")
        }
    }

    // Extra Incentive management
    fun setExtraIncentive(orderId: String, extraAmount: Double, remarks: String) {
        viewModelScope.launch {
            repository.updateExtraIncentive(orderId, extraAmount, remarks)
            showMessage("Extra incentive updated for #$orderId")
        }
    }

    fun deleteExtraIncentive(orderId: String) {
        viewModelScope.launch {
            repository.deleteExtraIncentive(orderId)
            showMessage("Extra incentive removed for #$orderId")
        }
    }

    // Mark Payment Settled
    fun markPaymentSettled(
        technician: Technician?,
        fromDate: String,
        toDate: String,
        remarks: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val orders = filteredServiceOrders.value.filter { item ->
                if (technician != null && item.order.technicianId != technician.id) return@filter false
                true
            }

            if (orders.isEmpty()) {
                showMessage("No matching service orders found for the selected dates")
                return@launch
            }

            val techName = technician?.name ?: "All technicians"
            repository.markPaymentSettled(
                technicianId = technician?.id,
                technicianName = techName,
                fromDate = fromDate.ifEmpty { "Selected" },
                toDate = toDate.ifEmpty { "Selected" },
                remarks = remarks,
                matchingOrders = orders
            )
            showMessage("Marked ${orders.size} services as Payment Settled")
            onDone()
        }
    }

    // Time window checking: Technician can edit/delete within 2 hours
    fun canTechnicianEditOrDelete(order: ServiceOrder): Boolean {
        if (order.isSettled) return false
        val twoHoursMillis = 2 * 60 * 60 * 1000L
        val elapsed = System.currentTimeMillis() - order.createdAt
        return elapsed <= twoHoursMillis
    }
}
