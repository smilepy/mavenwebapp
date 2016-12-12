package com.autohome.practice.web.controller;

import com.autohome.practice.web.util.TestClass;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Created by smilepy on 2016/11/15.
 */
@Controller
public class IndexController {
    public static Logger printLog = Logger.getLogger("print-log");
    @RequestMapping(value = "/")
    public ModelAndView getUser(){
        printLog.info("this is IndexController   "+new Date());
        ModelAndView mv = new ModelAndView("/vm2/index");
        String environment= TestClass.Environment;
        mv.addObject("environment",environment);
        return  mv;
    }
}
