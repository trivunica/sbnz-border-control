package com.ftn.sbnz.model.models;

import java.util.ArrayList;
import java.util.List;

public class DecisionTreeBuilder {

    public static DecisionNode buildRiskTree(
            String plate, String licNumber, String company,
            int totalScore, List<RiskFactor> factors) {

        DecisionNode root = new DecisionNode(
                "root", "DECISION",
                "Procena rizika: " + levelLabel(totalScore),
                "Ukupan skor: " + totalScore + "/125",
                totalScore, new ArrayList<>()
        );

        DecisionNode vehicleNode  = entityNode("vehicle_" + plate,  "Vozilo: " + plate);
        DecisionNode driverNode   = entityNode("driver_"  + licNumber, "Vozač: " + licNumber);
        DecisionNode companyNode  = entityNode("company_" + company, "Kompanija: " + company);

        for (RiskFactor f : factors) {
            DecisionNode child = new DecisionNode(
                    "factor_" + f.getProofStep(), "RISK_FACTOR",
                    f.getDescription(), f.getProofStep(),
                    f.getScore(), new ArrayList<>()
            );
            if (f.getEntityId().equals(plate))        vehicleNode.getChildren().add(child);
            else if (f.getEntityId().equals(licNumber)) driverNode.getChildren().add(child);
            else                                        companyNode.getChildren().add(child);
        }

        if (!vehicleNode.getChildren().isEmpty())  root.getChildren().add(vehicleNode);
        if (!driverNode.getChildren().isEmpty())   root.getChildren().add(driverNode);
        if (!companyNode.getChildren().isEmpty())  root.getChildren().add(companyNode);

        return root;
    }

    private static DecisionNode entityNode(String id, String label) {
        return new DecisionNode(id, "ENTITY", label, null, 0, new ArrayList<>());
    }

    private static String levelLabel(int score) {
        return score >= 60 ? "VISOK" : score >= 30 ? "SREDNJI" : "NIZAK";
    }
}