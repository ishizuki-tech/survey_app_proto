package com.negi.survey.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negi.survey.model.MultiQueueSpec
import com.negi.survey.model.QuestionSpec
import com.negi.survey.model.SingleBranchSpec
import com.negi.survey.model.SingleSpec
import com.negi.survey.model.SurveyGraph
import com.negi.survey.model.YesNoSpec
import com.negi.survey.model.buildCameraTestGraph
import com.negi.survey.model.buildGraph
import com.negi.survey.model.buildVideoTestGraph
import com.negi.survey.model.buildVoiceTestGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.ArrayDeque

/**
 * SurveyViewModel
 *
 * - 回答: Map<qid, String?>（FREE は自由入力、選択系は **key** を保存）
 * - 訪問ログ: List<qid>（サマリー表示順/送信可否判定に使用）
 * - サブフロー・キュー: 複数選択（MultiQueueSpec）で選ばれた key に対応する
 *   サブフロー開始 qid を優先順で enqueue → 順に消化
 */
class SurveyViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** 質問グラフ（必要に応じて buildGraph() を差し替え） */
    //    val graph: SurveyGraph = buildGraph()
    //    val graph: SurveyGraph = buildVoiceTestGraph()
    //    val graph: SurveyGraph = buildVoiceTestGraph()
    val graph: SurveyGraph = buildCameraTestGraph()
    // ---- 永続化キー ----
    private val keyAnswers = "answers"
    private val keyQueue = "queue"
    private val keyVisited = "visited"

    // ---- 回答状態 ----
    private val _answers = MutableStateFlow(
        // Map<qid, String?> を保存/復元
        savedStateHandle.get<Map<String, String?>>(keyAnswers) ?: emptyMap()
    )
    val answers: StateFlow<Map<String, String?>> = _answers.asStateFlow()

    // ---- サブフロー・キュー（List にシリアライズして保存/復元） ----
    private var pendingQueue: ArrayDeque<String> = ArrayDeque(
        savedStateHandle.get<List<String>>(keyQueue) ?: emptyList()
    )
    private val _queueState = MutableStateFlow(pendingQueue.toList())
    val queueState: StateFlow<List<String>> = _queueState.asStateFlow()

    // ---- 訪問ログ（通過順に記録） ----
    private val _visited = MutableStateFlow(
        savedStateHandle.get<List<String>>(keyVisited) ?: emptyList()
    )
    val visited: StateFlow<List<String>> = _visited.asStateFlow()

    init {
        // プロセスキル対策：変更を SavedStateHandle にバックアップ
        viewModelScope.launch { answers.collect { savedStateHandle[keyAnswers] = it } }
        viewModelScope.launch { queueState.collect { savedStateHandle[keyQueue] = it } }
        viewModelScope.launch { visited.collect { savedStateHandle[keyVisited] = it } }
    }

    /** 回答の設定（FREE: そのまま文字列 / 選択系: key を保存） */
    fun setAnswer(qid: String, value: String?) {
        _answers.update { current ->
            current.toMutableMap().apply { this[qid] = value }
        }
    }

    /** 質問を訪問済みに記録（連続重複は抑止） */
    fun markVisited(qid: String) {
        if (_visited.value.lastOrNull() != qid) {
            _visited.update { it + qid }
        }
    }

    // ---------- キュー操作ユーティリティ ----------

    private fun enqueueAll(ids: List<String>) {
        ids.forEach { if (it.isNotBlank()) pendingQueue.addLast(it) }
        _queueState.value = pendingQueue.toList()
    }

    private fun popNextFromQueue(): String? {
        val next = if (pendingQueue.isEmpty()) null else pendingQueue.removeFirst()
        _queueState.value = pendingQueue.toList()
        return next
    }

    // ---------- 回答ヘルパー ----------

    /** "a,b,c" を Set<key> に変換（空/空白は除去） */
    private fun parseSelected(answer: String?): Set<String> =
        (answer ?: "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

    // ---------- 遷移判定の本体 ----------

    /**
     * 現在の qid の回答が確定したあとに「次に進むべき qid」を返す。
     * null を返した場合、呼び出し側はサマリーへ遷移する。
     *
     * 決定順序:
     *  1) Yes/No: yesKey / noKey に応じた nextId
     *  2) SingleBranch: answer key -> nextId
     *  3) MultiQueue:
     *      - 選択 key の優先順（priority or options 順）でサブフロー開始IDを enqueue
     *      - enqueue 直後にキュー最優先を pop して遷移
     *      - 未選択なら fallbackNextId / nextId / (キュー)
     *  4) キューが残っていれば pop
     *  5) 直列 nextId
     *  6) どれも無ければ null
     */
    fun decideNext(qid: String): String? {
        val spec: QuestionSpec = graph.questions[qid] ?: return null
        val ans: String? = answers.value[qid]

        when (spec) {
            is YesNoSpec -> {
                val next = when (ans) {
                    spec.yesKey -> spec.nextIdIfYes
                    spec.noKey  -> spec.nextIdIfNo
                    else        -> null
                }
                if (next != null) return next
            }

            is SingleBranchSpec -> {
                val next = spec.nextIdByKey[ans]
                if (next != null) return next
            }

            is MultiQueueSpec -> {
                val selectedKeys = parseSelected(ans)
                if (selectedKeys.isEmpty()) {
                    // 未選択 → フォールバック / 直列 / キュー残
                    return spec.fallbackNextId ?: spec.nextId ?: popNextFromQueue()
                }

                // 優先順を決定（未指定なら options の key 順）
                val order: List<String> = spec.priority ?: spec.options.map { it.key }

                // 選択された key のうち、優先順に従ってサブフロー開始IDを積む
                val targets: List<String> = order
                    .filter { it in selectedKeys }
                    .mapNotNull { key -> spec.subflowStartIdByKey[key] }

                if (targets.isNotEmpty()) {
                    enqueueAll(targets)
                }

                // まずキューを消化
                return popNextFromQueue() ?: spec.nextId
            }

            is SingleSpec -> {
                // 分岐なしの単一選択はこの段では何もしない（後段の nextId/Queue に委ねる）
            }

            else -> {
                // FreeSpec 等：直列 nextId またはキュー/終端へ
            }
        }

        // 分岐で決まらない場合：キュー → 次ID → 終端(null)
        return popNextFromQueue() ?: spec.nextId
    }

    /**
     * 送信可否の例：訪問済みの設問が全て isValid を満たしているか。
     * （分岐でスキップされた設問は visited に入らない想定。）
     */
    fun allVisitedAnswered(): Boolean =
        visited.value.all { qid ->
            val q = graph.questions[qid] ?: return@all false
            q.isValid(answers.value[qid])
        }

    /** 全状態をまっさらにリセット */
    fun resetAll() {
        // 1) 回答をクリア
        _answers.value = emptyMap()
        // 2) キューを空にして StateFlow に反映
        pendingQueue.clear()
        _queueState.value = pendingQueue.toList()
        // 3) 訪問ログをクリア
        _visited.value = emptyList()
    }

    /**
     * 訪問済みの設問の中から、はじめに未回答（blank）のものを返す。
     * 何もなければ startId を返す。
     */
    fun getFirstUnanswered(): String {
        // visited は StateFlow<List<String>>
        val v = visited.value
        val ans = answers.value
        val first = v.firstOrNull { key -> ans[key].isNullOrBlank() }
        return first ?: graph.startId
    }
}

