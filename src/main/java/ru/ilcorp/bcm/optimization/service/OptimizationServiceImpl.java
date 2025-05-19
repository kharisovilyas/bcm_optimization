
package ru.ilcorp.bcm.optimization.service;

import org.springframework.stereotype.Service;
import ru.ilcorp.bcm.optimization.model.dto.dtoRange;
import ru.ilcorp.bcm.optimization.model.dto.dtoRequestOptimization;
import ru.ilcorp.bcm.optimization.model.dto.dtoResult;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

@Service
public class OptimizationServiceImpl implements OptimizationService {
    private static final int COMPLEX_SIZE = 4; // Для метода Бокса
    private static final int SIMPLEX_SIZE = 3; // Для Нелдера-Мида (n+1 точек, n=2)
    private static final int MAX_ITERATIONS = 200;
    private static final double EPSILON = 0.1;
    private final Random random = new Random();

    @Override
    public dtoResult runOptimization(dtoRequestOptimization request) {
        String method = request.getMethod().toUpperCase();
        switch (method) {
            case "BOX":
                return runBoxMethod(request);
            case "NELDER_MEAD":
                return runNelderMeadMethod(request);
            case "RANDOM_SEARCH":
                return runRandomSearch(request);
            case "SCANNING":
                return runScanning(request);
            case "SEQUENTIAL_VARIATION":
                return runSequentialVariation(request);
            default:
                throw new IllegalArgumentException("Unknown method: " + request.getMethod());
        }
    }

    // Метод Бокса
    private dtoResult runBoxMethod(dtoRequestOptimization request) {
        dtoRange t1Range = request.getT1Range();
        dtoRange t2Range = request.getT2Range();
        double alpha = request.getAlpha();
        double beta = request.getBeta();
        double gamma = request.getGamma();

        double[][] complex = initializeComplex(t1Range, t2Range, COMPLEX_SIZE);

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Arrays.sort(complex, Comparator.comparingDouble(point -> calculateObjective(point, alpha, beta, gamma)));

            double bestValue = calculateObjective(complex[0], alpha, beta, gamma);
            double worstValue = calculateObjective(complex[COMPLEX_SIZE - 1], alpha, beta, gamma);
            double size = calculateSimplexSize(complex);
            if (size < EPSILON) {
                break;
            }

            double[] centroid = calculateCentroid(complex, COMPLEX_SIZE);
            double[] worstPoint = complex[COMPLEX_SIZE - 1];
            double[] reflected = reflectPoint(worstPoint, centroid, 2.3);
            reflected = projectToConstraints(reflected, t1Range, t2Range);

            if (calculateObjective(reflected, alpha, beta, gamma) < worstValue) {
                complex[COMPLEX_SIZE - 1] = reflected;
            } else {
                shrinkComplex(complex);
            }
        }

