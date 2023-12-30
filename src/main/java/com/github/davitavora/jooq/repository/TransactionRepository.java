package com.github.davitavora.jooq.repository;

import static com.github.davitavora.jooq.util.JooqOperation.conditionIf;
import static com.github.davitavora.jooq.util.JooqOperation.nullableMapper;

import com.github.davitavora.jooq.model.projection.CategoryProjection;
import com.github.davitavora.jooq.model.projection.TransactionProjection;
import io.micrometer.common.util.StringUtils;
import io.vobiscum.jooqpoc.domain.Tables;
import io.vobiscum.jooqpoc.domain.tables.records.FinancialTransactionRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {

    private final DSLContext jooq;

    public List<TransactionProjection> search(String name,
                                              Integer categoryId,
                                              LocalDate createdAt) {
        return jooq
            .select(Tables.FINANCIAL_TRANSACTION.asterisk())
            .from(Tables.FINANCIAL_TRANSACTION)
            .where(
                conditionIf(Tables.FINANCIAL_TRANSACTION.NAME.likeIgnoreCase("%" + name + "%"), name, StringUtils::isNotBlank),
                conditionIf(Tables.FINANCIAL_TRANSACTION.CATEGORY_ID.eq(categoryId), categoryId, Objects::nonNull),
                conditionIf(Tables.FINANCIAL_TRANSACTION.CREATED_AT.eq(createdAt), createdAt, Objects::nonNull)
            )
            .fetchInto(TransactionProjection.class);
    }

    public Optional<TransactionProjection> findBy(Long id) {
        return jooq.select(
                Tables.FINANCIAL_TRANSACTION.asterisk().except(Tables.FINANCIAL_TRANSACTION.CATEGORY_ID),
                Tables.FINANCIAL_TRANSACTION.category()
                    .mapping(nullableMapper(CategoryProjection::new))
            )
            .from(Tables.FINANCIAL_TRANSACTION)
            .leftJoin(Tables.CATEGORY).on(Tables.CATEGORY.ID.eq(Tables.FINANCIAL_TRANSACTION.CATEGORY_ID))
            .where(
                Tables.FINANCIAL_TRANSACTION.ID.eq(id)
            ).fetchOptionalInto(TransactionProjection.class);
    }

    @Transactional
    public void save(FinancialTransactionRecord record) {
        jooq.executeInsert(record);
    }

    public void delete(Long id) {
        jooq.delete(Tables.FINANCIAL_TRANSACTION).where(Tables.FINANCIAL_TRANSACTION.ID.eq(id)).execute();
    }

}
