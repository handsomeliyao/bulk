package com.liyao.bulk.service;

import com.liyao.bulk.dto.MenuTreeItem;
import com.liyao.bulk.mapper.PermissionMapper;
import com.liyao.bulk.model.SysMenu;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final PermissionMapper permissionMapper;

    public MenuService(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public List<MenuTreeItem> queryMenuTree() {
        List<SysMenu> menus = permissionMapper.selectAllMenus();

        Map<Long, MenuTreeItem> nodeMap = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            MenuTreeItem node = new MenuTreeItem();
            node.setId(menu.getId());
            node.setPid(menu.getPid());
            node.setMenuCode(menu.getMenuCode());
            node.setMenuName(menu.getMenuName());
            node.setMenuOrder(menu.getMenuOrder());
            node.setIcon(menu.getIcon());
            node.setUrl(menu.getUrl());
            node.setChildren(new ArrayList<>());
            nodeMap.put(menu.getId(), node);
        }

        List<MenuTreeItem> roots = new ArrayList<>();
        for (MenuTreeItem node : nodeMap.values()) {
            Long pid = node.getPid();
            if (pid == null || pid == 0L) {
                roots.add(node);
                continue;
            }
            MenuTreeItem parent = nodeMap.get(pid);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }

        sortTree(roots);
        return roots;
    }

    private void sortTree(List<MenuTreeItem> nodes) {
        nodes.sort(Comparator
                .comparing(MenuTreeItem::getMenuOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuTreeItem::getId));
        for (MenuTreeItem node : nodes) {
            sortTree(node.getChildren());
        }
    }
}
