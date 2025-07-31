package com.negi.survey.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.negi.survey.R
import com.negi.survey.model.*
import com.negi.survey.vm.SurveyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    vm: SurveyViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val visited by vm.visited.collectAsState()
    val answers by vm.answers.collectAsState()
    val qmap = vm.graph.questions
    val ok = vm.allVisitedAnswered()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_summary)) }
            )
        },
        bottomBar = {
            BottomAppBar {
                BackButton(onClick = onBack)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onFinish,
                    enabled = ok,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(stringResource(R.string.action_submit))
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            visited.forEach { qid ->
                val q = qmap[qid] ?: return@forEach
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(q.titleRes),
                            style = MaterialTheme.typography.titleMedium
                        )
                        AnswerText(spec = q, value = answers[qid])
                    }
                }
            }
            if (!ok) {
                Text(
                    text = stringResource(R.string.msg_required),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AnswerText(
    spec: QuestionSpec,
    value: String?
) {
    val empty = stringResource(R.string.answer_empty)
    if (value.isNullOrBlank()) {
        Text(empty)
        return
    }

    when (spec) {
        is FreeSpec -> Text(value)
        is SingleSpec -> {
            val label = spec.options
                .firstOrNull { it.key == value }
                ?.let { stringResource(it.labelRes) }
                ?: value
            Text(label)
        }
        is YesNoSpec -> {
            val label = when (value) {
                spec.yesKey -> stringResource(spec.yesLabelRes)
                spec.noKey  -> stringResource(spec.noLabelRes)
                else        -> value
            }
            Text(label)
        }
        is SingleBranchSpec -> {
            val label = spec.options
                .firstOrNull { it.key == value }
                ?.let { stringResource(it.labelRes) }
                ?: value
            Text(label)
        }
        is MultiQueueSpec -> {
            val labels = value.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { key -> spec.options.firstOrNull { it.key == key }?.labelRes }
                .map { res -> stringResource(res) }
                .joinToString(", ")
            Text(if (labels.isBlank()) empty else labels)
        }
    }
}
