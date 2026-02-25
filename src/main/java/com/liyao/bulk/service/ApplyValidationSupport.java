package com.liyao.bulk.service;

import com.liyao.bulk.common.BusinessException;
import com.liyao.bulk.mapper.PlatformUserMapper;
import java.util.function.ToIntFunction;

public final class ApplyValidationSupport {

    private ApplyValidationSupport() {
    }

    public static void ensureOperCodeAvailable(PlatformUserMapper platformUserMapper,
                                               ToIntFunction<String> pendingCounter,
                                               String operCode) {
        if (platformUserMapper.selectByOperCode(operCode) != null) {
            throw new BusinessException("用户名已经存在，请查证后重新录入。");
        }
        if (pendingCounter.applyAsInt(operCode) > 0) {
            throw new BusinessException("用户名已经存在，请查证后重新录入。");
        }
    }
}
