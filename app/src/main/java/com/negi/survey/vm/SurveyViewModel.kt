/*
 * vm/SurveyViewModel.kt
 *
 * 質問フロー全体を司る ViewModel。
 *   ① 回答データ (answers)            : Map<qid, String?>
 *   ② 訪問ログ   (visited)            : List<qid>
 *   ③ サブフロー用キュー (pendingQueue): ArrayDeque<qid>
 *
 * すべて SavedStateHandle にバックアップされるため、
 * プロセスキル後も画面が復元されます。
 *
 * `decideNext()` が “次に遷移すべき設問 ID” を返すコアロジックです。
 */
package com.negi.survey.vm

// ──────────────────────────────────
//  AndroidX
// ──────────────────────────────────
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// ──────────────────────────────────
//  Kotlin Flow
// ──────────────────────────────────
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ──────────────────────────────────
//  質問モデル
// ──────────────────────────────────
import com.negi.survey.model.*
import java.util.ArrayDeque

/* -------------------------------------------------------------
 *  ViewModel 本体
 * ---------------------------------------------------------- */
class SurveyViewModel(
    private val state: SavedStateHandle
) : ViewModel() {

    /* ---------- 質問グラフ ----------
       必要に応じて buildGraph() を差し替える */
    // val graph: SurveyGraph = buildGraph()
    // val graph: SurveyGraph = buildVoiceTestGraph()
    // val graph: SurveyGraph = buildVideoTestGraph()
    // val graph: SurveyGraph = buildCameraTestGraph()

    val graph: SurveyGraph = buildAllComponentsTestGraph()

    /* ---------- SavedState keys ---------- */
    private val KEY_ANS     = "answers"
    private val KEY_QUEUE   = "queue"
    private val KEY_VISITED = "visited"

    /* ---------- 回答 Map<qid, String?> ---------- */
    private val _answers = MutableStateFlow(
        state.get<Map<String, String?>>(KEY_ANS) ?: emptyMap()
    )
    val answers: StateFlow<Map<String, String?>> = _answers.asStateFlow()

    /* ---------- サブフロー用キュー ---------- */
    private var pendingQueue = ArrayDeque(
        state.get<List<String>>(KEY_QUEUE) ?: emptyList()
    )
    private val _queueState = MutableStateFlow(pendingQueue.toList())
    val queueState: StateFlow<List<String>> = _queueState.asStateFlow()

    /* ---------- 訪問ログ ---------- */
    private val _visited = MutableStateFlow(
        state.get<List<String>>(KEY_VISITED) ?: emptyList()
    )
    val visited: StateFlow<List<String>> = _visited.asStateFlow()

    /* ---------- 初期化: StateFlow -> SavedStateHandle ---------- */
    init {
        viewModelScope.launch { answers    .collect { state[KEY_ANS]     = it } }
        viewModelScope.launch { queueState .collect { state[KEY_QUEUE]   = it } }
        viewModelScope.launch { visited    .collect { state[KEY_VISITED] = it } }
    }

    /* ---------------------------------------------------------
     *  1) 公開 API
     * ------------------------------------------------------ */

    /** 回答をセット（FREE は文字列、選択系は key を保存） */
    fun setAnswer(qid: String, value: String?) {
        _answers.update { it + (qid to value) }
    }

    /** 設問を訪問済みに追加（直前と重複する場合は無視） */
    fun markVisited(qid: String) {
        if (_visited.value.lastOrNull() != qid)
            _visited.update { it + qid }
    }

    /** すべての訪問済み設問が isValid を満たすか */
    fun allVisitedAnswered(): Boolean =
        visited.value.all { id ->
            graph.questions[id]?.isValid(answers.value[id]) == true
        }

    /** 状態を完全リセット */
    fun resetAll() {
        _answers.value = emptyMap()
        pendingQueue.clear(); _queueState.value = emptyList()
        _visited.value = emptyList()
    }

    /** 未回答の最初の設問 ID（無ければ startId） */
    fun getFirstUnanswered(): String =
        visited.value.firstOrNull { answers.value[it].isNullOrBlank() }
            ?: graph.startId

    /* ---------------------------------------------------------
     *  2) “次へ” 判定ロジック
     * ------------------------------------------------------ */

    /**
     * 現在 qid の回答確定後に進むべき設問 ID を返す。
     * - null の場合、画面側で Summary へ遷移する。
     */
    fun decideNext(qid: String): String? {
        val spec = graph.questions[qid] ?: return null
        val ans  = answers.value[qid]

        /* ----- 分岐タイプごとの処理 ----- */
        when (spec) {
            is YesNoSpec -> {
                return when (ans) {
                    spec.yesKey -> spec.nextIdIfYes
                    spec.noKey  -> spec.nextIdIfNo
                    else        -> null
                } ?: spec.fallback()
            }

            is SingleBranchSpec -> {
                return spec.nextIdByKey[ans] ?: spec.fallback()
            }

            is MultiQueueSpec -> {
                // 選択された key をセットに変換
                val selected = parseSelectedSet(ans)

                // 1) 未選択なら fallbackNextId / nextId / queue
                if (selected.isEmpty()) {
                    return spec.fallbackNextId ?: spec.nextId ?: spec.fallback()
                }

                // 2) 優先順（指定なし→options 順）でサブフローを enqueue
                val order = spec.priority ?: spec.options.map { it.key }
                val targets = order
                    .filter { it in selected }
                    .mapNotNull { spec.subflowStartIdByKey[it] }

                enqueueAll(targets)
                return spec.fallback()
            }

            // SingleSpec / FreeSpec などは直列 nextId のみ
            else -> return spec.fallback()
        }
    }

    /* ---------------------------------------------------------
     *  3) 内部ユーティリティ
     * ------------------------------------------------------ */

    /** 文字列 "a,b,c" → Set<key> へ変換 */
    private fun parseSelectedSet(raw: String?): Set<String> =
        raw.orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

    /** キューに追加 */
    private fun enqueueAll(ids: List<String>) {
        ids.filter { it.isNotBlank() }.forEach { pendingQueue.addLast(it) }
        _queueState.value = pendingQueue.toList()
    }

    /** キュー先頭をポップ（無ければ null） */
    private fun popQueue(): String? {
        val next = if (pendingQueue.isEmpty()) null else pendingQueue.removeFirst()
        _queueState.value = pendingQueue.toList()
        return next
    }

    /** “分岐で決まらなかったとき” の共通フォールバック */
    private fun QuestionSpec.fallback(): String? =
        popQueue() ?: this.nextId
}