        double[] bestPoint = complex[0];
        return new dtoResult(bestPoint[0], bestPoint[1], calculateObjective(bestPoint, alpha, beta, gamma));
    }

    // Метод Нелдера-Мида
    private dtoResult runNelderMeadMethod(dtoRequestOptimization request) {
        dtoRange t1Range = request.getT1Range();
        dtoRange t2Range = request.getT2Range();
        double alpha = request.getAlpha();
        double beta = request.getBeta();
        double gamma = request.getGamma();

        double[][] simplex = initializeComplex(t1Range, t2Range, SIMPLEX_SIZE);

        final double nmAlpha = 1.0; // Отражение
        final double nmBeta = 0.5;  // Сжатие
        final double nmGamma = 2.0; // Растяжение
        final double nmDelta = 0.5; // Редукция

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Arrays.sort(simplex, Comparator.comparingDouble(point -> calculateObjective(point, alpha, beta, gamma)));

            double bestValue = calculateObjective(simplex[0], alpha, beta, gamma);
            double worstValue = calculateObjective(simplex[SIMPLEX_SIZE - 1], alpha, beta, gamma);
            double size = calculateSimplexSize(simplex);
            if (size < EPSILON) {
                break;
            }

            double[] centroid = calculateCentroid(simplex, SIMPLEX_SIZE);
            double[] worst = simplex[SIMPLEX_SIZE - 1];
            double[] secondWorst = simplex[SIMPLEX_SIZE - 2];

            double[] reflected = reflectPoint(worst, centroid, nmAlpha);
            reflected = projectToConstraints(reflected, t1Range, t2Range);
            double reflectedValue = calculateObjective(reflected, alpha, beta, gamma);

            if (reflectedValue < bestValue) {
                double[] expanded = reflectPoint(worst, centroid, nmGamma);
                expanded = projectToConstraints(expanded, t1Range, t2Range);
                double expandedValue = calculateObjective(expanded, alpha, beta, gamma);
                simplex[SIMPLEX_SIZE - 1] = expandedValue < reflectedValue ? expanded : reflected;
            } else if (reflectedValue < calculateObjective(secondWorst, alpha, beta, gamma)) {
                simplex[SIMPLEX_SIZE - 1] = reflected;
            } else {
                if (reflectedValue < worstValue) {
                    simplex[SIMPLEX_SIZE - 1] = reflected;
                    worst = reflected;
                    worstValue = reflectedValue;
                }
                double[] contracted = reflectPoint(worst, centroid, -nmBeta);
                contracted = projectToConstraints(contracted, t1Range, t2Range);
                double contractedValue = calculateObjective(contracted, alpha, beta, gamma);
                if (contractedValue < worstValue) {
                    simplex[SIMPLEX_SIZE - 1] = contracted;
                } else {
                    for (int i = 1; i < SIMPLEX_SIZE; i++) {
                        for (int j = 0; j < 2; j++) {
                            simplex[i][j] = simplex[0][j] + nmDelta * (simplex[i][j] - simplex[0][j]);
                        }
                    }
                }
            }
        }

        double[] bestPoint = simplex[0];
        return new dtoResult(bestPoint[0], bestPoint[1], calculateObjective(bestPoint, alpha, beta, gamma));
    }

    // Случайный поиск
    private dtoResult runRandomSearch(dtoRequestOptimization request) {
        dtoRange t1Range = request.getT1Range();
        dtoRange t2Range = request.getT2Range();
        double alpha = request.getAlpha();
        double beta = request.getBeta();
        double gamma = request.getGamma();

        double[] bestPoint = {t1Range.getMin(), t2Range.getMin()};
        double bestValue = calculateObjective(bestPoint, alpha, beta, gamma);

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            double t1 = t1Range.getMin() + random.nextDouble() * (t1Range.getMax() - t1Range.getMin());
            double t2 = t2Range.getMin() + random.nextDouble() * (t2Range.getMax() - t2Range.getMin());
            if (t1 + t2 > 12) continue;
            double currentValue = calculateObjective(new double[]{t1, t2}, alpha, beta, gamma);
            if (currentValue < bestValue) {
                bestValue = currentValue;
                bestPoint = new double[]{t1, t2};
            }
        }

        return new dtoResult(bestPoint[0], bestPoint[1], bestValue);
    }

    // Сканирование
    private dtoResult runScanning(dtoRequestOptimization request) {
        dtoRange t1Range = request.getT1Range();
        dtoRange t2Range = request.getT2Range();
        double alpha = request.getAlpha();
        double beta = request.getBeta();
        double gamma = request.getGamma();
        double step = 0.1;

        double[] bestPoint = {t1Range.getMin(), t2Range.getMin()};
        double bestValue = calculateObjective(bestPoint, alpha, beta, gamma);

        for (double t1 = t1Range.getMin(); t1 <= t1Range.getMax(); t1 += step) {
            for (double t2 = t2Range.getMin(); t2 <= t2Range.getMax(); t2 += step) {
                if (t1 + t2 > 12) continue;
                double currentValue = calculateObjective(new double[]{t1, t2}, alpha, beta, gamma);
                if (currentValue < bestValue) {
                    bestValue = currentValue;
                    bestPoint = new double[]{t1, t2};
                }
            }
        }

        return new dtoResult(bestPoint[0], bestPoint[1], bestValue);
    }

    // Поочередное варьирование
    private dtoResult runSequentialVariation(dtoRequestOptimization request) {
        dtoRange t1Range = request.getT1Range();
        dtoRange t2Range = request.getT2Range();
        double alpha = request.getAlpha();
        double beta = request.getBeta();
        double gamma = request.getGamma();
        double step = 0.1;

        double[] currentPoint = {
                (t1Range.getMin() + t1Range.getMax()) / 2,
                (t2Range.getMin() + t2Range.getMax()) / 2
        };
        double bestValue = calculateObjective(currentPoint, alpha, beta, gamma);

        boolean improved;
        do {
            improved = false;
            // Варьируем T1
            double bestT1 = currentPoint[0];
            for (double t1 = t1Range.getMin(); t1 <= t1Range.getMax(); t1 += step) {
                if (t1 + currentPoint[1] > 12) continue;
                double value = calculateObjective(new double[]{t1, currentPoint[1]}, alpha, beta, gamma);
                if (value < bestValue) {
                    bestValue = value;
                    bestT1 = t1;
                    improved = true;
                }
            }
            currentPoint[0] = bestT1;

            // Варьируем T2
            double bestT2 = currentPoint[1];
            for (double t2 = t2Range.getMin(); t2 <= t2Range.getMax(); t2 += step) {
                if (currentPoint[0] + t2 > 12) continue;
                double value = calculateObjective(new double[]{currentPoint[0], t2}, alpha, beta, gamma);
                if (value < bestValue) {
                    bestValue = value;
                    bestT2 = t2;
                    improved = true;
                }
            }
            currentPoint[1] = bestT2;
        } while (improved && bestValue > EPSILON);

        return new dtoResult(currentPoint[0], currentPoint[1], bestValue);
    }

    // Целевая функция с коэффициентами
    private double calculateObjective(double[] point, double alpha, double beta, double gamma) {
        double t1 = point[0];
        double t2 = point[1];
        return alpha * 8 * (beta * Math.pow(t2 - t1, 2) + gamma * (1.0 / 9) * Math.pow(t1 + t2 - 10, 2));
    }

    // Инициализация комплекса/симплекса
    private double[][] initializeComplex(dtoRange t1Range, dtoRange t2Range, int size) {
        double[][] complex = new double[size][2];
        for (int i = 0; i < size; i++) {
            double t1, t2;
            do {
                t1 = t1Range.getMin() + random.nextDouble() * (t1Range.getMax() - t1Range.getMin());
                t2 = t2Range.getMin() + random.nextDouble() * (t2Range.getMax() - t2Range.getMin());
            } while (t1 + t2 > 12);
            complex[i] = new double[]{t1, t2};
        }
        return complex;
    }

    // Вычисление центроида
    private double[] calculateCentroid(double[][] complex, int size) {
        double[] centroid = new double[2];
        for (int i = 0; i < size - 1; i++) {
            centroid[0] += complex[i][0];
            centroid[1] += complex[i][1];
        }
        centroid[0] /= (size - 1);
        centroid[1] /= (size - 1);
        return centroid;
    }

    // Отражение точки
    private double[] reflectPoint(double[] point, double[] centroid, double coeff) {
        double[] reflected = new double[2];
        reflected[0] = centroid[0] + coeff * (centroid[0] - point[0]);
        reflected[1] = centroid[1] + coeff * (centroid[1] - point[1]);
        return reflected;
    }

    // Проверка ограничений
    private double[] projectToConstraints(double[] point, dtoRange t1Range, dtoRange t2Range) {
        double t1 = Math.max(t1Range.getMin(), Math.min(t1Range.getMax(), point[0]));
        double t2 = Math.max(t2Range.getMin(), Math.min(t2Range.getMax(), point[1]));
        if (t1 + t2 > 12) {
            double scale = 12 / (t1 + t2);
            t1 *= scale;
            t2 *= scale;
            t1 = Math.max(t1Range.getMin(), Math.min(t1Range.getMax(), t1));
            t2 = Math.max(t2Range.getMin(), Math.min(t2Range.getMax(), t2));
        }
        return new double[]{t1, t2};
    }

    // Сужение комплекса (для метода Бокса)
    private void shrinkComplex(double[][] complex) {
        double[] bestPoint = complex[0];
        for (int i = 1; i < COMPLEX_SIZE; i++) {
            complex[i][0] = bestPoint[0] + 0.5 * (complex[i][0] - bestPoint[0]);
            complex[i][1] = bestPoint[1] + 0.5 * (complex[i][1] - bestPoint[1]);
        }
    }

    // Размер комплекса/симплекса
    private double calculateSimplexSize(double[][] complex) {
        double maxDist = 0;
        int size = complex.length;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                double dist = Math.sqrt(
                        Math.pow(complex[i][0] - complex[j][0], 2) +
                                Math.pow(complex[i][1] - complex[j][1], 2)
                );
                maxDist = Math.max(maxDist, dist);
            }
        }
        return maxDist;
    }
}