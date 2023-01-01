package com.carrot.croquis.model.entity

import com.carrot.croquis.model.enums.EntityState
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.MappedSuperclass
import java.time.Instant

@MappedSuperclass
open class Auditable {

    @Column
    @Enumerated(EnumType.STRING)
    var state: EntityState = EntityState.ACTIVE

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()

    @Column(name = "created_by")
    var createdBy: Long? = null

    @Column(name = "last_modified_at")
    var lastModifiedAt: Instant = Instant.now()

    @Column(name = "last_modified_by")
    var lastModifiedBy: Long? = null
}