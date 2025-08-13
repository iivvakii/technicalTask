package com.example.technicaltask.controller;

import com.example.technicaltask.entity.JobFunctions;
import com.example.technicaltask.entity.JobFunctionsRequest;
import com.example.technicaltask.model.Job;
import com.example.technicaltask.service.JobService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jobs")
public class Controller {
    private final JobService jobService;

    @PostMapping
    public List<Job> getJobs(@RequestBody JobFunctionsRequest jobFunctions){
        return jobService.getJobs(jobFunctions.getJobFunctions());
    }
}
