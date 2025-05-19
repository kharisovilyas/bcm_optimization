package ru.ilcorp.bcm.optimization.service;

import ru.ilcorp.bcm.optimization.model.dto.dtoRequestOptimization;
import ru.ilcorp.bcm.optimization.model.dto.dtoResult;

public interface OptimizationService {
    dtoResult runBoxMethod(dtoRequestOptimization requestOptimization);
}
