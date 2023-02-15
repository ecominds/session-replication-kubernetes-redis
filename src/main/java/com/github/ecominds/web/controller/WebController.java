/**
 * @author	: Rajiv Kumar
 * @project	: boot-webapp
 * @since	: 1.0.0
 * @date	: 09-Feb-2023
 */
package com.github.ecominds.web.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class WebController {

    @RequestMapping(value = "/")
    public String index(HttpSession session) {
        Integer hits = (Integer) session.getAttribute("hits");
        if (hits == null) {
            hits = 0;
        }
        session.setAttribute("hits", ++hits);
        log.info("INDEX Access - SESSION ID:{} with total hits={}", session.getId(), hits);
        return "index";
    }
}