package ru.ilcorp.bcm.optimization.service;

import org.springframework.stereotype.Service;
import ru.ilcorp.bcm.optimization.model.dto.dtoRange;
import ru.ilcorp.bcm.optimization.model.dto.dtoRequestOptimization;
import ru.ilcorp.bcm.optimization.model.dto.dtoResult;

@Service
public class OptimizationServiceImpl implements OptimizationService{
    private static final double ALPHA = 1.3;
    private static final double EPSILON = 0.01;

    @Override
    public dtoResult runBoxMethod(dtoRequestOptimization requestOptimization) {
        dtoRange t1Range = requestOptimization.getT1Range();
        dtoRange t2Range = requestOptimization.getT1Range();
        // Initialize simplex with 5 points (2n + 1, where n = 2)
        double[][] simplex = {
                {-3, -3}, {-3, 9}, {9, -3}, {0, 0}, {6, 6}
        };

        while (true) {
            // Find worst point
            int worstIndex = 0;
            double worstValue = calculateObjective(simplex[0][0], simplex[0][1]);
            for (int i = 1; i < simplex.length; i++) {
                double value = calculateObjective(simplex[i][0], simplex[i][1]);
                if (value < worstValue) {
                    worstValue = value;
                    worstIndex = i;
                }
            }

            // Calculate centroid
            double[] centroid = new double[2];
            for (int i = 0; i < simplex.length; i++) {
                if (i != worstIndex) {
                    centroid[0] += simplex[i][0];
                    centroid[1] += simplex[i][1];
                }
            }
            centroid[0] /= (simplex.length - 1);
            centroid[1] /= (simplex.length - 1);

            // Reflect worst point
            double[] reflected = new double[2];
            reflected[0] = centroid[0] + ALPHA * (centroid[0] - simplex[worstIndex][0]);
            reflected[1] = centroid[1] + ALPHA * (centroid[1] - simplex[worstIndex][1]);

            // Check constraints
            reflected = projectToConstraints(reflected, t1Range, t2Range);

            // Evaluate reflected point
            double reflectedValue = calculateObjective(reflected[0], reflected[1]);
            if (reflectedValue > worstValue) {
                simplex[worstIndex][0] = reflected[0];
                simplex[worstIndex][1] = reflected[1];
            } else {
                // Shrink simplex
                for (int i = 0; i < simplex.length; i++) {
                    if (i != worstIndex) {
                        simplex[i][0] = (simplex[i][0] + simplex[worstIndex][0]) / 2;
                        simplex[i][1] = (simplex[i][1] + simplex[worstIndex][1]) / 2;
                    }
                }
            }

            // Check convergence
            double size = calculateSimplexSize(simplex);
            if (size < EPSILON) {
                break;
            }
        }

        // Return best point
        int bestIndex = 0;
        double bestValue = calculateObjective(simplex[0][0], simplex[0][1]);
        for (int i = 1; i < simplex.length; i++) {
            double value = calculateObjective(simplex[i][0], simplex[i][1]);
            if (value > bestValue) {
                bestValue = value;
                bestIndex = i;
            }
        }

        return new dtoResult(simplex[bestIndex][0], simplex[bestIndex][1], bestValue);
    }

    private double calculateObjective(double t1, double t2) {
        return 8 * (Math.pow(t2 - t1, 2) + (1.0 / 9) * Math.pow(t1 + t2 - 10, 2));
    }

    private double[] projectToConstraints(double[] point, dtoRange t1Range, dtoRange t2Range) {
        double t1 = Math.max(t1Range.getMin(), Math.min(t1Range.getMax(), point[0]));
        double t2 = Math.max(t2Range.getMin(), Math.min(t2Range.getMax(), point[1]));
        if (t1 + t2 > 12) {
            double scale = 12 / (t1 + t2);
            t1 *= scale;
            t2 *= scale;
        }
        return new double[]{t1, t2};
    }

    private double calculateSimplexSize(double[][] simplex) {
        double maxDist = 0;
        for (int i = 0; i < simplex.length; i++) {
            for (int j = i + 1; j < simplex.length; j++) {
                double dist = Math.sqrt(
                        Math.pow(simplex[i][0] - simplex[j][0], 2) +
                                Math.pow(simplex[i][1] - simplex[j][1], 2)
                );
                maxDist = Math.max(maxDist, dist);
            }
        }
        return maxDist;
    }
}