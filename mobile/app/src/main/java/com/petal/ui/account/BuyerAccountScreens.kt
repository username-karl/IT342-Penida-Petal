package com.petal.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.data.account.DeliveryAddressResponse
import com.petal.data.account.SavedDateResponse
import com.petal.ui.BuyerAccountViewModel
import com.petal.ui.BuyerAccountUiState
import com.petal.ui.components.EmptyPanel
import com.petal.ui.components.ErrorBanner
import com.petal.ui.components.PetalHeader
import com.petal.ui.components.PetalPrimaryButton
import com.petal.ui.components.PetalSecondaryButton
import com.petal.ui.components.PetalTextField
import com.petal.ui.components.SuccessBanner
import com.petal.ui.theme.BlushSoft
import com.petal.ui.theme.Paper
import com.petal.ui.theme.SageSoft
import com.petal.ui.theme.Stone200
import com.petal.ui.theme.Stone500
import com.petal.ui.theme.Stone700
import com.petal.ui.theme.Stone950
import com.petal.ui.theme.SurfaceWarm

@Composable
fun BuyerAccountScreen(
    viewModel: BuyerAccountViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(modifier.fillMaxSize().background(Paper)) {
        PetalHeader(title = "Petal Account", subtitle = "Recipients and reminders", action = {
            PetalSecondaryButton("Back", onBack)
        })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AddressBookSection(state = state, viewModel = viewModel)
            ImportantDatesSection(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun AddressBookSection(state: BuyerAccountUiState, viewModel: BuyerAccountViewModel) {
    AccountPanel(eyebrow = "Address Book", title = "Recipient Book", tint = SageSoft) {
        state.addressSuccess?.let {
            SuccessBanner(message = it)
            Spacer(Modifier.height(12.dp))
        }
        state.addressesError?.let {
            ErrorBanner(message = it)
            Spacer(Modifier.height(12.dp))
        }

        when {
            state.addressesLoading -> Text("Loading saved recipients...", color = Stone500)
            state.addresses.isEmpty() -> EmptyPanel(
                title = "No saved addresses yet",
                message = "Save frequent recipients for faster checkout."
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.addresses.forEach { address ->
                    AddressCard(
                        address = address,
                        actionLoading = state.addressActionLoading,
                        onDefault = { viewModel.makeDefaultAddress(address) },
                        onDelete = { viewModel.deleteAddress(address.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Add Recipient", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        PetalTextField("Label", state.addressLabel, viewModel::setAddressLabel)
        Spacer(Modifier.height(10.dp))
        PetalTextField("Recipient Name", state.addressRecipientName, viewModel::setAddressRecipientName)
        Spacer(Modifier.height(10.dp))
        PetalTextField("Phone Optional", state.addressPhoneNumber, viewModel::setAddressPhoneNumber)
        Spacer(Modifier.height(10.dp))
        PetalTextField(
            label = "Delivery Address",
            value = state.addressLine,
            onValueChange = viewModel::setAddressLine,
            singleLine = false
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = state.addressDefault, onCheckedChange = viewModel::setAddressDefault)
            Text("Make default recipient", color = Stone700)
        }
        PetalPrimaryButton(
            text = "Save Recipient",
            onClick = viewModel::saveAddress,
            loading = state.addressActionLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ImportantDatesSection(state: BuyerAccountUiState, viewModel: BuyerAccountViewModel) {
    AccountPanel(eyebrow = "Forget-Me-Not", title = "Important Dates", tint = BlushSoft) {
        state.dateSuccess?.let {
            SuccessBanner(message = it)
            Spacer(Modifier.height(12.dp))
        }
        state.datesError?.let {
            ErrorBanner(message = it)
            Spacer(Modifier.height(12.dp))
        }

        when {
            state.datesLoading -> Text("Loading important dates...", color = Stone500)
            state.savedDates.isEmpty() -> EmptyPanel(
                title = "No reminders yet",
                message = "Add birthdays and anniversaries you want Petal to remember."
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.savedDates.forEach { savedDate ->
                    SavedDateCard(savedDate)
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Add Important Date", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        PetalTextField("Label", state.savedDateLabel, viewModel::setSavedDateLabel)
        Spacer(Modifier.height(10.dp))
        PetalTextField("Event Date (YYYY-MM-DD)", state.savedDateEventDate, viewModel::setSavedDateEventDate)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = state.savedDateRecurring, onCheckedChange = viewModel::setSavedDateRecurring)
            Text("Repeat yearly", color = Stone700)
        }
        PetalPrimaryButton(
            text = "Save Important Date",
            onClick = viewModel::saveDate,
            loading = state.dateActionLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AccountPanel(eyebrow: String, title: String, tint: androidx.compose.ui.graphics.Color, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceWarm, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(18.dp)
    ) {
        Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = Stone500)
        Spacer(Modifier.height(6.dp))
        Text(
            title,
            modifier = Modifier
                .background(tint, RoundedCornerShape(4.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = Stone950
        )
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun AddressCard(
    address: DeliveryAddressResponse,
    actionLoading: Boolean,
    onDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Paper, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(address.label ?: "Recipient", style = MaterialTheme.typography.titleLarge)
            if (address.defaultAddress) {
                Text("Default", color = Stone700, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(address.recipientName ?: "Recipient", color = Stone950, fontWeight = FontWeight.SemiBold)
        Text(address.addressLine ?: "Address not provided", color = Stone700, style = MaterialTheme.typography.bodyMedium)
        address.phoneNumber?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = Stone500, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!address.defaultAddress) {
                PetalSecondaryButton("Default", onDefault)
            }
            PetalSecondaryButton(if (actionLoading) "Working" else "Remove", onDelete)
        }
    }
}

@Composable
private fun SavedDateCard(savedDate: SavedDateResponse) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Paper, RoundedCornerShape(4.dp))
            .border(1.dp, Stone200, RoundedCornerShape(4.dp))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(savedDate.label ?: "Important date", style = MaterialTheme.typography.titleLarge)
            Text(if (savedDate.recurring) "Yearly" else "One-time", color = Stone700, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(6.dp))
        Text(savedDate.eventDate ?: "Date not provided", color = Stone700)
    }
}
