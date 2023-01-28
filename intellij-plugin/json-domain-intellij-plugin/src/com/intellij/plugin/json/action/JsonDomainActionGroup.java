package com.intellij.plugin.json.action;

import com.intellij.openapi.actionSystem.DefaultActionGroup;

/**
 * Created by vincent on 2016/5/17.
 */
public class JsonDomainActionGroup extends DefaultActionGroup {
    @Override
    public boolean hideIfNoVisibleChildren() {
        return true;
    }
}
