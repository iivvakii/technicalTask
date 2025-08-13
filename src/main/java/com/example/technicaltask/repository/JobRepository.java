package com.example.technicaltask.repository;

import com.example.technicaltask.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job,Long> {
    Optional<Job> findByJobUrl(String jobUrl);
}
