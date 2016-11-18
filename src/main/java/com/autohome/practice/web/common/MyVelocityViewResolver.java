package com.autohome.practice.web.common;

import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shizhizhong
 * Date: 14-1-15
 * Time: 上午11:04
 * To change this template use File | Settings | File Templates.
 */
public class MyVelocityViewResolver extends VelocityViewResolver {


    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        AbstractUrlBasedView view = super.buildView(viewName);
        mergeContext(view);
        return view;
    }

    private void mergeContext(AbstractUrlBasedView view) {
        if (velocityUrl != null) {
            view.setAttributesMap(velocityUrl);
        }
        if (velocityTools != null) {
            view.setAttributesMap(velocityTools);
        }

    }

    @Resource
    private Map<String, Object> velocityTools;
    @Resource
    private Map<String, Object> velocityUrl;
}
