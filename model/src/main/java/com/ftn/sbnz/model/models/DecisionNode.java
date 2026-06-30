package com.ftn.sbnz.model.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecisionNode {
    private String nodeId;
    private String type;
    private String label;
    private String detail;
    private int score;
    private List<DecisionNode> children;
}