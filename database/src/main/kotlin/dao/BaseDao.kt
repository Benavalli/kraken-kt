package com.benavalli.kraken.dao

import com.benavalli.kraken.table.AuditTable
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant

abstract class BaseDao<T : Any>(
    private val table: Table,
    private val idColumn: Column<Int>
) : KoinComponent {

    private val dispatcher: CoroutineDispatcher by inject()

    suspend fun insert(value: T, insertStatement: T.(InsertStatement<Number>) -> Unit): Int =
        dbQuery {
            table.insert {
                value.insertStatement(it)
            } get idColumn
        }

    suspend fun delete(id: Int): Int =
        dbQuery {
            table.deleteWhere { idColumn eq id }
        }

    suspend fun update(id: Int, funcUpdateStatement: UpdateStatement.() -> Unit): Int =
        dbQuery {
            table.update({ idColumn eq id }) { updateStatement ->
                updateStatement.apply {
                    if (table is AuditTable) {
                        updateStatement[table.updatedAt] = Instant.now()
                    }
                }
                funcUpdateStatement(updateStatement)
            }
        }

    open suspend fun getById(id: Int, rowMapper: (ResultRow) -> T?): T? =
        dbQuery {
            table.selectAll()
                .where(predicate = idColumn eq id)
                .mapNotNull(rowMapper)
                .singleOrNull()
        }

    open suspend fun getAll(rowMapper: (ResultRow) -> T): List<T> =
        dbQuery { table.selectAll().map(rowMapper) }

    suspend fun <R> dbQuery(block: suspend () -> R) =
        newSuspendedTransaction(dispatcher) { block() }
}
