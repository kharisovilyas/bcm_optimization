package ru.ilcorp.bcm.optimization.controllers.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ilcorp.bcm.optimization.model.dto.dtoRequestOptimization;
import ru.ilcorp.bcm.optimization.model.dto.dtoResult;
import ru.ilcorp.bcm.optimization.service.OptimizationService;
import ru.ilcorp.bcm.optimization.service.OptimizationServiceImpl;

@RestController
@RequestMapping("/api/optimize")
public class OptimizationController {
    @Autowired
    private OptimizationService optimizationService;

    @PostMapping
    public dtoResult optimize(@RequestBody dtoRequestOptimization requestOptimization) {
        return optimizationService.runOptimization(requestOptimization);
    }
}