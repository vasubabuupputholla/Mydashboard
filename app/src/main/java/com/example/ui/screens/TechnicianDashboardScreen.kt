package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.dialogs.*
import androidx.compose.animation.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.motion.*
import com.example.ui.viewmodel.ServiceFlowViewModel
import com.example.ui.viewmodel.TechnicianTab
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TechnicianDashboardScreen(
    technician: Technician,
    viewModel: ServiceFlowViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.technicianTab.collectAsState()
    val fromDate by viewModel.filterFromDate.collectAsState()
    val toDate by viewModel.filterToDate.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val metrics by viewModel.dashboardMetrics.collectAsState()
    val serviceOrders by viewModel.filteredServiceOrders.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val techStock: List<TechnicianStock> by viewModel.getStockByTechnician(technician.id).collectAsState(initial = emptyList())
    val stockHistory: List<StockAdditionHistory> by viewModel.getStockHistoryByTechnician(technician.id).collectAsState(initial = emptyList())

    val userMessage by viewModel.userMessage.collectAsState()

    // Dialog states
    var showAssignServiceDialog by remember { mutableStateOf(false) }
    var orderToAssignProduct by remember { mutableStateOf<ServiceOrderWithItems?>(null) }
    var itemToEdit by remember { mutableStateOf<ServiceProductItem?>(null) }
    var itemToDeleteConfirm by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    var showStockHistoryDialog by remember { mutableStateOf(false) }

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
            // Header matching Screenshot 2
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        Icon(
                            Icons.Default.Engineering,
                            contentDescription = null,
                            tint = MetricBlueStart,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = technician.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Daily Completed services statistics",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { showAssignServiceDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign Service", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log out",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filters
            TopFilterPanel(
                fromDate = fromDate,
                toDate = toDate,
                technicianFilter = technician.name,
                technicianList = listOf(technician.name),
                onFromDateChange = { viewModel.setFilterFromDate(it) },
                onToDateChange = { viewModel.setFilterToDate(it) },
                onTechnicianChange = {},
                showTechnicianFilter = false
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Cards:
            // "Replace service amount with incentive in technician dashboard"
            // "Default value must not visible in anywhere of technician dashboard"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Total Services",
                    value = metrics.totalServices.toString(),
                    subtitle = "Completed assignments",
                    icon = Icons.Default.Description,
                    gradientColors = listOf(MetricBlueStart, MetricBlueEnd),
                    modifier = Modifier.weight(1f)
                )

                MetricStatCard(
                    title = "Total Products",
                    value = metrics.totalProducts.toString(),
                    subtitle = "Used items",
                    icon = Icons.Default.Inventory2,
                    gradientColors = listOf(MetricPurpleStart, MetricPurpleEnd),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // If technician incentive is enabled, show Incentive card with green background!
                if (technician.incentiveEnabled) {
                    MetricStatCard(
                        title = "Incentive",
                        value = "₹" + String.format(Locale.getDefault(), "%,.0f", metrics.totalIncentive),
                        subtitle = "Earned incentive",
                        icon = Icons.Default.AutoAwesome,
                        gradientColors = listOf(MetricGreenStart, MetricGreenEnd),
                        modifier = Modifier.weight(1f)
                    )
                }

                MetricStatCard(
                    title = "Received Amount",
                    value = "₹" + String.format(Locale.getDefault(), "%,.0f", metrics.receivedAmount),
                    subtitle = "Customer collections",
                    icon = Icons.Default.CurrencyRupee,
                    gradientColors = listOf(MetricOrangeStart, MetricOrangeEnd),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs: Services, Sales, Stock
            val haptic = LocalHapticFeedback.current

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TechnicianTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.setTechnicianTab(tab)
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            val icon = when (tab) {
                                TechnicianTab.SERVICES -> Icons.Default.Description
                                TechnicianTab.SALES -> Icons.Default.BarChart
                                TechnicianTab.STOCK -> Icons.Default.Warehouse
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

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content with Animated Content
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { dashboardTabTransitionSpec() },
                label = "TechTabMotion"
            ) { activeTab ->
                when (activeTab) {
                    TechnicianTab.SERVICES -> {
                        TechnicianServicesView(
                            serviceOrders = serviceOrders,
                            technician = technician,
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onAssignProductClick = { orderToAssignProduct = it },
                            onEditProductItemClick = { itemToEdit = it },
                            onDeleteProductItemClick = { item ->
                                itemToDeleteConfirm = Pair("Delete '${item.productName}'? Stock will be restocked.") {
                                    viewModel.deleteServiceProductItem(item)
                                }
                            },
                            onDeleteServiceClick = { order ->
                                itemToDeleteConfirm = Pair("Delete Service Order #${order.orderId}? All product items will be restocked.") {
                                    viewModel.deleteServiceOrder(order.orderId)
                                }
                            },
                            canEditOrDelete = { viewModel.canTechnicianEditOrDelete(it) }
                        )
                    }

                    TechnicianTab.SALES -> {
                        AdminSalesView(serviceOrders = serviceOrders)
                    }

                    TechnicianTab.STOCK -> {
                        TechnicianStockView(
                            technician = technician,
                            stockList = techStock,
                            stockHistory = stockHistory,
                            onShowHistoryClick = { showStockHistoryDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAssignServiceDialog) {
        AssignServiceDialog(
            allTechnicians = listOf(technician),
            preselectedTechnician = technician,
            isAdmin = false,
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
        AssignProductDialog(
            serviceOrderId = orderWithItems.order.orderId,
            technicianName = technician.name,
            technicianStock = techStock,
            allProducts = allProducts,
            isTechnician = true,
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
        val avail = techStock.find { it.productId == item.productId }?.availableQuantity ?: 0
        EditProductItemDialog(
            item = item,
            availableStockForProduct = avail,
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

    if (showStockHistoryDialog) {
        TechnicianStockHistoryDialog(
            stockHistory = stockHistory,
            onDismiss = { showStockHistoryDialog = false }
        )
    }
}

@Composable
fun TechnicianServicesView(
    serviceOrders: List<ServiceOrderWithItems>,
    technician: Technician,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAssignProductClick: (ServiceOrderWithItems) -> Unit,
    onEditProductItemClick: (ServiceProductItem) -> Unit,
    onDeleteProductItemClick: (ServiceProductItem) -> Unit,
    onDeleteServiceClick: (ServiceOrder) -> Unit,
    canEditOrDelete: (ServiceOrder) -> Boolean
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by Order ID, Location, Product...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MetricBlueStart,
                unfocusedBorderColor = CardBorderColor
            )
        )

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
                    Text("No services assigned yet", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                serviceOrders.forEachIndexed { index, item ->
                    val isEditable = canEditOrDelete(item.order)
                    StaggeredEntrance(
                        delayMillis = (index * 40).coerceAtMost(280),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TechnicianOrderCard(
                            orderWithItems = item,
                            technician = technician,
                            canEditOrDelete = isEditable,
                            onAssignProductClick = { onAssignProductClick(item) },
                            onEditProductItemClick = onEditProductItemClick,
                            onDeleteProductItemClick = onDeleteProductItemClick,
                            onDeleteServiceClick = { onDeleteServiceClick(item.order) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TechnicianOrderCard(
    orderWithItems: ServiceOrderWithItems,
    technician: Technician,
    canEditOrDelete: Boolean,
    onAssignProductClick: () -> Unit,
    onEditProductItemClick: (ServiceProductItem) -> Unit,
    onDeleteProductItemClick: (ServiceProductItem) -> Unit,
    onDeleteServiceClick: () -> Unit
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
                        Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(order.orderId, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                        if (canEditOrDelete) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onDeleteServiceClick, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(dateStr, fontSize = 11.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(order.location, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                if (order.isSettled) {
                    CircularPaidStamp(dateRange = order.settledDateRange)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Table without Default Value (completely hidden on Technician side!)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PRODUCT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(2f))
                        Text("QTY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(0.8f))
                        Text("RECEIVED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.3f))
                        if (technician.incentiveEnabled) {
                            Text("INCENTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.weight(1.3f))
                        }
                        if (canEditOrDelete) {
                            Text("ACT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(36.dp))
                        }
                    }

                    if (orderWithItems.items.isEmpty()) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
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
                                Text(item.quantity.toString(), fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                Text("₹${item.receivedAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.3f))
                                if (technician.incentiveEnabled) {
                                    Text(
                                        "₹${item.incentive.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.incentive >= 0) Color(0xFF15803D) else Color(0xFFDC2626),
                                        modifier = Modifier.weight(1.3f)
                                    )
                                }
                                if (canEditOrDelete) {
                                    IconButton(onClick = { onDeleteProductItemClick(item) }, modifier = Modifier.size(18.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Assign another product option: disabled if > 2 hours or settled!
            if (canEditOrDelete) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = onAssignProductClick) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MetricBlueStart)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Assign another product", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MetricBlueStart)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (order.isSettled) "Locked: Payment settled" else "Editing locked (2 hours time elapsed)",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                if (technician.incentiveEnabled) {
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
                }
            }

            if (order.extraIncentive > 0 && technician.incentiveEnabled) {
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
fun TechnicianStockView(
    technician: Technician,
    stockList: List<TechnicianStock>,
    stockHistory: List<StockAdditionHistory>,
    onShowHistoryClick: () -> Unit
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
                    Text("My Stock Inventory", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Available and used product counts", fontSize = 11.sp, color = Color.Gray)
                }
                OutlinedButton(
                    onClick = onShowHistoryClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Addition History", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                        Text("PRODUCT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(2f))
                        Text("AVAILABLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("USED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }

                    if (stockList.isEmpty()) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("No stock allocated to your account yet", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        stockList.forEach { stock ->
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stock.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
                                Text(stock.availableQuantity.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(stock.usedQuantity.toString(), fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechnicianStockHistoryDialog(
    stockHistory: List<StockAdditionHistory>,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy 'at' hh:mm a", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stock Addition History", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (stockHistory.isEmpty()) {
                    Text("No stock additions recorded yet", fontSize = 12.sp, color = Color.Gray)
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stockHistory.forEach { entry ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(entry.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "+${entry.quantityAdded} Added",
                                                color = Color(0xFF15803D),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        dateFormat.format(Date(entry.timestamp)),
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}
