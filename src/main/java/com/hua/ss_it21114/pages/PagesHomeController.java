package com.hua.ss_it21114.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages")
public class PagesHomeController {

    @GetMapping("/login")
    public String loginPage() {
        return "pages/login";
    }

    @GetMapping("/volunteer")
    public String volunteerHome() {
        return "redirect:/pages/volunteer/my-signups";
    }

    @GetMapping("/organization")
    public String organizationHome() {
        return "redirect:/pages/organization/events";
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "redirect:/pages/admin/pending";
    }

}
