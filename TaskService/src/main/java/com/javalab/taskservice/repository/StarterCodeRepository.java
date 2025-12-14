package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.request.StarterCodeRequestDto;
import com.javalab.taskservice.tables.records.StarterCodeRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.javalab.taskservice.Tables.STARTER_CODE;

@Repository
@RequiredArgsConstructor
public class StarterCodeRepository
{
    private final DSLContext dsl;

    @Transactional
    public StarterCodeRecord createStarterCode(Long taskId, StarterCodeRequestDto starterCode)
    {
        var insert = dsl.insertInto(STARTER_CODE)
                .set(STARTER_CODE.ID_TASK, taskId);

        if (starterCode != null)
            insert.set(STARTER_CODE.CODE, starterCode.code())
                    .set(STARTER_CODE.IS_DEFAULT, starterCode.isDefault());

        return insert.returning().fetchOne();
    }
}