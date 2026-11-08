package dev.zwander.installwithoptions.ui.views

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import dev.zwander.installwithoptions.R
import dev.zwander.installwithoptions.util.handleIncomingUris

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedFilesScreen(
    selectedFiles: Map<String, List<DocumentFile>>,
    onUpdatedFiles: (Map<String, List<DocumentFile>>) -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val fileSelector =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            context.handleIncomingUris(uris)
        }

    fun launchFileSelector() {
        try {
            fileSelector.launch(arrayOf("*/*"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.error_selecting_files, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.selected_files))
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_24px),
                            contentDescription = stringResource(id = R.string.close),
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onUpdatedFiles(mapOf()) },
                        enabled = selectedFiles.isNotEmpty(),
                    ) {
                        Text(text = stringResource(id = R.string.clear_all))
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = BottomAppBarDefaults.ContainerElevation,
                color = BottomAppBarDefaults.containerColor,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { launchFileSelector() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(id = R.string.add_files))
                    }

                    OutlinedButton(
                        onClick = onInstall,
                        enabled = selectedFiles.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(id = R.string.install))
                    }
                }
            }
        },
    ) { paddingValues ->
        if (selectedFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.no_files_selected),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = selectedFiles.entries.toList(),
                    key = { "${it.key}:${it.value.size}" },
                ) { entry ->
                    val packageName = entry.key
                    val files = entry.value

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = packageName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                    )

                                    Text(
                                        text = if (files.size > 1) {
                                            stringResource(id = R.string.split_apks_count, files.size)
                                        } else {
                                            stringResource(id = R.string.single_apk)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onUpdatedFiles(selectedFiles - packageName)
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.delete_24px),
                                        contentDescription = stringResource(id = R.string.remove),
                                    )
                                }
                            }

                            HorizontalDivider()

                            files.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 2.dp, bottom = 2.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = file.name ?: file.uri.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                    )

                                    IconButton(
                                        onClick = {
                                            onUpdatedFiles(
                                                selectedFiles.toMutableMap().apply {
                                                    val newList = this[packageName]?.minus(file)

                                                    if (newList.isNullOrEmpty()) {
                                                        remove(packageName)
                                                    } else {
                                                        this[packageName] = newList
                                                    }
                                                },
                                            )
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.delete_24px),
                                            contentDescription = stringResource(id = R.string.remove),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }
}
