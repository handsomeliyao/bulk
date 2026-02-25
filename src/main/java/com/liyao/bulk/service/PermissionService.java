package com.liyao.bulk.service;

import com.liyao.bulk.dto.PermissionButtonItem;
import com.liyao.bulk.dto.PermissionMenuTreeItem;
import com.liyao.bulk.mapper.PermissionMapper;
import com.liyao.bulk.model.SysButton;
import com.liyao.bulk.model.SysMenu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public List<PermissionMenuTreeItem> queryPermissionTree() {
        List<SysMenu> menus = permissionMapper.selectAllMenus();
        List<SysButton> buttons = permissionMapper.selectAllButtons();

        Map<Long, List<PermissionButtonItem>> buttonMap = buttons.stream()
                .map(this::toButtonItem)
                .collect(Collectors.groupingBy(
                        PermissionButtonItem::getMenuId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<Long, PermissionMenuTreeItem> nodeMap = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            PermissionMenuTreeItem node = new PermissionMenuTreeItem();
            node.setId(menu.getId());
            node.setPid(menu.getPid());
            node.setMenuCode(menu.getMenuCode());
            node.setMenuName(menu.getMenuName());
            node.setMenuOrder(menu.getMenuOrder());
            node.setIcon(menu.getIcon());
            node.setUrl(menu.getUrl());
            node.setButtons(buttonMap.getOrDefault(menu.getId(), Collections.emptyList()));
            node.setChildren(new ArrayList<>());
            nodeMap.put(menu.getId(), node);
        }

        List<PermissionMenuTreeItem> roots = new ArrayList<>();
        for (PermissionMenuTreeItem node : nodeMap.values()) {
            Long pid = node.getPid();
            if (pid == null || pid == 0L) {
                roots.add(node);
                continue;
            }
            PermissionMenuTreeItem parent = nodeMap.get(pid);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }

        removeButtonsFromNonLeaf(roots);
        sortTree(roots);
        return roots;
    }

    private void removeButtonsFromNonLeaf(List<PermissionMenuTreeItem> nodes) {
        for (PermissionMenuTreeItem node : nodes) {
            List<PermissionMenuTreeItem> children = node.getChildren();
            if (children != null && !children.isEmpty()) {
                node.setButtons(null);
                removeButtonsFromNonLeaf(children);
            }
        }
    }

    private void sortTree(List<PermissionMenuTreeItem> nodes) {
        nodes.sort(Comparator
                .comparing(PermissionMenuTreeItem::getMenuOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(PermissionMenuTreeItem::getId));
        for (PermissionMenuTreeItem node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private PermissionButtonItem toButtonItem(SysButton button) {
        PermissionButtonItem item = new PermissionButtonItem();
        item.setId(button.getId());
        item.setMenuId(button.getMenuId());
        item.setBtnCode(button.getBtnCode());
        item.setBtnName(button.getBtnName());
        item.setMethod(button.getMethod());
        item.setUri(button.getUri());
        return item;
    }
}
