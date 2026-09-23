package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.dialogs.*
import com.example.ui.motion.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.ServiceFlowViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    viewModel: ServiceFlowViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.adminTab.collectAsState()
    val fromDate by viewModel.filterFromDate.collectAsState()
    val toDate by viewModel.filterToDate.collectAsState()
    val techFilter by viewModel.filterTechnicianName.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val metrics by viewModel.dashboardMetrics.collectAsState()
    val serviceOrders by viewModel.filteredServiceOrders.collectAsState()
    val allTechnicians by viewModel.allTechnicians.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allStock by viewModel.allTechnicianStock.collectAsState()
    val settlements by viewModel.allPaymentSettlements.collectAsState()
    val incentiveRows by viewModel.incentiveSummaryRows.collectAsState()

    val userMessage by viewModel.userMessage.collectAsState()

    // Dialog states
    var showAssignServiceDialog by remember { mutableStateOf(false) }
    var orderToAssignProduct by remember { mutableStateOf<ServiceOrderWithItems?>(null) }
    var itemToEdit by remember { mutableStateOf<ServiceProductItem?>(null) }
    var orderForExtraIncentive by remember { mutableStateOf<ServiceOrderWithItems?>(null) }
    var showMarkSettledDialog by remember { mutableStateOf(false) }
    var showAddTechnicianDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var itemToDeleteConfirm by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SoftBgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            // Top Header: Admin Dashboard + Log out button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MetricBlueStart,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Admin Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Log out",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log out", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Text(
                text = "Signed in as vasubabu2026 • full control over technicians, products and services",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Top Filter Row: From Date, To Date, and Technician Filter
            TopFilterPanel(
                fromDate = fromDate,
                toDate = toDate,
                technicianFilter = techFilter,
                technicianList = allTechnicians.map { it.name },
                onFromDateChange = { viewModel.setFilterFromDate(it) },
                onToDateChange = { viewModel.setFilterToDate(it) },
                onTechnicianChange = { viewModel.setFilterTechnician(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5 Metric Cards matching Screenshot 1
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Total Services",
                        value = metrics.totalServices.toString(),
                        subtitle = techFilter,
                        icon = Icons.Default.Description,
                        gradientColors = listOf(MetricBlueStart, MetricBlueEnd),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Total Products",
                        value = metrics.totalProducts.toString(),
                        subtitle = "Items used in services",
                        icon = Icons.Default.Inventory2,
                        gradientColors = listOf(MetricPurpleStart, MetricPurpleEnd),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Service Amount",
                        value = "₹" + String.format(Locale.getDefault(), "%,.0f", metrics.serviceAmount),
                        subtitle = "Default product value",
                        icon = Icons.Default.TrendingUp,
                        gradientColors = listOf(MetricGreenStart, MetricGreenEnd),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Received Amount",
                        value = "₹" + String.format(Locale.getDefault(), "%,.0f", metrics.receivedAmount),
                        subtitle = "Collected from customers",
                        icon = Icons.Default.CurrencyRupee,
                        gradientColors = listOf(MetricOrangeStart, MetricOrangeEnd),
                        modifier = Modifier.weight(1f)
                    )
                }

                MetricStatCard(
                    title = "Total Incentive",
                    value = "₹" + String.format(Locale.getDefault(), "%,.0f", metrics.totalIncentive),
                    subtitle = "Received - default value + extra",
                    icon = Icons.Default.AutoAwesome,
                    gradientColors = listOf(MetricVioletStart, MetricVioletEnd),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Navigation Row with all tabs from screenshots
            val haptic = LocalHapticFeedback.current

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.setAdminTab(tab)
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            val icon = when (tab) {
                                AdminTab.SERVICES -> Icons.Default.Description
                                AdminTab.TECHNICIANS -> Icons.Default.People
                                AdminTab.DEFAULT_PRODUCTS -> Icons.Default.Inventory2
                                AdminTab.ASSIGN_PRODUCT -> Icons.Default.Hub
                                AdminTab.INCENTIVES -> Icons.Default.AutoAwesome
                                AdminTab.SALES -> Icons.Default.BarChart
                                AdminTab.STOCK -> Icons.Default.Warehouse
                                AdminTab.SECURITY -> Icons.Default.Security
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MetricBlueStart.copy(alpha = 0.12f),
                            selectedLabelColor = MetricBlueStart,
                            selectedLeadingIconColor = MetricBlueStart
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MetricBlueStart else CardBorderColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB CONTENT AREA WITH SMOOTH MOTION TRANSITIONS
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { dashboardTabTransitionSpec() },
                label = "AdminTabMotion"
            ) { activeTab ->
                when (activeTab) {
                    AdminTab.SERVICES -> {
                        AdminServicesView(
                            serviceOrders = serviceOrders,
                            allTechnicians = allTechnicians,
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onAssignServiceClick = { showAssignServiceDialog = true },
                            onAssignProductClick = { orderToAssignProduct = it },
                            onEditProductItemClick = { itemToEdit = it },
                            onDeleteProductItemClick = { item ->
                                itemToDeleteConfirm = Pair("Delete '${item.productName}' from order? Stock will be restocked to technician.") {
                                    viewModel.deleteServiceProductItem(item)
                                }
                            },
                            onDeleteServiceClick = { order ->
                                itemToDeleteConfirm = Pair("Delete Service Order #${order.orderId}? All product items will be restocked.") {
                                    viewModel.deleteServiceOrder(order.orderId)
                                }
                            },
                            onAddExtraIncentiveClick = { orderForExtraIncentive = it }
                        )
                    }

                    AdminTab.TECHNICIANS -> {
                        AdminTechniciansView(
                            technicians = allTechnicians,
                            onAddClick = { showAddTechnicianDialog = true },
                            onToggleIncentive = { viewModel.toggleTechnicianIncentive(it) },
                            onDeleteClick = { tech ->
                                itemToDeleteConfirm = Pair("Are you sure you want to delete technician '${tech.name}'? Double confirmation required.") {
                                    viewModel.deleteTechnician(tech.id)
                                }
                            }
                        )
                    }

                    AdminTab.DEFAULT_PRODUCTS -> {
                        AdminDefaultProductsView(
                            products = allProducts,
                            onAddClick = { showAddProductDialog = true },
                            onUpdateDefaultValue = { prod, newVal ->
                                viewModel.updateProductDefaultValue(prod, newVal)
                            },
                            onDeleteClick = { prod ->
                                itemToDeleteConfirm = Pair("Delete product '${prod.name}'?") {
                                    viewModel.deleteProduct(prod)
                                }
                            }
                        )
                    }

                    AdminTab.ASSIGN_PRODUCT -> {
                        AdminAssignProductStockView(
                            technicians = allTechnicians,
                            products = allProducts,
                            stockList = allStock,
                            onAssign = { techId, techName, prodId, prodName, qty ->
                                viewModel.assignStockToTechnician(techId, techName, prodId, prodName, qty) {}
                            },
                            onDeleteStock = { stockId ->
                                viewModel.deleteStockAssignment(stockId)
                            }
                        )
                    }

                    AdminTab.INCENTIVES -> {
                        AdminIncentivesView(
                            incentiveRows = incentiveRows,
                            settlements = settlements,
                            onMarkSettledClick = { showMarkSettledDialog = true }
                        )
                    }

                    AdminTab.SALES -> {
                        AdminSalesView(serviceOrders = serviceOrders)
                    }

                    AdminTab.STOCK -> {
                        AdminStockView(
                            technicians = allTechnicians,
                            allStock = allStock
                        )
                    }

                    AdminTab.SECURITY -> {
                        AdminSecurityView(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // DIALOG IMPLEMENTATIONS
    if (showAssignServiceDialog) {
        AssignServiceDialog(
            allTechnicians = allTechnicians,
            preselectedTechnician = null,
            isAdmin = true,
            onDismiss = { showAssignServiceDialog = false },
            onAssign = { orderId, tech, loc ->
                viewModel.createServiceOrder(
                    orderId = orderId,
                    technician = tech,
                    location = loc,
                    onSuccess = { showAssignServiceDialog = false },
                    onError = { viewModel.showMessage(it) }
                )
            }
        )
    }

    orderToAssignProduct?.let { orderWithItems ->
        val techStock = allStock.filter { it.technicianId == orderWithItems.order.technicianId }
        AssignProductDialog(
            serviceOrderId = orderWithItems.order.orderId,
            technicianName = orderWithItems.order.technicianName,
            technicianStock = techStock,
            allProducts = allProducts,
            isTechnician = false,
            onDismiss = { orderToAssignProduct = null },
            onAssign = { product, quantity, received ->
                viewModel.assignProductToService(
                    orderId = orderWithItems.order.orderId,
                    product = product,
                    quantity = quantity,
                    receivedAmount = received,
                    onSuccess = { orderToAssignProduct = null },
                    onError = { viewModel.showMessage(it) }
                )
            }
        )
    }

    itemToEdit?.let { item ->
        val stockForProduct = allStock.find { it.productId == item.productId }?.availableQuantity ?: 0
        EditProductItemDialog(
            item = item,
            availableStockForProduct = stockForProduct,
            onDismiss = { itemToEdit = null },
            onSave = { newQty, newReceived ->
                viewModel.editServiceProduct(
                    item = item,
                    newQuantity = newQty,
                    newReceivedAmount = newReceived,
                    onSuccess = { itemToEdit = null },
                    onError = { viewModel.showMessage(it) }
                )
            }
        )
    }

    orderForExtraIncentive?.let { orderWithItems ->
        AddExtraIncentiveDialog(
            serviceOrderId = orderWithItems.order.orderId,
            currentExtraIncentive = orderWithItems.order.extraIncentive,
            currentRemarks = orderWithItems.order.extraIncentiveRemarks,
            onDismiss = { orderForExtraIncentive = null },
            onSave = { extraAmt, remarks ->
                viewModel.setExtraIncentive(orderWithItems.order.orderId, extraAmt, remarks)
                orderForExtraIncentive = null
            },
            onDelete = {
                viewModel.deleteExtraIncentive(orderWithItems.order.orderId)
                orderForExtraIncentive = null
            }
        )
    }

    if (showMarkSettledDialog) {
        val selectedTechObj = allTechnicians.find { it.name.equals(techFilter, ignoreCase = true) }
        MarkPaymentSettledDialog(
            technicians = allTechnicians,
            selectedTech = selectedTechObj,
            defaultFromDate = fromDate,
            defaultToDate = toDate,
            onDismiss = { showMarkSettledDialog = false },
            onConfirm = { tech, fDate, tDate, remarks ->
                viewModel.markPaymentSettled(
                    technician = tech,
                    fromDate = fDate,
                    toDate = tDate,
                    remarks = remarks,
                    onDone = { showMarkSettledDialog = false }
                )
            }
        )
    }

    if (showAddTechnicianDialog) {
        AddTechnicianDialog(
            onDismiss = { showAddTechnicianDialog = false },
            onAdd = { name, phone, user, pass, email, incEnabled ->
                viewModel.addTechnician(name, phone, user, pass, email, incEnabled) {
                    showAddTechnicianDialog = false
                }
            }
        )
    }

    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onAdd = { name, dv ->
                viewModel.addProduct(name, dv) {
                    showAddProductDialog = false
                }
            }
        )
    }

    itemToDeleteConfirm?.let { (msg, action) ->
        ConfirmDeleteDialog(
            title = "Confirm Action",
            message = msg,
            onDismiss = { itemToDeleteConfirm = null },
            onConfirm = {
                action()
                itemToDeleteConfirm = null
            }
        )
    }
}

// -------------------------------------------------------------------------
// Subviews for Admin Tabs
// -------------------------------------------------------------------------

@Composable
fun AdminServicesView(
    serviceOrders: List<ServiceOrderWithItems>,
    allTechnicians: List<Technician>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAssignServiceClick: () -> Unit,
    onAssignProductClick: (ServiceOrderWithItems) -> Unit,
    onEditProductItemClick: (ServiceProductItem) -> Unit,
    onDeleteProductItemClick: (ServiceProductItem) -> Unit,
    onDeleteServiceClick: (ServiceOrder) -> Unit,
    onAddExtraIncentiveClick: (ServiceOrderWithItems) -> Unit
) {
    Column {
        // Search & Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by Order ID, Tech, Location...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MetricBlueStart,
                    unfocusedBorderColor = CardBorderColor
                )
            )

            Button(
                onClick = onAssignServiceClick,
                colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Assign Service", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (serviceOrders.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No services found matching criteria", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                serviceOrders.forEachIndexed { index, item ->
                    StaggeredEntrance(
                        delayMillis = (index * 40).coerceAtMost(280),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ServiceOrderCard(
                            orderWithItems = item,
                            isAdmin = true,
                            canEditOrDelete = !item.order.isSettled,
                            onAssignProductClick = { onAssignProductClick(item) },
                            onEditProductItemClick = onEditProductItemClick,
                            onDeleteProductItemClick = onDeleteProductItemClick,
                            onDeleteServiceClick = { onDeleteServiceClick(item.order) },
                            onAddExtraIncentiveClick = { onAddExtraIncentiveClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceOrderCard(
    orderWithItems: ServiceOrderWithItems,
    isAdmin: Boolean,
    canEditOrDelete: Boolean,
    onAssignProductClick: () -> Unit,
    onEditProductItemClick: (ServiceProductItem) -> Unit,
    onDeleteProductItemClick: (ServiceProductItem) -> Unit,
    onDeleteServiceClick: () -> Unit,
    onAddExtraIncentiveClick: () -> Unit
) {
    val order = orderWithItems.order
    val dateStr = SimpleDateFormat("dd MMM yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID, icons, and circular stamp if settled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.orderId,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        if (canEditOrDelete) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onDeleteServiceClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${order.technicianName} ${if (order.technicianPhone.isNotEmpty()) "· (${order.technicianPhone})" else ""}",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(dateStr, fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(order.location, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                // Circular PAID stamp on right side if settled
                if (order.isSettled) {
                    CircularPaidStamp(dateRange = order.settledDateRange)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Products Table
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PRODUCT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(2f))
                        if (isAdmin) {
                            Text("VALUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.2f))
                        }
                        Text("QTY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(0.8f))
                        Text("TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.2f))
                        Text("RECEIVED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.3f))
                        Text("INCENTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.weight(1.3f))
                        if (canEditOrDelete) {
                            Text("ACT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(36.dp))
                        }
                    }

                    if (orderWithItems.items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No products assigned yet", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        orderWithItems.items.forEach { item ->
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.productName, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
                                if (isAdmin) {
                                    Text("₹${item.defaultValue.toInt()}", fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                                }
                                Text(item.quantity.toString(), fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                Text("₹${item.totalValue.toInt()}", fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                                Text("₹${item.receivedAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.3f))
                                Text(
                                    text = "₹${item.incentive.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.incentive >= 0) Color(0xFF15803D) else Color(0xFFDC2626),
                                    modifier = Modifier.weight(1.3f)
                                )
                                if (canEditOrDelete) {
                                    Row(modifier = Modifier.width(36.dp)) {
                                        IconButton(onClick = { onDeleteProductItemClick(item) }, modifier = Modifier.size(18.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Assign another product button if not locked
            if (canEditOrDelete) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = onAssignProductClick) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MetricBlueStart)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Assign another product", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MetricBlueStart)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Badges Row (Service Amount, Received Amount, Incentive with green background, Extra Incentive)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAdmin) {
                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Text(
                            text = "Service amount: ₹${orderWithItems.totalServiceAmount.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFFFF7ED),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Text(
                        text = "Received amount: ₹${orderWithItems.totalReceivedAmount.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC2410C),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                // Incentive badge with green background as requested in prompt!
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Text(
                        text = "Incentive: ₹${orderWithItems.calculatedIncentive.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                if (isAdmin) {
                    Button(
                        onClick = onAddExtraIncentiveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("+ Add Extra Incentive", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (order.extraIncentive > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "★ Extra Incentive: ₹${order.extraIncentive.toInt()} (${order.extraIncentiveRemarks})",
                        fontSize = 11.sp,
                        color = Color(0xFF6B21A8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminTechniciansView(
    technicians: List<Technician>,
    onAddClick: () -> Unit,
    onToggleIncentive: (Technician) -> Unit,
    onDeleteClick: (Technician) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Technicians List", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Manage accounts, credentials, and incentive visibility", fontSize = 11.sp, color = Color.Gray)
                }
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Technician", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                technicians.forEach { tech ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MetricBlueStart.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MetricBlueStart, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Phone: ${tech.phone} | User: ${tech.username}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Incentive", fontSize = 10.sp, color = Color.Gray)
                                    Switch(
                                        checked = tech.incentiveEnabled,
                                        onCheckedChange = { onToggleIncentive(tech) },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                IconButton(onClick = { onDeleteClick(tech) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDefaultProductsView(
    products: List<Product>,
    onAddClick: () -> Unit,
    onUpdateDefaultValue: (Product, Double) -> Unit,
    onDeleteClick: (Product) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Default Products", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Default values sync automatically with child technician login", fontSize = 11.sp, color = Color.Gray)
                }
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Product", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                products.forEach { prod ->
                    var editing by remember { mutableStateOf(false) }
                    var editValueText by remember { mutableStateOf(prod.defaultValue.toInt().toString()) }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (editing) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = editValueText,
                                            onValueChange = { editValueText = it },
                                            modifier = Modifier.width(120.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = {
                                            val newVal = editValueText.toDoubleOrNull()
                                            if (newVal != null && newVal >= 0) {
                                                onUpdateDefaultValue(prod, newVal)
                                                editing = false
                                            }
                                        }) {
                                            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF16A34A))
                                        }
                                    }
                                } else {
                                    Text("Default Value: ₹${prod.defaultValue.toInt()}", fontSize = 12.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Row {
                                if (!editing) {
                                    IconButton(onClick = { editing = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(onClick = { onDeleteClick(prod) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAssignProductStockView(
    technicians: List<Technician>,
    products: List<Product>,
    stockList: List<TechnicianStock>,
    onAssign: (techId: Long, techName: String, prodId: Long, prodName: String, qty: Int) -> Unit,
    onDeleteStock: (Long) -> Unit
) {
    var selectedTech by remember { mutableStateOf(technicians.firstOrNull()) }
    var selectedProd by remember { mutableStateOf(products.firstOrNull()) }
    var qtyText by remember { mutableStateOf("10") }
    var techDropdownExpanded by remember { mutableStateOf(false) }
    var prodDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Assign Product Stock to Technician", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(14.dp))

            // Form inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Technician dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { techDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(selectedTech?.name ?: "Select technician", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(
                        expanded = techDropdownExpanded,
                        onDismissRequest = { techDropdownExpanded = false }
                    ) {
                        technicians.forEach { t ->
                            DropdownMenuItem(text = { Text(t.name) }, onClick = {
                                selectedTech = t
                                techDropdownExpanded = false
                            })
                        }
                    }
                }

                // Product dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { prodDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(selectedProd?.name ?: "Select product", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(
                        expanded = prodDropdownExpanded,
                        onDismissRequest = { prodDropdownExpanded = false }
                    ) {
                        products.forEach { p ->
                            DropdownMenuItem(text = { Text(p.name) }, onClick = {
                                selectedProd = p
                                prodDropdownExpanded = false
                            })
                        }
                    }
                }

                // Quantity
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    modifier = Modifier.width(80.dp),
                    placeholder = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val t = selectedTech ?: return@Button
                    val p = selectedProd ?: return@Button
                    val q = qtyText.toIntOrNull() ?: 1
                    onAssign(t.id, t.name, p.id, p.name, q)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Assign Stock", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "This stock will be shown to the technician in the product dropdown when assigning products to a service.",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stock table matching Screenshot 6
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("TECHNICIAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                        Text("PRODUCT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                        Text("AVAILABLE QTY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.2f))
                        Text("ACTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(60.dp))
                    }

                    if (stockList.isEmpty()) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("No technician stock assigned yet", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        stockList.forEach { stock ->
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stock.technicianName, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                Text(stock.productName, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                                Text(stock.availableQuantity.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                IconButton(onClick = { onDeleteStock(stock.id) }, modifier = Modifier.width(60.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminIncentivesView(
    incentiveRows: List<IncentiveSummaryRow>,
    settlements: List<PaymentSettlement>,
    onMarkSettledClick: () -> Unit
) {
    val totalIncentive = incentiveRows.sumOf { it.totalIncentive }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Incentives Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = onMarkSettledClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Payment Settled", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Table matching Screenshot 7
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("TECHNICIAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(2f))
                        Text("SERVICES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1f))
                        Text("TOTAL INCENTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    }

                    incentiveRows.forEach { row ->
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(2f)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(row.technicianName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(row.serviceCount.toString(), fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(
                                "₹" + String.format(Locale.getDefault(), "%,.0f", row.totalIncentive),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.weight(1.5f),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Total row
                    HorizontalDivider(color = Color(0xFF16A34A), thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCFCE7).copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF166534), modifier = Modifier.weight(3f))
                        Text(
                            "₹" + String.format(Locale.getDefault(), "%,.0f", totalIncentive),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF15803D),
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Payment Settlements matching Screenshot 7
            Text("Recent Payment Settlements", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(10.dp))

            if (settlements.isEmpty()) {
                Text("No payment settlements yet", fontSize = 12.sp, color = Color.Gray)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    settlements.forEach { s ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF16A34A))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PAID", color = Color(0xFF15803D), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                                Text(s.technicianName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                Text("${s.fromDate} - ${s.toDate}", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                                if (s.remarks.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(s.remarks, fontSize = 11.sp, color = Color(0xFF475569), textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSalesView(serviceOrders: List<ServiceOrderWithItems>) {
    // Group sales day-wise and month-wise
    val dayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    val dayWiseSales = serviceOrders
        .groupBy { dayFormat.format(Date(it.order.createdAt)) }
        .map { (day, list) -> Pair(day, list.sumOf { it.totalReceivedAmount }) }
        .takeLast(7)

    val monthWiseSales = serviceOrders
        .groupBy { monthFormat.format(Date(it.order.createdAt)) }
        .map { (month, list) -> Pair(month, list.sumOf { it.totalReceivedAmount }) }
        .takeLast(6)

    val highestIncentiveDay = serviceOrders
        .groupBy { dayFormat.format(Date(it.order.createdAt)) }
        .maxByOrNull { it.value.sumOf { order -> order.calculatedIncentive } }
        ?.let { "${it.key}: ₹${it.value.sumOf { o -> o.calculatedIncentive }.toInt()}" } ?: ""

    val highestSalesMonth = monthWiseSales.maxByOrNull { it.second }
        ?.let { "${it.first}: ₹${it.second.toInt()}" } ?: ""

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SimpleBarChart(
            title = "Day-wise Sales (Last 7 Days)",
            dataPoints = dayWiseSales,
            highlightLabel = if (highestIncentiveDay.isNotEmpty()) "Highest Incentive ($highestIncentiveDay)" else "",
            barColor = MetricBlueStart
        )

        SimpleBarChart(
            title = "Month-wise Sales",
            dataPoints = monthWiseSales,
            highlightLabel = if (highestSalesMonth.isNotEmpty()) "Highest Sales ($highestSalesMonth)" else "",
            barColor = MetricPurpleStart
        )
    }
}

@Composable
fun AdminStockView(
    technicians: List<Technician>,
    allStock: List<TechnicianStock>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Technician Stock Inventory", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Click on a technician to view and expand their product stock in 3 columns", fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(14.dp))

            technicians.forEach { tech ->
                var expanded by remember { mutableStateOf(true) }
                val techStockItems = allStock.filter { it.technicianId == tech.id }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MetricBlueStart, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "${techStockItems.size} products",
                                        fontSize = 10.sp,
                                        color = MetricBlueStart,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        if (expanded) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White
                            ) {
                                Column {
                                    // 3 Columns matching prompt requirement!
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF1F5F9))
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("PRODUCT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(2f))
                                        Text("AVAILABLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                        Text("USED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    }

                                    if (techStockItems.isEmpty()) {
                                        Box(modifier = Modifier.padding(12.dp)) {
                                            Text("No stock allocated to ${tech.name}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    } else {
                                        techStockItems.forEach { s ->
                                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(s.productName, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
                                                Text(s.availableQuantity.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                                Text(s.usedQuantity.toString(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSecurityView(viewModel: ServiceFlowViewModel) {
    val otpSent by viewModel.otpSent.collectAsState()
    val generatedOtp by viewModel.generatedOtp.collectAsState()

    var enteredOtp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MetricBlueStart.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = MetricBlueStart, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Change Admin Password", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text("An OTP will be sent to verify your identity", fontSize = 12.sp, color = Color(0xFF64748B))
            Text("Registered Email: ${viewModel.maskedTargetEmail}", fontSize = 11.sp, color = MetricBlueStart, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(20.dp))

            if (!otpSent) {
                Button(
                    onClick = { viewModel.sendSecurityOtp() },
                    colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send OTP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("OTP sent to ${viewModel.maskedTargetEmail}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E40AF))
                        Text("Verification Code: $generatedOtp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = enteredOtp,
                    onValueChange = {
                        enteredOtp = it
                        errorMessage = null
                    },
                    label = { Text("Enter 6-Digit OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("New Admin Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(successMessage ?: "", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            errorMessage = "Passwords do not match!"
                            return@Button
                        }
                        viewModel.verifyAndResetPassword(
                            enteredOtp = enteredOtp,
                            newPass = newPassword,
                            onSuccess = {
                                successMessage = "Admin password successfully updated!"
                                enteredOtp = ""
                                newPassword = ""
                                confirmPassword = ""
                            },
                            onError = { errorMessage = it }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Verify OTP & Change Password", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
