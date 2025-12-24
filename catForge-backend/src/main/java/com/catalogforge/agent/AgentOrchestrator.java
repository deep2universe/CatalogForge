package com.catalogforge.agent;

import com.catalogforge.agent.steps.ImageAnalysisStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Orchestrates the agent pipeline execution.
 * Selects the appropriate strategy and executes the pipeline.
 */
@Service
public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final List<PipelineStrategy> strategies;
    private final ImageAnalysisStep imageAnalysisStep;

    public AgentOrchestrator(List<PipelineStrategy> strategies, ImageAnalysisStep imageAnalysisStep) {
        // Sort by @Order annotation (lower = higher priority)
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(s -> {
                    var order = s.getClass().getAnnotation(org.springframework.core.annotation.Order.class);
                    return order != null ? order.value() : Integer.MAX_VALUE;
                }))
                .toList();
        this.imageAnalysisStep = imageAnalysisStep;
        
        log.info("AgentOrchestrator initialized with {} strategies: {}", 
                strategies.size(),
                strategies.stream().map(PipelineStrategy::name).toList());
    }

    /**
     * Executes the agent pipeline for the given context.
     * 
     * @param context The initial context
     * @return The final context after pipeline execution
     */
    public AgentContext execute(AgentContext context) {
        log.info("Starting orchestration: pipelineId={}", context.pipelineId());
        long startTime = System.currentTimeMillis();
        
        // Step 1: Analyze image if present
        AgentContext current = context;
        if (context.hasImage()) {
            log.debug("Analyzing reference image");
            current = imageAnalysisStep.execute(current);
        }
        
        // Step 2: Select strategy
        PipelineStrategy strategy = selectStrategy(current);
        log.info("Selected strategy: {}", strategy.name());
        
        // Step 3: Execute pipeline
        Pipeline pipeline = strategy.getPipeline();
        current = pipeline.run(current);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Orchestration completed: pipelineId={}, strategy={}, duration={}ms, valid={}", 
                context.pipelineId(), strategy.name(), duration, current.isValid());
        
        return current;
    }

    /**
     * Selects the appropriate strategy for the given context.
     */
    PipelineStrategy selectStrategy(AgentContext context) {
        return strategies.stream()
                .filter(s -> s.matches(context))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching strategy found"));
    }

    /**
     * Returns all available strategies.
     */
    public List<String> getAvailableStrategies() {
        return strategies.stream()
                .map(PipelineStrategy::name)
                .toList();
    }
}
