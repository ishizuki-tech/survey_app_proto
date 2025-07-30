package com.negi.survey.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.negi.survey.model.*
import com.negi.survey.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    spec: QuestionSpec,
    answer: String,                 // 保存は key（Single/YesNo/Branch/MultiQueue）または自由入力文字列（Free）
    onAnswer: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onBranchToId: (String) -> Unit, // YES/NO と SingleBranch の即時分岐で使用
) {
    val canNext = spec.isValid(answer)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = spec.titleRes)) })
        },
        bottomBar = {
            BottomAppBar {
                OutlinedButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                Spacer(Modifier.weight(1f))
                Button(onClick = onNext, enabled = canNext) {
                    Text(stringResource(R.string.action_next))
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuestionContent(
                spec = spec,
                answer = answer,
                onAnswer = onAnswer,
                onBranchToId = onBranchToId
            )

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
                // placeholder を使うなら strings.xml に用意して stringResource を参照してください
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
                        Text(text = stringResource(id = opt.labelRes))
                    }
                }
            }
        }

        is YesNoSpec -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    onAnswer(spec.yesKey)
                    onBranchToId(spec.nextIdIfYes)  // 即時分岐
                }) {
                    Text(stringResource(spec.yesLabelRes))
                }

                OutlinedButton(onClick = {
                    onAnswer(spec.noKey)
                    onBranchToId(spec.nextIdIfNo)   // 即時分岐
                }) {
                    Text(stringResource(spec.noLabelRes))
                }
            }
        }

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
                                onClick = {
                                    onAnswer(opt.key)
                                    spec.nextIdByKey[opt.key]?.let(onBranchToId) // 即時分岐
                                }
                            )
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                onAnswer(opt.key)
                                spec.nextIdByKey[opt.key]?.let(onBranchToId)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(id = opt.labelRes))
                    }
                }
            }
        }

        is MultiQueueSpec -> {
            // 簡易実装：複数選択は "a,b,c" のカンマ区切りで保存
            val selected = remember(answer) {
                answer.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toMutableSet()
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
                        Text(text = stringResource(id = opt.labelRes))
                    }
                }
            }
            // 分岐は「次へ」で確定（即時遷移は行わない）
        }
    }
}
