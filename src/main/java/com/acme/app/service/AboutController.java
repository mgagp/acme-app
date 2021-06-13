package com.acme.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AboutController {

    @Autowired
    private AboutService aboutService;

    @GetMapping(value = "/about", produces = "application/json")
    public ResponseEntity<About> about() {
        return new ResponseEntity<About>(this.aboutService.about(), HttpStatus.OK);
    }

}
