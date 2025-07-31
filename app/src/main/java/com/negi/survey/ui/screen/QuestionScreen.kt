package com.negi.survey.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.negi.survey.R
import com.negi.survey.model.*

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.padding(8.dp)
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.action_back))
    }
}

@Composable
fun NextButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.padding(8.dp)
    ) {
        Text(stringResource(R.string.action_next))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    spec: QuestionSpec,
    answer: String,
    onAnswer: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onBranchToId: (String) -> Unit
) {
    val canNext = spec.isValid(answer)

    // オーバーレイ（テキスト可読性向上用）
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.75f))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(spec.titleRes)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.Transparent) {
                BackButton(onClick = onBack)
                Spacer(Modifier.weight(1f))
                NextButton(enabled = canNext, onClick = onNext)
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            QuestionContent(spec, answer, onAnswer, onBranchToId)

            if (!canNext && spec.required) {
                Text(
                    text = stringResource(R.string.msg_required),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun QuestionContent(
    spec: QuestionSpec,
    answer: String,
    onAnswer: (String) -> Unit,
    onBranchToId: (String) -> Unit
) {
    when (spec) {
        is FreeSpec -> {
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswer,
                modifier = Modifier.fillMaxWidth(),
                singleLine = spec.singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = spec.keyboardType)
            )
        }

        is SingleSpec -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                spec.options.forEach { opt ->
                    val selected = answer == opt.key
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { onAnswer(opt.key) }
                            )
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { onAnswer(opt.key) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(opt.labelRes))
                    }
                }
            }
        }
        is YesNoSpec -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selectedKey = answer

                Button(
                    onClick = { onAnswer(spec.yesKey) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedKey == spec.yesKey)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedKey == spec.yesKey)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(spec.yesLabelRes))
                }

                Button(
                    onClick = { onAnswer(spec.noKey) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedKey == spec.noKey)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedKey == spec.noKey)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(spec.noLabelRes))
                }
            }
        }

//        is YesNoSpec -> {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                val selectedKey = answer
//
//                Button(
//                    onClick = {
//                        onAnswer(spec.yesKey)
//                        onBranchToId(spec.nextIdIfYes)
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (selectedKey == spec.yesKey)
//                            MaterialTheme.colorScheme.primary
//                        else
//                            MaterialTheme.colorScheme.surfaceVariant,
//                        contentColor = if (selectedKey == spec.yesKey)
//                            MaterialTheme.colorScheme.onPrimary
//                        else
//                            MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                ) {
//                    Text(stringResource(spec.yesLabelRes))
//                }
//
//                Button(
//                    onClick = {
//                        onAnswer(spec.noKey)
//                        onBranchToId(spec.nextIdIfNo)
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (selectedKey == spec.noKey)
//                            MaterialTheme.colorScheme.primary
//                        else
//                            MaterialTheme.colorScheme.surfaceVariant,
//                        contentColor = if (selectedKey == spec.noKey)
//                            MaterialTheme.colorScheme.onPrimary
//                        else
//                            MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                ) {
//                    Text(stringResource(spec.noLabelRes))
//                }
//            }
//        }
//        is YesNoSpec -> {
//            // 選択状態を answer に保存するだけ
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Button(onClick = { onAnswer(spec.yesKey) }) {
//                    Text(stringResource(spec.yesLabelRes))
//                }
//
//                OutlinedButton(onClick = { onAnswer(spec.noKey) }) {
//                    Text(stringResource(spec.noLabelRes))
//                }
//            }
//        }

//        is YesNoSpec -> {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Button(onClick = {
//                    onAnswer(spec.yesKey)
//                    onBranchToId(spec.nextIdIfYes)
//                }) {
//                    Text(stringResource(spec.yesLabelRes))
//                }
//
//                OutlinedButton(onClick = {
//                    onAnswer(spec.noKey)
//                    onBranchToId(spec.nextIdIfNo)
//                }) {
//                    Text(stringResource(spec.noLabelRes))
//                }
//            }
//        }

        is SingleBranchSpec -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                spec.options.forEach { opt ->
                    val selected = answer == opt.key
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { onAnswer(opt.key) }
                            )
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { onAnswer(opt.key) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(opt.labelRes))
                    }
                }
            }
        }
//        is SingleBranchSpec -> {
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                spec.options.forEach { opt ->
//                    val selected = answer == opt.key
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .selectable(
//                                selected = selected,
//                                onClick = {
//                                    onAnswer(opt.key)
//                                    spec.nextIdByKey[opt.key]?.let(onBranchToId)
//                                }
//                            )
//                            .padding(4.dp)
//                    ) {
//                        RadioButton(
//                            selected = selected,
//                            onClick = {
//                                onAnswer(opt.key)
//                                spec.nextIdByKey[opt.key]?.let(onBranchToId)
//                            }
//                        )
//                        Spacer(Modifier.width(8.dp))
//                        Text(stringResource(opt.labelRes))
//                    }
//                }
//            }
//        }

        is MultiQueueSpec -> {
            val selected = remember(answer) {
                answer.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                spec.options.forEach { opt ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = opt.key in selected,
                            onCheckedChange = { checked ->
                                if (checked) selected.add(opt.key) else selected.remove(opt.key)
                                onAnswer(selected.joinToString(","))
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(opt.labelRes))
                    }
                }
            }
        }
    }
}
