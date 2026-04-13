package com.oagp.repository;

import com.oagp.model.ViolationNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViolationNodeRepository extends JpaRepository<ViolationNode, Long> {
}
