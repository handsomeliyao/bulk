package com.liyao.bulk.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdminPermissionResponse {
    @Schema(description = "授权范围")
    private List<ScopeItem> authScopes;
    @Schema(description = "操作范围")
    private List<ScopeItem> operScopes;
}
