package com.ytdlp.videodownloader.controller;

import java.time.Year;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Render the home page.
     *
     * @param model the model to which the year is added
     * @return the name of the view to render
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("year", Year.now().getValue());
        return "index";
    }

}