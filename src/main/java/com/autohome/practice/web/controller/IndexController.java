package com.autohome.practice.web.controller;

import com.autohome.practice.web.util.TestClass;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by smilepy on 2016/11/15.
 */
@Controller
public class IndexController {
    @RequestMapping(value = "/")
    public ModelAndView getUser(){
        ModelAndView mv = new ModelAndView("/vm2/index");
        String environment= TestClass.Environment;
        mv.addObject("environment",environment);
        return  mv;
    }
}
