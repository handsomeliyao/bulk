package com.liyao.bulk.mapper;

import com.liyao.bulk.model.SysButton;
import com.liyao.bulk.model.SysMenu;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper {

    List<SysMenu> selectAllMenus();

    List<SysButton> selectAllButtons();
}
