package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.data.model.ServiceProductItem
import com.example.data.model.Technician
import com.example.data.model.TechnicianStock
import com.example.ui.components.CardBorderColor
import com.example.ui.components.MetricBlueStart

@Composable
fun AssignServiceDialog(
    allTechnicians: List<Technician>,
    preselectedTechnician: Technician?,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onAssign: (orderId: String, technician: Technician, location: String) -> Unit
) {
    var orderId by remember { mutableStateOf("") }
    var selectedTech by remember { mutableStateOf(preselectedTechnician ?: allTechnicians.firstOrNull()) }
    var techDropdownExpanded by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign Service to Technician",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Service ID
                Text(
                    text = "Service ID *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = orderId,
                    onValueChange = {
                        orderId = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 4955556955", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetricBlueStart,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Assigned Technician
                Text(
                    text = "Assigned Technician",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (isAdmin && allTechnicians.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { techDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MetricBlueStart, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(selectedTech?.name ?: "Select Technician", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), fontSize = 13.sp)
                                        if (selectedTech?.email?.isNotEmpty() == true) {
                                            Text(selectedTech?.email ?: "", fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = techDropdownExpanded,
                            onDismissRequest = { techDropdownExpanded = false }
                        ) {
                            allTechnicians.forEach { tech ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(tech.name, fontWeight = FontWeight.Medium)
                                            if (tech.email.isNotEmpty()) {
                                                Text(tech.email, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedTech = tech
                                        techDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MetricBlueStart, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(selectedTech?.name ?: "Technician", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                if (selectedTech?.email?.isNotEmpty() == true) {
                                    Text(selectedTech?.email ?: "", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location
                Text(
                    text = "Customer Location *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Kukatpally, Hyderabad", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetricBlueStart,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info note matching Screenshot 3
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: After creating the service, you can assign products to this service from the '+ Assign Products to Service' button.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E40AF),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (orderId.trim().isEmpty()) {
                                errorMessage = "Please enter a valid Service ID"
                                return@Button
                            }
                            val tech = selectedTech
                            if (tech == null) {
                                errorMessage = "Please select a technician"
                                return@Button
                            }
                            if (location.trim().isEmpty()) {
                                errorMessage = "Please enter customer location"
                                return@Button
                            }
                            onAssign(orderId.trim(), tech, location.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Assign Service")
                    }
                }
            }
        }
    }
}

@Composable
fun AssignProductDialog(
    serviceOrderId: String,
    technicianName: String,
    technicianStock: List<TechnicianStock>,
    allProducts: List<Product>,
    isTechnician: Boolean,
    onDismiss: () -> Unit,
    onAssign: (product: Product, quantity: Int, receivedAmount: Double) -> Unit
) {
    // Filter available products according to rule: "Without product available the technician must not be able to select product from available list data for the respective product"
    val availableProductList = remember(technicianStock, allProducts) {
        allProducts.mapNotNull { prod ->
            val stockItem = technicianStock.find { it.productId == prod.id }
            val available = stockItem?.availableQuantity ?: 0
            if (isTechnician && available <= 0) null
            else Pair(prod, available)
        }
    }

    var selectedProductWithStock by remember { mutableStateOf(availableProductList.firstOrNull()) }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var quantityText by remember { mutableStateOf("1") }
    var receivedAmountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign Product to Existing Service",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Service ID & Technician Summary Box
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Service ID", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(serviceOrderId, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Assigned Technician", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(technicianName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Product Dropdown
                Text(
                    text = "Select Product *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (availableProductList.isEmpty()) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No products with available stock for this technician. Admin must assign stock first.",
                            color = Color(0xFFB91C1C),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { productDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val current = selectedProductWithStock
                                if (current != null) {
                                    Text(
                                        text = "${current.first.name} (Available: ${current.second})",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                } else {
                                    Text("Select Product", fontSize = 13.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            availableProductList.forEach { pair ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(pair.first.name, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Surface(
                                                color = if (pair.second > 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Available: ${pair.second}",
                                                    fontSize = 11.sp,
                                                    color = if (pair.second > 0) Color(0xFF166534) else Color(0xFF991B1B),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedProductWithStock = pair
                                        productDropdownExpanded = false
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity
                Text(
                    text = "Quantity *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetricBlueStart,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // User prompt requirement: "Add received amount in the assign product window after quantity option"
                Text(
                    text = "Received Amount (₹) *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = receivedAmountText,
                    onValueChange = {
                        receivedAmountText = it
                        errorMessage = null
                    },
                    placeholder = { Text("Amount collected from customer (₹)", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Color(0xFF64748B)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetricBlueStart,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: This will add the selected product to this service assignment. The stock will be updated automatically.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E40AF),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pair = selectedProductWithStock
                            if (pair == null) {
                                errorMessage = "Please select a product"
                                return@Button
                            }
                            val qty = quantityText.toIntOrNull()
                            if (qty == null || qty <= 0) {
                                errorMessage = "Please enter a valid quantity greater than 0"
                                return@Button
                            }
                            if (qty > pair.second) {
                                errorMessage = "Cannot assign $qty units. Only ${pair.second} available in stock"
                                return@Button
                            }
                            val received = receivedAmountText.toDoubleOrNull()
                            if (received == null || received < 0) {
                                errorMessage = "Please enter a valid received amount"
                                return@Button
                            }

                            onAssign(pair.first, qty, received)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart),
                        shape = RoundedCornerShape(8.dp),
                        enabled = availableProductList.isNotEmpty()
                    ) {
                        Text("Add Product")
                    }
                }
            }
        }
    }
}

@Composable
fun EditProductItemDialog(
    item: ServiceProductItem,
    availableStockForProduct: Int,
    onDismiss: () -> Unit,
    onSave: (newQuantity: Int, newReceivedAmount: Double) -> Unit
) {
    var quantityText by remember { mutableStateOf(item.quantity.toString()) }
    var receivedAmountText by remember { mutableStateOf(item.receivedAmount.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val maxAllowedQty = item.quantity + availableStockForProduct

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Edit Product: ${item.productName}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Quantity (Available extra: $availableStockForProduct)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Received Amount (₹)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = receivedAmountText,
                    onValueChange = {
                        receivedAmountText = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = quantityText.toIntOrNull()
                            if (qty == null || qty <= 0) {
                                errorMessage = "Please enter valid quantity > 0"
                                return@Button
                            }
                            if (qty > maxAllowedQty) {
                                errorMessage = "Maximum allowed quantity is $maxAllowedQty"
                                return@Button
                            }
                            val rec = receivedAmountText.toDoubleOrNull()
                            if (rec == null || rec < 0) {
                                errorMessage = "Please enter valid received amount"
                                return@Button
                            }
                            onSave(qty, rec)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun AddExtraIncentiveDialog(
    serviceOrderId: String,
    currentExtraIncentive: Double,
    currentRemarks: String,
    onDismiss: () -> Unit,
    onSave: (extraAmount: Double, remarks: String) -> Unit,
    onDelete: () -> Unit
) {
    var amountText by remember { mutableStateOf(if (currentExtraIncentive > 0) currentExtraIncentive.toString() else "") }
    var remarksText by remember { mutableStateOf(currentRemarks) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add / Override Extra Incentive",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Service ID: $serviceOrderId",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Extra Incentive Amount (₹) *", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Remarks (Optional)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("extra incentive offered by admin") }
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentExtraIncentive > 0) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Text("Delete")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull()
                                if (amt == null || amt < 0) {
                                    errorMessage = "Please enter a valid amount"
                                    return@Button
                                }
                                onSave(amt, remarksText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                        ) {
                            Text("Save Incentive")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarkPaymentSettledDialog(
    technicians: List<Technician>,
    selectedTech: Technician?,
    defaultFromDate: String,
    defaultToDate: String,
    onDismiss: () -> Unit,
    onConfirm: (tech: Technician?, fromDate: String, toDate: String, remarks: String) -> Unit
) {
    var tech by remember { mutableStateOf(selectedTech) }
    var techDropdownExpanded by remember { mutableStateOf(false) }
    var fromDate by remember { mutableStateOf(defaultFromDate) }
    var toDate by remember { mutableStateOf(defaultToDate) }
    var remarks by remember {
        mutableStateOf("Total settled. Payment verified and adjusted with incentive")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Mark Payment Settled",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "This will apply the circular PAID stamp on all matching services for the technician within the selected date range.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Technician", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { techDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tech?.name ?: "All technicians", fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = techDropdownExpanded,
                        onDismissRequest = { techDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All technicians") },
                            onClick = {
                                tech = null
                                techDropdownExpanded = false
                            }
                        )
                        technicians.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name) },
                                onClick = {
                                    tech = t
                                    techDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        label = { Text("From Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        label = { Text("To Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Settlement Remarks (Optional)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Total 12900. Online 9650. Cash 750. remaining adjusted with incentive 2500") },
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(tech, fromDate, toDate, remarks) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm Settlement")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTechnicianDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, username: String, pass: String, email: String, incentiveEnabled: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var incentiveEnabled by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add New Technician", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Technician Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Show Incentive to Technician", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Enable incentive visibility on their login", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = incentiveEnabled, onCheckedChange = { incentiveEnabled = it })
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank() || username.isBlank() || password.isBlank()) {
                                error = "Please fill in all required fields"
                                return@Button
                            }
                            onAdd(name, phone, username, password, email, incentiveEnabled)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart)
                    ) {
                        Text("Add Technician")
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, defaultValue: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var defaultValueText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add Default Product", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. stand") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = defaultValueText,
                    onValueChange = { defaultValueText = it },
                    label = { Text("Default Value (₹) *") },
                    placeholder = { Text("e.g. 1500") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                error = "Please enter product name"
                                return@Button
                            }
                            val dv = defaultValueText.toDoubleOrNull()
                            if (dv == null || dv < 0) {
                                error = "Please enter valid default value"
                                return@Button
                            }
                            onAdd(name, dv)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MetricBlueStart)
                    ) {
                        Text("Add Product")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Confirm Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
