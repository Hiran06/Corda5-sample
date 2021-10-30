package net.corda.samples.iou.schema

import net.corda.v5.application.services.persistence.MappedSchema
import net.corda.v5.application.services.persistence.UUIDConverter
import net.corda.v5.ledger.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for IOUState.
 */
object IOUSchema

/**
 * An IOUState schema.
 */
object IOUSchemaV1 : MappedSchema(
    schemaFamily = IOUSchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentIOU::class.java)
) {

    override val migrationResource: String?
        get() = "iou.changelog-master"

    @Entity
    @Table(name = "iou_states")
    class PersistentIOU(
        @Column(name = "lender")
        var lenderName: String,

        @Column(name = "borrower")
        var borrowerName: String,

        @Column(name = "value")
        var value: Int,

        @Column(name = "linear_id")
        @Convert(converter = UUIDConverter::class)
        var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor() : this("", "", 0, UUID.randomUUID())
    }
}
