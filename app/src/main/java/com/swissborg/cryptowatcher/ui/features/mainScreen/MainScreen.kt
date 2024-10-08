/*
 * © Hugo 2024 for SwissBorg technical challenge
 */

package com.swissborg.cryptowatcher.ui.features.mainScreen

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swissborg.cryptowatcher.R
import com.swissborg.cryptowatcher.ui.components.ExpandableErrorLabel
import com.swissborg.cryptowatcher.ui.components.PulsatingDot
import com.swissborg.cryptowatcher.util.Util.formatNumber
import com.swissborg.domain.model.TickerModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val viewModel = hiltViewModel<MainViewModel>()

    val tickers by viewModel.tickers.collectAsState()
    val error by viewModel.error.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    var selectedTicker by remember { mutableStateOf<TickerModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }, actions = {
                if (isConnected) {
                    Text(text = stringResource(id = R.string.connected), color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(text = stringResource(id = R.string.no_internet), color = MaterialTheme.colorScheme.error)
                }
                PulsatingDot(color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            })
        })
    { paddingValues ->

        if (tickers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center
            ) {
                if (isConnected) CircularProgressIndicator() else Text(
                    text = stringResource(id = R.string.no_internet_message),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {

            selectedTicker = tickers.find { it.symbol == selectedTicker?.symbol } ?: tickers.first()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = stringResource(id = R.string.search)) },
                    modifier = Modifier.fillMaxWidth()
                )

                SelectedCryptoCard(selectedTicker!!)
                ExpandableErrorLabel(error)

                Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))

                LazyColumn() {
                    items(tickers.filter { it.symbol.startsWith(searchQuery.text, ignoreCase = true) }) { ticker ->
                        TickerItem(ticker = ticker, isSelected = ticker == selectedTicker) {
                            selectedTicker = ticker
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TickerItem(ticker: TickerModel, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .let {
                if (isSelected) it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                else it
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = ticker.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ticker.lastPrice.formatNumber()} ${stringResource(id = R.string.usd)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = "${stringResource(id = R.string.last_24)}: ${ticker.dailyChangePerc.formatNumber()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if(ticker.dailyChangePerc >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.End)
                )

                Text(
                    text = "${stringResource(id = R.string.vol)}: ${ticker.volume.formatNumber()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun SelectedCryptoCard(ticker: TickerModel) {

    val context = LocalContext.current

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = ticker.symbol, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(
                text = "${stringResource(id = R.string.price)}: ${ticker.lastPrice.formatNumber()} ${stringResource(id = R.string.usd)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${stringResource(id = R.string.high)}: ${ticker.high.formatNumber()} ${stringResource(id = R.string.usd)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "${stringResource(id = R.string.low)}: ${ticker.low.formatNumber()} ${stringResource(id = R.string.usd)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${stringResource(id = R.string.last_24)}: ${ticker.dailyChangePerc.formatNumber()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if(ticker.dailyChangePerc >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )

                Text(
                    text = "${stringResource(id = R.string.vol)}: ${ticker.volume.formatNumber()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_in),
                        contentDescription = stringResource(id = R.string.receive),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                            .clickable {Toast.makeText(context, context.getString(R.string.feature_fake), Toast.LENGTH_SHORT).show()}
                    )
                    Text(
                        text = stringResource(id = R.string.receive),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_out),
                        contentDescription = stringResource(id = R.string.send),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(50))
                            .clickable {Toast.makeText(context, context.getString(R.string.feature_fake), Toast.LENGTH_SHORT).show()}
                    )
                    Text(
                        text = stringResource(id = R.string.send),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_change),
                        contentDescription = stringResource(id = R.string.swap),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(50))
                            .clickable {Toast.makeText(context, context.getString(R.string.feature_fake), Toast.LENGTH_SHORT).show()}
                    )
                    Text(
                        text = stringResource(id = R.string.swap),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

        }
    }
}

