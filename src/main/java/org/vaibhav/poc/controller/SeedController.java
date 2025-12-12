package org.vaibhav.poc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.vaibhav.poc.util.DataSeeder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seed")
public class SeedController {

    @Autowired
    private DataSeeder seeder;

    @PostMapping("/events")
    public String seedEvents(@RequestBody List<Map<String, Object>> events) throws Exception {
        seeder.seedEvents(events);
        return "Seeding completed successfully!";
    }
}
