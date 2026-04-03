package com.javalab.taskservice.repository;

import com.javalab.taskservice.dto.request.StarterCodeRequestDto;
import com.javalab.taskservice.dto.response.StarterCodeResponseDto;
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

    @Transactional(readOnly = true)
    public StarterCodeResponseDto getStarterCode(Long taskId)
    {
        return dsl.select(
                        STARTER_CODE.ID_CODE,
                        STARTER_CODE.CODE,
                        STARTER_CODE.IS_DEFAULT
                )
                .from(STARTER_CODE)
                .where(STARTER_CODE.ID_TASK.eq(taskId))
                .fetchOneInto(StarterCodeResponseDto.class);
    }

    @Transactional
    public StarterCodeResponseDto createStarterCodeForTask(
            Long taskId, StarterCodeRequestDto starterCode
    )
    {
        var insert = dsl.insertInto(STARTER_CODE)
                .set(STARTER_CODE.ID_TASK, taskId);

        if (starterCode != null && starterCode.code() != null && !starterCode.code().isEmpty())
            insert.set(STARTER_CODE.CODE, starterCode.code())
                    .set(STARTER_CODE.IS_DEFAULT, starterCode.isDefault());

        return insert.returningResult(
                        STARTER_CODE.ID_CODE,
                        STARTER_CODE.CODE,
                        STARTER_CODE.IS_DEFAULT
                )
                .fetchOneInto(StarterCodeResponseDto.class);
    }

    @Transactional
    public StarterCodeResponseDto updateStarterCode(Long taskId, StarterCodeRequestDto requestDto)
    {
        return dsl.update(STARTER_CODE)
                .set(STARTER_CODE.CODE, requestDto.code())
                .where(STARTER_CODE.ID_TASK.eq(taskId))
                .returningResult(
                        STARTER_CODE.ID_CODE,
                        STARTER_CODE.CODE,
                        STARTER_CODE.IS_DEFAULT
                )
                .fetchOneInto(StarterCodeResponseDto.class);
    }
}