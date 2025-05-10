import org.example.logic.models.State
import org.example.logic.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi

typealias CsvLine = String

@OptIn(ExperimentalUuidApi::class)
fun List<CsvLine>.toStates(): List<State> {
    if (this.size <= 1) return emptyList()

    return this
        .drop(1)
        .filter { it.isNotBlank() }
        .map { line ->
            line
                .split(",")
                .map { it.trim() }
                .takeIf { it.size == 2 }
                ?.let { parts ->
                    State(
                        id = parts[0].toUuid(),
                        title = parts[1],
                    )
                } ?: throw IllegalArgumentException("CSV line doesn't have exactly 2 fields: $line")
        }
}

@OptIn(ExperimentalUuidApi::class)
fun List<State>.toCsvLines(): List<CsvLine> {
    val header = "id,title"
    val dataLines =
        this.map { state ->
            if (state.title.contains(",")) throw IllegalArgumentException("CSV fields cannot contain comma")
            listOf(
                state.id.toHexString(),
                state.title,
            ).joinToString(",")
        }

    return listOf(header) + dataLines
}
